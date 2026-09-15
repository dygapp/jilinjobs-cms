from __future__ import annotations

from pathlib import Path
import sys
import unittest

TOOL_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOL_DIR))
import rule_discovery as rd  # noqa: E402

REPO_ROOT = Path(__file__).resolve().parents[3]


class RuntimeActivationRegressionTests(unittest.TestCase):
    def test_integration_state_closure_is_discoverable_without_method_phase(self):
        result = rd.discover(
            repo_root=REPO_ROOT,
            rule_roots=[Path("docs/rules")],
            signals={
                "phases": None,
                "activities": ["integration"],
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
        self.assertIn("其 PASS 不得替代 ordinary runtime invocation", agents)

    def test_local_method_defines_responsibility_transition_checkpoint(self):
        method = (
            REPO_ROOT / "docs/project/rule-discovery-method.md"
        ).read_text(encoding="utf-8")
        self.assertIn("首个有副作用动作前必须完成一次 task-level discovery", method)
        self.assertIn("不得替代 ordinary runtime invocation", method)
        self.assertIn("旧 candidate set 不跨职责永久有效", method)

    def test_software_project_clarification_projection_is_bounded(self):
        method = (
            REPO_ROOT / "docs/project/development-method.md"
        ).read_text(encoding="utf-8")
        for token in (
            "establish-context",
            "requirement-clarification",
            "architecture-clarification",
            "clarification-convergence",
            "Clarified Project Context Ready",
        ):
            self.assertIn(token, method)
        self.assertIn("不等于** Specification created", method)
        self.assertIn("当前没有 active project-clarification lifecycle", method)

    def test_model_collaboration_is_not_activated_by_baseline_upgrade(self):
        method = (
            REPO_ROOT / "docs/project/development-method.md"
        ).read_text(encoding="utf-8")
        self.assertIn("reject / not-applicable for current instance", method)
        self.assertIn("不建立 runtime / provider", method)


if __name__ == "__main__":
    unittest.main()
