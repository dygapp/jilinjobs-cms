---
name: review-change
description: 对最终 Repository 变更执行独立复核，检查当前 Authority、范围、契约、适用 Rules 与证据是否一致；高影响 Authority 变更追加有界权威链语义复核，并输出可执行 findings 或有界通过结论。不得替代实现验证、人工产品验收或 merge 授权。
metadata:
  jilinjobs-cms-id: skill:review-change
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# 变更独立复核

## 目的

基于当前 Repository Authority 与最终变更状态进行独立复核，发现会阻止安全接受的语义、范围、授权、生命周期或证据问题。

人工评审与本 Skill 不是同一责任：人工评审用于理解、确认产品 / 需求 / 架构 / 技术内容；本 Skill 独立检查 Repository change 是否符合当前 Authority、范围、规则、证据与生命周期。二者互不替代。

## 输入

- 当前目标基线与拟接受变更；
- 当前 Repository Authority；
- 精确 diff / changed files；
- 当前验证证据；
- 当前 review responsibility 下已发现并确认适用的 Rules。

## 过程

1. 重新读取当前 Authority 与最终变更，不依赖作者说明或旧 Review 结论。
2. 明确本次复核 claim：判断当前变更是否可安全接受，而不是重新设计目标。
3. 从最终变更事实提取 review signals，执行当前 Rule Discovery，只读取并应用真实命中的 Rules。
4. 根据影响选择复核深度：普通变更执行 consistency / regression / scope / evidence / lifecycle 检查；命中高影响 Authority 条件时追加“权威链语义复核”。
5. 检查 Authority consistency、语义回归、scope、授权边界、证据与长期 artifact lifecycle；技术专项检查只在对应 Rule 适用时执行。
6. 记录可执行 findings，至少区分 blocking / medium / low；每项 finding 指向具体事实、影响与建议修复边界。
7. 修复后重新读取最终变更；旧 review / verification 只在能证明未受影响时复用。
8. 无未解决 blocking / medium finding 时，可以给出有界通过结论。

## 权威链语义复核

### 触发边界

该模式只用于会实质改变或重组长期 Authority chain 的高影响变更，例如：

- 大规模 Authority restructure；
- canonical owner 的 fold / replace / retire / archive；
- Requirement / Domain / Specification / Interface 的重大 convergence；
- Product / Architecture 明确声明 replaceability 或发生 technology substitution review；
- heterogeneous legacy / historical / current source reconstruction；
- high-impact semantic migration。

普通代码修复、局部文案修改或没有跨层 Authority 影响的常规 PR 不因为本模式存在而自动执行完整可再生性挑战。

### 核心挑战

1. **Current owner transition**：canonical owner 或 lifecycle 改变后，Current locator、selector、verification consumer 与 durable current-state wording 是否都已迁移；historical / archive / provenance 引用是否明确保持非 Current 角色。
2. **Single semantic owner + bounded projection**：重复表达是合法 locator / summary / observable projection，还是 competing Current truth；每层是否只拥有自己的 semantic responsibility。
3. **Downstream observable projection**：Requirement / Domain 中已经接受、且需要用户、运营人员或维护者观察、失败或验收的语义，是否存在 Current Observable / Failure / Acceptance projection。
4. **Replaceability seam**：只有明确命中 replaceability boundary 时，检查替代实现维持 compatibility 所需的 stable interface、failure semantics 与 responsibility split 是否可以脱离被替换源码恢复。
5. **Conflict classification**：Authority、implementation 与 verification 冲突时，先基于 owner / currentness / provenance 判断是 implementation defect、stale Authority 还是 stale verification contract；不得因为“代码当前如此”自动覆盖 Product / Architecture truth。
6. **Source role / promotion boundary**：用于当前 claim 的 source 是 canonical Authority、bounded canonical data、implementation evidence、historical evidence、Human decision 还是 unresolved material；Evidence 不得越权晋升成更高层 truth。

### 有界可再生性挑战

当变更涉及大规模 Authority restructuring、明确 replaceability、major Specification / Interface convergence、heterogeneous reconstruction 或 high-impact semantic migration 时，追加反事实挑战：

- 不读取当前 implementation，Current Authority 是否足以恢复 Product behavior、stable contracts、failure semantics、responsibility boundaries 与 acceptance / verification obligations？
- 若替换 implementation technology，stable compatibility seam 是否仍能从 Current Authority 确定？

该挑战可以限定在受影响 capability / seam，不要求每个高影响 Review 都重建完整系统。

## 输出与退出

输出 findings 或有界通过结论，以及仍需验证 / 人工判断的剩余边界。

以下情况升级：

- Repository Authority 自身冲突；
- 需要改变 Product Goal、Scope、User-visible Behavior、重大 Architecture 或 Security / Privacy boundary；
- 需要人工承担的高影响、难逆或 Integration 决定。

Review 通过不等于 merge、release 或 deploy 授权。
