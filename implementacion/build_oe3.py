import os

out_dir = r"d:\Ciclo X\Investigacion\implementacion\OE3_Agente_DQN"
os.makedirs(out_dir, exist_ok=True)

# 1. config.py
config_code = '''import json
import torch
from pathlib import Path
import numpy as np

DEVICE = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
OE2_DIR = Path("../OE2_Modelado_GRU/output_oe2")
OUTPUT_DIR = Path("output_oe3")
OUTPUT_DIR.mkdir(exist_ok=True)

# Hiperparametros RL
RL_EPISODES  = 3000
STEPS_PER_EP = 20
WARMUP_EPS   = 200

# Arbol de menu
MENU_ERP = [
    {"id": 1, "titulo": "Configuracion", "tipo": "GRUPO", "orden_default": 1, "children": [
        {"id": 111, "titulo": "Gestion de Usuarios", "tipo": "ITEM", "ruta": "configuracion/seguridad/users", "orden_default": 1},
        {"id": 112, "titulo": "Roles de Usuario", "tipo": "ITEM", "ruta": "configuracion/seguridad/roles", "orden_default": 2},
        {"id": 113, "titulo": "Catalogo de Permisos", "tipo": "ITEM", "ruta": "configuracion/seguridad/permisos", "orden_default": 3},
        {"id": 114, "titulo": "Administracion avanzada", "tipo": "ITEM", "ruta": "configuracion/seguridad/admin", "orden_default": 4},
        {"id": 115, "titulo": "Cambio de Clave", "tipo": "ITEM", "ruta": "configuracion/seguridad/cambio-clave", "orden_default": 5},
        {"id": 12, "titulo": "Puntos de Venta", "tipo": "ITEM", "ruta": "configuracion/puntos-venta", "orden_default": 6},
        {"id": 13, "titulo": "Atributos Puntos", "tipo": "ITEM", "ruta": "configuracion/puntos-atributos", "orden_default": 7},
        {"id": 14, "titulo": "Datos Clinicas", "tipo": "ITEM", "ruta": "configuracion/empresas", "orden_default": 8},
        {"id": 15, "titulo": "Sucursales", "tipo": "ITEM", "ruta": "configuracion/sucursales", "orden_default": 9},
        {"id": 16, "titulo": "Catalogo", "tipo": "ITEM", "ruta": "configuracion/catalogo-productos", "orden_default": 10},
        {"id": 17, "titulo": "Atributos Catalogo", "tipo": "ITEM", "ruta": "configuracion/atributos-producto", "orden_default": 11},
        {"id": 18, "titulo": "Utilitarios XNO", "tipo": "ITEM", "ruta": "configuracion/utilitarios", "orden_default": 12},
    ]},
    {"id": 2, "titulo": "Procesos", "tipo": "GRUPO", "orden_default": 2, "children": [
        {"id": 21, "titulo": "Ingresos y salidas", "tipo": "ITEM", "ruta": "procesos/historial-movimientos", "orden_default": 1},
        {"id": 22, "titulo": "Atributos Diversos", "tipo": "ITEM", "ruta": "procesos/atributos-diverso", "orden_default": 2},
        {"id": 23, "titulo": "Reimprimir comprobantes", "tipo": "ITEM", "ruta": "procesos/reimprimir-comprobantes", "orden_default": 3},
        {"id": 24, "titulo": "Detalle de documentos", "tipo": "ITEM", "ruta": "procesos/detalle-documentos", "orden_default": 4},
        {"id": 25, "titulo": "Documento por paciente", "tipo": "ITEM", "ruta": "procesos/documento-paciente", "orden_default": 5},
        {"id": 26, "titulo": "Transferencias", "tipo": "ITEM", "ruta": "procesos/transferencias", "orden_default": 6},
        {"id": 27, "titulo": "Solicitudes de Anulacion", "tipo": "ITEM", "ruta": "procesos/aprobaciones", "orden_default": 7},
    ]},
    {"id": 3, "titulo": "Reportes", "tipo": "GRUPO", "orden_default": 3, "children": [
        {"id": 31, "titulo": "Correlatividad", "tipo": "ITEM", "ruta": "reportes/correlatividad", "orden_default": 1},
        {"id": 32, "titulo": "Reporte de compras", "tipo": "ITEM", "ruta": "reportes/compras", "orden_default": 2},
        {"id": 33, "titulo": "Reporte de ventas", "tipo": "ITEM", "ruta": "reportes/ventas", "orden_default": 3},
        {"id": 34, "titulo": "Reporte de cajas", "tipo": "ITEM", "ruta": "reportes/cajas", "orden_default": 4},
        {"id": 35, "titulo": "Kardex", "tipo": "ITEM", "ruta": "reportes/kardex", "orden_default": 5},
        {"id": 36, "titulo": "Stock valorizado", "tipo": "ITEM", "ruta": "reportes/stock-valorizado", "orden_default": 6},
        {"id": 37, "titulo": "Reporte descuento", "tipo": "ITEM", "ruta": "reportes/descuento", "orden_default": 7},
        {"id": 38, "titulo": "Reporte anulados", "tipo": "ITEM", "ruta": "reportes/comprobantes-anulados", "orden_default": 8},
        {"id": 39, "titulo": "Reporte nota de creditos", "tipo": "ITEM", "ruta": "reportes/nota-credito", "orden_default": 9},
        {"id": 310, "titulo": "Reporte docs anulados", "tipo": "ITEM", "ruta": "reportes/documentos-anulados", "orden_default": 10},
    ]},
    {"id": 4, "titulo": "Compra", "tipo": "GRUPO", "orden_default": 4, "children": [
        {"id": 41, "titulo": "Orden de compra", "tipo": "ITEM", "ruta": "compra/orden", "orden_default": 1},
        {"id": 42, "titulo": "Registrar compra", "tipo": "ITEM", "ruta": "compra/registrar", "orden_default": 2},
        {"id": 43, "titulo": "Intercambio de Productos", "tipo": "ITEM", "ruta": "compra/intercambio", "orden_default": 3},
        {"id": 44, "titulo": "Proveedores", "tipo": "ITEM", "ruta": "compra/proveedores", "orden_default": 4},
        {"id": 45, "titulo": "Cuentas por pagar", "tipo": "ITEM", "ruta": "compra/pagos-pendientes", "orden_default": 5},
        {"id": 46, "titulo": "Estado de cuenta", "tipo": "ITEM", "ruta": "compra/estado-cuenta", "orden_default": 6},
    ]},
    {"id": 5, "titulo": "Ventas", "tipo": "GRUPO", "orden_default": 5, "children": [
        {"id": 51, "titulo": "Listado de Ventas", "tipo": "ITEM", "ruta": "venta/listado", "orden_default": 1},
        {"id": 52, "titulo": "Cotizacion de venta", "tipo": "ITEM", "ruta": "venta/cotizaciones", "orden_default": 2},
        {"id": 53, "titulo": "Notas de venta", "tipo": "ITEM", "ruta": "venta/notas-venta", "orden_default": 3},
        {"id": 54, "titulo": "Registrar venta", "tipo": "ITEM", "ruta": "venta/venta", "orden_default": 4},
        {"id": 55, "titulo": "Anular comprobante", "tipo": "ITEM", "ruta": "venta/anulaciones", "orden_default": 5},
        {"id": 56, "titulo": "Notas de credito", "tipo": "ITEM", "ruta": "venta/notas-credito", "orden_default": 6},
        {"id": 57, "titulo": "Clientes", "tipo": "ITEM", "ruta": "venta/clientes", "orden_default": 7},
        {"id": 58, "titulo": "Devoluciones", "tipo": "ITEM", "ruta": "venta/devoluciones", "orden_default": 8},
        {"id": 591, "titulo": "Gestion de Beneficios", "tipo": "ITEM", "ruta": "venta/descuentos/lista", "orden_default": 9},
        {"id": 592, "titulo": "Convenios", "tipo": "ITEM", "ruta": "venta/descuentos/convenio", "orden_default": 10},
    ]},
    {"id": 6, "titulo": "Facturacion", "tipo": "GRUPO", "orden_default": 6, "children": [
        {"id": 61, "titulo": "Comprobantes electronicos", "tipo": "ITEM", "ruta": "venta/comprobante-electronico", "orden_default": 1},
        {"id": 62, "titulo": "Resumen de boletas", "tipo": "ITEM", "ruta": "facturacion/resumenes", "orden_default": 2},
        {"id": 63, "titulo": "Resumen de bajas", "tipo": "ITEM", "ruta": "facturacion/anulaciones", "orden_default": 3},
    ]},
    {"id": 7, "titulo": "Caja", "tipo": "GRUPO", "orden_default": 7, "children": [
        {"id": 71, "titulo": "Arqueo Caja", "tipo": "ITEM", "ruta": "caja/arqueos", "orden_default": 1},
        {"id": 72, "titulo": "Caja Chica", "tipo": "ITEM", "ruta": "caja/chica", "orden_default": 2},
        {"id": 73, "titulo": "Caja General", "tipo": "ITEM", "ruta": "caja/general", "orden_default": 3},
        {"id": 74, "titulo": "Medio Pago", "tipo": "ITEM", "ruta": "caja/medio-pago", "orden_default": 4},
        {"id": 75, "titulo": "Diferencia de costos", "tipo": "ITEM", "ruta": "caja/diferencia-costos", "orden_default": 5},
        {"id": 76, "titulo": "Gestion de cuentas", "tipo": "ITEM", "ruta": "caja/cuentas", "orden_default": 6},
    ]},
    {"id": 8, "titulo": "Almacen", "tipo": "GRUPO", "orden_default": 8, "children": [
        {"id": 81, "titulo": "Gestion Almacenes", "tipo": "ITEM", "ruta": "almacen/almacenes", "orden_default": 1},
        {"id": 82, "titulo": "Productos", "tipo": "ITEM", "ruta": "almacen/productos", "orden_default": 2},
    ]},
    {"id": 9, "titulo": "Documentos", "tipo": "GRUPO", "orden_default": 9, "children": [
        {"id": 91, "titulo": "Plantillas", "tipo": "ITEM", "ruta": "documentos", "orden_default": 1},
        {"id": 92, "titulo": "Bloques de plantillas", "tipo": "ITEM", "ruta": "documentos/bloques", "orden_default": 2},
        {"id": 93, "titulo": "Puntos de Emision", "tipo": "ITEM", "ruta": "documentos/puntos", "orden_default": 3},
        {"id": 94, "titulo": "Formatos de Impresion", "tipo": "ITEM", "ruta": "documentos/formatos", "orden_default": 4},
    ]},
]

GRUPOS_RUTAS = {
    1: ["configuracion/seguridad/users","configuracion/seguridad/roles","configuracion/seguridad/permisos","configuracion/seguridad/admin","configuracion/sucursales","configuracion/puntos-venta","configuracion/catalogo-productos","configuracion/atributos-producto","configuracion/empresas"],
    2: ["procesos/historial-movimientos","procesos/transferencias"],
    3: ["reportes/ventas","reportes/compras","reportes/cajas","reportes/kardex","reportes/stock-valorizado","reportes/correlatividad","reportes/descuento","reportes/comprobantes-anulados","reportes/nota-credito"],
    4: ["compra/orden","compra/registrar","compra/proveedores","compra/pagos-pendientes","compra/estado-cuenta","compra/intercambio"],
    5: ["venta/listado","venta/cotizaciones","venta/venta","venta/devoluciones","venta/notas-credito","venta/notas-venta","venta/comprobante-electronico","venta/descuentos/lista","venta/descuentos/convenio"],
    6: ["venta/comprobante-electronico"],
    7: ["caja/general","caja/arqueos","caja/cuentas","caja/chica","caja/diferencia-costos","caja/medio-pago"],
    8: ["almacen/productos","almacen/almacenes"],
    9: ["documentos","documentos/bloques","documentos/puntos","documentos/formatos"],
}

PERMISOS_ROL = {
    "Ventas":        [5, 3, 8],
    "Compras":       [4, 3, 8],
    "Logistica":     [8, 3, 2],
    "Caja":          [7, 3, 5],
    "Reportes":      [3, 8, 5, 4],
    "Administrador": [1, 2, 3, 4, 5, 6, 7, 8, 9],
}
'''
with open(os.path.join(out_dir, "config.py"), "w", encoding="utf-8") as f: f.write(config_code)

