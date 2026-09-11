import json
import torch
import random
import numpy as np
from pathlib import Path

# ==========================================
# REPRODUCIBILIDAD (Para la Tesis)
# ==========================================
SEED = 42
random.seed(SEED)
np.random.seed(SEED)
torch.manual_seed(SEED)
if torch.cuda.is_available():
    torch.cuda.manual_seed_all(SEED)

DEVICE = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
OE2_DIR = Path("../OE2_Modelado_GRU/output_oe2")
OUTPUT_DIR = Path("output_oe3")
OUTPUT_DIR.mkdir(exist_ok=True)

# Hiperparametros RL
RL_EPISODES  = 3000
STEPS_PER_EP = 20
WARMUP_EPS   = 200

# ==========================================
# UNIVERSALIDAD (Carga dinamica de cualquier ERP)
# ==========================================
UNIVERSAL_CONFIG_PATH = Path("../erp_config_universal.json")
with open(UNIVERSAL_CONFIG_PATH, "r", encoding="utf-8") as f:
    _erp_data = json.load(f)

MENU_ERP = _erp_data["MENU_ERP"]
GRUPOS_RUTAS = {int(k): v for k, v in _erp_data["GRUPOS_RUTAS"].items()}
PERMISOS_ROL = _erp_data["PERMISOS_ROL"]
