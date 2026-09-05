#!/usr/bin/env python3
import argparse
import hashlib
import json
import shutil
from datetime import datetime, timezone
from pathlib import Path

ARTICLE_REF_KEY = 'zhutijiaoyu:content:154659859759104'
CAROUSEL_KEY = 'party-carousel:position:2'


def read_json(path: Path):
    return json.loads(path.read_text(encoding='utf-8'))


def write_json(path: Path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def fingerprint(value) -> str:
    raw = json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode('utf-8')
    return hashlib.sha256(raw).hexdigest()


def merge(candidate: Path, target: Path, run_id: str, head_sha: str):
    candidate_manifest = read_json(candidate / 'manifest.json')
    if candidate_manifest['status'] != 'candidate-review' or candidate_manifest['unresolved'] != 0:
        raise ValueError('theme candidate is not clean')
    if candidate_manifest['typeCode'] != 'zhutijiaoyu' or candidate_manifest['columnAlias'] != 'party-theme-education':
        raise ValueError('unexpected theme candidate scope')

    candidate_rows = [json.loads(line) for line in (candidate / 'index.ndjson').read_text(encoding='utf-8').splitlines() if line.strip()]
    if len(candidate_rows) != candidate_manifest['uniqueRecords']:
        raise ValueError('candidate index count mismatch')
    if not any(row['legacyKey'] == ARTICLE_REF_KEY for row in candidate_rows):
        raise ValueError(f'carousel article missing from candidate: {ARTICLE_REF_KEY}')

    target_rows = [json.loads(line) for line in (target / 'index.ndjson').read_text(encoding='utf-8').splitlines() if line.strip()]
    existing_keys = {row['legacyKey'] for row in target_rows}
    duplicates = existing_keys.intersection(row['legacyKey'] for row in candidate_rows)
    if duplicates:
        raise ValueError(f'candidate duplicates existing canonical identities: {sorted(duplicates)}')

    for row in candidate_rows:
        source_article = candidate / row['path']
        target_article = target / row['path']
        if target_article.exists() or target_article.parent.exists():
            raise ValueError(f'target article already exists: {row["path"]}')
        shutil.copytree(source_article.parent, target_article.parent, dirs_exist_ok=False)

    merged_rows = target_rows + candidate_rows
    merged_rows.sort(key=lambda row: (row['typeCode'], row['sourceOrder'], row['legacyKey']))
    (target / 'index.ndjson').write_text(
        ''.join(json.dumps(row, ensure_ascii=False, separators=(',', ':')) + '\n' for row in merged_rows),
        encoding='utf-8',
    )

    carousel_root = target / 'lists/PARTY_CAROUSEL'
    carousel_index = read_json(carousel_root / 'index.json')
    reference = next((row for row in carousel_index['items'] if row['legacyKey'] == CAROUSEL_KEY), None)
    if not reference:
        raise ValueError('carousel position 2 missing')
    item_path = carousel_root / reference['path']
    item = read_json(item_path)
    item['sourceType'] = 'ARTICLE'
    item['articleRef'] = {'sourceSystem': 'legacy-jilinjobs', 'legacyKey': ARTICLE_REF_KEY}
    item['sourceFingerprint'] = fingerprint({
        'legacyKey': item['legacyKey'],
        'sourceOrder': item['sourceOrder'],
        'sourceType': item['sourceType'],
        'articleRef': item['articleRef'],
        'legacyUrl': item.get('url'),
        'openMode': item['openMode'],
        'imageSha256': item['image']['sha256'],
    })
    write_json(item_path, item)
    reference['sourceFingerprint'] = item['sourceFingerprint']
    write_json(carousel_root / 'index.json', carousel_index)

    manifest = read_json(target / 'manifest.json')
    accepted = manifest['acceptedSnapshot']
    manifest['status'] = 'candidate-extension'
    manifest['candidateExtension'] = {
        'id': 'EU-30-theme-education',
        'status': 'pending-human-review',
        'generatedAt': datetime.now(timezone.utc).isoformat(),
        'sourceWorkflowRunId': int(run_id),
        'sourceHeadSha': head_sha,
        'addedArticles': len(candidate_rows),
        'internalArticles': candidate_manifest['internalArticles'],
        'externalArticles': candidate_manifest['externalArticles'],
        'reportedTotal': candidate_manifest['reportedTotal'],
        'unresolved': 0,
        'articleRefForCarouselPosition2': ARTICLE_REF_KEY,
        'runtimeDatasetArticles': len(merged_rows),
        'acceptedEu29Articles': accepted['articles'],
    }
    scopes = [scope for scope in manifest.get('contentScope', []) if scope['typeCode'] != 'zhutijiaoyu']
    scopes.append({
        'typeCode': 'zhutijiaoyu',
        'columnAlias': 'party-theme-education',
        'observedListCount': candidate_manifest['uniqueRecords'],
        'status': 'candidate-review',
    })
    manifest['contentScope'] = scopes
    manifest.setdefault('notes', []).append('EU-30 增量补采 legacy“主题教育2023”，新系统栏目名称收敛为“主题教育”；本扩展在 Human Review 前保持 candidate-extension。')
    write_json(target / 'manifest.json', manifest)
    write_json(target / 'reports/eu30-theme-education-candidate.json', candidate_manifest)

    print(json.dumps({
        'acceptedEu29Articles': accepted['articles'],
        'addedArticles': len(candidate_rows),
        'runtimeDatasetArticles': len(merged_rows),
        'carouselPosition2ArticleRef': ARTICLE_REF_KEY,
    }, ensure_ascii=False, indent=2))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('candidate', type=Path)
    parser.add_argument('target', type=Path)
    parser.add_argument('--run-id', required=True)
    parser.add_argument('--head-sha', required=True)
    args = parser.parse_args()
    merge(args.candidate.resolve(), args.target.resolve(), args.run_id, args.head_sha)


if __name__ == '__main__':
    main()
