# Historical Content Migration Workspace

`data-migrations/` 是历史内容迁移的 Repository workspace owner。它保存需要 provenance、stable identity、fingerprint、resource integrity、offline validation 与受控 Runtime import 的 canonical migration data；它不是 Generic Flyway、JilinJobs Site Definition、ordinary Runtime DB 或 Project Current State 的替代品。

## 1. 长期边界

```text
Generic schema              → Backend schema owner
JilinJobs stable definition → sites/jilinjobs/**
Historical canonical data   → data-migrations/**
ordinary operated state     → Runtime CMS Data
```

Generic migration capability可以支持多种 site-neutral record，但某一具体 scope 的数据是否属于 Historical Migration，必须由当前 Product / Domain / Architecture Authority决定。技术能力本身不创造数据 ownership。

## 2. Canonical migration contract

每个 accepted migration scope 必须由 Repository-owned canonical dataset表达，至少具备：

- stable migration identity，不依赖 Runtime numeric ID；
- source system / provenance；
- deterministic source fingerprint；
- stable target identity；
- 需要本地迁移的 resource path / size / SHA-256；
- explicit acceptance / error classification；
- 可验证的 dataset / manifest relationship。

具体 record count、resource bytes、dataset digest、source run / artifact provenance由各 scope 自己的 manifest / reports持有；本 README 不复制第二份 inventory。

## 3. Acquisition 与 stable import 分离

长期数据流：

```text
Legacy Source / Export / API
        ↓ explicit bounded acquisition
Source Evidence
        ↓ classification / review
Canonical Migration Dataset
        ↓ offline preflight
Controlled Runtime Import
        ↓
Runtime CMS Data
```

只有显式 Collect / Discovery / Retry activity可以访问 Legacy Source。稳定 CI、ordinary startup与 canonical import必须消费冻结的 Repository / authorized evidence bytes，不能在运行时重新抓取旧站补事实。

GitHub Actions artifact / ZIP可以是 transport/evidence carrier，但不能仅因被下载过就自动成为长期 canonical authority。

## 4. Preflight / import invariants

Canonical import在任何已知 mutation前必须尽可能完成结构、identity、target、path与resource完整性 preflight。

稳定语义：

```text
unknown identity + valid input       → CREATE
same identity + same fingerprint     → SKIP
same identity + changed fingerprint  → CONFLICT
invalid target / bytes / dependency  → INVALID / fail closed
```

Generic engine不得静默覆盖不同 fingerprint，也不得把某个历史 site 的一次性 correction推广成通用 overwrite policy。

如果存在已接受的 old → current transition，必须由该 scope 的 bounded compatibility authority显式定义，并保持 exact identity / fingerprint / Runtime precondition guard。

删除、源站消失或后来扫描未发现某记录，不自动等价于 Runtime delete。

## 5. Resource invariants

Historical resource必须：

- resolve在 authorized snapshot root 内；
- 使用真实文件和明确 media / safety contract；
- 在写入前验证 accepted size / digest；
- 需要本地化时不能让 canonical body继续依赖 Legacy Source URL；
- Runtime projection遵守 CMS Resource / StaticResource安全边界；
- source provenance、canonical bytes与 Runtime target责任保持可区分。

## 6. Main scope

当前 Main Historical Migration ownership是 **Article-only**：

- INTERNAL Article；
- EXTERNAL_LINK Article；
- Article body/resources/attachments；
- Article source identity、fingerprint与provenance。

Main Page与稳定 Site Definition数据不进入 Main historical import。Source discovery发现的非 Article 事实如果有长期价值，应交给其真实 owner审查，而不是为了不丢数据就自动纳入 Historical Migration。

Main 当前 accepted canonical dataset与其精确统计、digest、deferred/source-defect evidence由 `main/**` 内 manifest / reports持有。

### Main execution boundary

Main ordinary migration execution保持 **frozen / explicit reactivation only**。冻结的是执行能力，不是否定 Repository 中 canonical data / evidence的长期价值。

任何 reactivation都必须建立新的当前 Authority，明确 source/canonical scope、变更原因、deferred/source-defect disposition、实现兼容与所需 verification；不得从历史 Technical Plan、旧 Workflow或 completed EU继承 Execute Authority。

Deferred problem Article与 source-defect Article 的未来处理方向以 `docs/project/project-roadmap.md` 为准。

## 7. Party scope

Party 是早期 real Canonical Migration consumer，当前允许其 accepted Article与ListItem / carousel historical semantics继续由 `party/**` canonical workspace持有。

Party-specific alias、dataset cardinality、stable item identity、compatibility transition与accepted fingerprints不进入 Generic migration package。Generic engine只提供 site-neutral load / preflight / execute primitives；Party adapter / compatibility layer负责把其 accepted on-disk contract有界映射到 Generic capability。

Main 的 Article-only ownership不能反向改写 Party 已接受的 ListItem historical ownership；Party 的 historical ListItem ownership也不能反向推广成 Main规则。

## 8. Canonical data vs Site Definition

Historical Migration 与 Site Definition的判断依据是内容来源与 lifecycle：

- 版本化稳定站点结构 / accepted stable defaults / stable assets → `sites/jilinjobs/**`；
- 需要 legacy provenance / fingerprint / controlled import的历史内容 → `data-migrations/**`；
- bootstrap完成后由 operator维护的普通数据 → Runtime owner。

同一对象类型可以在不同场景拥有不同来源，但同一具体事实在当前时刻只能有一个 primary owner。

## 9. Verification

受影响 migration scope至少按风险验证：

- canonical manifest / index / item一致性；
- path containment与resource digest；
- target / dependency preflight；
- first import；
- second import idempotency；
- changed-fingerprint conflict；
- bounded compatibility transition（存在时）；
- Generic package不吸收site-specific policy；
- Runtime resource / mapping reconciliation；
- stable import阶段不访问 Legacy Source。

完整 Evidence claim规则见 `../docs/technical/verification-strategy.md` 与当前 live-discovered verification Rules。

## 10. Workspace 维护规则

本 README只在 migration workspace 的长期边界、canonical contract或 scope ownership发生稳定变化时更新。

不得在本文件维护：

- Current Ready Execution Unit；
- completed EU / PR / merge commit流水；
- 当前 Workflow Run状态；
- 可以从 manifest / reports唯一恢复的 count / bytes / digest；
- active migration task / Kotlin file inventory；
- 临时 source acquisition checklist。

精确 current事实应从各 scope manifest / reports、Repository implementation与GitHub原生 Evidence恢复。