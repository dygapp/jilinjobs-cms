#!/usr/bin/env python3
"""Consumer-local deterministic Rule Discovery.

Discovery scans only Rule YAML Front Matter, validates the local V4 contract,
and returns locator-only candidates. Rule bodies are never read for matching.
"""

from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import re
import sys
from dataclasses import dataclass
from typing import Any, Iterable, Sequence

SCOPE_KEYS = ("phases", "activities", "technologies", "artifacts", "risks")
RULE_KEYS = {"id", "type", "status", "scope"}
MAX_TASK_TOKENS_PER_DIMENSION = 6
TOKEN_RE = re.compile(r"^[a-z0-9]+(?:-[a-z0-9]+)*$")
RULE_ID_RE = re.compile(r"^rule:[a-z0-9]+(?:-[a-z0-9]+)*$")
SKILL_META_PREFIX = "jilinjobs-cms"


class ContractError(ValueError):
    pass


@dataclass(frozen=True)
class ParsedMarkdown:
    metadata: dict[str, Any]
    body: str


@dataclass(frozen=True)
class RuleRecord:
    id: str
    path: Path
    locator: str
    scope: dict[str, tuple[str, ...]]


def _parse_scalar(raw: str, *, source: str, line_no: int) -> Any:
    value = raw.strip()
    if value == "":
        raise ContractError(f"{source}:{line_no}: empty scalar value")
    if value.startswith("["):
        if not value.endswith("]"):
            raise ContractError(f"{source}:{line_no}: malformed inline array")
        inner = value[1:-1].strip()
        if not inner:
            return []
        return [
            _parse_scalar(item.strip(), source=source, line_no=line_no)
            for item in inner.split(",")
        ]
    if value.startswith("{") or value.endswith("}"):
        raise ContractError(f"{source}:{line_no}: inline mappings are not supported")
    if value.startswith('"'):
        try:
            parsed = json.loads(value)
        except json.JSONDecodeError as exc:
            raise ContractError(f"{source}:{line_no}: invalid quoted string") from exc
        if not isinstance(parsed, str):
            raise ContractError(f"{source}:{line_no}: quoted value must be a string")
        return parsed
    if value.startswith("'"):
        if len(value) < 2 or not value.endswith("'"):
            raise ContractError(f"{source}:{line_no}: invalid single-quoted string")
        return value[1:-1].replace("''", "'")
    return value


def parse_front_matter_text(text: str, *, source: str) -> ParsedMarkdown:
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        raise ContractError(f"{source}: missing YAML Front Matter")
    end_index: int | None = None
    for idx in range(1, len(lines)):
        if lines[idx].strip() == "---":
            end_index = idx
            break
    if end_index is None:
        raise ContractError(f"{source}: unterminated YAML Front Matter")

    metadata: dict[str, Any] = {}
    active_mapping: dict[str, Any] | None = None
    active_name: str | None = None
    for offset, raw_line in enumerate(lines[1:end_index], start=2):
        if "\t" in raw_line:
            raise ContractError(f"{source}:{offset}: tabs are not allowed")
        if not raw_line.strip() or raw_line.lstrip().startswith("#"):
            continue
        indent = len(raw_line) - len(raw_line.lstrip(" "))
        line = raw_line.strip()
        if ":" not in line:
            raise ContractError(f"{source}:{offset}: expected key: value")
        key, raw_value = line.split(":", 1)
        key = key.strip()
        if not TOKEN_RE.fullmatch(key):
            raise ContractError(f"{source}:{offset}: invalid metadata key {key!r}")

        if indent == 0:
            if key in metadata:
                raise ContractError(f"{source}:{offset}: duplicate key {key!r}")
            if raw_value.strip() == "":
                nested: dict[str, Any] = {}
                metadata[key] = nested
                active_mapping = nested
                active_name = key
            else:
                metadata[key] = _parse_scalar(raw_value, source=source, line_no=offset)
                active_mapping = None
                active_name = None
            continue

        if indent != 2 or active_mapping is None:
            raise ContractError(
                f"{source}:{offset}: only one two-space nested mapping level is supported"
            )
        if key in active_mapping:
            raise ContractError(f"{source}:{offset}: duplicate key {active_name}.{key}")
        if raw_value.strip() == "":
            raise ContractError(f"{source}:{offset}: deeper mappings are not supported")
        active_mapping[key] = _parse_scalar(raw_value, source=source, line_no=offset)

    body = "\n".join(lines[end_index + 1:]).strip()
    return ParsedMarkdown(metadata=metadata, body=body)


def parse_front_matter_file(path: Path) -> ParsedMarkdown:
    try:
        return parse_front_matter_text(path.read_text(encoding="utf-8"), source=str(path))
    except (OSError, UnicodeError) as exc:
        raise ContractError(f"{path}: unable to read UTF-8 Markdown: {exc}") from exc


