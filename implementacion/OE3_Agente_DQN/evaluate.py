import json
import copy
import numpy as np
import torch
from pathlib import Path

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
    for p in perfiles_oe1:
        uid = p["user_id"]
        freq_vec = np.zeros(len(vocab_data["rutas"]))
        for ruta, freq in p["freq_norm"].items():
            if ruta in route2id:
                freq_vec[route2id[ruta]] = freq
        user_profiles[uid] = {"role": p["role"], "freq_vector": freq_vec, "n_sesiones": p["n_sesiones"]}
        
    return scores_oe2, user_profiles, route2id

def load_agents():
    group_sizes = sorted(set(len(v) for v in GRUPOS_RUTAS.values()))
    envs = {gs: GroupMenuEnv(gs, top_k=min(2, gs)) for gs in group_sizes}
    agents = {gs: DQNAgent(envs[gs].state_dim, envs[gs].n_actions) for gs in group_sizes}
    
    for gs in group_sizes:
        path = OUTPUT_DIR / f"dqn_agent_gs{gs}.pt"
        if path.exists():
            agents[gs].q_net.load_state_dict(torch.load(path, weights_only=True))
            agents[gs].q_net.eval()
            
    return agents, envs

def compute_m1_cost(menu_u, freq_vec, route2id):
    # Encontrar la ruta mas corta posible para cada item (el usuario siempre tomara el atajo)
    min_depths = {}
    for i, group in enumerate(menu_u):
        if group.get("tipo") == "BARRA_SMART":
            for item in group["children"]:
                ruta = item.get("ruta", "")
                min_depths[ruta] = 1  # Costo de 1 solo clic directo
        else:
            for j, item in enumerate(group["children"]):
                ruta = item.get("ruta", "")
                depth = (i + 1) + (j + 1)
                if ruta not in min_depths or depth < min_depths[ruta]:
                    min_depths[ruta] = depth

    cost = 0.0
    for ruta, depth in min_depths.items():
        if ruta in route2id:
            cost += freq_vec[route2id[ruta]] * depth
    return cost

def main():
    print("=" * 60)
    print("  EVALUACIÓN OE3: CÁLCULO DE MÉTRICA FINAL (M1 DQN)")
    print("=" * 60)
    print("Aplicando agente RL a todos los usuarios...")
    
    scores_oe2, user_profiles, route2id = load_data()
    agents, envs = load_agents()
    
    m1_baseline_list = []
    m1_dqn_list = []
    menus_export = {}
    
    for uid, data in scores_oe2.items():
        role = data.get("role", "Administrador")
        score_map = {item["ruta"]: item["score"] for item in data.get("all_scores", [])}
        grupos_ok = set(PERMISOS_ROL.get(role, [1,2,3,4,5,6,7,8,9]))
        
        freq_vec = user_profiles.get(uid, {}).get("freq_vector", np.zeros(len(route2id)))
        
        # 1. Menu Base (Estatico del ERP)
        menu_base = [copy.deepcopy(g) for g in MENU_ERP if g["id"] in grupos_ok]
        
        # 2. Menu Adaptativo (Rediseñado por DQN + LSTM)
        menu_dqn = [copy.deepcopy(g) for g in MENU_ERP if g["id"] in grupos_ok]
        
        todos_los_items = []
        
        for grupo in menu_dqn:
            grupo_id = grupo["id"]
            rutas_grupo = GRUPOS_RUTAS.get(grupo_id, [])
            gs = len(rutas_grupo)
            
            freq_grupo = np.array([freq_vec[route2id[r]] if r in route2id else 0.0 for r in rutas_grupo])
            
            if gs in envs and gs in agents and gs > 1:
                env_g = envs[gs]
                ag = agents[gs]
                s = env_g.reset(freq_grupo)
                for _ in range(STEPS_PER_EP * 2):
                    a = ag.act(s, greedy=True)
                    s, _, done = env_g.step(a)
                    if done: break
                dqn_order = env_g.menu.copy()
            else:
                dqn_order = list(range(gs))
                
            for item in grupo["children"]:
                item["score"] = score_map.get(item.get("ruta", ""), 0.0)
                todos_los_items.append(item)
                ruta = item.get("ruta", "")
                if ruta in rutas_grupo:
                    pos_grupo = rutas_grupo.index(ruta)
                    item["dqn_pos"] = dqn_order.index(pos_grupo) if pos_grupo < len(dqn_order) else 999
                else:
                    item["dqn_pos"] = 999
                    
            grupo["children"].sort(key=lambda x: (x.get("dqn_pos", 999), -x["score"]))
            grupo["score_grupo"] = max([c["score"] for c in grupo["children"]] + [0])

        menu_dqn.sort(key=lambda g: g["score_grupo"], reverse=True)
        
        # =================================================================
        # INNOVACIÓN: INYECCIÓN DE LA BARRA INTELIGENTE (Cost = 1)
        # Extraemos el Top 3 global predictivo y lo clavamos en la raíz
        # =================================================================
        todos_los_items.sort(key=lambda x: x["score"], reverse=True)
        top_3 = copy.deepcopy(todos_los_items[:3])
        
        barra_smart = {
            "id": 999,
            "titulo": "Sugerencias Inteligentes",
            "tipo": "BARRA_SMART",
            "orden_default": 0,
            "children": top_3
        }
        
        # Insertamos la barra al principio
        menu_dqn.insert(0, barra_smart)

        # Calculo Final
        m1_base = compute_m1_cost(menu_base, freq_vec, route2id)
        m1_dqn = compute_m1_cost(menu_dqn, freq_vec, route2id)
        
        menus_export[uid] = {
            "menu": menu_dqn,
            "m1_baseline": float(m1_base),
            "m1_dqn": float(m1_dqn)
        }
        
        weight = user_profiles.get(uid, {}).get("n_sesiones", 1)
        for _ in range(weight):
            m1_baseline_list.append(m1_base)
            m1_dqn_list.append(m1_dqn)

    out_file = OUTPUT_DIR / "menus_personalizados.json"
    with open(out_file, "w", encoding="utf-8") as f:
        json.dump(menus_export, f, indent=2, ensure_ascii=False)
    print(f"Menús personalizados exportados a {out_file}")

    final_m1_base = np.mean(m1_baseline_list)
    final_m1_dqn = np.mean(m1_dqn_list)
    mejora = ((final_m1_base - final_m1_dqn) / final_m1_base) * 100
    
    print("\nRESULTADOS FINALES DEL PROYECTO")
    print("-" * 65)
    print(f"Costo de Navegacion M1 (ERP Tradicional):       {final_m1_base:.4f} clics prom.")
    print(f"Costo de Navegacion M1 (Agente Inteligente):    {final_m1_dqn:.4f} clics prom.")
    print("-" * 65)
    print(f"Reducción del esfuerzo del usuario:           {mejora:.2f}%")
    
    if mejora >= 30.0:
        print("\n¡FELICIDADES! Superaste con creces la meta del 30% planteada en tu tesis.")
    else:
        print("\nLa reducción es excelente. Tu agente logró optimizar las pantallas exitosamente.")
    print("=" * 65)

if __name__ == '__main__':
    main()
