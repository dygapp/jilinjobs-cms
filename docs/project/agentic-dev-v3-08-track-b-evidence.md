# agentic-dev V3-08 Track B — Existing Consumer Baseline Upgrade Evidence

## 1. Scope

本记录固化 `dygapp/agentic-dev` Issue #115 / V3-08 **Track B — Existing Consumer Explicit Baseline Upgrade** 在 `dygapp/jilinjobs-cms` 的 Consumer-local adoption decision / validation history。

它不是 Current Execution Lifecycle owner，不授予产品 Execute Authority，也不替代 `docs/work/current/README.md`、Project Roadmap、Requirement / Specification、GitHub PR / Actions native state。普通运行不默认读取本文。

## 2. Start gate

Consumer exact starting `main`：

```text
72fea274b34c40ef40b09fd3e3a7c2ddc44cb838
```

Experiment branch：

```text
experiment/v3-08-existing-consumer-baseline-upgrade
```

Track B 启动前重新核验结果：

- EU-54 implementation PR #138 已集成；Post-Integration closure PR #139 已集成，EU-54 Execute Authority terminated；
- Track A Current State ownership Medium 已通过独立 PR #140 集成到 exact starting `main`；
- Open PR = 0；`docs/work/current/README.md` = `Current Ready / active Execution Unit: NONE`；
- 没有新的 conflicting product Execute lifecycle；历史 / closed branches 不构成 active Execute Authority；
- Current Work locator 与当前 GitHub evidence 可协调；
- Consumer previous evaluated `agentic-dev` baseline 在 `AGENTS.md`、Roadmap、Development Method 三处一致解析为 `d9fad0da83dbdb61cac5eb9778b0258c6861eef1`。

Verdict：**Track B start gate PASS**。

## 3. Exact upstream baseline

Previous evaluated baseline：

```text
dygapp/agentic-dev@d9fad0da83dbdb61cac5eb9778b0258c6861eef1
```

Fixed candidate evaluated baseline：

```text
dygapp/agentic-dev@2fe193035c629f6b8805fd473bd322f70fe6e172
```

GitHub exact compare：

```text
base: d9fad0da83dbdb61cac5eb9778b0258c6861eef1
head: 2fe193035c629f6b8805fd473bd322f70fe6e172
status: ahead
ahead_by: 34
behind_by: 0
merge_base: d9fad0da83dbdb61cac5eb9778b0258c6861eef1
```

Candidate 后面的 `f3b63ccf6ce023ee39166afbc205289de4ce2927`（V3-08 Gate A）及更晚 `agentic-dev master` 状态不在本次 adoption candidate 中。

## 4. Exact delta classification

