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

    def test_generation_query_returns_only_locator_shape(self):
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
            {"rule:exact-machine-identifiers", "rule:implementation-minimality", "rule:surgical-change"},
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
        self.assertIn("rule:vue-props-one-way-input", ids)
        self.assertIn("rule:implementation-minimality", ids)

    def test_known_empty_dimension_excludes_restricted_rules(self):
        payload = self.discover({
            "phases": ["execute"],
            "activities": ["implementation"],
            "technologies": [],
            "artifacts": ["code"],
            "risks": [],
        })
        ids = {item["id"] for item in payload["candidates"]}
        self.assertNotIn("rule:vue-props-one-way-input", ids)
        self.assertIn("rule:implementation-minimality", ids)

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
        self.assertEqual(payload, {"status": "ok", "rules": 15, "skills": 9})


if __name__ == "__main__":
    unittest.main()
