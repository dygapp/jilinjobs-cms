# 主站历史内容采集与 Canonical Migration 需求

## 状态

- 来源：GitHub Issue #60 / E3；
- Requirement：**CURRENT / FROZEN**；
- EU-50 — Main Source Discovery & Article Snapshot Promotion：**COMPLETED**；
- EU-51 — Main Article Import, Runtime Reconciliation & Human Review：**COMPLETED**；
- Current migration scope：**ARTICLE ONLY**；
- Ordinary execution：**FROZEN / explicit reactivation only**；
- Execute Authority：**NONE**。

本文保留已接受 Main Historical Migration 的长期数据 / provenance contract；它不维护 Current Ready Execution Unit，也不因仍位于 requirements 根目录而重新授权采集或导入。

## 1. 目标

Main Historical Migration 负责把 Legacy Main Source 中需要长期 provenance / fingerprint / import lifecycle 的历史 **Article** 内容转化为 repository-owned、offline-verifiable Canonical Migration Dataset。

EU-50 / EU-51 已完成当前接受 subset 的采集、promotion、Runtime import、idempotency、Browser 与 Human Review。后续只有明确的客户确认、纠错、数据扩展或 reactivation Authority 才能重新打开执行。

## 2. Ownership boundary

### Historical Migration

只处理：

- INTERNAL Article；
- EXTERNAL_LINK Article；
- Article body images / attachments；
- source provenance / legacy identity / source fingerprint；
- import / idempotency / conflict evidence。

### Site Package

以下不属于 Main Historical Migration import unit：

- stable Page identity 与 Current Page content contract；
- stable CmsList definitions；
- Main one-time bootstrap ListItem initialization；
- stable navigation / site config / stable site assets。

Main ListItem 当前长期结论是 **Site Package one-time bootstrap → ordinary operator-managed Runtime Data**，不是 stable ListItem reconcile structure。不得从 EU-50 阶段旧 planning 恢复“stable ListItem provisioning capability gap”。

## 3. Source boundary

Legacy Source 网络访问只允许在显式 source discovery / retry / reactivation evidence run 中发生。

已接受 source evidence 识别 `https://24365.jl.smartedu.cn/` 为主要可达内容来源，`cms.jilinjobs.cn` 为相关 source / redirect host。这些只是 acquisition evidence，不是 Runtime dependency。

稳定 verification / import 只消费 repository-owned frozen bytes，不依赖 Legacy Source 在线可用性。

## 4. Error / deferred-review contract

不得静默修复、丢弃或猜测 source problem。

- 只有明确 HTTP `404 / 410` evidence 才可分类为 `SOURCE_RESOURCE_MISSING`；
- timeout / socket / transport error 不等同 source missing；
- blocking problem 保留 identity / title / source / error evidence；
- 当前 deferred / source-defect records 不属于 accepted import input；
- 后续处理必须经过新的明确 Authority，不得为了“让 migration 全绿”自动删除或改写。

## 5. Current accepted Article snapshot

当前 repository-owned accepted facts：

```text
3314 Article candidates
= 3078 import-eligible
  - 1577 INTERNAL
  - 1501 EXTERNAL_LINK
+ 6 source-defect Articles pending confirmation
+ 230 deferred problem Articles
```

资源：

- resource files：2603；
- resource bytes：450,273,166；
- dataset digest：`sha256:92f05017923ebff5ca3b77108e60d5d79521dba0d5487878035b727fbff9095a`。

Canonical root：

```text
data-migrations/main/v1/
├── manifest.json
├── index.ndjson
├── articles/**
├── reports/**
└── source-discovery/**
```

只有 3078 accepted Articles 进入 canonical index / article units；6 + 230 继续作为 durable reports/evidence。

## 6. Canonical contract

每个 accepted Article 至少保持：

- stable source system + legacy key；
- target Column alias；
- INTERNAL / EXTERNAL_LINK semantics；
- canonical body / external URL；
- source fingerprint；
- resource path / size / SHA-256（适用时）；
- provenance / publish metadata（源证据存在时）。

Canonical input 不依赖 Runtime DB id。

## 7. Page / List source handoff

EU-50 同一次 source pass 曾发现 Page / List surface，但它们只保留为 Site Package handoff / evidence，不进入 Main canonical import。

EU-52 / EU-53 / EU-55 后，Current responsibility 已分别收敛为：

- Page formal defaults / Structured contract → Site Package + Page Content Architecture；
- Main ListItem initial data → Site Package bootstrap；
- Historical Migration → Article-only。

## 8. Runtime import contract

当 explicit reactivation 发生时仍必须保持：

- Generic Flyway + Site Package stable structure ready before import；
- canonical preflight fail closed；
- stable identity mapping；
- same fingerprint → SKIP；
- changed fingerprint → CONFLICT，除非存在明确 compatibility Authority；
- resource integrity；
- same-input idempotency；
- no Page/List implicit import；
- no network dependency during stable import。

## 9. 非目标

- 不重新采集已冻结 accepted subset；
- 不把 Page / Main ListItem 放回 Main migration；
- 不把 historical content 写入 Backend Flyway；
- 不通过 migration 改写 Site Package stable assets；
- 不因为本文仍为 Current Requirement 就自动创建新的 Execution Unit；
- 不处理 6 + 230 records，除非取得新的明确确认 / reactivation Authority。
