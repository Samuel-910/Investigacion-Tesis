import torch
import torch.nn as nn
import torch.nn.functional as F

class CNN1DModel(nn.Module):
    """CNN 1D para extraccion de patrones locales (n-gramas) en secuencias de navegacion."""
    def __init__(self, vocab_size, embed_dim, hidden_dim, dropout, n_classes):
        super().__init__()
        self.embedding = nn.Embedding(vocab_size + 1, embed_dim, padding_idx=0)
        # Conv1d espera tensores con forma [batch, channels, length]
        self.conv1 = nn.Conv1d(in_channels=embed_dim, out_channels=hidden_dim, kernel_size=3, padding=1)
        self.conv2 = nn.Conv1d(in_channels=hidden_dim, out_channels=hidden_dim, kernel_size=3, padding=1)
        self.dropout = nn.Dropout(dropout)
        self.fc = nn.Linear(hidden_dim, n_classes)

    def forward(self, x):
        # x shape: [batch, seq_len]
        emb = self.embedding(x)                     # [batch, seq_len, embed_dim]
        emb = emb.permute(0, 2, 1)                  # [batch, embed_dim, seq_len]
        
        out = F.relu(self.conv1(emb))
        out = self.dropout(out)
        out = F.relu(self.conv2(out))
        
        # Global Max Pooling (extrae la caracteristica mas fuerte de toda la secuencia)
        pooled, _ = torch.max(out, dim=2)           # [batch, hidden_dim]
        
        return self.fc(self.dropout(pooled))
