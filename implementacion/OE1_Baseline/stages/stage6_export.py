from typing import List, Dict, Optional
from pathlib import Path
from collections import defaultdict
import json
import pandas as pd
from datetime import datetime
from config import PipelineConfig, RawEvent, NavigationSession, log

class Stage6_Export:
    """
    Stage 6: Exportación de artefactos del OE1.
    Produce exactamente los archivos que OE2 (GRU/Transformer/DQN) necesita.
    """

    def __init__(self, cfg: PipelineConfig):
        self.out = Path(cfg.output_dir)
        self.out.mkdir(parents=True, exist_ok=True)

    def run(self, df_costs: pd.DataFrame, profiles: Dict,
            sessions: List[NavigationSession]) -> dict:
        log.info("── STAGE 6: Exportación ─────────────────────────────")

        # 1. Métricas baseline → CSV (OE5)
        p1 = self.out / "costo_navegacion_baseline.csv"
        df_costs.to_csv(p1, index=False, encoding="utf-8")
        log.info(f"   ✓ {p1.name}  ({len(df_costs)} filas)")

        # 2. Perfiles de usuario → JSON (OE2: frecuencia + secuencias para GRU/TF)
        export_perfiles = [{
            "user_id":    p["user_id"],
            "role":       p["role"],
            "n_sesiones": p["n_sesiones"],
            "freq_norm":  p["freq_norm"],
            "secuencias": p["secuencias"],   # ← secuencias de rutas para GRU/Transformer
            "top5_rutas": [{"ruta": r, "freq": f} for r, f in p["top5_rutas"]],
        } for p in profiles.values()]
        p2 = self.out / "perfiles_usuario.json"
        p2.write_text(json.dumps(export_perfiles, indent=2, ensure_ascii=False))
        log.info(f"   ✓ {p2.name}  ({len(export_perfiles)} perfiles)")

        # 3. Vocabulario de rutas → JSON (OE2: encoding para embeddings)
        all_routes = sorted({
            e.route for s in sessions for e in s.events if e.route
        })
        vocab = {"rutas": all_routes, "vocab_size": len(all_routes)}
        p3 = self.out / "vocabulario_rutas.json"
        p3.write_text(json.dumps(vocab, indent=2, ensure_ascii=False))
        log.info(f"   ✓ {p3.name}  ({len(all_routes)} rutas únicas)")

        # 4. Frecuencia por rol → JSON (OE2: prior de rol para score compuesto)
        rol_freq = defaultdict(lambda: defaultdict(int))
        for s in sessions:
            for e in s.events:
                if e.route:
                    rol_freq[s.role][e.route] += 1
        # Normalizar por total de accesos del rol
        rol_freq_norm = {}
        for rol, rutas in rol_freq.items():
            total = sum(rutas.values())
            rol_freq_norm[rol] = {r: round(cnt/total, 6) for r, cnt in rutas.items()}
        p4 = self.out / "frecuencia_por_rol.json"
        p4.write_text(json.dumps(rol_freq_norm, indent=2, ensure_ascii=False))
        # También CSV para compatibilidad OE3
        df_rol = pd.DataFrame([
            {"role": rol, "ruta": ruta, "accesos": cnt}
            for rol, rutas in rol_freq.items()
            for ruta, cnt in sorted(rutas.items(), key=lambda x: -x[1])
        ])
        p4b = self.out / "frecuencia_por_rol.csv"
        df_rol.to_csv(p4b, index=False, encoding="utf-8")
        log.info(f"   ✓ {p4.name} + {p4b.name}")

        # 5. Resumen → JSON
        resumen = {
            "oe1_completado": True,
            "fecha_ejecucion": datetime.now().isoformat(),
            "corpus": {
                "total_eventos":  sum(s.n_clicks for s in sessions),
                "total_sesiones": len(sessions),
                "total_usuarios": len(profiles),
                "roles": list({s.role for s in sessions}),
                "periodo": {
                    "inicio": str(min(s.start_time.date() for s in sessions)),
                    "fin":    str(max(s.end_time.date()   for s in sessions)),
                },
            },
            "metricas_baseline": {
                "M1_clics_prom":   round(df_costs["M1_costo_clics"].mean(),   3),
                "M2_tiempo_s_prom":round(df_costs["M2_tiempo_top1_s"].mean(), 3),
                "M3_error_prom":   round(df_costs["M3_tasa_error"].mean(),    4),
                "por_rol": df_costs.groupby("role").agg(
                    M1=("M1_costo_clics",   "mean"),
                    M2=("M2_tiempo_top1_s", "mean"),
                    M3=("M3_tasa_error",    "mean"),
                ).round(3).to_dict(orient="index"),
            },
        }
        p5 = self.out / "resumen_oe1.json"
        p5.write_text(json.dumps(resumen, indent=2, ensure_ascii=False, default=str))
        log.info(f"   ✓ {p5.name}")
        log.info(f"\n   Directorio: {self.out.resolve()}/")
        return resumen