def _validate_token_list(value: Any, *, field: str, source: str) -> tuple[str, ...]:
    if not isinstance(value, list):
        raise ContractError(f"{source}: {field} must be an inline string array")
    result: list[str] = []
    seen: set[str] = set()
    for token in value:
        if not isinstance(token, str) or not TOKEN_RE.fullmatch(token):
            raise ContractError(f"{source}: {field} contains illegal token {token!r}")
        if token in seen:
            raise ContractError(f"{source}: {field} contains duplicate token {token!r}")
        seen.add(token)
        result.append(token)
    return tuple(result)


def validate_task_signals(signals: Any) -> dict[str, tuple[str, ...] | None]:
    if not isinstance(signals, dict):
        raise ContractError("task signals must be a JSON object")
    if set(signals) != set(SCOPE_KEYS):
        missing = sorted(set(SCOPE_KEYS) - set(signals))
        unknown = sorted(set(signals) - set(SCOPE_KEYS))
        raise ContractError(f"invalid task signal shape (missing={missing}, unknown={unknown})")
    normalized: dict[str, tuple[str, ...] | None] = {}
    for key in SCOPE_KEYS:
        value = signals[key]
        if value is None:
            normalized[key] = None
            continue
        tokens = _validate_token_list(value, field=key, source="task-signals")
        if len(tokens) > MAX_TASK_TOKENS_PER_DIMENSION:
            raise ContractError(
                f"task-signals: {key} contains too many tokens "
                f"({len(tokens)} > {MAX_TASK_TOKENS_PER_DIMENSION})"
            )
        normalized[key] = tokens
    return normalized


def validate_rule(parsed: ParsedMarkdown, *, path: Path, locator: str) -> RuleRecord:
    metadata = parsed.metadata
    unknown = sorted(set(metadata) - RULE_KEYS)
    missing = sorted(RULE_KEYS - set(metadata))
    if unknown or missing:
        raise ContractError(f"{path}: invalid Rule fields (missing={missing}, unknown={unknown})")

    rule_id = metadata["id"]
    if not isinstance(rule_id, str) or not RULE_ID_RE.fullmatch(rule_id):
        raise ContractError(f"{path}: invalid Rule id {rule_id!r}")
    if metadata["type"] != "rule" or metadata["status"] != "active":
        raise ContractError(f"{path}: discoverable Rule must be type: rule and status: active")
    if not parsed.body:
        raise ContractError(f"{path}: Rule normative body is empty")

    scope = metadata["scope"]
    if not isinstance(scope, dict) or set(scope) != set(SCOPE_KEYS):
        raise ContractError(f"{path}: scope must contain exactly {list(SCOPE_KEYS)}")
    normalized: dict[str, tuple[str, ...]] = {}
    for key in SCOPE_KEYS:
        normalized[key] = _validate_token_list(
            scope[key], field=f"scope.{key}", source=str(path)
        )
    if not any(normalized.values()):
        raise ContractError(f"{path}: Rule must restrict at least one scope dimension")
    return RuleRecord(id=rule_id, path=path, locator=locator, scope=normalized)


def _walk_markdown(root: Path) -> Iterable[Path]:
    if not root.exists() or not root.is_dir():
        raise ContractError(f"resource root is missing or not a directory: {root}")
    errors: list[OSError] = []
    found: list[Path] = []

    def onerror(error: OSError) -> None:
        errors.append(error)

    for current, dirs, files in os.walk(root, followlinks=False, onerror=onerror):
        current_path = Path(current)
        symlink_dirs = sorted(
            current_path / dirname
            for dirname in dirs
            if (current_path / dirname).is_symlink()
        )
        if symlink_dirs:
            raise ContractError(
                "symlink directories are not supported in resource roots: "
                + ", ".join(str(path) for path in symlink_dirs)
            )
        dirs.sort()
        files.sort()
        for filename in files:
            if filename.endswith(".md"):
                found.append(current_path / filename)
    if errors:
        raise ContractError(f"incomplete resource scan under {root}: {errors[0]}")
    return found


def scan_rules(*, repo_root: Path, rule_roots: Sequence[Path]) -> list[RuleRecord]:
    repo_root = repo_root.resolve()
    seen_paths: set[Path] = set()
    seen_ids: dict[str, str] = {}
    records: list[RuleRecord] = []
    for configured in rule_roots:
        root = configured if configured.is_absolute() else repo_root / configured
        root = root.resolve()
        try:
            root.relative_to(repo_root)
        except ValueError as exc:
            raise ContractError(f"Rule root must be inside repository root: {root}") from exc
        for path in _walk_markdown(root):
            physical = path.resolve()
            if physical in seen_paths:
                raise ContractError(f"same physical Rule scanned more than once: {physical}")
            seen_paths.add(physical)
            locator = physical.relative_to(repo_root).as_posix()
            record = validate_rule(parse_front_matter_file(physical), path=physical, locator=locator)
            if record.id in seen_ids:
                raise ContractError(
                    f"duplicate Rule id {record.id!r}: {seen_ids[record.id]} and {record.locator}"
                )
            seen_ids[record.id] = record.locator
            records.append(record)
    return sorted(records, key=lambda item: item.id)


