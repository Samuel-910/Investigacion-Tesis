import json
import copy
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import random
import torch

from config import *
from env import GroupMenuEnv
from agent import DQNAgent

def load_data():
    with open(OE2_DIR / "scores_relevancia.json", encoding="utf-8-sig") as f:
        scores_oe2 = json.load(f)
    with open("../OE1_Baseline/output_oe1/vocabulario_rutas.json", encoding="utf-8") as f:
        vocab_data = json.load(f)
    route2id = {r: i for i, r in enumerate(vocab_data["rutas"])}
    
    with open("../OE1_Baseline/output_oe1/perfiles_usuario.json", encoding="utf-8") as f:
        perfiles_oe1 = json.load(f)
        
    user_profiles = {}
    VOCAB_SIZE = len(vocab_data["rutas"])
    for p in perfiles_oe1:
        uid = p["user_id"]
        freq_vec = np.zeros(VOCAB_SIZE)
        for ruta, freq in p["freq_norm"].items():
            if ruta in route2id:
                freq_vec[route2id[ruta]] = freq
        user_profiles[uid] = {"role": p["role"], "freq_vector": freq_vec}
        
    return scores_oe2, user_profiles, route2id, VOCAB_SIZE

def train_dqn(scores_oe2, user_profiles, route2id):
    group_sizes = sorted(set(len(v) for v in GRUPOS_RUTAS.values()))
    envs = {gs: GroupMenuEnv(gs, top_k=min(2, gs)) for gs in group_sizes}
    agents = {gs: DQNAgent(envs[gs].state_dim, envs[gs].n_actions) for gs in group_sizes}
    
    shaped_history = {gs: [] for gs in group_sizes}
    user_list = list(user_profiles.keys())
    
    print(f"Entrenando Agentes DQN ({RL_EPISODES} episodios)...")
    for ep in range(RL_EPISODES):
        uid = random.choice(user_list)
        freq_vec = user_profiles[uid]["freq_vector"]
        ep_shaped = {gs: [] for gs in group_sizes}
        
        for grupo_id, rutas_grupo in GRUPOS_RUTAS.items():
            freq_grupo = np.array([freq_vec[route2id[r]] if r in route2id else 0.0 for r in rutas_grupo])
            gs = len(rutas_grupo)
            env_g = envs[gs]
            ag = agents[gs]
            
            s = env_g.reset(freq_grupo)
            for step in range(STEPS_PER_EP):
                a = ag.act(s)
                ns, r, done = env_g.step(a)
                ag.buf.push(s, a, r, ns)
                if ep >= WARMUP_EPS:
                    ag.update()
                s = ns
                if done: break
            ep_shaped[gs].append(env_g._shaped_reward())
            
        for gs in group_sizes:
            shaped_history[gs].append(np.mean(ep_shaped[gs]) if ep_shaped[gs] else 0)
            
        if (ep + 1) % 500 == 0:
            print(f"  Ep {ep+1:4d} | Epsilon: {agents[group_sizes[0]].eps:.3f}")

    for gs, ag in agents.items():
        torch.save(ag.q_net.state_dict(), OUTPUT_DIR / f"dqn_agent_gs{gs}.pt")
    
    return agents, envs, shaped_history

def main():
    print("=" * 60)
    print("  INICIANDO PIPELINE OE3: AGENTE DE REINFORCEMENT LEARNING")
    print("=" * 60)
    
    scores_oe2, user_profiles, route2id, VOCAB_SIZE = load_data()
    agents, envs, history = train_dqn(scores_oe2, user_profiles, route2id)
    
    print("✅ Entrenamiento DQN completado. Modelos guardados.")
    print("El siguiente paso será aplicar estos agentes a los menús reales para calcular el M1_DQN.")

if __name__ == '__main__':
    main()
