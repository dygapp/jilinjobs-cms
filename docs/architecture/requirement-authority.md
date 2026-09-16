---
id: architecture:requirement-authority
type: architecture
status: active
---

# Requirement Authority 架构

## 目标

本 Architecture 定义 `jilinjobs-cms` 长期 Requirement Authority 的 semantic ownership、Human / Agent 导航边界与 artifact lifecycle，确保同一长期业务事实只有一个长期 owner，Fresh Context Agent 能从有限入口确定性找到当前 Requirement，临时分析不会成为隐藏的第二事实源。

本文件不修改任何 JilinJobs Product Requirement，也不要求 G0 立即重排现有 `docs/requirements/**`。物理 IA 是否迁移、拆分或合并属于后续 Requirement Baseline governance；G0 只采用本 semantic contract。

## 四类责任

### Requirement Human Navigation

回答“如何阅读、维护这套 Requirement 体系”。只服务人类理解，不拥有业务事实或动态 inventory。

### Requirement Authority Index

回答“当前某类 Requirement 的唯一 owner 在哪里”。只持有 stable label / relation / locator，不复制 Requirement 正文。

### Requirement Fact Authority

回答“当前长期 Product / Domain / NFR 事实是什么”。真正业务事实必须进入这里，而不是停在 README、index、聊天、Issue、分析表或派生视图。

### Requirement Analysis Workspace

保存 source inventory、extraction、ambiguity / conflict、comparison、flow / state / relationship view、review batch 等 transitional material。默认非 Authority；事实 promote 到 durable owner 后应退出 ordinary runtime，并按需要删除、归档或保留为明确 historical evidence。

## 推荐 IA 与本地投影

通用推荐形态可以是：

```text
docs/requirements/
├── README.md          # Human Navigation
├── index.md           # Authority Index
├── overview/          # project-wide Requirement context
├── business/          # main durable Requirement capabilities
├── aspects/           # admitted cross-capability Requirement only
├── non-functional/    # durable NFR
└── analysis/          # non-Authority workspace
```

这是推荐 projection，不是硬编码 runtime contract。当前 Consumer 可以在 G2 依据真实 semantic ownership 决定是否采用完全相同物理结构。

## README / Index / Fact owner 边界

```text
README = 如何使用 Requirement 体系
index  = 当前 Requirement Authority 在哪里
owner  = Requirement 事实是什么
```

README 不维护完整动态文件清单或复制业务规则；index 不成为第二份“总需求说明书”；owner docs 不依赖 README / index 才能解释其核心事实。

## Requirement Fact ownership

长期事实只进入一个真实 owner。多个下游文档需要同一事实时引用 owner，并只描述本地适用范围或输入输出，不复制完整规则正文。

Capability 边界优先按核心业务对象、主要业务活动 / outcome、生命周期、Actor / responsibility、关键规则与独立验收意义确定；不得仅根据页面、菜单、微服务、数据库表、代码 package 或组织部门机械拆分。

独立 aspect owner 只有在事实跨多个 Capability 持续成立、具有独立 Product / Acceptance 意义、不能合理归属于单一 Capability / overview / NFR owner，且独立 owner 会减少重复与冲突时才建立。

NFR owner 只持有跨实现方案仍持续成立、可验收的 security / privacy、performance / capacity、availability / reliability、compatibility、accessibility、compliance、retention / audit 等要求；普通实现 HOW 不因影响性能或可靠性就自动成为 NFR。

## Fresh Context consumption

普通 Feature Agent 不默认扫描全部 Requirement 或 analysis：

```text
Repository Authority
→ Requirement Authority Index / equivalent locator
→ current Feature / Capability owner
→ explicitly related overview / aspect / NFR owner
→ Specification / Technical Plan
```

如果当前 Consumer 尚未建立独立 `index.md`，必须通过现有 canonical locator 确定性找到 owner；是否建立 index 由 G2 按本 Architecture 判断，不由 G0 预先决定。

## Artifact lifecycle

主要 producer：`method:requirement-baseline-establishment`、后续 Feature 中经确认需提升的长期 Requirement fact、Human / Product Authority 的长期决定。

修改事实时先更新真实 owner，再最小传播 locator / relation / affected references；不得在下游 Specification、Technical Plan 或聊天中静默覆盖 Requirement。

新 owner 替代旧 owner 时应更新当前 locator / references，并删除、归档或明确废止旧 current owner；历史由 Git / Issue / PR 与必要 archive 保存，不维护两套 current fact。

发现 current Authority 冲突、unique owner 不清、多种合理答案会改变 Product behavior / Acceptance、变更超出授权或批量语义变换需要独立 review 时，返回 Requirement Method / Human Authority。

## 与 Specification / Architecture 的边界

Requirement 持有跨多个 Feature 长期持续成立的 Product / Domain / NFR 事实；Feature Specification 只拥有当前 change 的具体适用、Scope、Observable Behavior、Failure Behavior 与 Acceptance。

Architecture 持有系统如何被长期组织以及跨 Feature 持续成立的结构约束。Architecture 不得通过技术判断覆盖 Requirement；冲突先返回 Requirement owner。
