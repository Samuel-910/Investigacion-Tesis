from typing import List, Dict, Optional
from pathlib import Path
import pandas as pd
from config import PipelineConfig, RawEvent, NavigationSession, log

class Stage3_Filter:
    """
    Stage 3: Filtración de accesos accidentales y sesiones incompletas.
    
    Criterios definidos en PPI §2.3.1:
    - Acceso accidental: tiempo hasta siguiente clic < 3 segundos
    - Sesión incompleta: menos de 2 eventos válidos
    """

    def __init__(self, cfg: PipelineConfig):
        self.min_stay   = cfg.min_stay_seconds    # 3 segundos por defecto
        self.min_events = cfg.min_session_events  # 2 eventos mínimo

    def run(self, sessions: List[NavigationSession]) -> List[NavigationSession]:
        log.info("── STAGE 3: Filtración de ruido ─────────────────────")

        total_sesiones_orig = len(sessions)
        total_eventos_orig  = sum(s.n_clicks for s in sessions)
        n_accidentales      = 0
        n_sesiones_cortas   = 0
        sesiones_limpias    = []

        for session in sessions:
            eventos_limpios = []

            for i, evt in enumerate(session.events):
                # Siempre descartar errores HTTP (is_error = True)
                if evt.is_error:
                    continue

                # Verificar tiempo de permanencia
                if i < len(session.events) - 1:
                    delta_seg = (session.events[i+1].timestamp - evt.timestamp).total_seconds()
                    if delta_seg < self.min_stay:
                        n_accidentales += 1
                        continue  # acceso accidental → descartar

                eventos_limpios.append(evt)

            # Actualizar sesión con eventos limpios
            session.events = eventos_limpios
            if eventos_limpios:
                session.start_time = eventos_limpios[0].timestamp
                session.end_time   = eventos_limpios[-1].timestamp

            # Descartar sesiones con muy pocos eventos
            if len(session.events) < self.min_events:
                n_sesiones_cortas += 1
                continue

            sesiones_limpias.append(session)

        total_eventos_limpio = sum(s.n_clicks for s in sesiones_limpias)

        log.info(f"   Sesiones:  {total_sesiones_orig:,} → {len(sesiones_limpias):,}  "
                 f"(-{n_sesiones_cortas} incompletas)")
        log.info(f"   Eventos:   {total_eventos_orig:,} → {total_eventos_limpio:,}  "
                 f"(-{n_accidentales} accidentales)")
        log.info(f"   Retención: {total_eventos_limpio/total_eventos_orig*100:.1f}% de los eventos")

        return sesiones_limpias

