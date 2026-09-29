---
id: project:agentic-dev-adoption
type: project
status: active
updated_at: 2026-09-29
---

# agentic-dev 采用状态

## 当前采用版本

本仓库当前采用的不可变版本为：

- Repository：dygapp/agentic-dev
- Tag：agentic-dev-v0.2.1
- annotated tag object：951529b10301d451165f09aa87afd94e3dba018d
- peeled commit：e8940369599cd9a823c7ab7d7dbff604c70dec21

以上绑定在本轮升级时通过远端 refs/tags/agentic-dev-v0.2.1 与 annotated tag object 重新核验。

## Consumer runtime 边界

agentic-dev 在本 Consumer 中只以标准安装的 Agent Skills 作为正式 runtime product：

- 安装目录：.agents/skills/**
- provenance / lock：skills-lock.json
- discovery：宿主原生 Agent Skills discovery
- Provider Guide：只在需要方法论导航或下一步判断时按当前 exact tag 读取
- exact-version Guide root：https://github.com/dygapp/agentic-dev/tree/agentic-dev-v0.2.1/docs/guides

普通 Skill execution 不在线读取 Provider docs/** 补 procedure。本仓库不再复制或运行 Provider Method、Rule、Capability、Release、Gate、Project Knowledge 或 self-adoption 模型。

activate-model-collaboration 被安装只表示 v0.2.1 Skill inventory 完整，不表示本项目已经启用多模型协作；任何可选能力仍以当前 Consumer facts 与显式采用结果为准。

evals/** 是 jilinjobs-cms 自己的可选模型评审实验与 Evidence 资产，不属于 Provider runtime。其是否继续、调整或退出由 Consumer 自己的实验结论决定；安装 agentic-dev v0.2.1 不自动启用、替换或删除该实验。

## 安装 provenance

本轮使用标准 installer 显式安装 exact tag：

    npx -y skills@1.7.0 add https://github.com/dygapp/agentic-dev/tree/agentic-dev-v0.2.1 --skill '*' --agent codex --copy --yes

安装器发现并复制 15 个 Skill 到 .agents/skills/**，并生成 skills-lock.json。相对 v0.2.0，inventory 不变，只有 execute-unit 与 converge 的内容 / hash 发生变化；完整 inventory 与逐 Skill hash 由 lock file 和安装目录机械恢复，不在本文维护第二份清单。

安装完成后的工作树只出现 .agents/skills/execute-unit/SKILL.md、.agents/skills/converge/SKILL.md 与 skills-lock.json 三项 installer 变化，Consumer-owned AGENTS.md、项目 Authority、constraints 与 current work 未被覆盖。

针对实际 Skill delta 的定向验证确认：execute-unit 按 Consumer-local Git policy 区分 Working State 与 Candidate Commit，不把测试、Readiness、Gate、debug 或修复轮次机械变成永久 commit；SHA 改写后重新判断 exact-Head Evidence。converge 在 READY 前执行 History Convergence，并对 nested independent Git repositories 逐仓恢复 identity / policy / authorization；submodule 场景显式核对 160000 gitlink 与最终组件 SHA。当前 Consumer-local Git policy 与这些语义兼容，且其他 Repository 的授权仍须分别确认。

## Consumer ownership 边界

以下内容始终由 jilinjobs-cms 自己拥有，不由安装或升级自动修改：

- Product / Domain Requirement；
- Architecture / ADR；
- Specification / Design / Technical contract；
- technology policy；
- authorization / Repository boundary；
- terminology；
- Roadmap / current work；
- Consumer-local constraints；
- Runtime / CI / deployment configuration；
- 其他项目文件。

Provider release 的验证结果只能证明 Provider 声明的安装 / 分发边界，不能外推为本 Consumer 的业务或工程行为已经通过。

## 后续升级

后续升级只需要：

    恢复当前 Consumer
    → 明确选择新的 immutable tag
    → 核验 tag 绑定
    → 使用标准 installer 覆盖安装 Skills
    → 比较真实 Skill / Guide delta
    → 只重新验证本 Consumer 实际受影响的行为
    → 验证 Consumer-owned 文件未被覆盖
    → 最后更新本文 adopted ref

不要同步 Provider Source tree，不要重新建立 Consumer-local Provider Framework，也不要使用无版本 latest 作为隐式升级协议。
