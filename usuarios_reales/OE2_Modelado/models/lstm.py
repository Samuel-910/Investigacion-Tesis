import torch
import torch.nn as nn

class LSTMModel(nn.Module):
    """LSTM para prediccion del siguiente clic de navegacion."""
    def __init__(self, vocab_size, embed_dim, hidden_dim, n_layers, dropout, n_classes):
        super().__init__()
        self.embedding = nn.Embedding(vocab_size + 1, embed_dim, padding_idx=0)
        self.lstm      = nn.LSTM(embed_dim, hidden_dim, n_layers, batch_first=True,
                                 dropout=dropout if n_layers > 1 else 0.0)
        self.dropout   = nn.Dropout(dropout)
        self.fc        = nn.Linear(hidden_dim, n_classes)

    def forward(self, x):
        emb = self.dropout(self.embedding(x))
        out, (hn, cn) = self.lstm(emb)
        return self.fc(self.dropout(out[:, -1, :]))
