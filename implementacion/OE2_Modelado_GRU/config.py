import json
import math
import random
import collections
import warnings
from pathlib import Path
from datetime import datetime

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from tqdm import tqdm

import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
from torch.utils.data import Dataset, DataLoader
from sklearn.model_selection import train_test_split

warnings.filterwarnings("ignore")

# Reproducibilidad
SEED = 42
random.seed(SEED)
np.random.seed(SEED)
torch.manual_seed(SEED)

DEVICE = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print(f'Dispositivo: {DEVICE}')

# Hiperparametros
SEQ_LEN    = 10
EMBED_DIM  = 32
HIDDEN_DIM = 64
N_HEADS    = 4
N_LAYERS   = 2
DROPOUT    = 0.2
BATCH_SIZE = 32
EPOCHS     = 100
LR         = 1e-3

# Rutas
OE1_DIR    = Path("output_oe1")
OUTPUT_DIR = Path("output_oe2")
OUTPUT_DIR.mkdir(exist_ok=True)

# Pesos score compuesto
ALPHA = 0.5   # frecuencia historica
BETA  = 0.3   # prediccion modelo neuronal (GRU/Transformer)
GAMMA = 0.2   # prior por rol

# Parametros DQN
RL_EPISODES  = 500
STEPS_PER_EP = 20

print("Configuracion lista")
print(f"   OE1 input:  {OE1_DIR}/")
print(f"   OE2 output: {OUTPUT_DIR}/")

# Adjust paths for execution from this folder
OE1_DIR = Path('../OE1_Baseline/output_oe1')
OUTPUT_DIR = Path('output_oe2')
OUTPUT_DIR.mkdir(exist_ok=True)
