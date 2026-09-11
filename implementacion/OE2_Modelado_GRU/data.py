from config import *
import json
import numpy as np
import pandas as pd
import torch
from torch.utils.data import Dataset, DataLoader
from sklearn.model_selection import train_test_split

# Verificar que OE1 haya corrido
archivos_req = [
    "perfiles_usuario.json",
    "vocabulario_rutas.json",
    "frecuencia_por_rol.json",
    "costo_navegacion_baseline.csv",
]
for fn in archivos_req:
    p = OE1_DIR / fn
    if not p.exists():
        raise FileNotFoundError(f"Falta {p} - ejecuta OE1 primero.")
    print(f"   OK {fn}")

# Vocabulario de rutas
with open(OE1_DIR / "vocabulario_rutas.json", encoding="utf-8") as f:
    vocab_data = json.load(f)
all_routes = vocab_data["rutas"]
VOCAB_SIZE = len(all_routes)
route2id   = {r: i for i, r in enumerate(all_routes)}
id2route   = {i: r for i, r in enumerate(all_routes)}
print(f"\nVocabulario: {VOCAB_SIZE} rutas")

# Perfiles de usuario
with open(OE1_DIR / "perfiles_usuario.json", encoding="utf-8") as f:
    perfiles_oe1 = json.load(f)

user_profiles = {}
for p in perfiles_oe1:
    uid      = p["user_id"]
    freq_vec = np.zeros(VOCAB_SIZE)
    for ruta, freq in p["freq_norm"].items():
        if ruta in route2id:
            freq_vec[route2id[ruta]] = freq
    user_profiles[uid] = {
        "role":        p["role"],
        "n_sessions":  p["n_sesiones"],
        "freq_vector": freq_vec,
        "secuencias":  p.get("secuencias", []),
        "top_routes":  {x["ruta"]: x["freq"] for x in p.get("top5_rutas", [])},
    }
print(f"Perfiles cargados: {len(user_profiles)} usuarios")
for uid, p in user_profiles.items():
    print(f"   {uid:35s} rol={p['role']:15s} sesiones={p['n_sessions']:3d}  secuencias={len(p['secuencias'])}")

# Prior por rol
with open(OE1_DIR / "frecuencia_por_rol.json", encoding="utf-8") as f:
    rol_freq_raw = json.load(f)
role_priors = {}
for role, ruta_freq in rol_freq_raw.items():
    vec = np.zeros(VOCAB_SIZE)
    for ruta, freq in ruta_freq.items():
        if ruta in route2id:
            vec[route2id[ruta]] = freq
    role_priors[role] = vec
print(f"Priors por rol: {list(role_priors.keys())}")

# Baseline M1
df_baseline = pd.read_csv(OE1_DIR / "costo_navegacion_baseline.csv", encoding="utf-8-sig")
M1_BASELINE = df_baseline["M1_costo_clics"].mean()
print(f"\nM1 baseline global: {M1_BASELINE:.3f}")
print(f"Meta (-30%):        {M1_BASELINE * 0.7:.3f}")

def build_sequences(user_profiles, route2id, seq_len=SEQ_LEN):
    X, y_list, uids, roles = [], [], [], []
    for uid, p in user_profiles.items():
        for seq in p["secuencias"]:
            ids = [route2id[r] for r in seq if r in route2id]
            if len(ids) < 2:
                continue
            for i in range(1, len(ids)):
                hist = ids[max(0, i - seq_len):i]
                hist = [0] * (seq_len - len(hist)) + hist
                X.append(hist)
                y_list.append(ids[i])
                uids.append(uid)
                roles.append(p["role"])
    return np.array(X), np.array(y_list), uids, roles



class NavDataset(Dataset):
    def __init__(self, X, y):
        self.X = torch.tensor(X, dtype=torch.long)
        self.y = torch.tensor(y, dtype=torch.long)
    def __len__(self): return len(self.X)
    def __getitem__(self, i): return self.X[i], self.y[i]

def get_dataloaders():
    X, y, uids, roles = build_sequences(user_profiles, route2id)
    print(f"Muestras totales: {len(X):,}")
    
    X_train, X_tmp, y_train, y_tmp = train_test_split(X, y, test_size=0.30, random_state=SEED)
    X_val,   X_test, y_val, y_test = train_test_split(X_tmp, y_tmp, test_size=0.50, random_state=SEED)
    
    train_loader = DataLoader(NavDataset(X_train, y_train), batch_size=BATCH_SIZE, shuffle=True)
    val_loader   = DataLoader(NavDataset(X_val,   y_val),   batch_size=BATCH_SIZE)
    test_loader  = DataLoader(NavDataset(X_test,  y_test),  batch_size=BATCH_SIZE)
    
    return train_loader, val_loader, test_loader, user_profiles, role_priors, route2id, id2route, VOCAB_SIZE
