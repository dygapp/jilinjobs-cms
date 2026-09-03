#!/usr/bin/env bash
set -euo pipefail
python3 <<'PY'
from pathlib import Path
path = Path('.github/scripts/party-naming-refactor.sh')
text = path.read_text(encoding='utf-8')
text = text.replace("    if path in skip:\n        continue", "    if path in skip or path.parts[:2] == ('.github', 'workflows'):\n        continue")
text = text.replace("':!.github/scripts/party-naming-refactor.sh' || true)", "':!.github/scripts/party-naming-refactor.sh' ':!.github/scripts/run-party-naming-refactor.sh' ':!.github/workflows/**' || true)")
path.write_text(text, encoding='utf-8')
PY
bash .github/scripts/party-naming-refactor.sh
