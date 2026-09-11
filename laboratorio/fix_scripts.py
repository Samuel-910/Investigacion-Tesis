import os
import re

base_dir = r"D:\RECUPERADOS 2026\DOCUMENTOS\GitHub\articulos\articulos-backend\src\main\java\com\pe\articulos\pipelines"
files = ["oe1_pipeline.py", "oe2_pipeline.py", "oe3_pipeline.py"]

for fname in files:
    path = os.path.join(base_dir, fname)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Remove %pip and !pip
    content = re.sub(r'^(%|!).*$', '', content, flags=re.MULTILINE)
    
    # Fix OUTPUT_DIR to point to the correct path in the backend
    # Example: OUTPUT_DIR = Path("output_oe1") -> OUTPUT_DIR = Path("src/main/java/com/pe/articulos/pipelines/output_oe1")
    content = re.sub(r'OUTPUT_DIR\s*=\s*Path\("([^"]+)"\)', r'OUTPUT_DIR = Path("src/main/java/com/pe/articulos/pipelines/\1")', content)

    # In OE1, fix input_path to point to the absolute ndjson file
    if fname == "oe1_pipeline.py":
        content = re.sub(r'input_path:\s*str\s*=\s*"[^"]+"', r'input_path: str = r"D:\investigacion\Laboratorio\navigation_logs_augmented.ndjson"', content)
        # In case the config doesn't have type hint
        content = re.sub(r'INPUT_LOGS\s*=\s*"[^"]+"', r'INPUT_LOGS = r"D:\investigacion\Laboratorio\navigation_logs_augmented.ndjson"', content)

    # In OE2 and OE3, they might read from previous stages using relative paths like "output_oe1/..."
    # We need to make sure they read from the backend pipelines path
    content = re.sub(r'"output_oe(\d)/([^"]+)"', r'"src/main/java/com/pe/articulos/pipelines/output_oe\1/\2"', content)
    content = re.sub(r'\'output_oe(\d)/([^\']+)\'', r'"src/main/java/com/pe/articulos/pipelines/output_oe\1/\2"', content)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Scripts fixed successfully.")
