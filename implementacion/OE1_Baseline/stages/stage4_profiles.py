from typing import List, Dict, Optional
from pathlib import Path
from collections import defaultdict
import hashlib
from config import PipelineConfig, RawEvent, NavigationSession, log

class Stage4_ProfileVectors:
    """
    Stage 4: Construcción de vectores de perfil de usuario.

    Extrae:
    - Vector de frecuencia normalizada por usuario (v_u) → alimenta score compuesto OE2
    - Secuencias de rutas por sesión → alimenta GRU/Transformer en OE2
    - Frecuencia por rol → alimenta prior de rol en OE2
    """

    def __init__(self, cfg: PipelineConfig):
        self.anonymize = cfg.anonymize

    def run(self, sessions: List[NavigationSession]) -> Dict:
        log.info("── STAGE 4: Vectores de perfil de usuario ───────────")

        user_sessions: Dict[str, List[NavigationSession]] = defaultdict(list)
        for s in sessions:
            uid = self._hash_uid(s.user_id) if self.anonymize else s.user_id
            user_sessions[uid].append(s)

        all_routes = sorted({
            e.route
            for s in sessions
            for e in s.events
            if e.route
        })

        profiles = {}

        for uid, u_sessions in user_sessions.items():
            freq_abs: Dict[str, int] = defaultdict(int)
            # Secuencias de rutas por sesión (input para GRU/Transformer en OE2)
            secuencias: List[List[str]] = []
            role = u_sessions[0].role

            for sess in u_sessions:
                rutas = [e.route for e in sess.events if e.route]
                for r in rutas:
                    freq_abs[r] += 1
                if len(rutas) >= 2:
                    secuencias.append(rutas)

            n_sesiones = len(u_sessions)
            freq_norm  = {r: cnt / n_sesiones for r, cnt in freq_abs.items()}
            top5       = sorted(freq_abs.items(), key=lambda x: x[1], reverse=True)[:5]

            profiles[uid] = {
                "user_id":       uid,
                "role":          role,
                "n_sesiones":    n_sesiones,
                "freq_absoluta": dict(freq_abs),
                "freq_norm":     freq_norm,
                "secuencias":    secuencias,   # ← OE2: input para GRU/Transformer
                "top5_rutas":    top5,
                "all_routes":    all_routes,
            }

        log.info(f"   Perfiles generados:      {len(profiles):,} usuarios")
        log.info(f"   Rutas únicas en corpus:  {len(all_routes):,}")
        total_seq = sum(len(p["secuencias"]) for p in profiles.values())
        log.info(f"   Secuencias para GRU/TF:  {total_seq:,}")
        return profiles

    def _hash_uid(self, user_id: str) -> str:
        """Anonimización SHA-256 irreversible (PPI §3.2.3)."""
        return "u_" + hashlib.sha256(user_id.encode()).hexdigest()[:8]