def discover(*, repo_root: Path, rule_roots: Sequence[Path], signals: Any) -> dict[str, Any]:
    normalized = validate_task_signals(signals)
    records = scan_rules(repo_root=repo_root, rule_roots=rule_roots)
    candidates: list[dict[str, str]] = []
    for record in records:
        applicable = True
        for key in SCOPE_KEYS:
            rule_tokens = record.scope[key]
            if not rule_tokens:
                continue
            task_tokens = normalized[key]
            if task_tokens is None:
                continue
            if not (set(rule_tokens) & set(task_tokens)):
                applicable = False
                break
        if applicable:
            candidates.append({"id": record.id, "path": record.locator})
    return {
        "status": "ok",
        "scanned": len(records),
        "candidate_count": len(candidates),
        "candidates": candidates,
    }


def validate_skill(parsed: ParsedMarkdown, *, path: Path) -> str:
    metadata = parsed.metadata
    if not parsed.body:
        raise ContractError(f"{path}: SKILL.md body is empty")
    for required in ("name", "description", "metadata"):
        if required not in metadata:
            raise ContractError(f"{path}: missing Skill field {required!r}")
    if any(key in metadata for key in ("id", "type", "status")):
        raise ContractError(f"{path}: SKILL.md must not use custom top-level id/type/status")
    name = metadata["name"]
    description = metadata["description"]
    project_meta = metadata["metadata"]
    if not isinstance(name, str) or not TOKEN_RE.fullmatch(name) or path.parent.name != name:
        raise ContractError(f"{path}: invalid Skill name/path {name!r}")
    if not isinstance(description, str) or not description.strip():
        raise ContractError(f"{path}: Skill description must be non-empty")
    if not isinstance(project_meta, dict):
        raise ContractError(f"{path}: Skill metadata must be a string mapping")
    expected = {
        f"{SKILL_META_PREFIX}-id": f"skill:{name}",
        f"{SKILL_META_PREFIX}-type": "skill",
        f"{SKILL_META_PREFIX}-status": "active",
    }
    for key, value in expected.items():
        if project_meta.get(key) != value:
            raise ContractError(f"{path}: metadata.{key} must be {value!r}")
    for key, value in project_meta.items():
        if not isinstance(key, str) or not isinstance(value, str):
            raise ContractError(f"{path}: Skill metadata must contain string pairs")
    return expected[f"{SKILL_META_PREFIX}-id"]


def lint_repository(
    *, repo_root: Path, rule_roots: Sequence[Path], skills_root: Path
) -> dict[str, Any]:
    repo_root = repo_root.resolve()
    rules = scan_rules(repo_root=repo_root, rule_roots=rule_roots)
    ids: dict[str, str] = {r.id: r.locator for r in rules}
    skills_dir = skills_root if skills_root.is_absolute() else repo_root / skills_root
    skills_dir = skills_dir.resolve()
    skill_count = 0
    if skills_dir.exists():
        for path in _walk_markdown(skills_dir):
            if path.name != "SKILL.md":
                raise ContractError(f"{path}: only SKILL.md is allowed under skills/")
            resource_id = validate_skill(parse_front_matter_file(path), path=path)
            locator = path.relative_to(repo_root).as_posix()
            if resource_id in ids:
                raise ContractError(f"duplicate resource id {resource_id!r}")
            ids[resource_id] = locator
            skill_count += 1
    return {"status": "ok", "rules": len(rules), "skills": skill_count}


def _load_signals(args: argparse.Namespace) -> Any:
    if bool(args.signals_json) == bool(args.signals_file):
        raise ContractError("provide exactly one of --signals-json or --signals-file")
    try:
        if args.signals_json:
            return json.loads(args.signals_json)
        return json.loads(Path(args.signals_file).read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError, UnicodeError) as exc:
        raise ContractError(f"unable to load task signals: {exc}") from exc


def _rule_roots(values: Sequence[str] | None) -> list[Path]:
    return [Path(v) for v in (values or ["docs/rules"])]


def _emit(payload: dict[str, Any]) -> None:
    json.dump(payload, sys.stdout, ensure_ascii=False)
    sys.stdout.write("\n")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="jilinjobs-cms Rule Discovery")
    parser.add_argument("--repo-root", default=".")
    subparsers = parser.add_subparsers(dest="command", required=True)

    discover_parser = subparsers.add_parser("discover")
    discover_parser.add_argument("--rules-root", action="append", dest="rule_roots")
    discover_parser.add_argument("--signals-json")
    discover_parser.add_argument("--signals-file")

    lint_parser = subparsers.add_parser("lint")
    lint_parser.add_argument("--rules-root", action="append", dest="rule_roots")
    lint_parser.add_argument("--skills-root", default="skills")
    return parser


def main(argv: Sequence[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    repo_root = Path(args.repo_root).resolve()
    try:
        if args.command == "discover":
            payload = discover(
                repo_root=repo_root,
                rule_roots=_rule_roots(args.rule_roots),
                signals=_load_signals(args),
            )
        else:
            payload = lint_repository(
                repo_root=repo_root,
                rule_roots=_rule_roots(args.rule_roots),
                skills_root=Path(args.skills_root),
            )
    except ContractError as exc:
        _emit({"status": "fail-closed", "candidates": [], "diagnostics": [str(exc)]})
        return 2
    _emit(payload)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
