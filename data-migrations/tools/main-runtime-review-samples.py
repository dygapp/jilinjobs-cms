#!/usr/bin/env python3
import argparse
import json
from collections import defaultdict
from pathlib import Path

TARGET_ALIASES = [
    'notice',
    'employment-news',
    'recruitment-announcement',
    'policy-month',
    'policy-outside',
    'policy-jilin',
    'policy-national',
    'typical-grassroots',
    'typical-startup',
    'typical-military',
]


def parse_args():
    parser = argparse.ArgumentParser(description='Build deterministic EU-51 Main Runtime review samples.')
    parser.add_argument('--canonical-root', required=True)
    parser.add_argument('--runtime-articles', required=True)
    parser.add_argument('--runtime-resources', required=True)
    parser.add_argument('--output', required=True)
    return parser.parse_args()


def read_ndjson(path):
    return [json.loads(line) for line in Path(path).read_text(encoding='utf-8').splitlines() if line.strip()]


def read_json(path):
    return json.loads(Path(path).read_text(encoding='utf-8'))


def article_projection(record, runtime, runtime_resources):
    resources = []
    by_role_order = {(item['role'], int(item['sortOrder'])): item for item in runtime_resources}
    role_order = defaultdict(int)
    for canonical in record.get('resources', []):
        role = canonical['role']
        order = role_order[role]
        role_order[role] += 1
        mapped = by_role_order.get((role, order))
        if mapped is None:
            raise AssertionError(f"missing Runtime resource mapping {record['source']['legacyKey']} {role}#{order}")
        resources.append({
            'role': role,
            'sortOrder': order,
            'runtimeId': int(mapped['resourceId']),
            'sizeBytes': int(canonical['sizeBytes']),
            'sha256': canonical['sha256'],
            'contentType': canonical.get('contentType'),
            'sourceUrl': canonical['sourceUrl'],
        })
    if len(resources) != len(runtime_resources):
        raise AssertionError(f"Runtime resource count mismatch {record['source']['legacyKey']}")
    body = record['content']['bodyHtml'] or ''
    return {
        'runtimeId': int(runtime['runtimeId']),
        'legacyKey': record['source']['legacyKey'],
        'columnAlias': record['target']['columnAlias'],
        'articleType': record['target']['articleType'],
        'title': record['content']['title'],
        'source': record['content']['source'],
        'publishDate': record['content'].get('publishDate'),
        'externalUrl': record['content'].get('externalUrl'),
        'sortOrder': int(runtime['sortOrder']),
        'sourceOrder': int(record['evidence']['sourceOrder']),
        'bodyLength': len(body),
        'bodyImages': [item for item in resources if item['role'] == 'BODY_IMAGE'],
        'attachments': [item for item in resources if item['role'] == 'ATTACHMENT'],
        'resourceCount': len(resources),
    }


def choose_sorted(records, key):
    values = list(records)
    if not values:
        return None
    return sorted(values, key=key)[0]


def compact(sample):
    if sample is None:
        return None
    return dict(sample)


