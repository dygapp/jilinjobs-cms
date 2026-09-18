---
id: architecture:requirement-authority
type: architecture
status: active
---

# 需求权威架构（Requirement Authority）

## 目标

本 Architecture 定义 `jilinjobs-cms` 长期 Requirement Authority 的 semantic ownership、Human / Agent 导航边界与 artifact lifecycle，确保同一长期业务事实只有一个长期 owner，Fresh Context Agent 能从有限入口确定性找到当前 Requirement，临时分析不会成为隐藏的第二事实源。

本文件不修改任何 JilinJobs Product Requirement，也不要求 G0 立即重排现有 `docs/requirements/**`。物理 IA 是否迁移、拆分或合并属于后续 Requirement Baseline governance；G0 只采用本 semantic contract。

## 四类责任

### Requirement 人类导航

回答“如何阅读、维护这套 Requirement 体系”。只服务人类理解，不拥有 Requirement inventory 或业务事实。

### Requirement Authority Index（权威索引）

回答“当前某类 Requirement 的唯一 owner 在哪里”。拥有 Requirement → owner locator / relation 的全局定位责任，但不复制 Requirement 正文。

### Requirement Fact Authority（事实权威）

回答“当前长期 Product / Domain / NFR 事实是什么”。真正业务事实必须进入这里，而不是停在 README、index、聊天、Issue、分析表或派生视图。

### Requirement Analysis Workspace（分析工作区）

保存 source inventory、extraction、ambiguity / conflict、comparison、flow / state / relationship view、migration analysis、human review batch 等 transitional material。默认非 Authority；事实 promote 到 durable owner 后应退出 ordinary runtime，并按需要删除、归档或保留为明确 historical evidence。

Analysis artifact 必须能说明 producer、当前用途、Authority promotion path 与 exit / archive / delete boundary；只要派生表达可以从 Current Requirement Authority 唯一再生，就不建立长期同步副本。

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

`overview/` 只持有跨多个 Capability 持续成立的 Product Goal、Scope、系统责任边界、Actor overview、Capability map、project-wide business constraint 与必要 domain semantics；不把内部组件划分或 deployment topology 误写成 Product Requirement。

`business/` 是主要纵向 Requirement Fact owner；Capability 边界优先按核心业务对象、主要业务活动 / outcome、生命周期、Actor / responsibility、关键规则与独立验收意义确定，不按页面、菜单、微服务、数据库表、代码 package 或组织部门机械拆分。

## 项目术语治理

项目级术语治理属于 Requirement Authority 内的可选横向语义责任，不是每个 Consumer 都必须建立的中央 glossary，也不要求固定物理路径。只有出现真实跨 Capability / Feature 的命名漂移时才建立或扩展独立术语 owner，例如：

- 同一长期业务概念出现多个中文名称并可能改变业务理解；
- 同一业务概念出现多个英文 business term，形成并行领域命名；
- current / legacy / external 名称需要稳定映射；
- 命名差异会影响 actor、state、data semantics、fact ownership、cross-capability interaction 或 Acceptance；
- 多个 Specification、Technical、Interface、Test 或带业务语义的代码对象需要共享同一 semantic root。

单一 Capability 内已经由真实 Requirement / Domain owner 清楚定义、且不会向外形成 drift 的局部词不机械提升为项目级术语。

术语 owner 只拥有 canonical business term、必要 definition / distinction、historical / external alias mapping、适用范围与真实 Requirement owner reference；不拥有完整业务规则、状态机、权限、Acceptance、字段字典、API 字典、数据库字典或代码命名风格。

原则：

```text
Terminology Authority = 选择哪个业务概念 / business semantic root
Technical convention   = 该业务词怎样按语言 / 框架形成 identifier
```

当前 Consumer 不因为采用本契约就新建中央中英文术语总表。业务术语继续由 `information-publishing.md`、`cms-domain.md` 等真实 Requirement / Domain owner 持有；只有后续出现跨 owner 的真实 terminology pressure，才通过 Requirement Authority Index 建立唯一横向 owner。

术语变化不得进行盲目全局替换。必须先更新真实 owner，再区分 Current internal canonical reference、external preserved name、legacy source name 与 historical provenance，只迁移真正属于 Current internal semantics 的引用。