| Delta family | Disposition | Consumer-local result / owner |
|---|---|---|
| Consumer Lifecycle | **adopt** | `docs/project/development-method.md`：显式 adoption / upgrade、逐项 disposition、verification-before-baseline-progression、ordinary local-only、explicit upstream re-entry |
| Agent Resource Model | **adopt** | `docs/project/development-method.md`：规范正文 → 资源固有结构 → 可选派生发现投影；真实 semantic owner 保持正文所有权 |
| Resource Discovery Architecture | **adopt** | `docs/README.md` 作为 Local Discovery Entry；state-only / routing-only / execution progressive loading；one primary + minimal supporting；local fail-closed |
| Skill identity / admission | **adopt** | `docs/project/development-method.md` §10：稳定触发 / 输入 / 独立过程 / 输出 / exit / stage return / escalation / composability / JIT complexity；“重要 / 可复用 / 有步骤”等单独不构成 Skill |
| Skill supporting-resource boundary | **adopt** | supporting resource 不因物理目录取得平级所有权；只有形成独立 semantic owner / 独立消费才重新分类 |
| Engineering Capability ownership | **retain / override** | Consumer 已有 Method / Engineering Discipline / Technology Profile / Verification owner；只吸收 ownership / loading boundary，不复制 upstream capability architecture 文件 |
| Verification / Evidence | **retain / override** | `docs/technical/verification-strategy.md` + Development Method 已覆盖 current evidence、fresh runtime、claim-specific evidence reuse、visual / human review、stale verification、scope / boundedness 等；不复制 upstream Guide |
| External Operation | **retain / override** | `AGENTS.md` Repository Operation Boundary + Development Method §8 + Execution Continuity 等已拥有 read/write/reread、per-repository authorization、async bounded observation、shared-resource concurrency、artifact promotion；不复制 upstream Guide |
| Technology Profile | **retain / override** | Consumer 当前版本 / Architecture / package scripts / Verification Strategy 覆盖 upstream defaults；不因 baseline upgrade 改依赖 |
| Terminology / lifecycle / integration contracts | **adopt where durable** | evaluated baseline / provenance / decision-history 分离、local-only re-entry、progressive discovery、Skill identity；stacked PR / verification trigger 等已由现有 Consumer owner 继续承载 |
| `agentic-dev` AGENTS / README / Roadmap / V3 project state | **reject / not applicable** | upstream project-only，不进入 Consumer current authority |
| upstream Research / Eval / tasks/plans / historical / rejected design | **reject / not applicable** | 只作为本次 comparison evidence，不投射为 Consumer ordinary runtime |
| `agentic-dev/docs/discovery/**` self-adoption instance | **reject physical copy** | 不复制；Consumer 复用自己的 `docs/README.md` 物理入口 |
| old Consumer baseline pointer as current | **supersede** | adoption verification 通过后由 `2fe193...` 取代 current evaluated frontier；`d9fad0...` 作为 Previous Evaluated Baseline / provenance 保留 |

## 5. Provenance separation

本次采用保持三类事实分离：

1. **evaluated upstream baseline**：比较 / adoption frontier；
2. **Consumer-local current owner / provenance**：每项当前规则继续由真实本地 semantic owner / code / workflow 持有；
3. **upgrade-only decision history**：本文只记录本次 exact delta、disposition 与 adoption validation。

因此：

- `Current Evaluated Baseline = 2fe193...` 不表示 upstream 全部资源被采用；
- `docs/project/git-commit-guidelines.md` 等既有 local asset 的历史 source provenance 未因 baseline progression 被机械重写；
- upstream V3 Project / Research / Eval / self-adoption evidence 不成为 Consumer current project fact。

## 6. Consumer-local projection

Projection content head before this evidence record：

```text
516243ec07ee8c538d5b1850e35b609dcc5d8d85
```

Modified long-lived Consumer assets：

- `AGENTS.md`；
- root `README.md`；
- `docs/README.md`；
- `docs/project/development-method.md`；
- `docs/project/project-roadmap.md`；
- 本 upgrade-only evidence record。

Intentionally unchanged owners：

- `docs/work/README.md`；
- `docs/work/current/README.md`；
- `docs/technical/verification-strategy.md`；
- `.github/workflows/**`；
- product Requirement / Specification / Architecture / code / migrations。

Track A protection check：candidate 中 `docs/work/current/README.md` blob 仍为 `733dbd1e15145e04c5f1165661c02c48f0019717`，`Current Ready / active Execution Unit = NONE`；没有重新把 Roadmap / Bootstrap / Issue 变成 Current Execute truth。

## 7. Local Discovery decisions

### 7.1 Local Discovery Entry

Decision：**reuse `docs/README.md`**。

理由：现有 Documentation Authority Map 已处在稳定 Bootstrap 与各 native owner 之间；只需增加 state-only / routing-only / execution、one-primary + supporting locator 与 fail-closed 契约，不需要新增第二个物理入口。

### 7.2 Reviewed Discovery Map

Decision：**NOT REQUIRED**。

Inventory 已覆盖：Method、Engineering Discipline、Technology / Verification Profile、Work lifecycle、Migration、Human Review、GitHub Actions、Consumer Authority 与 cross-cutting governance。当前 stable entry + native resource identity / locator 能可靠路由；没有证据证明长期跨资源正规化成本需要派生 Map。

### 7.3 Runtime View

