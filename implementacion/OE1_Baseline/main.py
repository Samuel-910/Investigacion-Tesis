import os
from config import PipelineConfig, log
from stages.stage1_extract import Stage1_Extract
from stages.stage2_sessionize import Stage2_Sessionize
from stages.stage3_filter import Stage3_Filter
from stages.stage4_profiles import Stage4_ProfileVectors
from stages.stage5_cost import Stage5_NavigationCost
from stages.stage6_export import Stage6_Export

def main():
    print("=" * 60)
    print("  INICIANDO PIPELINE OE1: CÁLCULO DE LÍNEA BASE (M1, M2, M3)")
    print("=" * 60)

    # Configuración principal
    cfg = PipelineConfig(
        input_path          = "../00_Generador_Datos/logs_sinteticos_ppi.ndjson",
        session_timeout_min = 30,
        min_stay_seconds    = 3,
        anonymize           = False,
        output_dir          = "output_oe1"
    )

    # Ejecución secuencial de los módulos
    raw_events     = Stage1_Extract(cfg).run()
    sessions       = Stage2_Sessionize(cfg).run(raw_events)
    clean_sessions = Stage3_Filter(cfg).run(sessions)
    profiles       = Stage4_ProfileVectors(cfg).run(clean_sessions)
    df_costs       = Stage5_NavigationCost(cfg).run(clean_sessions, profiles)
    resumen        = Stage6_Export(cfg).run(df_costs, profiles, clean_sessions)

    # Imprimir Resumen Ejecutivo
    m = resumen["metricas_baseline"]
    print("\n" + "=" * 62)
    print("  OE1 COMPLETADO EXITOSAMENTE")
    print("=" * 62)
    print(f"  MÉTRICAS BASELINE (pre-adaptación)")
    print(f"  M1 Costo clics prom: {m['M1_clics_prom']} clics/ítem")
    print(f"  M2 Tiempo top1 prom: {m['M2_tiempo_s_prom']} segundos")
    print(f"  M3 Tasa error prom:  {m['M3_error_prom']*100:.2f}%")
    print("=" * 62)

if __name__ == "__main__":
    main()