# 2. env.py
env_code = '''import numpy as np

class GroupMenuEnv:
    """Entorno RL que ordena un GRUPO de items. Acciones limitadas a swaps adyacentes."""
    def __init__(self, n_items, top_k=2):
        self.n_items   = n_items
        self.top_k     = top_k
        self.actions   = [(i, i+1) for i in range(n_items - 1)] if n_items > 1 else [(0, 0)]
        self.n_actions = max(len(self.actions), 1)
        self.menu      = list(range(n_items))
        self.freq_norm = np.zeros(n_items)
        self.target_set = set()

    def reset(self, freq_group):
        self.menu = list(range(self.n_items))
        f = np.array(freq_group[:self.n_items], dtype=np.float64)
        total = f.sum()
        self.freq_norm = f / total if total > 1e-9 else np.ones(self.n_items) / self.n_items
        nonzero_idx    = np.where(self.freq_norm > 1e-9)[0]
        top_k_real     = min(self.top_k, len(nonzero_idx))
        if top_k_real > 0:
            top_idx = nonzero_idx[np.argsort(self.freq_norm[nonzero_idx])[::-1][:top_k_real]]
            self.target_set = set(top_idx.tolist())
        else:
            self.target_set = set()
        return self._state()

    def _shaped_reward(self):
        if not self.target_set: return 1.0
        top_pos = set(self.menu[:len(self.target_set)])
        return len(self.target_set & top_pos) / len(self.target_set)

    def _state(self):
        pos_arr = np.zeros(self.n_items)
        for pos, item in enumerate(self.menu):
            pos_arr[item] = pos / max(self.n_items - 1, 1)
        return np.concatenate([self.freq_norm, pos_arr]).astype(np.float32)

    def step(self, action_idx):
        r_antes  = self._shaped_reward()
        i, j     = self.actions[min(action_idx, len(self.actions)-1)]
        self.menu[i], self.menu[j] = self.menu[j], self.menu[i]
        r_despues = self._shaped_reward()
        reward    = r_despues - r_antes + (0.5 if r_despues >= 1.0 else 0.0)
        return self._state(), reward, r_despues >= 1.0

    @property
    def state_dim(self): return self.n_items * 2
'''
with open(os.path.join(out_dir, "env.py"), "w", encoding="utf-8") as f: f.write(env_code)

