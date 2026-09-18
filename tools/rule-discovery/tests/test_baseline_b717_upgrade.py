from __future__ import annotations

from pathlib import Path
import unittest

REPO_ROOT = Path(__file__).resolve().parents[3]


class BaselineB717UpgradeContractTests(unittest.TestCase):
    def read(self, path: str) -> str:
        return (REPO_ROOT / path).read_text(encoding="utf-8")

    def test_bootstrap_activates_communication_before_substantive_output(self):
        agents = self.read("AGENTS.md")
        self.assertIn("首次向人工输出包含项目事实", agents)
        self.assertIn("communication", agents)
        self.assertIn("human-facing-content", agents)
        self.assertIn("preflight infrastructure invocation", agents)

    def test_root_readme_is_not_fixed_bootstrap_input(self):
        agents = self.read("AGENTS.md")
        docs_readme = self.read("docs/README.md")
        self.assertIn("不再作为 ordinary Fresh Context 的固定预读", agents)
        self.assertIn("不作为固定 Bootstrap", docs_readme)

    def test_human_facing_rule_covers_communication_and_review(self):
        rule = self.read("docs/rules/repository/human-facing-content-integrity.md")
        self.assertIn("activities: [documentation, communication, review, implementation]", rule)
        self.assertIn("artifacts: [human-facing-content, machine-identifier, authority, document]", rule)
        self.assertIn("稳定状态值", rule)
        self.assertIn("中央中英文术语表", rule)

    def test_cloud_rule_discovery_uses_exact_sha_and_same_local_tool(self):
        workflow = self.read(".github/workflows/rule-discovery.yml")
        self.assertIn("workflow_dispatch:", workflow)
        self.assertIn("issue_comment:", workflow)
        self.assertIn("target_sha", workflow)
        self.assertIn("Checkout exact target", workflow)
        self.assertIn("--signals-file", workflow)
        self.assertIn("checked-out SHA does not match requested SHA", workflow)

    def test_human_intervention_and_external_write_safety_are_local_rules(self):
        human = self.read("docs/rules/operations/human-intervention-necessity.md")
        external = self.read("docs/rules/operations/safe-external-write.md")
        self.assertIn("activities: [human-escalation]", human)
        self.assertIn("risks: [human-intervention]", human)
        self.assertIn("## 创建前检查可复用对象", external)

    def test_human_review_and_independent_review_are_distinct_capabilities(self):
        human_arch = self.read("docs/architecture/human-review.md")
        human_skill = self.read("skills/human-review/SKILL.md")
        review_skill = self.read("skills/review-change/SKILL.md")
        profile = self.read("docs/project/project-capability-profile.md")
        self.assertIn("人工评审完成不授予 merge", human_arch)
        self.assertIn("不替代独立变更复核", human_skill)
        self.assertIn("## 权威链语义复核", review_skill)
        self.assertIn("human-review", profile)
        self.assertIn("review-change", profile)

    def test_terminology_governance_has_no_central_glossary_by_default(self):
        architecture = self.read("docs/architecture/requirement-authority.md")
        requirements = self.read("docs/requirements/README.md")
        self.assertIn("## 项目术语治理", architecture)
        self.assertIn("不因为采用本契约就新建中央中英文术语总表", architecture)
        self.assertIn("当前 Consumer 不维护中央中英文术语表", requirements)

    def test_consumer_retains_local_technology_rules(self):
        self.assertTrue((REPO_ROOT / "docs/rules/technology/vue/component-authoring.md").is_file())
        self.assertTrue((REPO_ROOT / "docs/rules/technology/vue/typecheck.md").is_file())

    def test_generic_data_migration_owner_is_not_duplicated(self):
        self.assertFalse((REPO_ROOT / "docs/architecture/data-migration.md").exists())
        self.assertTrue((REPO_ROOT / "docs/requirements/cms-domain.md").is_file())
        self.assertTrue((REPO_ROOT / "docs/specifications/content-migration.md").is_file())
        self.assertTrue((REPO_ROOT / "docs/architecture/cms-architecture.md").is_file())


if __name__ == "__main__":
    unittest.main()
