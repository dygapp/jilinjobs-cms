#!/usr/bin/env python3
import argparse
import hashlib
import json
import re
import shutil
from pathlib import Path

SHA_RE = re.compile(r'^[0-9a-f]{64}$')
SAFE_RE = re.compile(r'[^A-Za-z0-9._-]+')


def stable_id(legacy_key: str) -> str:
    value = SAFE_RE.sub('-', legacy_key).strip('-').lower()
    if not value:
        raise ValueError(f'invalid legacyKey: {legacy_key!r}')
    return value


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as file:
        for chunk in iter(lambda: file.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def dump_json(path: Path, value) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def copy_verified(source_root: Path, source_rel: str, target: Path, expected_sha: str, expected_size: int) -> None:
    source = (source_root / source_rel).resolve()
    root = source_root.resolve()
    if root not in source.parents or not source.is_file():
        raise ValueError(f'resource path outside source root or missing: {source_rel}')
    if source.stat().st_size != expected_size:
        raise ValueError(f'resource size mismatch: {source_rel}')
    if sha256(source) != expected_sha:
        raise ValueError(f'resource sha mismatch: {source_rel}')
    target.parent.mkdir(parents=True, exist_ok=True)
    if not target.exists():
        shutil.copy2(source, target)
    elif target.stat().st_size != expected_size or sha256(target) != expected_sha:
        raise ValueError(f'conflicting target resource: {target}')


def promote(source_root: Path, target_root: Path, generated_at: str, artifact_digest: str) -> dict:
    articles_file = source_root / 'articles.ndjson'
    carousel_file = source_root / 'carousel.json'
    reconciliation_file = source_root / 'reports/reconciliation.json'
    if not all(path.is_file() for path in [articles_file, carousel_file, reconciliation_file]):
        raise ValueError('legacy candidate snapshot is incomplete')

    target_root.mkdir(parents=True, exist_ok=True)
    articles_dir = target_root / 'articles'
    lists_dir = target_root / 'lists/PARTY_CAROUSEL'
    reports_dir = target_root / 'reports'
    for directory in [articles_dir, lists_dir, reports_dir]:
        if directory.exists():
            shutil.rmtree(directory)
        directory.mkdir(parents=True, exist_ok=True)

    index_rows = []
    article_count = 0
    internal_count = 0
    external_count = 0
    article_asset_hashes = set()
    article_asset_copies = set()

    with articles_file.open(encoding='utf-8') as file:
        for line in file:
            if not line.strip():
                continue
            article = json.loads(line)
            legacy_key = article['source']['legacyKey']
            sid = stable_id(legacy_key)
            article_root = articles_dir / sid
            if article_root.exists():
                raise ValueError(f'duplicate stable id: {sid}')
            article_root.mkdir(parents=True)

            body_html = article['content']['bodyHtml']
            canonical_resources = []
            seen_asset_targets = set()
            for resource in article.get('resources', []):
                expected_sha = resource['sha256']
                if not SHA_RE.fullmatch(expected_sha):
                    raise ValueError(f'invalid resource sha: {expected_sha}')
                extension = Path(resource['snapshotPath']).suffix.lower() or '.bin'
                asset_rel = f'assets/{expected_sha}{extension}'
                asset_target = article_root / asset_rel
                if asset_rel not in seen_asset_targets:
                    copy_verified(source_root, resource['snapshotPath'], asset_target, expected_sha, int(resource['sizeBytes']))
                    seen_asset_targets.add(asset_rel)
                    article_asset_copies.add(str(asset_target.relative_to(target_root)))
                article_asset_hashes.add(expected_sha)

                canonical = dict(resource)
                canonical['snapshotPath'] = asset_rel
                canonical_resources.append(canonical)
                token = resource.get('token')
                if token:
                    body_html = body_html.replace(token, asset_rel)

            article['content']['bodyHtml'] = body_html
            article['resources'] = canonical_resources
            dump_json(article_root / 'article.json', article)

            index_rows.append({
                'legacyKey': legacy_key,
                'path': f'articles/{sid}/article.json',
                'contentId': article['source'].get('contentId'),
                'typeCode': article['source']['typeCode'],
                'detailPath': article['source']['detailPath'],
                'sourceUrl': article['source']['url'],
                'columnAlias': article['target']['columnAlias'],
                'articleType': article['target']['articleType'],
                'sourceFingerprint': article['sourceFingerprint'],
                'sourceOrder': article['evidence']['sourceOrder'],
                'sourceObservation': 'ACTIVE',
                'firstSeenAt': generated_at,
                'lastSeenAt': generated_at,
            })
            article_count += 1
            if article['target']['articleType'] == 'INTERNAL':
                internal_count += 1
            elif article['target']['articleType'] == 'EXTERNAL_LINK':
                external_count += 1
            else:
                raise ValueError(f"unexpected articleType: {article['target']['articleType']}")

    index_rows.sort(key=lambda row: (row['typeCode'], row['sourceOrder'], row['legacyKey']))
    with (target_root / 'index.ndjson').open('w', encoding='utf-8') as output:
        for row in index_rows:
            output.write(json.dumps(row, ensure_ascii=False, separators=(',', ':')) + '\n')

    legacy_carousel = json.loads(carousel_file.read_text(encoding='utf-8'))
    item_index = {
        'listCode': legacy_carousel['listCode'],
        'sourceSystem': legacy_carousel['sourceSystem'],
        'sourcePage': legacy_carousel['sourcePage'],
        'items': [],
    }
    carousel_hashes = set()
    for item in sorted(legacy_carousel['items'], key=lambda value: value['sourceOrder']):
        sid = stable_id(item['legacyKey'])
        item_root = lists_dir / 'items' / sid
        image = dict(item['image'])
        expected_sha = image['sha256']
        extension = Path(image['snapshotPath']).suffix.lower() or '.bin'
        asset_rel = f'assets/{expected_sha}{extension}'
        copy_verified(source_root, image['snapshotPath'], item_root / asset_rel, expected_sha, int(image['sizeBytes']))
        carousel_hashes.add(expected_sha)
        image['snapshotPath'] = asset_rel
        canonical_item = dict(item)
        canonical_item['image'] = image
        dump_json(item_root / 'item.json', canonical_item)
        item_index['items'].append({
            'legacyKey': item['legacyKey'],
            'path': f'items/{sid}/item.json',
            'sourceOrder': item['sourceOrder'],
            'sourceFingerprint': item['sourceFingerprint'],
        })
    dump_json(lists_dir / 'index.json', item_index)

    for name in ['reconciliation.json', 'issues.json', 'home-image-candidates.json']:
        source = source_root / 'reports' / name
        if source.is_file():
            shutil.copy2(source, reports_dir / name)

    reconciliation = json.loads(reconciliation_file.read_text(encoding='utf-8'))
    expected_articles = reconciliation['normalizedRecords']
    if article_count != expected_articles:
        raise ValueError(f'article count mismatch: {article_count} != {expected_articles}')
    if len(legacy_carousel['items']) != 4:
        raise ValueError('carousel item count mismatch')

    for row in index_rows:
        article_path = target_root / row['path']
        article = json.loads(article_path.read_text(encoding='utf-8'))
        article_root = article_path.parent
        resource_paths = {resource['snapshotPath'] for resource in article.get('resources', [])}
        for resource in article.get('resources', []):
            path = article_root / resource['snapshotPath']
            if not path.is_file() or path.stat().st_size != resource['sizeBytes'] or sha256(path) != resource['sha256']:
                raise ValueError(f'canonical article resource invalid: {path}')
        for relative_path in re.findall(r'(?:src|href)=["\'](assets/[^"\']+)["\']', article['content']['bodyHtml']):
            if relative_path not in resource_paths:
                raise ValueError(f'body references undeclared asset: {row["legacyKey"]}: {relative_path}')

    manifest = {
        'migrationId': 'party-v1',
        'formatVersion': 1,
        'status': 'accepted-canonical',
        'sourceSystem': 'legacy-jilinjobs',
        'sourceOrigin': 'https://24365.jl.smartedu.cn',
        'sourceStrategy': 'server-rendered-html-with-browser-cross-check',
        'acceptedSnapshot': {
            'generatedAt': generated_at,
            'artifactDigest': artifact_digest,
            'articles': article_count,
            'internalArticles': internal_count,
            'externalArticles': external_count,
            'uniqueArticleResources': len(article_asset_hashes),
            'articleAssetFiles': len(article_asset_copies),
            'carouselItems': len(legacy_carousel['items']),
            'carouselResources': len(carousel_hashes),
            'unresolved': reconciliation['unresolved'],
        },
        'contentScope': [
            {
                'typeCode': scope['typeCode'],
                'columnAlias': scope['columnAlias'],
                'observedListCount': scope['uniqueCount'],
            }
            for scope in reconciliation['scopes']
        ],
        'notes': [
            'Canonical dataset 是从已冻结并完成 Human Review 的 EU-29 Run #7 Artifact 无损重排产生，不重新访问原站。',
            '文章采用 article-level self-contained unit；正文资源引用改为 article-relative assets/**。',
            '后续增量直接修改 index.ndjson 与对应文章/列表项目录，不建立新的全量 Snapshot ZIP。',
        ],
    }
    dump_json(target_root / 'manifest.json', manifest)

    summary = {
        'articles': article_count,
        'internal': internal_count,
        'external': external_count,
        'uniqueArticleResources': len(article_asset_hashes),
        'articleAssetFiles': len(article_asset_copies),
        'carouselItems': len(legacy_carousel['items']),
        'carouselResources': len(carousel_hashes),
        'unresolved': reconciliation['unresolved'],
    }
    dump_json(reports_dir / 'canonical-promotion.json', summary)
    return summary


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('source_root', type=Path)
    parser.add_argument('target_root', type=Path)
    parser.add_argument('--generated-at', required=True)
    parser.add_argument('--artifact-digest', required=True)
    args = parser.parse_args()
    summary = promote(args.source_root.resolve(), args.target_root.resolve(), args.generated_at, args.artifact_digest)
    print(json.dumps(summary, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
