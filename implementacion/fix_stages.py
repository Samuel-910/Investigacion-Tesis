import json
import os
import re

in_file = r"d:\Ciclo X\Investigacion\laboratorio\OE1_Pipeline_PPI_C9_2026_v2.ipynb"
stages_dir = r"d:\Ciclo X\Investigacion\implementacion\OE1_Baseline\stages"

with open(in_file, 'r', encoding='utf-8') as f:
    nb = json.load(f)

for cell in nb['cells']:
    if cell['cell_type'] != 'code':
        continue
    src = "".join(cell['source'])
    
    # Strip the execution part at the end of the cells
    src = re.sub(r'\n# ── Ejecutar Stage.*', '', src, flags=re.DOTALL)
    
    if "class Stage1_Extract:" in src:
        with open(os.path.join(stages_dir, 'stage1_extract.py'), 'w', encoding='utf-8') as f:
            f.write("from typing import List, Dict, Optional\nfrom pathlib import Path\nimport json\nimport pandas as pd\nfrom datetime import datetime\nfrom config import PipelineConfig, RawEvent, NavigationSession, log\n\n" + src)
    elif "class Stage2_Sessionize:" in src:
        with open(os.path.join(stages_dir, 'stage2_sessionize.py'), 'w', encoding='utf-8') as f:
            f.write("from typing import List, Dict, Optional\nfrom pathlib import Path\nimport pandas as pd\nimport numpy as np\nfrom datetime import datetime, timedelta\nfrom config import PipelineConfig, RawEvent, NavigationSession, log\n\n" + src)
    elif "class Stage3_Filter:" in src:
        with open(os.path.join(stages_dir, 'stage3_filter.py'), 'w', encoding='utf-8') as f:
            f.write("from typing import List, Dict, Optional\nfrom pathlib import Path\nimport pandas as pd\nfrom config import PipelineConfig, RawEvent, NavigationSession, log\n\n" + src)
    elif "class Stage4_ProfileVectors:" in src:
        with open(os.path.join(stages_dir, 'stage4_profiles.py'), 'w', encoding='utf-8') as f:
            f.write("from typing import List, Dict, Optional\nfrom pathlib import Path\nfrom collections import defaultdict\nimport hashlib\nfrom config import PipelineConfig, RawEvent, NavigationSession, log\n\n" + src)
    elif "class Stage5_NavigationCost:" in src:
        with open(os.path.join(stages_dir, 'stage5_cost.py'), 'w', encoding='utf-8') as f:
            f.write("from typing import List, Dict, Optional\nfrom pathlib import Path\nimport numpy as np\nimport pandas as pd\nfrom config import PipelineConfig, RawEvent, NavigationSession, log\n\n" + src)
    elif "class Stage6_Export:" in src:
        with open(os.path.join(stages_dir, 'stage6_export.py'), 'w', encoding='utf-8') as f:
            f.write("from typing import List, Dict, Optional\nfrom pathlib import Path\nfrom collections import defaultdict\nimport json\nimport pandas as pd\nfrom datetime import datetime\nfrom config import PipelineConfig, RawEvent, NavigationSession, log\n\n" + src)

print("✅ Extracción robusta de las clases completada.")
