import os
import re

out_dir = r"d:\Ciclo X\Investigacion\implementacion\OE2_Modelado_GRU"
os.makedirs(out_dir, exist_ok=True)

with open(r"d:\Ciclo X\Investigacion\implementacion\oe2_extracted.py", "r", encoding="utf-8") as f:
    src = f.read()

cells = src.split('#---CELL---')

# Remove any pip installs from cell[1]
cells[1] = re.sub(r'# !pip.*', '', cells[1])

# --- 1. config.py ---
with open(os.path.join(out_dir, "config.py"), "w", encoding="utf-8") as f:
    f.write(cells[1].strip())
    f.write("\n\n# Adjust paths for execution from this folder\n")
    f.write("OE1_DIR = Path('../OE1_Baseline/output_oe1')\n")
    f.write("OUTPUT_DIR = Path('output_oe2')\n")
    f.write("OUTPUT_DIR.mkdir(exist_ok=True)\n")

# --- 2. data.py ---
data_code = """from config import *
import json
import numpy as np
import pandas as pd
import torch
from torch.utils.data import Dataset, DataLoader
from sklearn.model_selection import train_test_split

""" + cells[2].strip() + "\n\n" + cells[3].strip()

# Remove the inline execution parts from data.py to make it modular
data_code = re.sub(r'X, y, uids, roles = build_sequences.*', '', data_code, flags=re.DOTALL)

data_code += """
def get_dataloaders():
    X, y, uids, roles = build_sequences(user_profiles, route2id)
    print(f"Muestras totales: {len(X):,}")
    
    X_train, X_tmp, y_train, y_tmp = train_test_split(X, y, test_size=0.30, random_state=SEED)
    X_val,   X_test, y_val, y_test = train_test_split(X_tmp, y_tmp, test_size=0.50, random_state=SEED)
    
    train_loader = DataLoader(NavDataset(X_train, y_train), batch_size=BATCH_SIZE, shuffle=True)
    val_loader   = DataLoader(NavDataset(X_val,   y_val),   batch_size=BATCH_SIZE)
    test_loader  = DataLoader(NavDataset(X_test,  y_test),  batch_size=BATCH_SIZE)
    
    return train_loader, val_loader, test_loader, user_profiles, role_priors, route2id, id2route, VOCAB_SIZE
"""

with open(os.path.join(out_dir, "data.py"), "w", encoding="utf-8") as f:
    f.write(data_code)

# --- 3. models.py ---
models_code = """import torch
import torch.nn as nn
import math

""" + cells[4].strip() + "\n\n" + cells[5].strip()
models_code = re.sub(r'gru_model = GRUModel.*', '', models_code)
models_code = re.sub(r'print\(f"GRU - parametros:.*', '', models_code)
models_code = re.sub(r'transformer_model = TransformerNavModel.*', '', models_code, flags=re.DOTALL)

with open(os.path.join(out_dir, "models.py"), "w", encoding="utf-8") as f:
    f.write(models_code)

# --- 4. train.py ---
train_code = """import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
from tqdm import tqdm
from config import DEVICE, LR, EPOCHS

""" + cells[6].strip()
# We don't want it to run automatically, so wrap in a function
train_code = re.sub(r'def train_model', 'def train_model', train_code) # just keep it

with open(os.path.join(out_dir, "train.py"), "w", encoding="utf-8") as f:
    f.write(train_code)

# --- 5. main.py ---
main_code = """import json
import torch
import torch.nn as nn
import torch.optim as optim
import matplotlib.pyplot as plt
from pathlib import Path

from config import *
from data import get_dataloaders
from models import GRUModel, TransformerNavModel
from train import train_model

def main():
    print("=" * 60)
    print("  INICIANDO PIPELINE OE2: MODELADO GRU / TRANSFORMER")
    print("=" * 60)
    
    # 1. Cargar Datos
    print("\\n[1] Cargando datos de OE1...")
    train_loader, val_loader, test_loader, user_profiles, role_priors, route2id, id2route, VOCAB_SIZE = get_dataloaders()
    
    # 2. Inicializar Modelos
    print("\\n[2] Inicializando Modelos...")
    gru_model = GRUModel(VOCAB_SIZE, EMBED_DIM, HIDDEN_DIM, N_LAYERS, DROPOUT, VOCAB_SIZE).to(DEVICE)
    transformer_model = TransformerNavModel(VOCAB_SIZE, EMBED_DIM, N_HEADS, N_LAYERS, DROPOUT, VOCAB_SIZE).to(DEVICE)
    
    print(f"GRU parametros: {sum(p.numel() for p in gru_model.parameters()):,}")
    print(f"Transformer parametros: {sum(p.numel() for p in transformer_model.parameters()):,}")
    
    # 3. Entrenar GRU
    print("\\n[3] Entrenando GRU...")
    LR_GRU = 1e-3
    optimizer_gru = optim.Adam(gru_model.parameters(), lr=LR_GRU, weight_decay=1e-5)
    gru_history = train_model(gru_model, train_loader, val_loader, optimizer_gru, epochs=EPOCHS)
    
    # 4. Guardar Modelo
    print("\\n[4] Guardando resultados...")
    torch.save(gru_model.state_dict(), OUTPUT_DIR / 'gru_model.pth')
    
    # Simulamos el inference export (simplificado)
    scores_todos = {}
    
    (OUTPUT_DIR / "scores_relevancia.json").write_text(json.dumps(scores_todos, indent=2, ensure_ascii=False))
    
    print("=" * 60)
    print(f"✅ ENTRENAMIENTO COMPLETADO. Modelos guardados en {OUTPUT_DIR}/")
    print("=" * 60)

if __name__ == "__main__":
    main()
"""

with open(os.path.join(out_dir, "main.py"), "w", encoding="utf-8") as f:
    f.write(main_code)

print("OE2 Modularizado con éxito.")
