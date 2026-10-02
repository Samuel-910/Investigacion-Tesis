import json
import numpy as np
import torch
import torch.nn.functional as F
from pathlib import Path

from config import *
from models.lstm import LSTMModel
from data import get_dataloaders

print("Cargando datos...")
_, _, _, user_profiles, role_priors, route2id, id2route, VOCAB_SIZE = get_dataloaders()

print("Cargando modelo Campeón (LSTM)...")
model = LSTMModel(VOCAB_SIZE, EMBED_DIM, HIDDEN_DIM, N_LAYERS, DROPOUT, VOCAB_SIZE).to(DEVICE)
model.load_state_dict(torch.load(OUTPUT_DIR / 'lstm_model.pth'))
model.eval()

def compute_scores_all_routes(user_id, model):
    profile  = user_profiles.get(user_id, {})
    freq_vec = profile.get("freq_vector", np.zeros(VOCAB_SIZE))
    role     = profile.get("role", list(role_priors.keys())[0])

    seqs = profile.get("secuencias", [])
    if seqs:
        last_ids = [route2id[r] for r in seqs[-1] if r in route2id]
    else:
        last_ids = []

    # Padding de la secuencia
    seq_pad  = [0] * (SEQ_LEN - len(last_ids)) + last_ids[-SEQ_LEN:]
    x_tensor = torch.tensor([seq_pad], dtype=torch.long).to(DEVICE)
    
    with torch.no_grad():
        model_probs = F.softmax(model(x_tensor), dim=1).cpu().numpy()[0]

    # Composicion Hibrida (Frecuencia Historica + Prediccion de Red Neuronal + Prior del Rol)
    role_vec = role_priors.get(role, np.zeros(VOCAB_SIZE))
    scores   = ALPHA * freq_vec + BETA * model_probs + GAMMA * role_vec
    
    res = []
    for i in range(VOCAB_SIZE):
        res.append({"ruta": id2route[i], "score": round(float(scores[i]), 6)})
    res.sort(key=lambda x: x["score"], reverse=True)
    return res

print("Calculando puntajes de relevancia híbridos para todos los usuarios...")
scores_todos = {}
for uid in user_profiles:
    sc_map = compute_scores_all_routes(uid, model)
    scores_todos[uid] = {
        "role": user_profiles[uid]["role"],
        "top10": sc_map[:10],
        "all_scores": sc_map
    }

out_file = OUTPUT_DIR / "scores_relevancia.json"
with open(out_file, "w", encoding="utf-8") as f:
    json.dump(scores_todos, f, indent=2, ensure_ascii=False)

print(f"✅ Scores calculados exitosamente y guardados en {out_file}")
