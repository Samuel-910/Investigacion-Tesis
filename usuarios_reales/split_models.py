import os
import re

base_dir = r"d:\Ciclo X\Investigacion\implementacion\OE2_Modelado_GRU"
models_dir = os.path.join(base_dir, "models_arch") # Named differently to avoid collision with models.py before deleting
os.makedirs(models_dir, exist_ok=True)
open(os.path.join(models_dir, "__init__.py"), "w").close()

with open(os.path.join(base_dir, "models.py"), "r", encoding="utf-8") as f:
    src = f.read()

# Splitting logic
# Find GRUModel class
gru_match = re.search(r'(class GRUModel.*?)(?=class PositionalEncoding)', src, flags=re.DOTALL)
if gru_match:
    gru_code = "import torch\nimport torch.nn as nn\n\n" + gru_match.group(1).strip()
    with open(os.path.join(models_dir, "gru.py"), "w", encoding="utf-8") as f:
        f.write(gru_code)

# Find Transformer classes
trans_match = re.search(r'(class PositionalEncoding.*)', src, flags=re.DOTALL)
if trans_match:
    trans_code = "import torch\nimport torch.nn as nn\nimport math\n\n" + trans_match.group(1).strip()
    with open(os.path.join(models_dir, "transformer.py"), "w", encoding="utf-8") as f:
        f.write(trans_code)

# Update main.py imports
with open(os.path.join(base_dir, "main.py"), "r", encoding="utf-8") as f:
    main_src = f.read()

main_src = main_src.replace("from models import GRUModel, TransformerNavModel", 
                            "from models_arch.gru import GRUModel\nfrom models_arch.transformer import TransformerNavModel")

with open(os.path.join(base_dir, "main.py"), "w", encoding="utf-8") as f:
    f.write(main_src)

print("Split success")
