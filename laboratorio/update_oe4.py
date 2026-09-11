import sys
import json

file_path = r'd:\investigacion\Laboratorio\OE4_Pipeline_PPI_C9_2026_v2.ipynb'
with open(file_path, 'r', encoding='utf-8') as f:
    nb = json.load(f)

for cell in nb['cells']:
    if cell['cell_type'] == 'code':
        source = cell['source']
        new_source = []
        for line in source:
            if '"evaluacion_m1.csv"]),\n' in line:
                line = line.replace('"evaluacion_m1.csv"]),\n', '"evaluacion_m1.csv", "evaluacion_m1.json", "resumen_oe3.json"]),\n')
            
            if '("OE3 ordenes_db.csv",' in line:
                line += '    ("OE3 evaluacion_m1.csv",           OE3_OUTPUT / "evaluacion_m1.csv"),\n'
                line += '    ("OE3 evaluacion_m1.json",          OE3_OUTPUT / "evaluacion_m1.json"),\n'
                line += '    ("OE3 resumen_oe3.json",            OE3_OUTPUT / "resumen_oe3.json"),\n'
            
            new_source.append(line)
        cell['source'] = new_source

with open(file_path, 'w', encoding='utf-8') as f:
    json.dump(nb, f, indent=1)

print('OE4 modificado correctamente.')
