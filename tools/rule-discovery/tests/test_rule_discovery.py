import tempfile
from pathlib import Path
import unittest

import importlib.util
import sys

MODULE_PATH = Path(__file__).resolve().parents[1] / "rule_discovery.py"
SPEC = importlib.util.spec_from_file_location("rule_discovery", MODULE_PATH)
rd = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
sys.modules["rule_discovery"] = rd
SPEC.loader.exec_module(rd)


class RuleDiscoveryTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.repo_root = Path(__file__).resolve().parents[3]

    def discover(self, signals):
        return rd.discover(
            repo_root=self.repo_root,
            rule_roots=[Path("docs/rules")],
            signals=signals,
        )

    def test_generation_query_returns_task_level_locator_shape(self):
        payload = self.discover({
            "phases": ["execute"],
            "activities": ["implementation"],
            "technologies": ["python"],
            "artifacts": ["code", "configuration", "test"],
            "risks": [],
        })
        self.assertEqual(payload["status"], "ok")
        ids = {item["id"] for item in payload["candidates"]}
        self.assertEqual(
            ids,
            {
                "rule:execution-continuity",
                "rule:human-facing-content-integrity",
                "rule:implementation-discipline",
            },
        )
        for item in payload["candidates"]:
            self.assertEqual(set(item), {"id", "path"})
            self.assertTrue(item["path"].startswith("docs/rules/"))

    def test_verification_query_is_deterministic(self):
        payload = self.discover({
            "phases": ["converge"],
            "activities": ["verification"],
            "technologies": [],
            "artifacts": ["commit", "evidence"],
            "risks": ["evidence-reuse"],
        })
        ids = [item["id"] for item in payload["candidates"]]
        self.assertEqual(ids, [
            "rule:evidence-claim-reuse-across-commits",
            "rule:evidence-type-must-match-claim",
            "rule:execution-continuity",
            "rule:integration-state-closure-review",
        ])

    def test_unknown_dimension_is_conservative(self):
        payload = self.discover({
            "phases": ["execute"],
            "activities": ["implementation"],
            "technologies": None,
            "artifacts": ["code"],
            "risks": [],
        })
        ids = {item["id"] for item in payload["candidates"]}
        self.assertIn("rule:vue-component-authoring", ids)
        self.assertIn("rule:implementation-discipline", ids)
        self.assertIn("rule:execution-continuity", ids)

    def test_known_empty_dimension_excludes_restricted_rules(self):
        payload = self.discover({
            "phases": ["execute"],
            "activities": ["implementation"],
            "technologies": [],
            "artifacts": ["code"],
            "risks": [],
        })
        ids = {item["id"] for item in payload["candidates"]}
        self.assertNotIn("rule:vue-component-authoring", ids)
        self.assertIn("rule:implementation-discipline", ids)
        self.assertIn("rule:execution-continuity", ids)

    def test_nested_technology_directory_does_not_change_matching_semantics(self):
        rule_text = """---
id: rule:nested-technology-example
type: rule
status: active
scope:
  phases: [execute]
  activities: [implementation]
  technologies: [vue3]
  artifacts: [vue-sfc]
  risks: []
---

# nested technology example
"""
        signals = {
            "phases": ["execute"],
            "activities": ["implementation"],
            "technologies": ["vue3"],
            "artifacts": ["vue-sfc"],
            "risks": [],
        }
        with tempfile.TemporaryDirectory() as flat_tmp, tempfile.TemporaryDirectory() as nested_tmp:
            flat_root = Path(flat_tmp)
            nested_root = Path(nested_tmp)
            (flat_root / "docs/rules/technology").mkdir(parents=True)
            (nested_root / "docs/rules/technology/vue").mkdir(parents=True)
            (flat_root / "docs/rules/technology/example.md").write_text(rule_text, encoding="utf-8")
            (nested_root / "docs/rules/technology/vue/example.md").write_text(rule_text, encoding="utf-8")

            flat = rd.discover(
                repo_root=flat_root,
                rule_roots=[Path("docs/rules")],
                signals=signals,
            )
            nested = rd.discover(
                repo_root=nested_root,
                rule_roots=[Path("docs/rules")],
                signals=signals,
            )

        self.assertEqual(
            [item["id"] for item in flat["candidates"]],
            [item["id"] for item in nested["candidates"]],
        )
        self.assertEqual(flat["candidate_count"], nested["candidate_count"])

    def test_consumer_vue_rule_uses_nested_human_ia(self):
        payload = self.discover({
            "phases": ["execute"],
            "activities": ["implementation"],
            "technologies": ["vue3"],
            "artifacts": ["vue-sfc", "code"],
            "risks": [],
        })
        candidate = next(
            item for item in payload["candidates"] if item["id"] == "rule:vue-component-authoring"
        )
        self.assertEqual(
            candidate["path"],
            "docs/rules/technology/vue/component-authoring.md",
        )

    def test_safe_external_write_is_task_level_candidate(self):
        payload = self.discover({
            "phases": ["execute"],
            "activities": ["external-operation"],
            "technologies": [],
            "artifacts": ["repository"],
            "risks": [],
        })
        ids = {item["id"] for item in payload["candidates"]}
        self.assertEqual(ids, {"rule:execution-continuity", "rule:safe-external-write"})
        self.assertNotIn("rule:post-write-state-verification", ids)

    def test_clarification_phase_does_not_hide_cross_cutting_document_and_write_rules(self):
        payload = self.discover({
            "phases": ["establish-context"],
            "activities": ["documentation", "external-operation"],
            "technologies": [],
            "artifacts": ["document", "repository"],
            "risks": [],
        })
        ids = {item["id"] for item in payload["candidates"]}
        self.assertEqual(
            ids,
            {"rule:human-facing-content-integrity", "rule:safe-external-write"},
        )

    def test_read_only_state_inspection_is_independently_discoverable(self):
        payload = self.discover({
            "phases": None,
            "activities": ["status-inspection"],
            "technologies": [],
            "artifacts": ["repository"],
            "risks": [],
        })
        ids = {item["id"] for item in payload["candidates"]}
        self.assertEqual(ids, {"rule:read-only-state-inspection"})

    def test_git_commit_governance_is_independently_discoverable(self):
        payload = self.discover({
            "phases": None,
            "activities": [],
            "technologies": [],
            "artifacts": ["git-commit"],
            "risks": [],
        })
        ids = {item["id"] for item in payload["candidates"]}
        self.assertEqual(ids, {"rule:git-commit-governance"})

    def test_high_cost_runtime_activation_is_independently_discoverable(self):
        payload = self.discover({
            "phases": None,
            "activities": [],
            "technologies": [],
            "artifacts": ["review-environment"],
            "risks": [],
        })
        ids = {item["id"] for item in payload["candidates"]}
        self.assertEqual(ids, {"rule:high-cost-runtime-activation"})

    def test_retired_rule_ids_are_absent_from_current_corpus(self):
        retired = {
            "rule:implementation-minimality",
            "rule:surgical-change",
            "rule:post-write-state-verification",
            "rule:exact-machine-identifiers",
            "rule:human-facing-chinese-default",
            "rule:vue-build-vs-typecheck",
            "rule:vue-props-one-way-input",
        }
        records = rd.scan_rules(
            repo_root=self.repo_root,
            rule_roots=[Path("docs/rules")],
        )
        ids = {record.id for record in records}
        self.assertTrue(retired.isdisjoint(ids))

    def test_more_than_six_tokens_fails_closed_at_contract_layer(self):
        with self.assertRaises(rd.ContractError):
            rd.validate_task_signals({
                "phases": ["a", "b", "c", "d", "e", "f", "g"],
                "activities": [],
                "technologies": [],
                "artifacts": [],
                "risks": [],
            })

    def test_duplicate_rule_id_fails_closed(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            rules = root / "docs/rules"
            rules.mkdir(parents=True)
            content = """---
id: rule:duplicate
type: rule
status: active
scope:
  phases: [execute]
  activities: []
  technologies: []
  artifacts: []
  risks: []
---

# duplicate
"""
            (rules / "a.md").write_text(content, encoding="utf-8")
            (rules / "b.md").write_text(content, encoding="utf-8")
            with self.assertRaises(rd.ContractError):
                rd.scan_rules(repo_root=root, rule_roots=[Path("docs/rules")])

    def test_lint_validates_rules_and_consumer_skills(self):
        payload = rd.lint_repository(
            repo_root=self.repo_root,
            rule_roots=[Path("docs/rules")],
            skills_root=Path("skills"),
        )
        self.assertEqual(payload, {"status": "ok", "rules": 17, "skills": 11})


if __name__ == "__main__":
    unittest.main()
