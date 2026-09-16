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

    def test_requirement_and_architecture_methods_replace_clarification_super_method(self):
        requirement_method = (
            REPO_ROOT / "docs/methods/requirement-baseline-establishment.md"
        ).read_text(encoding="utf-8")
        architecture_method = (
            REPO_ROOT / "docs/methods/architecture-clarification.md"
        ).read_text(encoding="utf-8")
        ai_development = (
            REPO_ROOT / "docs/methods/ai-development.md"
        ).read_text(encoding="utf-8")
        profile = (
            REPO_ROOT / "docs/project/project-capability-profile.md"
        ).read_text(encoding="utf-8")

        for token in (
            "establish-requirement-sources",
            "extract-requirements",
            "structure-requirements",
            "resolve-requirements",
            "review-requirements",
            "requirement-convergence",
            "Requirement Baseline Ready",
        ):
            self.assertIn(token, requirement_method)

        for token in (
            "establish-architecture-drivers",
            "clarify-architecture",
            "architecture-convergence",
            "Architecture Context Ready",
        ):
            self.assertIn(token, architecture_method)

        self.assertIn("method:requirement-baseline-establishment", ai_development)
        self.assertIn("method:architecture-clarification", ai_development)
        self.assertIn("method:requirement-baseline-establishment", profile)
        self.assertIn("method:architecture-clarification", profile)
        self.assertFalse(
            (REPO_ROOT / "docs/methods/software-project-clarification.md").exists()
        )

    def test_model_collaboration_is_not_activated_by_current_consumer_profile(self):
        profile = (
            REPO_ROOT / "docs/project/project-capability-profile.md"
        ).read_text(encoding="utf-8")
        self.assertIn("不采用、不启用", profile)
        self.assertIn("persistent runtime config：none", profile)
        self.assertIn("provider / delegation instance：none", profile)


if __name__ == "__main__":
    unittest.main()
