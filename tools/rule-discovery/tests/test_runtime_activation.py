from __future__ import annotations

from pathlib import Path
import importlib.util
import sys
import unittest

MODULE_PATH = Path(__file__).resolve().parents[1] / "rule_discovery.py"
SPEC = importlib.util.spec_from_file_location("rule_discovery", MODULE_PATH)
rd = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
sys.modules["rule_discovery"] = rd
SPEC.loader.exec_module(rd)

REPO_ROOT = Path(__file__).resolve().parents[3]


class RuntimeActivationRegressionTests(unittest.TestCase):
    def test_integration_state_closure_is_discoverable_without_method_phase(self):
        result = rd.discover(
            repo_root=REPO_ROOT,
            rule_roots=[Path("docs/rules")],
            signals={
                "phases": None,
                "activities": ["verification"],
                "technologies": [],
                "artifacts": ["commit", "evidence"],
                "risks": [],
            },
        )

        ids = {item["id"] for item in result["candidates"]}
        self.assertIn("rule:integration-state-closure-review", ids)

    def test_bootstrap_requires_task_level_discovery_before_side_effects(self):
        agents = (REPO_ROOT / "AGENTS.md").read_text(encoding="utf-8")
        self.assertIn("首个有副作用动作前必须完成本次 task-level discovery", agents)
        self.assertIn("不能替代 ordinary runtime invocation", agents)

    def test_local_method_defines_responsibility_transition_checkpoint(self):
        method = (
            REPO_ROOT / "docs/project/rule-discovery-method.md"
        ).read_text(encoding="utf-8")
        self.assertIn("首个有副作用动作前", method)
        self.assertIn("不能替代 ordinary runtime invocation", method)
        self.assertIn("旧 candidate set 不跨职责永久有效", method)


if __name__ == "__main__":
    unittest.main()
