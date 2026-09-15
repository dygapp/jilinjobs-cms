# 主站历史内容采集与 Canonical Migration 技术方案

## Authority

- `docs/requirements/main-historical-content-migration.md`
- `docs/specifications/main-historical-content-migration.md`
- `data-migrations/README.md`
- `docs/technical/cms-site-package-boundary.md`
- GitHub Issue #60 / E3

## 状态

- Technical Authority：**CURRENT / FROZEN**；
- EU-50 source/promotion implementation：**COMPLETED**；
- EU-51 Runtime import/reconciliation/review：**COMPLETED**；
- Current scope：**ARTICLE ONLY**；
- Ordinary execution：**FROZEN / explicit reactivation only**。

## 1. Current durable topology

```text
Legacy Source
   ↓ explicit bounded acquisition only
source evidence / retries / classification
   ↓
accepted Article canonical subset
   ↓ repository-owned data-migrations/main/v1/**
Generic Content Migration application
   ↓
CMS Runtime Article / Resource / legacy mapping
```

同一 source pass 发现的 Page / List facts 只形成 Site Package handoff evidence；Current Main import 不消费它们。

## 2. Repository implementation surface

Durable canonical source 位于：

```text
data-migrations/main/v1/**
```

Generic import runtime 位于独立 `content-migration` application，并复用 `cms-core` site-neutral capability。Ordinary `cms-server` 不承担 Main migration execution responsibility。

Source acquisition / analysis tooling 与 historical workflows 可以保留用于 traceability 或未来 explicit reactivation，但不得被 ordinary startup / CI 当作 Main Runtime dependency。

## 3. Accepted subset

Current canonical input：3078 Articles。

```text
1577 INTERNAL
1501 EXTERNAL_LINK
```

Excluded from current canonical import：

```text
6   source-defect records
230 deferred problem records
```

Resources：2603 files / 450,273,166 bytes。

Dataset digest：`sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`。

## 4. Canonical preflight

任何 future reactivation 在 Runtime mutation 前必须至少验证：

- manifest / index / item parse；
- stable `sourceSystem + legacyKey` uniqueness；
- target Column alias exists；
- normalized path stays under snapshot root；
- resource size / SHA-256；
- body resource reference resolvable；
- article type / external URL / local body contract；
- dataset arithmetic 与 problem reports 一致。

Preflight failure 必须 fail closed，不执行 partial silent import。

## 5. Import semantics

Generic migration semantics：

```text
no mapping                    -> CREATE
mapping + same fingerprint    -> SKIP
mapping + changed fingerprint -> CONFLICT
```

只有明确、repository-owned compatibility Authority 才能允许特定 old→current transition；Generic engine 不内建 Main-specific guess。

Resource import 必须保持 path safety、content validation、digest、body reference rewrite 与 DB/filesystem side-effect boundary。

## 6. Runtime prerequisite

Reactivation Runtime 顺序：

```text
Generic Flyway V1～V4
→ Site Package stable structure reconcile
→ stable asset projection
→ canonical Main Article import
→ Runtime / Browser verification
```

Main Article import 不要求 Main ordinary bootstrap 才能解析历史 Article；需要的 stable Column identities 来自 Site Package structure。

## 7. Page / List ownership

Current Main ownership：

- Page → Site Package + Page Content Architecture；
- Main ListItem defaults → Site Package one-time bootstrap；
- Historical Article → Main canonical migration。

EU-50 handoff 中“stable ListItem future reconcile”的旧推断已经被 EU-53 current Authority 取代。Technical implementation 不新增 `cms_list_item` stable package identity，也不把 Main ListItem 重新导入 Historical Migration。

## 8. Verification

Future reactivation 最小 Current Evidence：

- canonical static validation；
- Current Fresh MySQL V1～V4；
- Site Package stable structure；
- first import accepted counts；
- second import all accepted items idempotent SKIP；
- injected changed-fingerprint conflict；
- resource bytes / paths；
- Public/Admin representative content；
- zero Main Page/List import units；
- no Legacy Source access during stable import。

## 9. 冻结与重新激活

EU-51 closure 后 ordinary Main Historical Migration execution 保持冻结。

重新激活必须由新的明确 Authority 说明：

- 为什么需要 reactivation；
- source / canonical scope；
- 是否处理 deferred / source-defect；
- expected accepted count / digest change；
- current implementation / workflow compatibility；
- required verification / Human Review。

不得从本 Technical Plan 的 `CURRENT` 分类、历史 Issue comment 或旧 workflow 名称自动取得 Execute Authority。
