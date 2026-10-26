from typing import List, Dict, Optional
from pathlib import Path
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from config import PipelineConfig, RawEvent, NavigationSession, log

class Stage2_Sessionize:
    """
    Stage 2: Agrupación de eventos en sesiones por timeout de inactividad.
    
    Referencia: Aung & Kumoi (2026) — timeout de 30 min estándar para análisis de clickstream
    Timeout estándar: 30 minutos (adoptado en PPI §2.3.1)
    """

    def __init__(self, cfg: PipelineConfig):
        self.timeout = timedelta(minutes=cfg.session_timeout_min)

    def run(self, events: List[RawEvent]) -> List[NavigationSession]:
        log.info("── STAGE 2: Segmentación de sesiones ────────────────")
        log.info(f"   Timeout: {self.timeout}")

        # Paso 1: ordenar por usuario y tiempo
        sorted_events = sorted(events, key=lambda e: (e.user_id, e.timestamp))

        sessions: List[NavigationSession] = []
        current: Optional[NavigationSession] = None

        for event in sorted_events:
            es_nuevo_usuario = current is None or event.user_id != current.user_id
            hay_timeout      = current is not None and                                (event.timestamp - current.events[-1].timestamp) > self.timeout

            if es_nuevo_usuario or hay_timeout:
                # Guardar sesión anterior si existe
                if current is not None:
                    sessions.append(current)

                # Crear nueva sesión
                sid = f"{event.user_id}_{event.timestamp.strftime('%Y%m%d%H%M%S')}"
                current = NavigationSession(
                    session_id = sid,
                    user_id    = event.user_id,
                    role       = event.role,
                    start_time = event.timestamp,
                    end_time   = event.timestamp,
                    events     = [event],
                )
            else:
                # Agregar evento a sesión actual
                current.events.append(event)
                current.end_time = event.timestamp

        if current:
            sessions.append(current)

        # Estadísticas
        clicks_por_sesion    = [s.n_clicks for s in sessions]
        duracion_por_sesion  = [s.duration_seconds / 60 for s in sessions]

        log.info(f"   Sesiones detectadas:    {len(sessions):,}")
        log.info(f"   Clics/sesión  — prom:   {np.mean(clicks_por_sesion):.1f}  "
                 f"min: {min(clicks_por_sesion)}  max: {max(clicks_por_sesion)}")
        log.info(f"   Duración/sesión — prom: {np.mean(duracion_por_sesion):.1f} min")

        return sessions