Decision：**NOT REQUIRED**。

没有真实 ordinary-runtime 成本 / drift evidence 支持增加纯生成 Runtime View。

## 8. B-01 ～ B-11 candidate validation

### B-01 Exact baseline — PASS

Previous / candidate 均由精确 commit SHA 解析；exact compare = 34 ahead / 0 behind；未把 candidate 后的 upstream master 纳入范围。

### B-02 Per-item adoption — PASS

§4 对 reusable delta family 逐项给出 `adopt / retain-or-override / reject-or-not-applicable / supersede`，不存在“整体同步 upstream 文档”的未分类变化。

### B-03 Provenance separation — PASS

§5 与 Consumer Method / AGENTS 分离 evaluated frontier、local owner / provenance、upgrade history；旧 asset provenance 不因 baseline pointer 机械改写。

### B-04 Local Discovery Entry — PASS

选择现有 `docs/README.md`，没有复制 upstream `docs/discovery/README.md` path / body。

### B-05 Discovery Map necessity — PASS

Reviewed Discovery Map = NOT REQUIRED；Runtime View = NOT REQUIRED。该结论来自 Consumer inventory，而不是 upstream V3-07 self-adoption 形态。

### B-06 State-only — PASS

Candidate actual read set：

```text
AGENTS.md
README.md
docs/README.md
docs/work/current/README.md
+ current Open execution PR / branch and necessary GitHub native evidence
```

协调结果仍为 `Current Ready / active Execution Unit = NONE` 时即可安全停止。Candidate validation 未为了 state-only 继续读取完整 Roadmap、完整 Development Method、全部 Skills 或 upstream。

验证过程中发现 root `README.md` 最初仍保留旧 fixed recovery order，会强制继续 Roadmap + Method，分类为 **Medium candidate**；已在 `516243ec...` 修复为从 `docs/README.md` 进入 progressive local discovery。修复后复核无残留冲突。

### B-07 Routing-only — PASS

实际 routing scenario：**“当前 GitHub Actions 验证职责由谁负责，下一步应读取什么？”**

Resolution：

```text
primary responsibility:
  github-actions-verification

minimal supporting locator:
  docs/project/development-method.md §10
  docs/technical/verification-strategy.md
  relevant .github/workflows/<workflow>.yml + GitHub native run state
```

Routing-only 先由 `docs/README.md` 定位 owner / supporting locator，不需要读取 upstream Skill 文件，也不需要预加载完整 Skill / Engineering Capability corpus。

### B-08 Execute loading — PASS

同一场景真正进入 GitHub Actions verification execution 时，才进一步加载：

- Development Method §10 中 `github-actions-verification` 的 Consumer-local process semantics；
- `docs/technical/verification-strategy.md`；
- 当前目标 workflow，例如 `.github/workflows/ci.yml`；
- 当前 PR / Head / Run / Job / Step / Artifact evidence。

Consumer 没有复制 upstream physical Skill directory；这里的 primary Skill identity / procedure 已按长期采用规则投射到 Consumer-local Method，ordinary execution 不依赖 upstream 文件。

### B-09 Local-only ordinary runtime — PASS

Candidate 投射完成后，state / routing / execution 所需长期规则均有 Consumer-local owner。`agentic-dev` 只在显式 baseline upgrade / experiment / Repository-authorized re-entry 时读取；upstream 新 commit、local discovery stale 或 Agent uncertainty 不自动触发 upstream fallback。

### B-10 Fail-closed — PASS

| Probe | Candidate behavior | Result |
|---|---|---|
| stale semantic mapping | 不信任陈旧 derived discovery；回到 local owner / stable entry 重新定位 | PASS |
| source missing | 实际 probe `docs/project/__v3_08_missing_owner_probe__.md` 返回 GitHub 404；停止，不访问 upstream 修补 | PASS |
| locator missing | Local Discovery Entry 规定停止当前 route、扩大最小本地 Authority read set | PASS |
| coverage drift | 当前无 Map；stable locator / owner inventory 发生缺失即按 source / locator drift fail closed，不伪造 Map coverage | PASS |
| ambiguous primary | 多个 primary 无法消歧时停止，不任选一个执行 | PASS |
| no-match + governance / verification risk | 明确 fail closed，不把 no-match 当成“无规则” | PASS |
| current owner / override / supersede conflict | 回到 Consumer Repository Authority 解析；无法协调则不执行 / 不继承 authority | PASS |

