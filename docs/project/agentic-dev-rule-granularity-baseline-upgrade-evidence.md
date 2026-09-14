# agentic-dev Rule 粒度 baseline 升级证据

状态：**INTEGRATION CANDIDATE / UPGRADE-ONLY EVIDENCE**。

本文件记录 `jilinjobs-cms` 对 `dygapp/agentic-dev@8e7e94eff62b958b2044407cf6d85de3dde48ee9` 的显式 baseline upgrade 判断。它只承担 provenance、disposition 与 upgrade verification contract，不构成产品 Requirement、Planning Authority、Execute Authority、Rule routing table 或普通运行依赖。

只有本升级 PR 经人工 Integration Gate 合并后，本文件所描述的 candidate projection 才成为 `main` 上已集成状态；PR Head、Checks、Review 与 mergeability 始终以 GitHub native state 为准。

## 1. 精确基线与 lineage

- Consumer upgrade base：`jilinjobs-cms@dd29d335f378bb58ec683bf383b43538a8023b02`
- upgrade base lifecycle：Open PR = 0；`docs/work/current/README.md` = `Current Ready Execution Unit: NONE`
- Previous Evaluated Baseline：`agentic-dev@1c8cdfea9ecf23ef33ffab20eec3c93679fd4578`
- Candidate Evaluated Baseline：`agentic-dev@8e7e94eff62b958b2044407cf6d85de3dde48ee9`
- Candidate source：已合并 upstream PR #128 — `Rule Granularity Consolidation + Technology Rule IA`
- exact compare：`1c8cdfea... -> 8e7e94ef...` = ahead 7 / behind 0
- historical V4 Foundation projection：`agentic-dev@3e0b2f5a29caeb344da79f8c96ebffbeb5c2b0cb`

`3e0b2f5a...` 与本次 target 不构成线性 upgrade ancestry；GitHub compare 显示两者从 `1c8cdfea...` 后分叉。因此本轮 evaluated frontier 的真实迁移只按 `1c8cdfea... -> 8e7e94ef...` 计算，historical projection 继续只作为此前 V4-08 Consumer validation provenance。

## 2. Capability delta 判断

本轮只传播 reusable capability，不传播 upstream Project state。

### 2.1 adopt / adapt / replace

| Consumer 旧 owner / id | disposition | Consumer 新 owner / id | 判断 |
|---|---|---|---|
| `rule:implementation-minimality` + `rule:surgical-change` | replace | `rule:implementation-discipline` | 两条 Rule scope 相同、同一 implementation task 中共同发现与消费；合并后保留最低必要复杂度与精准 diff 两个本地约束 |
| `rule:post-write-state-verification` | adapt / replace | `rule:safe-external-write` | 采用 task-level authorization → minimal write → post-write verification 责任链，并保留 Consumer 对 ref / Head / changed files / status 的具体复核语义 |
| `rule:exact-machine-identifiers` + `rule:human-facing-chinese-default` | adapt / replace | `rule:human-facing-content-integrity` | 合并共同的人类内容完整性责任，同时保留 Consumer 更严格的 Current 文档中文主语言与机器标识精度 |
| `rule:vue-props-one-way-input` | adapt / replace | `rule:vue-component-authoring` | Consumer 已使用 Vue 3.5.x，Development Method 已持有兼容的 component-authoring defaults；聚合 `<script setup>`、props、props/emits、标准 `v-model`，但不授权批量迁移稳定组件 |
| `rule:vue-build-vs-typecheck` | adapt / replace | `rule:vue-typecheck` | 保留 Consumer 实际 `vue-tsc` / package script / Verification Strategy 边界，并迁入 `technology/vue/` 人类 IA |

### 2.2 keep-local

以下现有 Consumer Rule 保持独立 current owner，不因 upstream corpus 收敛而覆盖：

- `rule:data-access-boundedness`
- `rule:async-operation-bounded-observation`
- `rule:cross-repository-authorization`
- `rule:integration-state-closure-review`
- `rule:evidence-claim-reuse-across-commits`
- `rule:evidence-type-must-match-claim`
- `rule:verification-contract-currentness`
- `rule:visual-evidence`

其中 `cross-repository-authorization` 拥有真实独立 `cross-repository` risk，并承载本 Consumer 明确的多 Repository 操作边界；独立 discovery 可实际排除普通 external write，因此不并入 `safe-external-write`。

### 2.3 not-applicable / retain elsewhere