## README / Index / Fact owner 边界

```text
README = 如何使用 Requirement 体系
index  = 当前 Requirement Authority 在哪里
owner  = Requirement 事实是什么
```

README 不维护完整 Requirement 文件清单、动态 inventory 或复制业务规则；index 只持有必要 stable label、分类、unique owner locator 与跨 owner relation，不成为第二份“总需求说明书”；owner docs 持有事实本身。

## Requirement 事实归属

长期事实只进入一个真实 owner。多个下游文档需要同一事实时引用 owner，并只描述本地适用范围或输入输出，不复制完整规则正文。

独立 aspect owner 只有在事实跨多个 Capability 持续成立、具有独立 Product / Acceptance 意义、不能合理归属于单一 Capability / overview / NFR owner，且独立 owner 会减少重复与冲突时才建立；它不是“无法分类内容”的收容目录。

NFR owner 只持有跨实现方案仍持续成立、可验收的 security / privacy、performance / capacity、availability / reliability、compatibility、accessibility、compliance、retention / audit 等要求；普通实现 HOW 不因影响性能或可靠性就自动成为 NFR。

Consumer 不要求通用 `L1/L2/L3` 编号。主要长期事实都有 owner、主要边界清楚、后续 Feature 能确定性找到 Requirement，比机械穷举未来 Scenario 更重要。

## Fresh Context 消费方式

普通 Feature Agent 不默认扫描全部 Requirement 或 analysis：

```text
Repository Authority
→ Requirement Authority Index / equivalent locator
→ current Feature / Capability owner
→ explicitly related overview / aspect / NFR owner
→ Specification / Technical Plan
```

如果当前 Consumer 尚未建立独立 `index.md`，必须通过现有 canonical locator 确定性找到 owner；是否建立 index 由 G2 按本 Architecture 判断，不由 G0 预先决定。

`README.md` 主要供人理解，不作为 ordinary runtime Requirement router；`analysis/` 也不进入 ordinary runtime，除非当前任务明确需要原始 Evidence、历史原因或尚未 promote 的分析材料。

## 产物生命周期

### 生产者

主要 producer：`method:requirement-baseline-establishment`、后续 Feature / Clarification 中经 Repository Authority 确认需提升的长期 Requirement fact，以及 Human / Product Authority 的长期决定。

### 触发条件

典型 trigger：新项目建立 Requirement Baseline；Requirement fact 被确认或变更；长期 owner 缺失、冲突或需要拆分 / 合并；Feature 中出现具有跨 Feature 长期价值的新业务事实；external / policy change 改变长期 Requirement。

### 使用方

主要 consumer：`method:ai-development`、`method:architecture-clarification`、Feature Specification、Technical Planning、verification / review 与 Human Product / Requirement Review。

### 持久化 / 更新 / 替代

长期 Requirement Fact 必须保存在 Consumer Repository Authority 可定位的 durable owner。修改事实时先更新真实 owner，再最小传播 locator / relation / affected references；不得在下游 Specification、Technical Plan 或聊天中静默覆盖 Requirement。

新 owner 替代旧 owner 时应更新当前 locator / references，并删除、归档或明确废止旧 current owner；历史由 Git / Issue / PR 与必要 archive 保存，不维护两套 current fact。

### 升级

发现 current Authority 冲突、unique owner 不清、多种合理答案会实质改变 Product behavior / Acceptance、变更超出授权，或批量语义变换需要独立 review 时，返回 Requirement Method / Human Authority。

## 与 Specification / Architecture 的边界

Requirement 持有跨多个 Feature 长期持续成立的 Product / Domain / NFR 事实；Feature Specification 只拥有当前 change 的具体适用、Scope、Observable Behavior、Failure Behavior 与 Acceptance。

Feature 中确认的新术语、不变量、规则或 data semantics 如果具有长期跨 Feature 价值，应提升到真实 Requirement owner，再由 Specification 引用。

Architecture 持有系统如何被长期组织以及跨 Feature 持续成立的结构约束。Requirement 可以拥有产品 / 系统责任边界，但不拥有内部 component boundary、deployment topology、framework composition 等普通 Architecture fact。Architecture 不得通过技术判断覆盖 Requirement；冲突先返回 Requirement owner。