所有 fail-closed scenario 都禁止 ordinary runtime 自动打开 upstream 作为隐式修补路径。

### B-11 Context cost — PASS with correctness preserved

Current baseline 的 root README 固定恢复顺序要求 state-only 继续加载 Roadmap + Development Method。按当时实际 blob size：

```text
AGENTS.md                         23,650 bytes
README.md                          5,479 bytes
docs/README.md                     5,806 bytes
docs/work/current/README.md        2,347 bytes
docs/project/project-roadmap.md   16,278 bytes
docs/project/development-method.md37,516 bytes
------------------------------------------------
bootstrap document bytes          91,076 bytes
+ GitHub native state
```

Candidate state-only actual document read set：

```text
AGENTS.md                         21,083 bytes
README.md                          5,858 bytes
docs/README.md                     8,540 bytes
docs/work/current/README.md        2,347 bytes
------------------------------------------------
bootstrap document bytes          37,828 bytes
+ GitHub native state
```

Raw bootstrap bytes 减少 `53,248`（约 58%）只是 secondary cost signal，不替代 correctness。Correctness check 同时确认：Current Work locator unchanged、Open execution evidence reconciliation retained、`NONE` 不推导无 Planning Candidate、conflict / missing state 继续 fail closed。

Routing-only 也不再因为固定 Fresh Context 顺序机械读取完整 37.5KB / 41.7KB Method；只有 execution 或 method-specific routing 才按需读取相应 section / owner。

## 9. Base drift / conflict recheck

Candidate validation 时重新检查：

```text
main = 72fea274b34c40ef40b09fd3e3a7c2ddc44cb838
Open PR = 0
candidate branch ahead of start main
```

没有 start-after drift，也没有新的 conflicting product Execute lifecycle。

## 10. Candidate findings

Resolved during candidate validation：

- **Medium candidate — Bootstrap routing conflict**：root `README.md` 旧 fixed recovery order 会强制 state-only 继续 Roadmap + Method，削弱 progressive discovery。已修复并复核。

Remaining：

```text
Blocking = 0
Medium = 0
```

Consumer-local finding：现有 `docs/README.md` 足以成为薄 Local Discovery Entry；不需要复制 upstream self-adoption topology。

Reusable V3 finding candidate：**Existing Consumer 若已有稳定 Documentation / Authority Map，应允许原地升级为 Local Discovery Entry；Reviewed Discovery Map / Runtime View 必须由真实 Consumer discovery complexity / runtime cost 触发，而不是 V3 self-adoption 形态触发。** 该 finding 只作为 Evidence candidate 回流 `dygapp/agentic-dev` Issue #115，不由 Consumer 修改 upstream。

## 11. Candidate verdict before PR integration

```text
B-01 Exact baseline                 PASS
B-02 Per-item adoption              PASS
B-03 Provenance separation          PASS
B-04 Local Discovery Entry          PASS
B-05 Discovery Map necessity        PASS
B-06 State-only                     PASS
B-07 Routing-only                   PASS
B-08 Execute loading                PASS
B-09 Local-only ordinary runtime    PASS
B-10 Fail-closed                    PASS
B-11 Context cost                   PASS
Track A ownership regression        NONE
Blocking                            0
Medium                              0
```

**Candidate adoption verdict: PASS / eligible to proceed to normal Consumer PR + exact-head integration verification.**

Final PR candidate Head、PR / exact-head checks、Consumer integration / non-integration、post-integration `main` 与 final evaluated baseline 由 GitHub native state及 V3-08 Track B final Evidence comment 固化；本文不试图自引用包含自身写入 commit 的 SHA。