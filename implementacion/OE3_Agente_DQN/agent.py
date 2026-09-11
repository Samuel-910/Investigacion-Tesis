import collections
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
