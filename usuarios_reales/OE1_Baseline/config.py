import logging
import warnings
from pathlib import Path
from datetime import datetime, timedelta
from dataclasses import dataclass, field
from typing import List, Dict, Tuple, Optional
from collections import defaultdict
import numpy as np
import pandas as pd
import json
import hashlib

warnings.filterwarnings("ignore")

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s", datefmt="%H:%M:%S")
log = logging.getLogger("PPI-OE1")


@dataclass
class PipelineConfig:
    # ── Fuente de datos ──────────────────────────────────────────────
    input_path:          str  = "navigation_logs_augmented.ndjson"
    db_url:              str  = None  # Alternativa: URL de PostgreSQL

    # ── Segmentación de sesiones (Stage 2) ───────────────────────────
    session_timeout_min: int  = 30    # minutos sin actividad → nueva sesión

    # ── Filtración de ruido (Stage 3) ────────────────────────────────
    min_stay_seconds:    int  = 3     # accesos < 3s son "accidentales" (PPI §2.3.1)
    min_session_events:  int  = 2     # sesiones con 1 clic → incompletas

    # ── Privacidad (Stage 4) ─────────────────────────────────────────
    anonymize:           bool = True  # SHA-256 de user_id (ético §3.2.3)

    # ── Profundidad del árbol de menú del sistema ─────────────
    schema_path:         str  = "../erp_menu_schema.json"
    menu_depth:          Dict = field(default_factory=dict)

    def __post_init__(self):
        # Carga dinámica del manifiesto para garantizar universalidad
        if not self.menu_depth:
            schema_file = Path(self.schema_path)
            if schema_file.exists():
                with open(schema_file, "r", encoding="utf-8") as f:
                    self.menu_depth = json.load(f)
            else:
                log.warning(f"No se encontró el esquema en {self.schema_path}. Usando costo por defecto (3).")

    # ── Salida ────────────────────────────────────────────────────────
    output_dir: str = "output_oe1"




@dataclass
class RawEvent:
    """Un clic de navegación en el ERP."""
    timestamp:   datetime
    user_id:     str
    role:        str
    action:      str    # "Read Kardex", "Create Orden Compra", etc.
    route:       str    # "almacen/kardex", "compra/orden", etc.
    exec_time_s: float  # tiempo de respuesta del servidor en segundos
    is_error:    bool   # si el servidor devolvió error HTTP


@dataclass
class NavigationSession:
    """Una sesión de trabajo de un usuario (conjunto de eventos consecutivos)."""
    session_id:  str
    user_id:     str
    role:        str
    start_time:  datetime
    end_time:    datetime
    events:      List[RawEvent] = field(default_factory=list)

    @property
    def duration_seconds(self) -> float:
        """Duración total de la sesión en segundos."""
        return (self.end_time - self.start_time).total_seconds()

    @property
    def n_clicks(self) -> int:
        """Número de clics (navegaciones) en la sesión."""
        return len(self.events)

