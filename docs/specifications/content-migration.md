---
id: specification-content-migration
title: 历史内容迁移规格
type: specification
status: accepted
version: "V1.0"
relations:
  requirements:
    - docs/requirements/information-publishing.md
    - docs/requirements/cms-domain.md
  architecture:
    - docs/architecture/cms-architecture.md
  technical:
    - docs/technical/backend-service.md
  verification:
    - docs/technical/verification-strategy.md
updated_at: 2026-09-16
---

# 历史内容迁移规格

## 1. 范围

本规格定义项目维护者在**已经获得当前执行 Authority** 时，对 历史内容迁移 可以观察和验收的受控导入行为，包括 preflight、执行结果、冲突 / 无效状态、幂等性与可审计 report。

Historical object 的 stable source identity、fingerprint、CREATE / SKIP / CONFLICT、accepted Main / Party scope 与 compatibility business rule 由 `docs/requirements/cms-domain.md` 持有；canonical dataset、digest、provenance 与具体记录由 `data-migrations/**` 持有；Content Migration application boundary 由 `docs/architecture/cms-architecture.md` 持有；具体 CLI 名称、Gradle task、class、parser、文件布局与 process wiring 属于 Technical / implementation。

本规格**不授予 migration execute authority**。Main historical migration 是否可执行继续服从 Repository 当前治理状态；根 `README.md` 当前定义的 Main `FROZEN / explicit reactivation only` 不因本规格存在而解除。Party 或其他 migration activity 同样必须从当前 Work / Governance Authority 获得真实执行入口。

## 2. 受控输入

Stable verification / import 消费明确授权、可追溯的 canonical snapshot / dataset，而不是把 Legacy Source 在线响应当作普通 Runtime input。

项目维护者应能够确认本次输入的 scope 与 provenance；缺失、source-defect、deferred 或尚未获得接受 Authority 的记录不能为了完成导入而被静默猜测、补值、删除或覆盖。

Legacy Source acquisition / retry / reactivation 是独立 activity，不因为执行 canonical import 自动发生。

## 3. 加载 → 预检 → 执行 → 报告

历史迁移 的可观察生命周期至少区分：

```text
load
→ preflight
→ execute（只有 preflight 允许时）
→ report
```

在产生已知会导致错误的 Runtime mutation 前，preflight 必须能够识别当前 Authority 要求的 canonical shape、path / resource integrity、stable target、dependency、duplicate identity、fingerprint conflict 与 compatibility condition。

只要当前 dataset 存在已知 `INVALID` / unresolved `CONFLICT` 或必要 dependency 无法满足，本次受影响 dataset 不得继续执行成“部分成功但整体显示成功”。纯 preflight failure 不产生该 dataset 的 Runtime mutation。

## 4. 结果语义

每个 canonical migration unit 的结果必须能够落入当前 Domain semantics，并由项目维护者从 report 中辨认：

- `CREATED`：此前没有 accepted mapping，本次建立新的 Runtime object / mapping；
- `SKIPPED`：stable identity 与 accepted fingerprint 已一致，本次不重复创建；
- `CONFLICT`：同一 stable identity 的 fingerprint / accepted state 不允许按当前 Authority 自动覆盖；
- `INVALID`：canonical input、resource、target、dependency 或其他前置条件不满足，不能进入有效执行。

若存在 explicit、versioned、scope-bounded compatibility authority，允许的 historical transition 可以产生该 compatibility contract 定义的受控结果；这不能变成 ordinary arbitrary overwrite policy。

重复执行相同 accepted canonical input 必须保持幂等，不制造 duplicate Runtime content 或重复 mapping。

## 5. 可审计报告 / 流程结果

每次受控 migration 都必须形成足以审计本次范围和结果的 report。Report 至少能够恢复：

- 本次处理总量；
- created / skipped / conflict / invalid 汇总；
- 每个异常或需要追溯的 unit 的 stable identity；
- 对应 result status；
- 可诊断 message / reason；
- 本次停止在 preflight 还是已经进入 execute 的事实。

具体 JSON field name、stdout label、文件名或序列化库不是 Product contract；替换 migration implementation 时可以变化，只要同等审计语义仍可恢复。

存在 unresolved `CONFLICT` / `INVALID` 时，process / task 必须产生明确失败结果，不能只打印 warning 后以成功完成伪装通过。

## 6. 副作用边界

- 已知 structural / dependency / conflict 问题应尽可能在 execute 前集中暴露；
- preflight 未通过时不创建与本次失败 dataset 无关的 Runtime business record；
- execute 阶段的业务写入继续遵守 Core transaction / resource lifecycle；
- 如果某类外部文件 / managed resource side effect 无法与最终 DB mapping 形成分布式原子事务，失败 report 必须能够定位受影响 identity，不能把残余 side effect 隐藏为完整成功；
- migration 不启动 ordinary CMS Server HTTP lifecycle，也不把 Legacy Source 变成稳定 Runtime dependency。

## 7. 失败行为

以下情况必须 失败关闭，并在 report / process outcome 中可诊断：

- canonical path 越界、缺失文件、digest / size 不一致；
- duplicate stable source identity；
- 必要 stable target 不存在 / 不可用；
- dependency 缺失或无法解析；
- same identity + changed fingerprint 且没有 accepted compatibility；
- canonical shape / resource relation 不满足当前 Domain contract；
- 执行结果无法确定为当前 accepted migration status。

不得通过默认 target、silent overwrite、跳过异常但仍返回成功等方式制造假阳性完成状态。

## 8. 验收

触达 历史内容迁移 capability 时，最终结果至少满足实际涉及的以下 contract：

- canonical input 在没有 Legacy Source 在线依赖的情况下可以完成 stable preflight / import；
- first accepted import 能形成 `CREATED` 结果；
- 对同一 accepted canonical bytes 重复执行形成 `SKIPPED`，不制造 duplicate content；
- changed fingerprint 在没有 compatibility authority 时形成 `CONFLICT`；
- path / resource / dependency 等 invalid input 形成 `INVALID` 并在 mutation 前被阻止；
- report 能恢复汇总、stable identity、status 与诊断原因；
- unresolved conflict / invalid 不能以成功 process outcome 收口；
- Main / Party concrete dataset scope 与 compatibility 不被 Generic migration capability反向改写；
- migration execution 是否允许启动继续由当前 Repository / Work Authority 决定，而不是由本规格自动激活。

具体 automated fixture、CLI invocation、Workflow、artifact 与 证据 procedure 由 Verification Authority 与 Repository implementation 决定。

## 9. 非目标

- 解除 Main 历史迁移 freeze；
- 定义具体 historical record inventory / count / digest；
- 在线抓取 Legacy Source；
- 为 source defect 猜测修复内容；
- 用 migration 取代 Generic schema evolution 或 Site Definition bootstrap；
- 固化旧 EU / Phase 名、旧 command label、旧 report prefix 或当前 Kotlin class；
- 把 Party-specific compatibility 推广为 Generic overwrite rule。
