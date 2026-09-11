# -*- coding: utf-8 -*-
import json

filepath = 'd:/investigacion/Laboratorio/OE2_modelo_comportamiento.ipynb'
with open(filepath, 'r', encoding='utf-8') as f: 
    data = json.load(f)

changed = False
for cell in data['cells']:
    if cell['cell_type'] == 'code':
        new_source = []
        skip_lines = False
        for line in cell['source']:
            if 'INPUT_LOGS =' in line:
                new_source.extend([
                    '# El OE2 debe consumir lo que genero el OE1 para mantener la coherencia\n',
                    'INPUT_TRANSICIONES = \"output_oe1/matriz_transiciones.csv\"\n',
                    'df = pd.read_csv(INPUT_TRANSICIONES)\n',
                    'rutas_unicas = sorted(set(df[\'origen\'].tolist() + df[\'destino\'].tolist()))\n',
                    'rutas_unicas.insert(0, \"<PAD>\")\n',
                    'vocab_size = len(rutas_unicas)\n',
                    'route_to_idx = {ruta: i for i, ruta in enumerate(rutas_unicas)}\n',
                    'idx_to_route = {i: ruta for i, ruta in enumerate(rutas_unicas)}\n',
                    'log.info(f\"Rutas unicas encontradas (Vocabulario): {vocab_size}\")\n',
                    'SEQ_LENGTH = 5\n',
                    'sequences = []\n',
                    'targets = []\n',
                    'for user_id, group in df.groupby(\'user_id\'):\n',
                    '    user_routes = group[\'origen\'].tolist() + [group[\'destino\'].iloc[-1]]\n',
                    '    user_indices = [route_to_idx[r] for r in user_routes]\n',
                    '    for i in range(len(user_indices) - SEQ_LENGTH):\n',
                    '        seq = user_indices[i : i + SEQ_LENGTH]\n',
                    '        target = user_indices[i + SEQ_LENGTH]\n',
                    '        sequences.append(seq)\n',
                    '        targets.append(target)\n'
                ])
                skip_lines = True
                changed = True
            elif skip_lines and ('dataset = NavDataset' in line):
                skip_lines = False
                new_source.append(line)
            elif not skip_lines:
                new_source.append(line)
        cell['source'] = new_source

if changed:
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=1, ensure_ascii=False)
    print(f'Updated {filepath}')
else:
    print(f'No changes made to {filepath}')
