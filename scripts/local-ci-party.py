#!/usr/bin/env python3
"""Party migration assertions shared by the local Docker CI path."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
from pathlib import Path


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def migration_report(path: Path):
    text = path.read_text(encoding="utf-8")
    matches = re.findall(r"CONTENT_MIGRATION_REPORT (\{.*\})", text)
    assert matches, f"missing CONTENT_MIGRATION_REPORT: {path}"
    return json.loads(matches[-1])


def validate_current(root: Path) -> None:
    manifest = load_json(root / "manifest.json")
    assert manifest["status"] == "accepted-canonical", manifest
    accepted = manifest["acceptedSnapshot"]
    extension = manifest.get("candidateExtension") or {}
    runtime_articles = int(extension.get("runtimeDatasetArticles", accepted["articles"]))
    runtime_internal = int(accepted["internalArticles"]) + int(extension.get("internalArticles", 0))
    runtime_external = int(accepted["externalArticles"]) + int(extension.get("externalArticles", 0))
    assert accepted["articles"] == 181
    assert accepted["carouselItems"] == 4
    assert accepted["unresolved"] == 0
    assert runtime_articles == 183

    rows = [
        json.loads(line)
        for line in (root / "index.ndjson").read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]
    assert len(rows) == runtime_articles, (len(rows), runtime_articles)
    assert len({row["legacyKey"] for row in rows}) == runtime_articles
    assert len({row["path"] for row in rows}) == runtime_articles
    assert all(set(row) == {"legacyKey", "path"} for row in rows), rows[0]

    article_types = {"INTERNAL": 0, "EXTERNAL_LINK": 0}
    by_alias: dict[str, int] = {}
    for row in rows:
        article_path = (root / row["path"]).resolve()
        assert root.resolve() in article_path.parents and article_path.is_file(), row
        article = load_json(article_path)
        assert article["source"]["legacyKey"] == row["legacyKey"]
        assert re.fullmatch(r"[0-9a-f]{64}", article["sourceFingerprint"])
        article_type = article["target"]["articleType"]
        article_types[article_type] += 1
        alias = article["target"]["columnAlias"]
        by_alias[alias] = by_alias.get(alias, 0) + 1
        declared = {resource["snapshotPath"] for resource in article["resources"]}
        for resource in article["resources"]:
            path = article_path.parent / resource["snapshotPath"]
            assert path.is_file(), path
            assert path.stat().st_size == resource["sizeBytes"]
            assert digest(path) == resource["sha256"]
        for relative in re.findall(r"""(?:src|href)=["'](assets/[^"']+)["']""", article["content"]["bodyHtml"]):
            assert relative in declared, (row["legacyKey"], relative)

    expected_scope = {
        scope["columnAlias"]: int(scope["observedListCount"])
        for scope in manifest["contentScope"]
    }
    assert by_alias == expected_scope, (by_alias, expected_scope)
    assert article_types == {
        "INTERNAL": runtime_internal,
        "EXTERNAL_LINK": runtime_external,
    }, article_types

    carousel_root = root / "lists/PARTY_CAROUSEL"
    carousel = load_json(carousel_root / "index.json")
    assert carousel["listCode"] == "PARTY_CAROUSEL"
    assert len(carousel["items"]) == 4
    assert {item["sourceOrder"] for item in carousel["items"]} == {1, 2, 3, 4}
    by_order = {}
    for reference in carousel["items"]:
        item_path = (carousel_root / reference["path"]).resolve()
        assert carousel_root.resolve() in item_path.parents and item_path.is_file(), reference
        item = load_json(item_path)
        assert item["legacyKey"] == reference["legacyKey"]
        assert item["sourceFingerprint"] == reference["sourceFingerprint"]
        assert item["sourceOrder"] == reference["sourceOrder"]
        assert item["sourceType"] in {"LINK", "ARTICLE"}
        assert item["openMode"] in {"DEFAULT", "SAME_WINDOW", "NEW_WINDOW"}
        image = item["image"]
        image_path = item_path.parent / image["snapshotPath"]
        assert image_path.is_file(), image_path
        assert image_path.stat().st_size == image["sizeBytes"]
        assert digest(image_path) == image["sha256"]
        if item["sourceType"] == "ARTICLE":
            assert "articleReference" in item and "url" not in item, item
        else:
            assert item["url"].startswith(("http://", "https://")), item
            assert item["staticTarget"].startswith("migrated/"), item
        by_order[item["sourceOrder"]] = item

    assert by_order[2]["sourceType"] == "ARTICLE", by_order[2]
    assert by_order[2]["articleReference"]["sourceSystem"] == manifest["sourceSystem"], by_order[2]
    assert by_order[2]["articleReference"]["legacyKey"] == extension["articleRefForCarouselPosition2"], by_order[2]
    assert all(by_order[order]["sourceType"] == "LINK" for order in (1, 3, 4))

    compatibility = load_json(root / "compatibility.json")
    assert compatibility["version"] == 1
    transitions = compatibility["listItemTransitions"]
    assert len(transitions) == 1
    transition = transitions[0]
    assert transition["legacyKey"] == by_order[2]["legacyKey"]
    assert transition["fromSourceType"] == "LINK"
    assert transition["preserveRuntimeId"] is True
    print(
        json.dumps(
            {
                "runtimeArticles": runtime_articles,
                "carouselItems": 4,
                "canonicalTotal": runtime_articles + 4,
            },
            ensure_ascii=False,
        )
    )


def verify_canonical_first(manifest_path: Path, log_path: Path, tsv_path: Path, runtime_root: Path) -> None:
    manifest = load_json(manifest_path)
    expected_articles = manifest.get("candidateExtension", {}).get(
        "runtimeDatasetArticles", manifest["acceptedSnapshot"]["articles"]
    )
    expected_total = expected_articles + manifest["acceptedSnapshot"]["carouselItems"]
    report = migration_report(log_path)
    assert report["total"] == expected_total and report["created"] == expected_total, report
    assert report["updated"] == 0 and report["skipped"] == 0
    assert report["conflicts"] == 0 and report["invalid"] == 0

    root = manifest_path.parent / "lists/PARTY_CAROUSEL"
    index = load_json(root / "index.json")
    expected = [
        load_json(root / ref["path"])
        for ref in sorted(index["items"], key=lambda item: item["sourceOrder"])
    ]
    rows = list(csv.reader(tsv_path.open(encoding="utf-8"), delimiter="\t"))
    assert len(rows) == 4, rows
    for item, row in zip(expected, rows):
        (
            order,
            title,
            source_type,
            article_id,
            article_legacy_key,
            image_path,
            image_resource_id,
            storage_key,
            image_sha,
        ) = row
        assert int(order) == item["sourceOrder"]
        assert title == item["title"]
        assert source_type == item["sourceType"]
        assert image_sha == item["image"]["sha256"]
        if source_type == "ARTICLE":
            assert article_id and image_resource_id and storage_key and not image_path, row
            assert article_legacy_key == item["articleReference"]["legacyKey"], row
            file = runtime_root / "uploads" / storage_key
        else:
            assert not article_id and not article_legacy_key and not image_resource_id and not storage_key, row
            assert image_path == "/static/" + item["staticTarget"], (image_path, item)
            file = runtime_root / "static" / item["staticTarget"]
        assert file.is_file(), file
        assert digest(file) == image_sha


def verify_canonical_second(manifest_path: Path, log_path: Path, article_count: int, list_count: int) -> None:
    manifest = load_json(manifest_path)
    expected_articles = manifest.get("candidateExtension", {}).get(
        "runtimeDatasetArticles", manifest["acceptedSnapshot"]["articles"]
    )
    expected_total = expected_articles + manifest["acceptedSnapshot"]["carouselItems"]
    report = migration_report(log_path)
    assert report["total"] == expected_total and report["created"] == 0
    assert report["updated"] == 0 and report["skipped"] == expected_total, report
    assert report["conflicts"] == 0 and report["invalid"] == 0, report
    assert article_count == 183, article_count
    assert list_count == 4, list_count


def prepare_eu29(root: Path) -> None:
    manifest = load_json(root / "manifest.json")
    assert manifest["status"] == "accepted-canonical"
    assert manifest["acceptedSnapshot"]["articles"] == 181
    assert manifest["acceptedSnapshot"]["carouselItems"] == 4
    rows = [
        json.loads(line)
        for line in (root / "index.ndjson").read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]
    (root / "index.ndjson").write_text(
        "".join(
            json.dumps({"legacyKey": row["legacyKey"], "path": row["path"]}, ensure_ascii=False) + "\n"
            for row in rows
        ),
        encoding="utf-8",
    )
    list_root = root / "lists/PARTY_CAROUSEL"
    index = load_json(list_root / "index.json")
    for reference in index["items"]:
        item_path = list_root / reference["path"]
        old = load_json(item_path)
        assert old.get("sourceType", "LINK") == "LINK", old
        image = old["image"]
        extension = Path(image["snapshotPath"]).suffix.lstrip(".").lower()
        generic = {
            "legacyKey": old["legacyKey"],
            "sourceOrder": old["sourceOrder"],
            "sourceType": "LINK",
            "title": old["title"],
            "url": old["url"],
            "sourceProvenanceUrl": old["url"],
            "openMode": old.get("openMode", "DEFAULT"),
            "enabled": True,
            "sourceFingerprint": old["sourceFingerprint"],
            "image": {
                "sourceUrl": image["sourceUrl"],
                "snapshotPath": image["snapshotPath"],
                "sha256": image["sha256"],
                "contentType": image.get("contentType"),
                "sizeBytes": image["sizeBytes"],
            },
            "staticTarget": "migrated/party/carousel/" + image["sha256"] + "." + extension,
        }
        item_path.write_text(json.dumps(generic, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (root / "compatibility.json").unlink(missing_ok=True)
    position2 = load_json(list_root / "items/party-carousel-position-2/item.json")
    assert position2["legacyKey"] == "party-carousel:position:2"
    assert position2["sourceType"] == "LINK"
    assert position2["sourceFingerprint"] == "c2ad182b8b2dc981a3cbe3b0153a1e3e47604c1f01dd43e6d25971e1deed10dc"


def verify_upgrade_old(log_path: Path, tsv_path: Path, id_output: Path) -> None:
    report = migration_report(log_path)
    assert report["total"] == 185 and report["created"] == 185 and report["updated"] == 0, report
    assert report["skipped"] == 0 and report["conflicts"] == 0 and report["invalid"] == 0, report
    row = next(csv.reader(tsv_path.open(encoding="utf-8"), delimiter="\t"))
    item_id, source_type, article_id, image_path, image_resource_id, fingerprint, image_sha = row
    assert item_id and source_type == "LINK", row
    assert not article_id and not image_resource_id, row
    assert image_path == "/static/migrated/party/carousel/a00db48e094e24b778374ae621b6f150e235625c62f17efc860391223fac830b.png", row
    assert fingerprint == "c2ad182b8b2dc981a3cbe3b0153a1e3e47604c1f01dd43e6d25971e1deed10dc", row
    assert image_sha == "a00db48e094e24b778374ae621b6f150e235625c62f17efc860391223fac830b", row
    id_output.write_text(item_id, encoding="utf-8")


def verify_upgrade_current(log_path: Path, tsv_path: Path, before_id: Path, uploads: Path) -> None:
    report = migration_report(log_path)
    assert report["total"] == 187 and report["created"] == 2 and report["updated"] == 1 and report["skipped"] == 184, report
    assert report["conflicts"] == 0 and report["invalid"] == 0, report
    position2 = next(result for result in report["results"] if result["legacyKey"] == "party-carousel:position:2")
    assert position2["status"] == "UPDATED", position2
    assert any(
        result["legacyKey"] == "zhutijiaoyu:content:154659859759104" and result["status"] == "CREATED"
        for result in report["results"]
    ), report

    before = before_id.read_text(encoding="utf-8").strip()
    row = next(csv.reader(tsv_path.open(encoding="utf-8"), delimiter="\t"))
    (
        item_id,
        source_type,
        article_id,
        article_legacy_key,
        image_path,
        image_resource_id,
        storage_key,
        fingerprint,
        image_sha,
    ) = row
    assert item_id == before, (item_id, before)
    assert source_type == "ARTICLE" and article_id, row
    assert article_legacy_key == "zhutijiaoyu:content:154659859759104", row
    assert not image_path and image_resource_id and storage_key, row
    assert fingerprint == "f8b5d8df87021373803639b174bf88e46ae6cef7f2599a205763b5887c78be84", row
    assert image_sha == "a00db48e094e24b778374ae621b6f150e235625c62f17efc860391223fac830b", row
    image = uploads / storage_key
    assert image.is_file(), image
    assert digest(image) == image_sha, image


def verify_simple_report(log_path: Path, kind: str) -> None:
    report = migration_report(log_path)
    if kind == "upgrade-second":
        assert report["total"] == 187 and report["created"] == 0 and report["updated"] == 0 and report["skipped"] == 187, report
        assert report["conflicts"] == 0 and report["invalid"] == 0, report
    elif kind == "upgrade-conflict":
        assert report["total"] == 187 and report["conflicts"] == 1 and report["invalid"] == 0, report
        position2 = next(result for result in report["results"] if result["legacyKey"] == "party-carousel:position:2")
        assert position2["status"] == "CONFLICT", position2
    else:
        raise AssertionError(kind)


def main() -> None:
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest="command", required=True)
    p = sub.add_parser("validate-current")
    p.add_argument("root", type=Path)

    p = sub.add_parser("verify-canonical-first")
    p.add_argument("manifest", type=Path)
    p.add_argument("log", type=Path)
    p.add_argument("tsv", type=Path)
    p.add_argument("runtime_root", type=Path)

    p = sub.add_parser("verify-canonical-second")
    p.add_argument("manifest", type=Path)
    p.add_argument("log", type=Path)
    p.add_argument("article_count", type=int)
    p.add_argument("list_count", type=int)

    p = sub.add_parser("prepare-eu29")
    p.add_argument("root", type=Path)

    p = sub.add_parser("verify-upgrade-old")
    p.add_argument("log", type=Path)
    p.add_argument("tsv", type=Path)
    p.add_argument("id_output", type=Path)

    p = sub.add_parser("verify-upgrade-current")
    p.add_argument("log", type=Path)
    p.add_argument("tsv", type=Path)
    p.add_argument("before_id", type=Path)
    p.add_argument("uploads", type=Path)

    for name in ("verify-upgrade-second", "verify-upgrade-conflict"):
        p = sub.add_parser(name)
        p.add_argument("log", type=Path)

    args = parser.parse_args()
    if args.command == "validate-current":
        validate_current(args.root)
    elif args.command == "verify-canonical-first":
        verify_canonical_first(args.manifest, args.log, args.tsv, args.runtime_root)
    elif args.command == "verify-canonical-second":
        verify_canonical_second(args.manifest, args.log, args.article_count, args.list_count)
    elif args.command == "prepare-eu29":
        prepare_eu29(args.root)
    elif args.command == "verify-upgrade-old":
        verify_upgrade_old(args.log, args.tsv, args.id_output)
    elif args.command == "verify-upgrade-current":
        verify_upgrade_current(args.log, args.tsv, args.before_id, args.uploads)
    elif args.command == "verify-upgrade-second":
        verify_simple_report(args.log, "upgrade-second")
    elif args.command == "verify-upgrade-conflict":
        verify_simple_report(args.log, "upgrade-conflict")


if __name__ == "__main__":
    main()