- upstream `rule:git-commit-discipline`：不新增 Consumer Rule。`docs/project/git-commit-guidelines.md` 已是本 Consumer 完整 Git commit policy owner；再增加 discoverable Rule 会制造双 semantic owner。
- upstream `rule:typescript-type-safety`：不新增独立 Consumer Rule。当前 TypeScript inference / `any` 等语义继续由 `docs/project/development-method.md` 的 Consumer-local Vue 3 + TypeScript Technology Profile 承担；本轮没有证据需要新增独立 discovery 单元。
- upstream 其余未被 Consumer 采用的 Rule：保持 not-applicable；不为与 upstream 27 条 inventory 一致而扩张 Consumer corpus。
- upstream Project Charter / Capability Profile / Roadmap / Evolution、Research、Eval、self-adoption 与当前 GitHub Project state：只作 provenance / comparison context，不进入 Consumer current Authority。

## 3. Rule breaking change closure

升级前 Consumer discoverable Rule = **15**；candidate projection = **13**。

retired current Rule ids / paths：

- `rule:implementation-minimality`
- `rule:surgical-change`
- `rule:post-write-state-verification`
- `rule:exact-machine-identifiers`
- `rule:human-facing-chinese-default`
- `rule:vue-build-vs-typecheck`
- `rule:vue-props-one-way-input`

新 task-level ids：

- `rule:implementation-discipline`
- `rule:safe-external-write`
- `rule:human-facing-content-integrity`
- `rule:vue-component-authoring`
- `rule:vue-typecheck`

升级不保留旧 alias / compatibility Rule；长期 current owner、deterministic tests 与 workflow smoke 均迁移到新 id / path。历史 Evidence 可以保留旧 id 作为历史事实，但不得成为 ordinary runtime locator。

## 4. Technology Rule IA

当前 Consumer 只有真实 Vue discoverable Rule，因此 candidate tree 使用：

```text
docs/rules/technology/
└── vue/
    ├── component-authoring.md
    └── typecheck.md
```

不建立空 `technology/typescript/`。目录仅服务人类维护；`tools/rule-discovery/rule_discovery.py` 继续递归扫描 Front Matter 并只按五维 scope 与 task signals 匹配，目录路径不进入匹配决策。

## 5. Rule Discovery contract

本轮**不替换** Consumer-local Rule Discovery 算法。继续保持：

- 五维 `phases / activities / technologies / artifacts / risks` signals；
- known / known-empty / unknown 三态；
- 每维最多 6 个 canonical token；
- metadata-only deterministic filtering；
- locator-only `{id,path}` output；
- candidate body semantic confirmation；
- malformed metadata / duplicate id / invalid signal / incomplete scan fail closed；
- ordinary runtime 不枚举未命中 Rule，不在线回 upstream。

任务级 Rule 聚合只减少无意义 locator / body fragmentation，不改变 matching semantics。

## 6. Targeted verification contract

本升级 PR 必须以 exact PR Head 的 GitHub native Current Evidence 证明：

1. Consumer-local Rule / Skill lint PASS，Rule count = 13、Skill count = 9；
2. Rule id 唯一性与 invalid metadata fail-closed 继续 PASS；
3. 7 个 retired current id 不在 active corpus；
4. implementation discovery 返回 task-level `human-facing-content-integrity` + `implementation-discipline`；
5. safe external write 的 task-level candidate 可确定性发现；
6. Vue `technology/vue/` nested path 与 flat path 对相同 Front Matter 产生相同 candidate id，证明目录不参与 matching；
7. Consumer Vue candidate locator 指向 `docs/rules/technology/vue/component-authoring.md`；
8. existing verification discovery regression 保持稳定；
9. ordinary runtime 仍只依赖 Consumer-local `AGENTS.md`、Local Discovery、Method、Rules / Skills 与 Tool，不需要 upstream Repository；
10. Repository 当前要求的 CI / 文档治理检查在 exact PR Head 上 PASS。

PR Actions / Checks 是上述 exact Head 运行结果的原生 owner；本文件不复制易陈旧的 Run id 或 status。

## 7. Integration boundary

本升级：

- 不授予新的产品 Planning / Execute Authority；
- 不启动新的 Execution Unit；
- 不修改 `dygapp/agentic-dev`；
- 不允许普通运行 fallback 到 upstream；
- 不因 upstream 27 条 Rule inventory 扩张 Consumer-local policy surface；
- 在人工明确 merge 前保持 PR Integration Gate，不自动合并。

只有 exact PR Head targeted verification 通过、Blocking = 0、Medium = 0、base drift 可接受且人工完成 Integration Gate 后，`8e7e94eff62b958b2044407cf6d85de3dde48ee9` 才成为 `main` 上已集成的 Current Evaluated Baseline。
