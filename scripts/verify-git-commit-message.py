#!/usr/bin/env python3
import argparse
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
POLICY_PATH = ROOT / "docs/governance/git-commit-conventions.md"
TYPE_ROW_RE = re.compile(r"^\| `([a-z]+)` \|", re.MULTILINE)
SUBJECT_RE = re.compile(
    r"^(?P<type>[a-z]+)(?:\((?P<scope>[a-z0-9][a-z0-9.-]*)\))?: (?P<summary>.+)$"
)
HAN_RE = re.compile(r"[\u3400-\u9fff]")


def load_allowed_types() -> set[str]:
    policy = POLICY_PATH.read_text(encoding="utf-8")
    types = set(TYPE_ROW_RE.findall(policy))
    if not types:
        raise RuntimeError(f"无法从 {POLICY_PATH.relative_to(ROOT)} 读取稳定 Type 集合")
    return types


def git(*args: str, check: bool = True) -> str:
    proc = subprocess.run(
        ["git", *args],
        cwd=ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if check and proc.returncode != 0:
        raise RuntimeError(proc.stderr.strip() or f"git {' '.join(args)} failed")
    return proc.stdout.strip()


def commit_subject(commit: str) -> str:
    return git("show", "-s", "--format=%s", commit)


def commits_to_check(base_ref: str) -> list[str]:
    head = git("rev-parse", "HEAD")
    if subprocess.run(
        ["git", "rev-parse", "--verify", f"{base_ref}^{{commit}}"],
        cwd=ROOT,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    ).returncode == 0:
        base = git("merge-base", "HEAD", base_ref)
        commits = git("rev-list", "--reverse", f"{base}..HEAD")
        if commits:
            return commits.splitlines()
    return [head]


def validate(subject: str, allowed_types: set[str] | None = None) -> list[str]:
    errors: list[str] = []
    match = SUBJECT_RE.fullmatch(subject)
    if not match:
        return ["必须使用 <type>(<scope>): <中文摘要> 或 <type>: <中文摘要>"]

    if allowed_types is None:
        allowed_types = load_allowed_types()

    commit_type = match.group("type")
    summary = match.group("summary")
    if commit_type not in allowed_types:
        errors.append(
            "type 不在当前稳定集合中：" + ", ".join(sorted(allowed_types))
        )
    if not HAN_RE.search(summary):
        errors.append("摘要必须以中文主述，至少包含中文字符")
    if summary.endswith(("。", ".")):
        errors.append("摘要默认不得以句号结尾")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description="验证 jilinjobs-cms Git Commit Message")
    parser.add_argument("--base-ref", default="origin/main")
    args = parser.parse_args()

    try:
        allowed_types = load_allowed_types()
        commits = commits_to_check(args.base_ref)
    except RuntimeError as exc:
        print(f"commit-message verification: FAIL: {exc}", file=sys.stderr)
        return 2

    failures = 0
    for commit in commits:
        subject = commit_subject(commit)
        errors = validate(subject, allowed_types)
        short = commit[:12]
        if errors:
            failures += 1
            print(f"FAIL {short} {subject}", file=sys.stderr)
            for error in errors:
                print(f"  - {error}", file=sys.stderr)
        else:
            print(f"PASS {short} {subject}")

    if failures:
        print(
            f"Git Commit Message 校验失败：{failures}/{len(commits)} 个提交不符合规范。",
            file=sys.stderr,
        )
        return 1

    print(f"Git Commit Message 校验通过：{len(commits)} 个提交。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
