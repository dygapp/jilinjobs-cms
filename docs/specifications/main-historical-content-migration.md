# 主站历史内容采集与 Canonical Migration 规格说明

## Authority

- `docs/requirements/main-historical-content-migration.md`
- `docs/requirements/main-external-link-boundary.md`
- `docs/specifications/cms-site-package-boundary.md`
- `data-migrations/README.md`
- GitHub Issue #60 / E3

## 状态

- Specification：**CURRENT / FROZEN**；
- EU-50：**COMPLETED**；
- EU-51：**COMPLETED**；
- Migration scope：**ARTICLE ONLY**；
- Ordinary execution：**FROZEN / explicit reactivation only**。

## 1. 已完成 pipeline

```text
Legacy Main Source
→ bounded discovery / collection / retry             [EU-50 COMPLETED]
→ Article eligibility + explicit problem classification
→ Page/List Site Package handoff
→ accepted Article subset promotion                  [EU-50 COMPLETED]
→ Generic Article migration                          [EU-51 COMPLETED]
→ Runtime reconciliation / idempotency
→ Public/Admin/Browser + bounded Human Review        [EU-51 COMPLETED]
```

只有 acquisition stage 可访问 Legacy Source。Current stable verification / import 只消费 repository-owned canonical bytes。

## 2. Article canonical root

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/<stable-id>/article.json
├── articles/<stable-id>/assets/**
├── reports/**
└── source-discovery/**
```

Current accepted canonical input 只包含 3078 import-eligible Articles。Deferred 230 与 source-defect 6 保留在 reports / evidence，不是 current import unit。

## 3. Mapping

- INTERNAL → local canonical body/resources + stable Column alias；
- EXTERNAL_LINK → stable Column alias + external URL，不复制外站正文；
- source identity 使用 `sourceSystem + legacyKey`；
- fingerprint 从 accepted canonical fields deterministic 生成；
- resources 使用 migration-relative safe paths + size + SHA-256；
- Runtime DB id 不进入长期 canonical identity。

## 4. Main Page / List boundary

Main migration 不包含 Page / List import units。

Page/List discovery evidence 可以保留 source observation，但 Current delivery responsibility 是：

```text
Page -> JilinJobs Site Package + Page Content Architecture
Main ListItem initial data -> Site Package one-time bootstrap
Article -> Main Historical Migration
```

Main ListItem bootstrap 完成后是 ordinary operator-managed Runtime Data，不建立 stable ListItem reconcile。

## 5. Problem classification

Canonical promotion / import 必须显式区分：

- accepted import-eligible；
- source-defect / missing；
- deferred review；
- transient transport failure；
- validation / integrity failure。

不得把 transport failure 当作 `404/410`，也不得为了 current import 成功而从 evidence 中删除 problem record。

## 6. Runtime import

Explicit reactivation import 必须：

1. 使用 Current Generic Flyway + Site Package stable structure；
2. 在 mutation 前完成 canonical / path / digest / target identity preflight；
3. CREATE missing accepted Article；
4. exact same fingerprint SKIP；
5. changed fingerprint CONFLICT，除非存在当前明确 compatibility rule；
6. import managed resources 并校验 bytes；
7. 建立 / 保持 legacy mapping；
8. second same-input run idempotent；
9. report created / skipped / conflict / invalid；
10. 不调用 Legacy Source 网络。

## 7. Current accepted counts

```text
Candidates:        3314
Import eligible:   3078
  INTERNAL:        1577
  EXTERNAL_LINK:   1501
Source defect:        6
Deferred:           230
Resources:         2603 files / 450,273,166 bytes
```

Dataset digest：`sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`。

## 8. Verification

任何 future reactivation 至少重验：

- canonical structure / digest；
- stable target Column identity；
- resource path / size / SHA-256；
- first import / second import；
- conflict behavior；
- Public/Admin visibility；
- no Page/List mappings；
- no source-network dependency；
- problem/deferred arithmetic closure。

## 9. 非目标

本文不授予新的 collection/import Execute Authority，不处理 deferred/source-defect backlog，不改变 Site Package / Page / Main ListItem ownership。
