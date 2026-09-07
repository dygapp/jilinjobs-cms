#!/usr/bin/env python3
"""Run the AR-04 retrospective blind architecture review with the shared eval runner."""

from pathlib import Path

import run_architecture_review as runner


runner.CORPUS = (
    Path(__file__).resolve().parent
    / "architecture"
    / "ar04-migration-sequencing-review.json"
)


if __name__ == "__main__":
    raise SystemExit(runner.main())
