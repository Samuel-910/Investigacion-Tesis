from typing import List, Dict, Optional
from pathlib import Path
import json
import pandas as pd
from datetime import datetime
from config import PipelineConfig, RawEvent, NavigationSession, log

class Stage1_Extract:
    """
    Stage 1: Extracción de logs desde JSON o PostgreSQL.
    
    CORRECCIÓN v2: manejo robusto de BOM UTF-8 (Windows),
    NDJSON, JSON array y archivos con encoding mixto.
    """
    def __init__(self, cfg: PipelineConfig):
        self.cfg = cfg

    def run(self) -> List[RawEvent]:
        log.info("── STAGE 1: Extracción ──────────────────────────────")
        if self.cfg.db_url:
            events = self._from_postgres()
        else:
            events = self._from_json()
        log.info(f"   Eventos cargados: {len(events):,}")
        return events

    def _from_json(self) -> List[RawEvent]:
        path = Path(self.cfg.input_path)
        if not path.exists():
            raise FileNotFoundError(
                f"\n❌ Archivo no encontrado: {path.resolve()}\n"
                f"   Asegúrate de que 'navigation_logs_augmented.ndjson' esté\n"
                f"   en la misma carpeta que este notebook."
            )

        # ── FIX WINDOWS: utf-8-sig elimina el BOM automáticamente ──
        raw = path.read_text(encoding="utf-8-sig").strip()

        records = []

        # Caso 1: JSON array  →  [ {...}, {...}, ... ]
        if raw.startswith("["):
            try:
                records = json.loads(raw)
                log.info("   Formato detectado: JSON array")
                return [self._parse_record(r) for r in records]
            except json.JSONDecodeError as e:
                raise ValueError(f"El archivo parece un JSON array pero no es válido: {e}")

        # Caso 2: NDJSON  →  una línea = un objeto JSON
        if raw.startswith("{"):
            errores = []
            for i, line in enumerate(raw.splitlines(), 1):
                line = line.strip()
                if not line:
                    continue
                try:
                    records.append(json.loads(line))
                except json.JSONDecodeError as e:
                    errores.append(f"Línea {i}: {e}")

            if errores and not records:
                raise ValueError(
                    f"No se pudo parsear ninguna línea del archivo.\n"
                    f"Primeros errores: {errores[:3]}"
                )
            if errores:
                log.warning(f"   {len(errores)} líneas con error omitidas (de {i} total)")

            log.info("   Formato detectado: NDJSON (una línea = un objeto)")
            return [self._parse_record(r) for r in records]

        # Caso 3: formato desconocido → intentar json.loads directo
        try:
            data = json.loads(raw)
            if isinstance(data, list):
                log.info("   Formato detectado: JSON anidado")
                return [self._parse_record(r) for r in data]
            if isinstance(data, dict) and "data" in data:
                return [self._parse_record(r) for r in data["data"]]
        except json.JSONDecodeError:
            pass

        raise ValueError(
            f"❌ Formato de archivo no reconocido.\n"
            f"   El archivo debe ser JSON array [...] o NDJSON (un objeto por línea).\n"
            f"   Primeros 100 caracteres: {repr(raw[:100])}"
        )

    def _from_postgres(self) -> List[RawEvent]:
        from sqlalchemy import create_engine, text
        engine = create_engine(self.cfg.db_url)
        query = text("""
            SELECT
                to_char(timestamp, 'YYYY-MM-DD HH24:MI:SS') as timestamp,
                CONCAT('user_', role, '_', user_id::text)   as user_id,
                role,
                CONCAT('Read ', menu_item_titulo)            as action,
                '/' || route                                 as route,
                COALESCE(execution_time_ms / 1000.0, 0)     as execution_time,
                COALESCE(is_error, false)                    as is_error
            FROM navigation_logs
            ORDER BY timestamp ASC
        """)
        with engine.connect() as conn:
            rows = conn.execute(query).fetchall()
        return [self._parse_record(dict(r._mapping)) for r in rows]

    def _parse_record(self, r: dict) -> RawEvent:
        ts_str = r.get("timestamp", "")
        try:
            ts = datetime.strptime(ts_str, "%Y-%m-%d %H:%M:%S")
        except ValueError:
            try:
                ts = datetime.fromisoformat(ts_str)
            except ValueError:
                ts = datetime.now()
                log.warning(f"   Timestamp inválido: {ts_str!r}, usando now()")

        route = r.get("route", "").lstrip("/").replace("api/", "")

        return RawEvent(
            timestamp   = ts,
            user_id     = str(r.get("user_id", "unknown")),
            role        = str(r.get("role", "unknown")),
            action      = str(r.get("action", "")),
            route       = route,
            exec_time_s = float(r.get("execution_time", 0)),
            is_error    = bool(r.get("is_error", False)),
        )

