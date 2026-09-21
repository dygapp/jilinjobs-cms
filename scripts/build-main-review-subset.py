#!/usr/bin/env python3
import argparse
import hashlib
import json
import os
import shutil
from datetime import date
from pathlib import Path


def hardlink_or_copy(src: str, dst: str) -> str:
    try:
        os.link(src, dst)
        return dst
    except OSError:
        return shutil.copy2(src, dst)


def parse_args():
    parser = argparse.ArgumentParser(description="从主站 canonical dataset 派生人工评审用最新文章子集")
    parser.add_argument("--source-root", required=True)
    parser.add_argument("--output-root", required=True)
    parser.add_argument("--source-surfaces", required=True)
    parser.add_argument("--per-column", type=int, default=30)
    return parser.parse_args()


def article_date(record: dict) -> date:
    raw = (
        record.get("content", {}).get("publishDate")
        or record.get("evidence", {}).get("detailPublishDate")
        or record.get("evidence", {}).get("listPublishDate")
    )
    return date.fromisoformat(raw) if raw else date.min


def main():
    args = parse_args()
    if args.per_column <= 0:
        raise SystemExit("--per-column 必须大于 0")

    source_root = Path(args.source_root).resolve()
    output_root = Path(args.output_root).resolve()
    surfaces = json.loads(Path(args.source_surfaces).read_text(encoding="utf-8"))
    aliases = [surface["columnAlias"] for surface in surfaces["articleSurfaces"]]
    if len(aliases) != len(set(aliases)):
        raise SystemExit("source-surfaces articleSurfaces 存在重复 columnAlias")

    source_manifest = json.loads((source_root / "manifest.json").read_text(encoding="utf-8"))
    entries = [
        json.loads(line)
        for line in (source_root / "index.ndjson").read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]

    grouped = {alias: [] for alias in aliases}
    for entry in entries:
        article_path = source_root / entry["path"]
        record = json.loads(article_path.read_text(encoding="utf-8"))
        alias = record["target"]["columnAlias"]
        if alias not in grouped:
            continue
        if record["source"]["legacyKey"] != entry["legacyKey"]:
            raise SystemExit(f"index/article legacyKey 不一致：{entry['legacyKey']}")
        grouped[alias].append((entry, record, article_path))

    selected = []
    columns = []
    for alias in aliases:
        candidates = grouped[alias]
        if not candidates:
            raise SystemExit(f"主站 canonical dataset 缺少栏目数据：{alias}")
        candidates.sort(
            key=lambda item: (
                -article_date(item[1]).toordinal(),
                item[1]["evidence"]["sourceOrder"],
                item[1]["source"]["legacyKey"],
            )
        )
        chosen = candidates[: args.per_column]
        selected.extend(chosen)
        newest = chosen[0][1]
        columns.append(
            {
                "columnAlias": alias,
                "available": len(candidates),
                "selected": len(chosen),
                "latestLegacyKey": newest["source"]["legacyKey"],
                "latestPublishDate": newest["content"].get("publishDate"),
                "latestTitle": newest["content"]["title"],
            }
        )

    if output_root.exists():
        shutil.rmtree(output_root)
    output_root.mkdir(parents=True)

    selected_entries = []
    for entry, _record, article_path in selected:
        src_dir = article_path.parent
        dst_dir = output_root / src_dir.relative_to(source_root)
        shutil.copytree(src_dir, dst_dir, copy_function=hardlink_or_copy)
        selected_entries.append(entry)

    index_text = "".join(
        json.dumps(entry, ensure_ascii=False, separators=(",", ":")) + "\n"
        for entry in selected_entries
    )
    (output_root / "index.ndjson").write_text(index_text, encoding="utf-8")

    summary = {
        "schemaVersion": 1,
        "sourceMigrationId": source_manifest["migrationId"],
        "sourceDatasetDigest": source_manifest["acceptedSnapshot"]["datasetDigest"],
        "policy": {
            "kind": "latest-per-column",
            "perColumn": args.per_column,
            "ordering": "publishDate DESC, sourceOrder ASC, legacyKey ASC",
            "columnAuthority": "data-migrations/main/source-surfaces.json#articleSurfaces",
        },
        "selectedArticles": len(selected_entries),
        "indexSha256": hashlib.sha256(index_text.encode("utf-8")).hexdigest(),
        "columns": columns,
    }
    (output_root / "review-subset-manifest.json").write_text(
        json.dumps(summary, ensure_ascii=False, sort_keys=True, indent=2) + "\n",
        encoding="utf-8",
    )
    print(json.dumps(summary, ensure_ascii=False))


if __name__ == "__main__":
    main()
