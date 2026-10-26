import numpy as np


class GroupMenuEnv:
    """Entorno RL que ordena los ítems de un GRUPO del menú.

    Acción a = "mover el ítem que está en la posición a al inicio del grupo".
    Con esta acción cualquier orden de las primeras k posiciones se alcanza en
    k pasos (antes, con intercambios adyacentes, subir un ítem desde el final de
    un grupo de 12 costaba 11 pasos y el agente casi nunca lo lograba).

    Recompensa: calidad de la cabecera del grupo (primeras top_k posiciones),
    medida como DCG normalizado sobre la frecuencia de uso del usuario:
    1.0 = las funciones más usadas están arriba y en orden de frecuencia.
    """

    def __init__(self, n_items, top_k=4):
        self.n_items = n_items
        self.top_k = min(top_k, n_items)
        self.n_actions = n_items
        self.menu = list(range(n_items))
        self.freq_norm = np.zeros(n_items)
        self.k = self.top_k
        self.pesos = 1.0 / np.log2(np.arange(n_items) + 2)  # descuento por posición (DCG)
        self.ideal = 1.0

    def reset(self, freq_group):
        self.menu = list(range(self.n_items))
        f = np.array(freq_group[:self.n_items], dtype=np.float64)
        total = f.sum()
        self.freq_norm = f / total if total > 1e-9 else np.zeros(self.n_items)
        # Solo se ordenan las funciones que el usuario usa (máx. top_k)
        self.k = min(self.top_k, int((self.freq_norm > 1e-9).sum()))
        orden_ideal = np.sort(self.freq_norm)[::-1][:self.k]
        self.ideal = float((orden_ideal * self.pesos[:self.k]).sum())
        return self._state()

    def _shaped_reward(self):
        if self.k == 0 or self.ideal <= 1e-12:
            return 1.0
        cabecera = self.freq_norm[self.menu[:self.k]]
        return float((cabecera * self.pesos[:self.k]).sum() / self.ideal)

    def _state(self):
        """Estado en el ORDEN ACTUAL del menú, para que la acción p (mover la
        posición p al inicio) se corresponda directo con el dato p:
          - frecuencia del ítem que está en cada posición
          - ranking deseado de ese ítem (1 = el más usado ... 0 = fuera de la cabecera)
        """
        f_pos = self.freq_norm[self.menu]
        orden = np.argsort(-self.freq_norm, kind="stable")[:self.k]
        rank_item = np.zeros(self.n_items)
        for r, item in enumerate(orden):
            rank_item[item] = 1.0 - r / max(self.top_k, 1)
        return np.concatenate([f_pos, rank_item[self.menu]]).astype(np.float32)

    def step(self, action_idx):
        r_antes = self._shaped_reward()
        p = min(int(action_idx), self.n_items - 1)
        item = self.menu.pop(p)
        self.menu.insert(0, item)
        r_despues = self._shaped_reward()
        done = r_despues >= 0.999
        # Mejora de la cabecera + bono al terminar - pequeño costo por paso (prefiere menús cortos)
        reward = (r_despues - r_antes) + (0.5 if done else 0.0) - 0.05
        return self._state(), reward, done

    @property
    def state_dim(self):
        return self.n_items * 2
