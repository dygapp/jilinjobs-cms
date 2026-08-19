# jilinjobs-cms

`jilinjobs-cms` 是吉林省智慧就业云平台中“信息发布与网站服务”相关能力的独立 Consumer 项目。

本项目从 Greenfield（绿色场）状态开始建设，目标是在明确业务需求和最小项目治理基础上，逐步形成可持续开发的软件系统。

## 当前项目目标

本项目当前迭代首先实现中心主站的信息发布核心能力。

目标包括：

- 栏目管理；
- 菜单 / 导航组织；
- 信息发布核心能力；
- 网站前端三级页面：
  - 首页；
  - 二级页面；
  - 内容详情页面。

## 当前迭代范围（Scope）

### In Scope（本轮范围内）

- 栏目和分类管理；
- 菜单与导航组织；
- 信息内容发布；
- 中心主站公开访问页面：
  - 首页；
  - 栏目/二级页面；
  - 内容详情页面。

### Out of Scope（本轮不包含）

- 外部内容嵌入；
- 用户与权限管理；
- “中心党建”二级网站；
- 评论及其他互动能力；
- 复杂统计分析；
- 多站点扩展能力。

详细业务需求范围可能比当前迭代更广。只有属于当前 Scope 的内容才是本轮有效实现要求。

## 权威业务需求

当前详细业务需求：

```text
docs/requirements/information-publishing.md
```

该需求文档已经由本仓库显式采纳，作为当前迭代 Scope 内的详细业务依据。

该来源文档中引用的 `docs/project/project.md` 和 `docs/requirements/overview/system-module-boundaries.md` 当前不存在于本 Consumer Repository，因此这些 upstream references 只保留来源关系，不构成本项目 Authority。当前需求解释必须同时服从本 README 的迭代 Scope 与 `AGENTS.md` 的 Authority Boundary。

需求事实必须来自明确的 Authority，不得通过实现习惯、其他项目经验或常见做法自行扩展、修改或替换。

## 当前 Specification

当前迭代的 WHAT / WHY Specification：

```text
docs/specifications/center-main-site-core.md
```

Specification 只收敛当前 Scope 内的 Required Behavior、Boundary 与 Acceptance，不替代更高优先级 Authority，也不提前规定实现 HOW。

## 开发方法来源

本项目采用 `agentic-dev` 作为 AI Agent 驱动开发 Method 与 Skills 的知识来源：

```text
Repository:
dygapp/agentic-dev

Experiment Validation Baseline:
master@3e3f9c9abd338680f5944dd43355404109b8b326
```

使用原则：

- 按当前阶段加载需要的 Method / Skill；
- 不为了流程完整性创建无实际价值的文档或目录；
- Specification 负责 WHAT / WHY；
- Technical Plan 只在必要时产生；
- Execution Unit 应适合 Fresh Context 独立执行和验证。

## 当前阶段

```text
Specification Ready → Technical Planning Required → Human Architecture Decision
```

Technical Planning 已确认有必要：当前 Greenfield 系统需要跨多个 Execution Units 长期协调公开前端、后台管理、内容状态、数据持久化、文件资源和共享契约，不能安全地把这些 HOW 全部留给各 Unit 独立决定。

当前存在两个必须在形成 Technical Plan 前解除的高影响阻塞点：

1. **目标技术与部署架构基线**：当前 Authority 未说明本项目应作为独立应用/服务建设，还是嵌入既有智慧就业云平台，也未提供前端、后端、运行时或部署技术基线。该选择会决定主要组件边界与共享契约，属于 Major Architecture Direction。
2. **后台认证与授权集成边界**：当前 Scope 明确不建设用户与权限管理，但详细业务需求要求后台只面向具备相应资格的中心管理用户。当前 Authority 未提供应接入的认证/授权机制或安全边界，不能通过“暂时无认证”静默替代，属于 Security-sensitive / External Integration Decision。

在上述 Human Decision 明确前：

- 不创建假定性 Technical Plan；
- 不开始生产代码实现；
- 不从缺失的 upstream references、其他项目或 Conversation History 推断技术基线；
- 其余普通、低影响、可逆的技术细节仍由 Agent 在后续 Technical Planning / Execution 中自主处理。

## 当前仓库结构

```text
.
├── AGENTS.md
├── README.md
└── docs/
    ├── requirements/
    │   └── information-publishing.md
    └── specifications/
        └── center-main-site-core.md
```

项目结构应随着真实开发需要逐步演进。

只有具有 Authority、协调、追踪或长期知识价值的信息，才应形成新的项目 Artifact。

## GitHub Repository 操作约定

`dygapp/jilinjobs-cms` 已获得项目负责人持续授权。

后续 Agent 可以直接通过 GitHub Repository 进行本项目范围内的正常开发操作，不需要重复请求操作许可。

该授权不包含：

- 改变项目目标或 Scope；
- 替代产品决策；
- 未授权的外部系统操作；
- Production 发布或部署。
