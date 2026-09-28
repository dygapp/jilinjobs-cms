---
id: project:agentic-dev-adoption
type: project
status: active
updated_at: 2026-09-28
---

# agentic-dev 采用状态

## 当前采用版本

本仓库当前采用的不可变版本为：

- Repository：dygapp/agentic-dev
- Tag：agentic-dev-v0.2.0
- annotated tag object：1c0f49d801d8e72df26b5d97bc74c3476bc8642c
- peeled commit：75f2cfba8b0bb726ae033f7a3467efbeafc438cb

以上绑定在本轮升级时通过远端 refs/tags/agentic-dev-v0.2.0 与 peeled ref 重新核验。

## Consumer runtime 边界

agentic-dev 在本 Consumer 中只以标准安装的 Agent Skills 作为正式 runtime product：

- 安装目录：.agents/skills/**
- provenance / lock：skills-lock.json
- discovery：宿主原生 Agent Skills discovery
- Provider Guide：只在需要方法论导航或下一步判断时按当前 exact tag 读取
- exact-version Guide root：https://github.com/dygapp/agentic-dev/tree/agentic-dev-v0.2.0/docs/guides

普通 Skill execution 不在线读取 Provider docs/** 补 procedure。本仓库不再复制或运行 Provider Method、Rule、Capability、Release、Gate、Project Knowledge 或 self-adoption 模型。

activate-model-collaboration 被安装只表示 v0.2.0 Skill inventory 完整，不表示本项目已经启用多模型协作；任何可选能力仍以当前 Consumer facts 与显式采用结果为准。

## 安装 provenance

本轮使用标准 installer 显式安装 exact tag：

    npx -y skills@1.7.0 add https://github.com/dygapp/agentic-dev/tree/agentic-dev-v0.2.0 --skill '*' --agent codex --copy --yes

安装器发现并复制 15 个 Skill 到 .agents/skills/**，并生成 skills-lock.json。完整 inventory 与逐 Skill hash 由 lock file 和安装目录机械恢复，不在本文维护第二份清单。

安装前后对 AGENTS.md、根 README.md、docs/README.md、Requirement Index、CMS Architecture、Roadmap、Current Work 与 Verification Strategy 的 SHA-256 进行了对比，安装过程没有覆盖这些 Consumer-owned 文件。

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
