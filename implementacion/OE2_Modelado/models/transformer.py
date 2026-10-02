import torch
import torch.nn as nn
import math

class PositionalEncoding(nn.Module):
    def __init__(self, embed_dim, max_len=512, dropout=0.1):
        super().__init__()
        self.dropout = nn.Dropout(dropout)
        pe  = torch.zeros(max_len, embed_dim)
        pos = torch.arange(0, max_len).unsqueeze(1).float()
        div = torch.exp(torch.arange(0, embed_dim, 2).float() * (-math.log(10000.0) / embed_dim))
        pe[:, 0::2] = torch.sin(pos * div)
        pe[:, 1::2] = torch.cos(pos * div)
        self.register_buffer('pe', pe.unsqueeze(0))

    def forward(self, x):
        return self.dropout(x + self.pe[:, :x.size(1), :])


class TransformerNavModel(nn.Module):
    """
    Transformer encoder para prediccion de navegacion.
    Estabilizado para datasets pequenos:
      - norm_first=True  (Pre-LN): normaliza ANTES de atencion, evita NaN
      - dropout reducido en encoder layer (0.1 fijo, no heredado)
      - inicializacion de pesos con xavier_uniform
    Referencia: Park & Oh (2023)
    """
    def __init__(self, vocab_size, embed_dim, n_heads, n_layers, dropout, n_classes):
        super().__init__()
        assert embed_dim % n_heads == 0, f"embed_dim ({embed_dim}) debe ser divisible por n_heads ({n_heads})"
        self.embedding   = nn.Embedding(vocab_size + 1, embed_dim, padding_idx=0)
        self.pos_enc     = PositionalEncoding(embed_dim, dropout=dropout)
        enc_layer        = nn.TransformerEncoderLayer(
            d_model         = embed_dim,
            nhead           = n_heads,
            dim_feedforward = embed_dim * 4,
            dropout         = 0.1,       # fijo para evitar inestabilidad
            batch_first     = True,
            norm_first      = True,      # Pre-LN: previene NaN en datasets pequenos
        )
        self.transformer = nn.TransformerEncoder(enc_layer, n_layers,
                                                  enable_nested_tensor=False)
        self.norm_out    = nn.LayerNorm(embed_dim)   # normalizacion extra al final
        self.dropout     = nn.Dropout(dropout)
        self.fc          = nn.Linear(embed_dim, n_classes)

        # Inicializacion de pesos (xavier evita exploding gradients)
        self._init_weights()

    def _init_weights(self):
        nn.init.xavier_uniform_(self.embedding.weight)
        nn.init.xavier_uniform_(self.fc.weight)
        nn.init.zeros_(self.fc.bias)

    def forward(self, x):
        # Mascara de padding: True donde hay cero (padding)
        pad_mask = (x == 0)                                    # (batch, seq_len)
        emb      = self.pos_enc(self.embedding(x))             # (batch, seq_len, embed_dim)
        out      = self.transformer(emb, src_key_padding_mask=pad_mask)
        out      = self.norm_out(out)

        # Pooling: promedio solo sobre tokens no-padding
        mask_f   = (~pad_mask).unsqueeze(-1).float()           # (batch, seq_len, 1)
        pooled   = (out * mask_f).sum(1) / mask_f.sum(1).clamp(min=1e-9)
        return self.fc(self.dropout(pooled))                   # (batch, n_classes)