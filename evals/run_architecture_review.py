#!/usr/bin/env python3
"""Run bounded architecture-review evals in isolated Codex CLI sessions.

This is a Consumer-local experiment, not a permanent project method.

Properties:
- one fresh `codex exec --ephemeral --json` process per scenario;
- only scenario-declared context paths are copied into an external temp workspace;
- assertions / expected grading data are never copied into the runtime workspace;
- read-only runtime, no repository modification;
- model and reasoning effort are explicitly requested on the command line;
- exact planning HEAD, context digests, Codex version, stdout JSONL, stderr and
  run metadata are preserved as ephemeral evidence;
- semantic grading remains a human responsibility.
"""

from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
CORPUS = ROOT / "evals" / "architecture" / "pre-e1e3-convergence-review.json"
RESULTS = ROOT / "evals" / "results" / "architecture"
MODEL_PATTERN = re.compile(r"^[A-Za-z0-9._-]+$")
EFFORTS = {"low", "medium", "high", "xhigh"}


def load_corpus() -> dict[str, Any]:
    return json.loads(CORPUS.read_text(encoding="utf-8"))


def run_text(command: list[str], *, cwd: Path = ROOT) -> str:
    completed = subprocess.run(
        command,
        cwd=cwd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        check=False,
    )
    if completed.returncode != 0:
        raise RuntimeError(
            f"Command failed ({completed.returncode}): {' '.join(command)}\n"
            f"{completed.stderr.strip()}"
        )
    return completed.stdout.strip()


def ensure_clean_git_head() -> str:
    head = run_text(["git", "rev-parse", "HEAD"])
    dirty = subprocess.run(
        ["git", "status", "--porcelain", "--untracked-files=all"],
        cwd=ROOT,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        check=False,
    )
    if dirty.returncode != 0:
        raise RuntimeError(dirty.stderr.strip() or "git status failed")
    if dirty.stdout.strip():
        raise RuntimeError(
            "Working tree contains tracked or untracked changes. Commit or remove them "
            "first so every copied context file is provably tied to the exact HEAD."
        )
    return head


def validate_model(model: str) -> str:
    if not MODEL_PATTERN.fullmatch(model) or model.startswith("-"):
        raise RuntimeError(f"Invalid model identifier: {model!r}")
    return model


def validate_effort(effort: str) -> str:
    if effort not in EFFORTS:
        raise RuntimeError(
            f"Invalid reasoning effort: {effort!r}; expected one of {sorted(EFFORTS)}"
        )
    return effort


def check_codex(codex_bin: str) -> str:
    return run_text([codex_bin, "--version"])


def scenario_map(corpus: dict[str, Any]) -> dict[str, dict[str, Any]]:
    return {item["id"]: item for item in corpus["scenarios"]}


def iter_files(path: Path) -> list[Path]:
    if path.is_file():
        return [path]
    return sorted(item for item in path.rglob("*") if item.is_file())


def copy_context(workspace: Path, context_paths: list[str]) -> tuple[str, list[dict[str, str]]]:
    hasher = hashlib.sha256()
    manifest: list[dict[str, str]] = []

    for raw_relative in sorted(context_paths):
        relative = Path(raw_relative)
        if relative.is_absolute() or ".." in relative.parts:
            raise RuntimeError(f"Invalid context path: {raw_relative}")

        source = (ROOT / relative).resolve()
        if not source.is_relative_to(ROOT):
            raise RuntimeError(f"Context path escapes repository: {raw_relative}")
        if not source.exists():
            raise RuntimeError(f"Context path does not exist: {raw_relative}")

        target = workspace / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        if source.is_dir():
            shutil.copytree(source, target)
        else:
            shutil.copy2(source, target)

        for source_file in iter_files(source):
            repo_relative = source_file.relative_to(ROOT).as_posix()
            data = source_file.read_bytes()
            digest = hashlib.sha256(data).hexdigest()
            hasher.update(repo_relative.encode("utf-8"))
            hasher.update(b"\0")
            hasher.update(data)
            hasher.update(b"\0")
            manifest.append({"path": repo_relative, "sha256": digest})

    manifest.sort(key=lambda item: item["path"])
    return hasher.hexdigest(), manifest


