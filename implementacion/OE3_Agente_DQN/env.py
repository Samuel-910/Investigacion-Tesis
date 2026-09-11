import numpy as np

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
