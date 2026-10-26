import json
import argparse
import torch
import torch.optim as optim
from pathlib import Path

from config import *
from data import get_dataloaders
from models.gru import GRUModel
from models.transformer import TransformerNavModel
from models.lstm import LSTMModel
from models.cnn1d import CNN1DModel
from train import run_training

def evaluar_test(model, loader):
    """Loss, accuracy top-1 y top-3 en el conjunto de prueba (15 %)."""
    model.eval()
    crit = torch.nn.CrossEntropyLoss(reduction='sum')
    loss, c1, c3, n = 0.0, 0, 0, 0
    with torch.no_grad():
        for xb, yb in loader:
            xb, yb = xb.to(DEVICE), yb.to(DEVICE)
            logits = model(xb)
            loss += crit(logits, yb).item()
            top3 = logits.topk(3, dim=1).indices
            c1 += (top3[:, 0] == yb).sum().item()
            c3 += (top3 == yb.unsqueeze(1)).any(1).sum().item()
            n += len(yb)
    return {"loss": loss / n, "acc@1": c1 / n, "acc@3": c3 / n, "n_test": n}


def main():
    parser = argparse.ArgumentParser(description="Entrenador Modular OE2")
    parser.add_argument('--model', type=str, default='all', 
                        choices=['all', 'gru', 'lstm', 'cnn1d', 'transformer'],
                        help='Modelo especifico a entrenar')
    args = parser.parse_args()

    print("=" * 60)
    print(f"  INICIANDO ENTRENAMIENTO: {args.model.upper()}")
    print("=" * 60)
    
    train_loader, val_loader, test_loader, user_profiles, role_priors, route2id, id2route, VOCAB_SIZE = get_dataloaders()
    
    modelos_a_entrenar = ['gru', 'lstm', 'cnn1d', 'transformer'] if args.model == 'all' else [args.model]
    metricas_path = OUTPUT_DIR / 'metricas_test.json'
    metricas = json.loads(metricas_path.read_text(encoding='utf-8')) if metricas_path.exists() else {}

    for m in modelos_a_entrenar:
        print(f"\n[{m.upper()}] Inicializando arquitectura...")
        if m == 'gru':
            model = GRUModel(VOCAB_SIZE, EMBED_DIM, HIDDEN_DIM, N_LAYERS, DROPOUT, VOCAB_SIZE).to(DEVICE)
            lr = 1e-3
        elif m == 'lstm':
            model = LSTMModel(VOCAB_SIZE, EMBED_DIM, HIDDEN_DIM, N_LAYERS, DROPOUT, VOCAB_SIZE).to(DEVICE)
            lr = 1e-3
        elif m == 'cnn1d':
            model = CNN1DModel(VOCAB_SIZE, EMBED_DIM, HIDDEN_DIM, DROPOUT, VOCAB_SIZE).to(DEVICE)
            lr = 1e-3
        elif m == 'transformer':
            model = TransformerNavModel(VOCAB_SIZE, EMBED_DIM, N_HEADS, N_LAYERS, DROPOUT, VOCAB_SIZE).to(DEVICE)
            model._init_weights()
            lr = 5e-4
            
        print(f"Parametros a entrenar: {sum(p.numel() for p in model.parameters()):,}")
        
        hist = run_training(model, m.upper(), train_loader, val_loader, epochs=EPOCHS, lr=lr)
        
        # Guardado independiente para no chocar si corres varios en paralelo
        print(f"[{m.upper()}] Guardando modelo e historial...")
        torch.save(model.state_dict(), str(OUTPUT_DIR / f'{m}_model.pth'))
        with open(OUTPUT_DIR / f'historial_{m}.json', 'w', encoding='utf-8') as f:
            json.dump(hist, f, indent=2)

        metricas[m] = evaluar_test(model, test_loader)
        print(f"[{m.upper()}] TEST -> loss={metricas[m]['loss']:.4f}  "
              f"acc@1={metricas[m]['acc@1']:.4f}  acc@3={metricas[m]['acc@3']:.4f}")
        metricas_path.write_text(json.dumps(metricas, indent=2), encoding='utf-8')

    print("=" * 60)
    print("[OK] ENTRENAMIENTO FINALIZADO.")
    print("Para unificar y graficar el benchmark, corre: py plot_benchmark.py")

if __name__ == '__main__':
    main()
