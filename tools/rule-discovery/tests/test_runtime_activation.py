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

    def test_rule_discovery_architecture_defines_responsibility_transition_checkpoint(self):
        contract = (
            REPO_ROOT / "docs/architecture/rule-discovery.md"
        ).read_text(encoding="utf-8")
        self.assertIn("首个有副作用动作前必须完成 task-level discovery", contract)
        self.assertIn("必须在下一次有副作用动作前重新发现", contract)
        self.assertIn("不替代当前 Agent 的 live discovery", contract)

    def test_software_project_clarification_projection_is_bounded(self):
        method = (
            REPO_ROOT / "docs/methods/software-project-clarification.md"
        ).read_text(encoding="utf-8")
        for token in (
            "establish-context",
            "requirement-clarification",
            "architecture-clarification",
            "clarification-convergence",
            "Clarified Project Context Ready",
        ):
            self.assertIn(token, method)
        self.assertIn("不等于 Specification / Execution Unit 已创建", method)
        self.assertIn("多个当前或预期 Feature", method)

    def test_model_collaboration_is_not_activated_by_current_consumer_profile(self):
        profile = (
            REPO_ROOT / "docs/project/project-capability-profile.md"
        ).read_text(encoding="utf-8")
        self.assertIn("不采用、不启用", profile)
        self.assertIn("persistent runtime config：none", profile)
        self.assertIn("provider / delegation instance：none", profile)


if __name__ == "__main__":
    unittest.main()
