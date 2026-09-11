import torch
import torch.nn as nn
import torch.optim as optim
from tqdm import tqdm
from config import DEVICE

def train_epoch(model, loader, optimizer, criterion):
    model.train()
    tl, correct, total = 0.0, 0, 0
    for xb, yb in loader:
        xb, yb = xb.to(DEVICE), yb.to(DEVICE)
        optimizer.zero_grad()
        logits = model(xb)
        loss   = criterion(logits, yb)
        if torch.isnan(loss) or torch.isinf(loss):
            optimizer.zero_grad()
            continue
        loss.backward()
        nn.utils.clip_grad_norm_(model.parameters(), 0.5)
        optimizer.step()
        tl      += loss.item() * len(yb)
        correct += (logits.argmax(1) == yb).sum().item()
        total   += len(yb)
    return (tl / total, correct / total) if total > 0 else (float('inf'), 0.0)

def eval_epoch(model, loader, criterion):
    model.eval()
    tl, correct, total = 0.0, 0, 0
    with torch.no_grad():
        for xb, yb in loader:
            xb, yb = xb.to(DEVICE), yb.to(DEVICE)
            logits = model(xb)
            loss   = criterion(logits, yb)
            if torch.isnan(loss) or torch.isinf(loss):
                continue
            tl      += loss.item() * len(yb)
            correct += (logits.argmax(1) == yb).sum().item()
            total   += len(yb)
    return (tl / total, correct / total) if total > 0 else (float('inf'), 0.0)

def run_training(model, name, train_loader, val_loader, epochs, lr):
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=lr, eps=1e-8, weight_decay=1e-5)
    scheduler = optim.lr_scheduler.ReduceLROnPlateau(optimizer, patience=5, factor=0.5, min_lr=1e-6)
    history   = {"train_loss": [], "val_loss": [], "train_acc": [], "val_acc": []}
    best_loss, best_state = float('inf'), None
    nan_count = 0
    epochs_no_improve = 0
    patience_es = 10

    for ep in tqdm(range(1, epochs + 1), desc=name):
        tl, ta = train_epoch(model, train_loader, optimizer, criterion)
        vl, va = eval_epoch(model,  val_loader,   criterion)

        if torch.isnan(torch.tensor(vl)) or vl == float('inf'):
            nan_count += 1
            if nan_count >= 3:
                print(f"  [{name}] 3 epocas NaN consecutivas deteniendo entrenamiento.")
                break
            continue
        else:
            nan_count = 0

        scheduler.step(vl)
        history["train_loss"].append(tl); history["val_loss"].append(vl)
        history["train_acc"].append(ta);  history["val_acc"].append(va)

        if vl < best_loss:
            best_loss  = vl
            best_state = {k: v.cpu().clone() for k, v in model.state_dict().items()}
            epochs_no_improve = 0
        else:
            epochs_no_improve += 1
            if epochs_no_improve >= patience_es:
                print(f"  [{name}] Early Stopping activado en época {ep}.")
                break

    if best_state is not None:
        model.load_state_dict(best_state)
        print(f"  [{name}] Mejor val_loss: {best_loss:.4f}")
    else:
        print(f"  [{name}] ADVERTENCIA: no se encontro estado valido.")

    return history