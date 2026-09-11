import json
from pathlib import Path
import pandas as pd

base = Path(r'd:\investigacion\Laboratorio')

print('--- Estado de los Archivos Generados ---')
for oe in range(1, 6):
    folder = base / f'output_oe{oe}'
    print(f'\n[OE{oe}] {folder.name}:')
    if not folder.exists():
        print('  No existe la carpeta.')
        continue
    
    files = list(folder.glob('*.*'))
    for f in files:
        size = f.stat().st_size / 1024
        print(f'  - {f.name} ({size:.1f} KB)')

print('\n--- Verificacion de Consistencia ---')

try:
    oe1_base = pd.read_csv(base / 'output_oe1/costo_navegacion_baseline.csv')
    print(f'Usuarios OE1: {len(oe1_base)}')
except Exception as e:
    print('Error leyendo OE1:', e)

try:
    oe3_eval = pd.read_csv(base / 'output_oe3/evaluacion_m1.csv')
    print(f'Usuarios OE3: {len(oe3_eval)}')
    print(f'Reduccion M1 (Promedio global): {oe3_eval["reduccion_pct"].mean():.2f}%')
    print(f'Reduccion M1 (Maximo): {oe3_eval["reduccion_pct"].max():.2f}%')
except Exception as e:
    print('Error leyendo OE3:', e)

try:
    oe5_res = json.loads((base / 'output_oe5/resumen_oe5.json').read_text(encoding='utf-8'))
    print('\nResumen OE5:')
    for k, v in oe5_res.items():
        print(f'  {k}: {v}')
except Exception as e:
    print('Error leyendo OE5:', e)