def extract_runtime_hints(stdout: str) -> dict[str, list[str]]:
    """Best-effort observation only; Codex JSONL schemas can vary by version."""
    models: set[str] = set()
    efforts: set[str] = set()

    def walk(value: Any) -> None:
        if isinstance(value, dict):
            for key, item in value.items():
                lowered = key.lower()
                if isinstance(item, str):
                    if lowered == "model" or lowered.endswith("_model"):
                        models.add(item)
                    if "effort" in lowered:
                        efforts.add(item)
                walk(item)
        elif isinstance(value, list):
            for item in value:
                walk(item)

    for line in stdout.splitlines():
        try:
            walk(json.loads(line))
        except json.JSONDecodeError:
            continue

    return {
        "observed_models": sorted(models),
        "observed_reasoning_efforts": sorted(efforts),
    }


def build_prompt(scenario: dict[str, Any], context_digest: str) -> str:
    context_list = "\n".join(f"- {path}" for path in scenario["context_paths"])
    return (
        f"Architecture Review Scenario: {scenario['id']} — {scenario['title']}\n"
        f"Context digest: sha256:{context_digest}\n\n"
        "以下路径与本提示构成本次评审的全部可用项目上下文。"
        "不得读取当前工作目录之外的路径；不得寻找评分 assertions 或历史结果：\n"
        f"{context_list}\n\n"
        f"{scenario['prompt']}"
    )


