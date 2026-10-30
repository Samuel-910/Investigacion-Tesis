"""Pipeline completo con el menú REAL del ERP (catalog.json de la app del estudio).

  00  construir_config_erp.py   catalog.json -> erp_config_universal.json + erp_menu_schema.json
  00  simulador_erp_real.py     logs sintéticos con las rutas reales
  OE1 main.py --fuente mixto    línea base con logs sintéticos + reales
  OE2 main.py                   reentrena GRU, LSTM, CNN1D y Transformer
  OE2 plot_benchmark.py / generate_scores.py
  OE3 main.py / evaluate.py     reentrena el DQN y genera los menús personalizados
  OE4 / OE5                     auditoría y evaluación

Uso:
  python run_all_pipeline.py            # todo (genera datos y reentrena)
  python run_all_pipeline.py --sin-entrenar   # reutiliza los modelos ya entrenados
  python run_all_pipeline.py --solo-reales    # solo datos reales (P001...) con los modelos ya entrenados
"""
import os
import subprocess
import sys
import time
from pathlib import Path

base_dir = Path(__file__).resolve().parent
venv_python = base_dir / "venv" / "Scripts" / "python.exe"
if not venv_python.exists():  # se reutiliza el entorno de implementacion/
    venv_python = base_dir.parent / "implementacion" / "venv" / "Scripts" / "python.exe"

solo_reales = "--solo-reales" in sys.argv          # solo participantes reales, sin entrenar
entrenar = "--sin-entrenar" not in sys.argv and not solo_reales

steps = [
    ("00_Generador_Datos", ["construir_config_erp.py"]),
    *([] if solo_reales else [("00_Generador_Datos", ["simulador_erp_real.py"])]),
    ("OE1_Baseline", ["main.py", "--fuente", "real" if solo_reales else "mixto"]),
    *([("OE2_Modelado", ["main.py", "--model", "all"]),
       ("OE2_Modelado", ["plot_benchmark.py"])] if entrenar else []),
    ("OE2_Modelado", ["generate_scores.py"]),
    *([("OE3_Agente_DQN", ["main.py"])] if entrenar else []),
    ("OE3_Agente_DQN", ["evaluate.py"]),
    ("OE4_Auditoria", ["main.py"]),
    ("OE5_Evaluacion", ["main.py"]),
]

log_path = base_dir / "pipeline_execution.log"

print(f"Starting end-to-end pipeline execution (python: {venv_python})...")
start_time = time.time()

env = os.environ.copy()
env["PYTHONIOENCODING"] = "utf-8"

with open(log_path, "w", encoding="utf-8") as log_file:
    for folder, cmds in steps:
        target_dir = base_dir / folder
        header = f"\n{'='*60}\n[EXECUTION] {folder} > python {' '.join(cmds)}\n{'='*60}\n"
        log_file.write(header)
        log_file.flush()
        print(header, flush=True)
        t0 = time.time()
        result = subprocess.run(
            [str(venv_python)] + cmds,
            cwd=str(target_dir),
            capture_output=True,
            text=True,
            encoding='utf-8',
            errors='replace',
            env=env
        )
        if result.stdout:
            print(result.stdout, flush=True)
        log_file.write(result.stdout or "")
        if result.stderr:
            log_file.write("\n[STDERR]\n" + result.stderr)
        log_file.write(f"\n[{folder}/{cmds[0]}] exit={result.returncode} ({time.time() - t0:.1f}s)\n")
        log_file.flush()
        if result.returncode != 0:
            print(result.stderr, flush=True)
            print(f"[ERROR] {folder}/{cmds[0]} falló (exit {result.returncode}). Pipeline detenido.")
            sys.exit(result.returncode)

elapsed = time.time() - start_time
summary = f"\nPipeline execution completed in {elapsed:.2f} seconds."
print(summary)
with open(log_path, "a", encoding="utf-8") as log_file:
    log_file.write(summary + "\n")
