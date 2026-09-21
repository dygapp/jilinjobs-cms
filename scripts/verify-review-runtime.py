#!/usr/bin/env python3
import hashlib
import json
import sys
from pathlib import Path
from urllib.parse import urlsplit
from urllib.request import urlopen

base = sys.argv[1].rstrip('/')
root = Path('data-migrations/party/v1')
manifest = json.loads((root / 'manifest.json').read_text(encoding='utf-8'))

def read(path: str) -> bytes:
    assert path.startswith('/') and not path.startswith('//'), path
    with urlopen(base + path, timeout=30) as response:
        assert urlsplit(response.url).netloc == urlsplit(base).netloc, response.url
        return response.read()

def get(path: str):
    return json.loads(read(path))

aliases = ['party'] + [scope['columnAlias'] for scope in manifest['contentScope']]
columns = {alias: get('/api/public/columns/by-alias/' + alias) for alias in aliases}
counts = {}
for scope in manifest['contentScope']:
    alias = scope['columnAlias']
    column = columns[alias]
    assert column['parentId'] == columns['party']['id'], column
    result = get(f"/api/public/articles?columnId={column['id']}&page=0&size=1")
    assert result['total'] == scope['observedListCount'], (alias, result['total'])
    counts[alias] = result['total']

carousel_root = root / 'lists/PARTY_CAROUSEL'
index = json.loads((carousel_root / 'index.json').read_text(encoding='utf-8'))
expected = [
    json.loads((carousel_root / ref['path']).read_text(encoding='utf-8'))
    for ref in sorted(index['items'], key=lambda ref: ref['sourceOrder'])
]
carousel = get('/api/public/lists/by-code/PARTY_CAROUSEL')
assert carousel['imagePolicy'] == 'REQUIRED', carousel
items = carousel['items']
assert len(items) == len(expected) == manifest['acceptedSnapshot']['carouselItems'], items

evidence = []
for item, source in zip(items, expected):
    source_type = source.get('sourceType', 'LINK')
    assert item['sourceType'] == source_type, item
    assert item['title'] == source['title'], item
    assert item['sortOrder'] == source['sourceOrder'], item
    expected_open_mode = {
        'NEW_WINDOW': '_blank',
        'SAME_WINDOW': '_self',
        'DEFAULT': None,
    }[source.get('openMode', 'DEFAULT')]
    assert item['openMode'] == expected_open_mode, (item, source)

    if source_type == 'ARTICLE':
        assert item['articleId'] and item['effectiveImageResourceId'], item
        assert item['imageResourceId'] == item['effectiveImageResourceId'], item
        assert item['imagePath'] is None and item['url'] is None, item
        assert item['articleType'] == 'INTERNAL' and item['articleStatus'] == 'PUBLISHED', item
        article = get(f"/api/public/articles/{item['articleId']}")
        assert article['columnId'] == columns['party-theme-education']['id'], article
        assert article['title'] == source['title'], article
        image_path = f"/api/public/resources/{item['effectiveImageResourceId']}/content"
    else:
        assert item['articleId'] is None and item['effectiveImageResourceId'] is None, item
        assert item['url'] == source['url'], item
        image_path = item['imagePath']
        assert image_path == '/static/' + source['staticTarget'], (item, source)

    content = read(image_path)
    assert len(content) == source['image']['sizeBytes'], image_path
    digest = hashlib.sha256(content).hexdigest()
    assert digest == source['image']['sha256'], (image_path, digest)
    evidence.append({
        'position': source['sourceOrder'],
        'sourceType': source_type,
        'articleId': item['articleId'],
        'imageResourceId': item['effectiveImageResourceId'],
        'imageSha256': digest,
    })

main_subset_evidence = {}
baseline_manifest_path = Path('review-baseline-restore/manifest.json')
if baseline_manifest_path.is_file():
    baseline_manifest = json.loads(baseline_manifest_path.read_text(encoding='utf-8'))
    main_subset = baseline_manifest.get('mainReviewSubset')
    if main_subset:
        assert main_subset['policy']['kind'] == 'latest-per-column', main_subset
        assert main_subset['policy']['perColumn'] == 30, main_subset
        for expected_column in main_subset['columns']:
            alias = expected_column['columnAlias']
            column = get('/api/public/columns/by-alias/' + alias)
            result = get(f"/api/public/articles?columnId={column['id']}&page=0&size=50")
            assert result['total'] == expected_column['selected'], (alias, result['total'], expected_column['selected'])
            titles = {item['title'] for item in result['items']}
            assert expected_column['latestTitle'] in titles, (alias, expected_column['latestTitle'])
            main_subset_evidence[alias] = {
                'selected': expected_column['selected'],
                'runtimeTotal': result['total'],
                'latestTitle': expected_column['latestTitle'],
                'latestPublishDate': expected_column['latestPublishDate'],
            }

print(json.dumps({
    'base': base,
    'status': manifest['status'],
    'columns': counts,
    'carousel': evidence,
    'mainReviewSubset': main_subset_evidence,
}, ensure_ascii=False))
