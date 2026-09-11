import os
import subprocess
import time
from pathlib import Path

base_dir = Path(r"d:\Ciclo X\Investigacion\implementacion")
venv_python = base_dir / "venv" / "Scripts" / "python.exe"

# List of steps to execute in order
steps = [
    ("OE1_Baseline", ["main.py"]),
    # Note: Only running generate_scores.py for OE2 to save time, assuming the model is already trained.
    # If the model wasn't trained, we would run main.py here first.
    ("OE2_Modelado_GRU", ["generate_scores.py"]), 
    ("OE3_Agente_DQN", ["main.py"]),
    ("OE3_Agente_DQN", ["evaluate.py"]),
    ("OE4_Auditoria", ["main.py"]),
    ("OE5_Evaluacion", ["main.py"]),
]

log_path = base_dir / "pipeline_execution.log"

print(f"Starting end-to-end pipeline execution...")
start_time = time.time()

with open(log_path, "w", encoding="utf-8") as log_file:
    for folder, cmds in steps:
        target_dir = base_dir / folder
        header = f"\n{'='*60}\n[EXECUTION] {folder} > python {' '.join(cmds)}\n{'='*60}\n"
        log_file.write(header)
        print(f"Running {folder} / {' '.join(cmds)}...")
        
        try:
            result = subprocess.run(
                [str(venv_python)] + cmds,
                cwd=str(target_dir),
                capture_output=True,
                text=True,
                encoding='utf-8',
                errors='replace'
            )
            log_file.write(result.stdout)
            if result.stderr:
                log_file.write("\n[STDERR]\n" + result.stderr)
            log_file.write("\n")
        except Exception as e:
            err_msg = f"\n[SCRIPT ERROR] Failed to run {cmds}: {e}\n"
            log_file.write(err_msg)
            print(err_msg)

elapsed = time.time() - start_time
summary = f"\nPipeline execution completed in {elapsed:.2f} seconds."
print(summary)
with open(log_path, "a", encoding="utf-8") as log_file:
    log_file.write(summary + "\n")