def write_run_metadata(
    *,
    result_dir: Path,
    run_id: str,
    scenario: dict[str, Any],
    exact_head: str,
    codex_version: str,
    model: str,
    effort: str,
    context_digest: str,
    context_manifest: list[dict[str, str]],
    command_prefix: list[str],
    prompt: str,
    returncode: int,
    runtime_hints: dict[str, list[str]],
) -> None:
    metadata = {
        "run_id": run_id,
        "scenario_id": scenario["id"],
        "scenario_title": scenario["title"],
        "planning_head": exact_head,
        "codex_version": codex_version,
        "requested_model": model,
        "requested_reasoning_effort": effort,
        "context_digest": f"sha256:{context_digest}",
        "context_manifest": context_manifest,
        "prompt_sha256": f"sha256:{hashlib.sha256(prompt.encode('utf-8')).hexdigest()}",
        "command_prefix": command_prefix,
        "returncode": returncode,
        "grading": "pending-human-semantic-review",
        **runtime_hints,
    }
    (result_dir / f"{run_id}.run.json").write_text(
        json.dumps(metadata, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def run_scenario(
    *,
    scenario: dict[str, Any],
    exact_head: str,
    codex_bin: str,
    codex_version: str,
    model: str,
    effort: str,
) -> int:
    timestamp = dt.datetime.now(dt.timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    run_id = f"{scenario['id']}-{timestamp}-{model}-{effort}"
    result_dir = RESULTS / scenario["id"]
    result_dir.mkdir(parents=True, exist_ok=True)

    with tempfile.TemporaryDirectory(
        prefix=f"jilinjobs-cms-architecture-review-{scenario['id']}-"
    ) as temp_dir:
        workspace = Path(temp_dir)
        context_digest, context_manifest = copy_context(
            workspace, scenario["context_paths"]
        )
        prompt = build_prompt(scenario, context_digest)

        command_prefix = [
            codex_bin,
            "exec",
            "--ephemeral",
            "--json",
            "--model",
            model,
            "-c",
            f'model_reasoning_effort="{effort}"',
            "--sandbox",
            "read-only",
            "--skip-git-repo-check",
            "-C",
            str(workspace),
        ]
        command = [*command_prefix, prompt]

        runtime_env = os.environ.copy()
        runtime_env["PWD"] = str(workspace)
        runtime_env.pop("OLDPWD", None)
        for key in ("GIT_DIR", "GIT_WORK_TREE", "GIT_COMMON_DIR", "GIT_INDEX_FILE"):
            runtime_env.pop(key, None)

        print(
            f"[{scenario['id']}] fresh architecture review: "
            f"model={model}, effort={effort}, head={exact_head[:12]}"
        )
        completed = subprocess.run(
            command,
            cwd=workspace,
            env=runtime_env,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=False,
        )

        stdout_path = result_dir / f"{run_id}.jsonl"
        stderr_path = result_dir / f"{run_id}.stderr.txt"
        stdout_path.write_text(completed.stdout, encoding="utf-8")
        stderr_path.write_text(completed.stderr, encoding="utf-8")

        runtime_hints = extract_runtime_hints(completed.stdout)
        write_run_metadata(
            result_dir=result_dir,
            run_id=run_id,
            scenario=scenario,
            exact_head=exact_head,
            codex_version=codex_version,
            model=model,
            effort=effort,
            context_digest=context_digest,
            context_manifest=context_manifest,
            command_prefix=command_prefix,
            prompt=prompt,
            returncode=completed.returncode,
            runtime_hints=runtime_hints,
        )

        status = "OK" if completed.returncode == 0 else f"EXIT {completed.returncode}"
        print(f"[{scenario['id']}] {status}; semantic grading is still required")
        return completed.returncode


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Run bounded Consumer-local architecture review evals."
    )
    parser.add_argument(
        "--scenario",
        action="append",
        default=[],
        help="Scenario id to run; repeat for multiple scenarios. Required for execution.",
    )
    parser.add_argument("--model", help="Explicit Codex model id, e.g. gpt-5.6-sol or gpt-6-astra")
    parser.add_argument(
        "--reasoning-effort",
        default="medium",
        choices=sorted(EFFORTS),
        help="Requested Codex model_reasoning_effort (default: medium)",
    )
    parser.add_argument(
        "--allow-high-capability",
        action="store_true",
        help="Required when requesting a gpt-6* model, to avoid accidental high-cost runs.",
    )
    parser.add_argument(
        "--codex-bin",
        default=os.environ.get("CODEX_BIN", "codex"),
        help="Codex CLI executable (default: CODEX_BIN or codex)",
    )
    parser.add_argument(
        "--list",
        action="store_true",
        help="List scenarios and exit without invoking Codex.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    corpus = load_corpus()
    scenarios = scenario_map(corpus)

    if args.list:
        for scenario in corpus["scenarios"]:
            marker = "high-capability-default" if scenario.get("default_high_capability") else "dry-run-first"
            print(f"{scenario['id']}: {scenario['title']} [{marker}]")
        return 0

    if not args.scenario:
        print("At least one --scenario is required.", file=sys.stderr)
        return 2
    if not args.model:
        print("--model is required so review evidence is never tied to an implicit default.", file=sys.stderr)
        return 2

    unknown = set(args.scenario) - set(scenarios)
    if unknown:
        print(f"Unknown scenario(s): {', '.join(sorted(unknown))}", file=sys.stderr)
        return 2

    model = validate_model(args.model)
    effort = validate_effort(args.reasoning_effort)
    if model.lower().startswith("gpt-6") and not args.allow_high_capability:
        print(
            "A gpt-6* run requires --allow-high-capability. "
            "Run a lower-cost dry-run first and opt in explicitly.",
            file=sys.stderr,
        )
        return 2

    try:
        exact_head = ensure_clean_git_head()
        codex_version = check_codex(args.codex_bin)
    except RuntimeError as exc:
        print(str(exc), file=sys.stderr)
        return 2

    failures = 0
    for scenario_id in args.scenario:
        failures += run_scenario(
            scenario=scenarios[scenario_id],
            exact_head=exact_head,
            codex_bin=args.codex_bin,
            codex_version=codex_version,
            model=model,
            effort=effort,
        ) != 0

    if failures:
        print(f"Codex process failures: {failures}", file=sys.stderr)
        return 1

    print("All selected Codex processes exited successfully.")
    print("Process exit 0 is NOT an architecture-review PASS; human semantic grading remains required.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