def main():
    args = parse_args()
    root = Path(args.canonical_root).resolve()
    manifest = read_json(root / 'manifest.json')
    if manifest.get('migrationId') != 'main-v1' or manifest.get('status') != 'accepted-current-subset':
        raise AssertionError('Main canonical manifest is not the accepted current subset')

    runtime_rows = read_ndjson(args.runtime_articles)
    resource_rows = read_ndjson(args.runtime_resources)
    runtime = {row['legacyKey']: row for row in runtime_rows}
    if len(runtime) != 3078:
        raise AssertionError(f'expected 3078 Runtime Main Articles, got {len(runtime)}')

    resources_by_key = defaultdict(list)
    for row in resource_rows:
        resources_by_key[row['legacyKey']].append(row)

    index = [json.loads(line) for line in (root / 'index.ndjson').read_text(encoding='utf-8').splitlines() if line.strip()]
    if len(index) != 3078:
        raise AssertionError(f'expected 3078 canonical Main Articles, got {len(index)}')

    records = []
    for entry in index:
        key = entry['legacyKey']
        row = runtime.get(key)
        if row is None:
            raise AssertionError(f'missing Runtime Main Article {key}')
        canonical = read_json(root / entry['path'])
        if canonical['source']['legacyKey'] != key:
            raise AssertionError(f'canonical legacyKey mismatch {key}')
        if row['fingerprint'] != canonical['sourceFingerprint']:
            raise AssertionError(f'Runtime fingerprint mismatch {key}')
        if row['alias'] != canonical['target']['columnAlias']:
            raise AssertionError(f'Runtime column mismatch {key}')
        if row['articleType'] != canonical['target']['articleType']:
            raise AssertionError(f'Runtime type mismatch {key}')
        if row['title'] != canonical['content']['title']:
            raise AssertionError(f'Runtime title mismatch {key}')
        if row['status'] != 'PUBLISHED':
            raise AssertionError(f'Runtime Article not published {key}')
        records.append(article_projection(canonical, row, resources_by_key.get(key, [])))

    by_alias = defaultdict(list)
    for record in records:
        by_alias[record['columnAlias']].append(record)
    if set(by_alias) != set(TARGET_ALIASES):
        raise AssertionError(f'unexpected Main target aliases: {sorted(by_alias)}')

    # Public list ordering: pinned(false) -> sortOrder DESC -> publishDate DESC -> id DESC.
    # Imported Main Articles are never pinned, so the remaining Runtime fields fully determine the sample.
    column_samples = []
    for alias in TARGET_ALIASES:
        ordered = sorted(
            by_alias[alias],
            key=lambda item: (item['sortOrder'], item['publishDate'] or '', item['runtimeId']),
            reverse=True,
        )
        sample = dict(ordered[0])
        sample['columnArticleCount'] = len(ordered)
        column_samples.append(sample)

    internal = [item for item in records if item['articleType'] == 'INTERNAL']
    external = [item for item in records if item['articleType'] == 'EXTERNAL_LINK']
    with_images = [item for item in internal if item['bodyImages']]
    with_attachments = [item for item in internal if item['attachments']]
    dated = [item for item in records if item['publishDate']]

    resource_rich = choose_sorted(internal, lambda item: (-item['resourceCount'], -item['bodyLength'], item['legacyKey']))
    body_image = choose_sorted(with_images, lambda item: (-len(item['bodyImages']), -item['bodyLength'], item['legacyKey']))
    attachment = choose_sorted(with_attachments, lambda item: (-len(item['attachments']), -item['bodyLength'], item['legacyKey']))
    long_body = choose_sorted(internal, lambda item: (-item['bodyLength'], item['legacyKey']))
    external_sample = choose_sorted(external, lambda item: (-(int((item['publishDate'] or '0000-00-00').replace('-', ''))), item['legacyKey']))
    oldest = choose_sorted(dated, lambda item: (item['publishDate'], item['legacyKey']))
    newest = choose_sorted(dated, lambda item: (''.join(chr(255 - ord(ch)) for ch in item['publishDate']), item['legacyKey']))

    output = {
        'migrationId': 'main-v1',
        'datasetDigest': manifest['acceptedSnapshot']['datasetDigest'],
        'acceptedArticles': 3078,
        'targetAliases': TARGET_ALIASES,
        'columnSamples': column_samples,
        'riskSamples': {
            'resourceRichInternal': compact(resource_rich),
            'bodyImageInternal': compact(body_image),
            'attachmentInternal': compact(attachment),
            'longBodyInternal': compact(long_body),
            'externalLink': compact(external_sample),
            'oldestPublished': compact(oldest),
            'newestPublished': compact(newest),
        },
    }

    if len(column_samples) != 10:
        raise AssertionError('review set must cover all ten target Columns')
    if not resource_rich or not long_body or not external_sample or not oldest or not newest:
        raise AssertionError('review set missing required representative/boundary sample')
    if with_images and body_image is None:
        raise AssertionError('body-image records exist but no body-image review sample was selected')
    if with_attachments and attachment is None:
        raise AssertionError('attachment records exist but no attachment review sample was selected')

    target = Path(args.output)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(output, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('EU51_MAIN_REVIEW_SAMPLE_SET', json.dumps({
        'columns': len(column_samples),
        'bodyImageAvailable': bool(with_images),
        'attachmentAvailable': bool(with_attachments),
        'resourceRichLegacyKey': resource_rich['legacyKey'],
        'longBodyLegacyKey': long_body['legacyKey'],
        'externalLegacyKey': external_sample['legacyKey'],
        'oldestLegacyKey': oldest['legacyKey'],
        'newestLegacyKey': newest['legacyKey'],
        'sampleSet': 'PASS',
    }, ensure_ascii=False))


if __name__ == '__main__':
    main()
