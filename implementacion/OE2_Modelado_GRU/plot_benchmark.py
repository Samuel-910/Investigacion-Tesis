import json
import matplotlib.pyplot as plt
from pathlib import Path

OUTPUT_DIR = Path('output_oe2')
modelos = ['gru', 'lstm', 'cnn1d', 'transformer']
colores = {'gru': 'blue', 'lstm': 'green', 'cnn1d': 'red', 'transformer': 'orange'}

historiales = {}

for m in modelos:
    path = OUTPUT_DIR / f'historial_{m}.json'
    if path.exists():
        with open(path, 'r', encoding='utf-8') as f:
            historiales[m] = json.load(f)

if not historiales:
    print("❌ No hay historiales guardados aún. Entrena algún modelo primero.")
    exit()

print(f"Generando benchmark para: {list(historiales.keys())}")

plt.figure(figsize=(14, 6))

# Subplot 1: Loss
plt.subplot(1, 2, 1)
for m, hist in historiales.items():
    plt.plot(hist["val_loss"], label=f'{m.upper()} Val', color=colores[m], linewidth=2)
    plt.plot(hist["train_loss"], alpha=0.2, color=colores[m])
plt.title('Pérdida (Loss) por Época')
plt.xlabel('Época')
plt.ylabel('Loss (CrossEntropy)')
plt.legend()
plt.grid(True, alpha=0.3)

# Subplot 2: Accuracy
plt.subplot(1, 2, 2)
for m, hist in historiales.items():
    plt.plot(hist["val_acc"], label=f'{m.upper()} Val', color=colores[m], linewidth=2)
    plt.plot(hist["train_acc"], alpha=0.2, color=colores[m])
plt.title('Precisión (Accuracy) por Época')
plt.xlabel('Época')
plt.ylabel('Accuracy')
plt.legend()
plt.grid(True, alpha=0.3)

plt.tight_layout()
plot_path = OUTPUT_DIR / 'curvas_entrenamiento.png'
plt.savefig(plot_path)

print("=" * 60)
print(f"✅ Benchmark graficado exitosamente.")
print(f"Revisa la imagen en: {plot_path}")
print("=" * 60)