# 3. agent.py
agent_code = '''import collections
import random
import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
from config import DEVICE

class QNetwork(nn.Module):
    def __init__(self, state_dim, n_actions, hidden=128):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(state_dim, hidden), nn.LayerNorm(hidden), nn.ReLU(),
            nn.Linear(hidden, hidden),                            nn.ReLU(),
            nn.Linear(hidden, n_actions),
        )
        for layer in self.net:
            if isinstance(layer, nn.Linear):
                nn.init.xavier_uniform_(layer.weight)
                nn.init.zeros_(layer.bias)
    def forward(self, x): return self.net(x)

class ReplayBuffer:
    def __init__(self, cap=20000): self.buf = collections.deque(maxlen=cap)
    def push(self, *t): self.buf.append(t)
    def sample(self, bs):
        s, a, r, ns = zip(*random.sample(self.buf, bs))
        f = lambda x, dt: torch.tensor(np.array(x), dtype=dt).to(DEVICE)
        return f(s, torch.float32), f(a, torch.long), f(r, torch.float32), f(ns, torch.float32)
    def __len__(self): return len(self.buf)

class DQNAgent:
    def __init__(self, state_dim, n_actions):
        self.n_actions = n_actions
        self.gamma = 0.99
        self.eps = 1.0
        self.eps_end = 0.05
        self.eps_decay = 0.995
        self.bs = 64
        self.step_cnt = 0
        self.upd_tgt = 20
        self.q_net = QNetwork(state_dim, n_actions, hidden=128).to(DEVICE)
        self.tgt = QNetwork(state_dim, n_actions, hidden=128).to(DEVICE)
        self.tgt.load_state_dict(self.q_net.state_dict())
        self.tgt.eval()
        self.opt = optim.Adam(self.q_net.parameters(), lr=5e-4)
        self.buf = ReplayBuffer(20000)

    def act(self, state, greedy=False):
        if not greedy and random.random() < self.eps:
            return random.randint(0, self.n_actions - 1)
        s = torch.tensor(state, dtype=torch.float32).unsqueeze(0).to(DEVICE)
        with torch.no_grad():
            return self.q_net(s).argmax(1).item()

    def update(self):
        if len(self.buf) < self.bs: return 0.0
        s, a, r, ns = self.buf.sample(self.bs)
        with torch.no_grad():
            best_a   = self.q_net(ns).argmax(1, keepdim=True)
            next_val = self.tgt(ns).gather(1, best_a).squeeze()
            target   = r + self.gamma * next_val
        q_val = self.q_net(s).gather(1, a.unsqueeze(1)).squeeze()
        loss  = F.smooth_l1_loss(q_val, target)
        self.opt.zero_grad(); loss.backward()
        nn.utils.clip_grad_norm_(self.q_net.parameters(), 1.0)
        self.opt.step()
        self.step_cnt += 1
        if self.step_cnt % self.upd_tgt == 0:
            self.tgt.load_state_dict(self.q_net.state_dict())
        self.eps = max(self.eps_end, self.eps * self.eps_decay)
        return loss.item()
'''
with open(os.path.join(out_dir, "agent.py"), "w", encoding="utf-8") as f: f.write(agent_code)

# 4. main.py
main_code = '''import json
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
'''
with open(os.path.join(out_dir, "main.py"), "w", encoding="utf-8") as f: f.write(main_code)

print("Estructura modular del OE3 creada correctamente.")
