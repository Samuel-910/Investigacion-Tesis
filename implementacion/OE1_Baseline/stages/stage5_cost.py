from typing import List, Dict, Optional
from pathlib import Path
import numpy as np
import pandas as pd
from config import PipelineConfig, RawEvent, NavigationSession, log

class Stage5_NavigationCost:
    """
    Stage 5: Cálculo de las 3 métricas de costo de navegación (PPI §2.3.3).

    M1 — Clics promedio para llegar a función objetivo desde la raíz del menú
    M2 — Tiempo (seg) desde inicio de sesión hasta primera función objetivo
    M3 — Tasa de error: proporción de clics fuera de la trayectoria óptima
    """

    def __init__(self, cfg: PipelineConfig):
        self.menu_depth = cfg.menu_depth

    def run(self, sessions: List[NavigationSession], profiles: Dict) -> pd.DataFrame:
        log.info("── STAGE 5: Métricas de costo de navegación ─────────")
        rows = []

        for session in sessions:
            if not session.events:
                continue

            uid = session.user_id

            # ── M1: Clics promedio desde raíz ─────────────────────────
            clics = [self._profundidad(e.route) for e in session.events]
            m1 = np.mean(clics) if clics else 0

            # ── M2: Tiempo hasta función top1 del perfil ──────────────
            # FIX: verificar que top5_rutas no esté vacío antes de acceder [0]
            perfil    = profiles.get(uid, {})
            top5      = perfil.get("top5_rutas", [])
            top1_ruta = top5[0][0] if top5 else ""
            m2        = self._tiempo_hasta_ruta(session, top1_ruta)

            # ── M3: Tasa de error de navegación ───────────────────────
            m3 = self._tasa_error(session)

            rows.append({
                "session_id":         session.session_id,
                "user_id":            uid,
                "role":               session.role,
                "fecha":              session.start_time.date(),
                "hora_inicio":        session.start_time.hour,
                "n_clics":            session.n_clicks,
                "duracion_min":       round(session.duration_seconds / 60, 2),
                "M1_costo_clics":     round(m1, 3),
                "M2_tiempo_top1_s":   round(m2, 1),
                "M3_tasa_error":      round(m3, 4),
                "costo_sesion_total": round(m1 * session.n_clicks, 2),
            })

        df = pd.DataFrame(rows)

        if not df.empty:
            log.info(f"   Sesiones procesadas:    {len(df):,}")
            log.info(f"   M1 costo clics prom:    {df['M1_costo_clics'].mean():.3f}")
            log.info(f"   M2 tiempo top1 prom:    {df['M2_tiempo_top1_s'].mean():.1f} s")
            log.info(f"   M3 tasa error prom:     {df['M3_tasa_error'].mean():.4f}")

        return df

    # ── Funciones auxiliares ────────────────────────────────────────

    def _profundidad(self, route: str) -> int:
        """
        Devuelve la profundidad del ítem en el árbol de menú.
        1 = GRUPO (1 clic), 2 = SUBMENU (2 clics), 3 = ITEM (3 clics).
        Si la ruta no está mapeada, asume 3 (peor caso conservador).
        """
        return self.menu_depth.get(route, 3)

    def _tiempo_hasta_ruta(self, session: NavigationSession, target: str) -> float:
        """
        Segundos desde el inicio de sesión hasta el primer acceso a 'target'.
        Si target es vacío o el usuario nunca llegó, retorna la duración total.
        """
        if not target:
            return session.duration_seconds
        for evt in session.events:
            if evt.route == target:
                return (evt.timestamp - session.start_time).total_seconds()
        return session.duration_seconds

    def _tasa_error(self, session: NavigationSession) -> float:
        """
        Tasa de clics que no avanzan hacia un destino nuevo.
        Proxy: clics consecutivos sobre la misma ruta → usuario confundido.
        """
        if session.n_clicks <= 1:
            return 0.0
        errores = sum(
            1 for i in range(1, len(session.events))
            if session.events[i].route == session.events[i - 1].route
        )
        return errores / session.n_clicks

