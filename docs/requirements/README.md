# 需求权威导航（Requirement Human Navigation）

`docs/requirements/` 保存当前长期 Product / Domain Requirement Authority。本文件只回答“人应该如何阅读和维护这套 Requirement”，不拥有 Requirement inventory，也不复制业务事实。

## 如何阅读

1. 先读取 `index.md`，按当前 Feature / Capability 定位唯一 Requirement fact owner；
2. 只读取命中的 Product / Domain owner 及 Index 明确关联的最小上下文；
3. 用户可观察行为继续进入 `docs/specifications/`；跨 Feature 长期结构进入 `docs/architecture/`；实现 HOW 进入 `docs/technical/`；
4. 需要 concrete Site Definition / Historical canonical data 时，读取对应 versioned source workspace，但不得把这些 concrete source 反向提升为 Product Requirement。

普通 Fresh Context 不应扫描全部 Requirement，也不应把 README 当作第二份总需求说明书。

## 当前信息架构

当前 Consumer 采用轻量 projection：

```text
docs/requirements/
├── README.md                  # Human Navigation；不拥有 inventory / fact
├── index.md                   # Requirement Authority Index；只拥有 locator / relation
├── information-publishing.md # Product Requirement fact owner
├── cms-domain.md              # CMS Domain Requirement fact owner
└── archive/                   # superseded / historical requirement evidence
```

G2 复核后没有证据支持按页面、菜单、前端应用、数据库对象或历史 Feature 再拆更多长期 Requirement owner。多个 Requirement Capability 可以由同一个语义一致的 fact owner 承担，并由 `index.md` 定位到稳定章节；是否未来物理拆分，必须由真实 ownership pressure 驱动。

## Fact owner 边界

- `information-publishing.md`：产品目标、用户、范围、长期产品能力、Main / Party 业务定位、canonical public contract、跨 Capability 产品级质量与验收不变量；
- `cms-domain.md`：CMS business objects、identity、state / lifecycle、cross-object rule、content ownership、stable / Runtime lifecycle、Historical Migration domain semantics 与 domain failure invariants。

同一长期事实只能有一个 primary fact owner。Product 文档可以引用 Domain rule 的业务结果，但不得复制状态机、identity policy、fingerprint 等 Domain 正文；Domain 文档可以说明 Product scope 对对象模型的约束，但不得重新定义 Product Goal、用户或公开站定位。

## Analysis / Human Review material

source inventory、extraction table、ambiguity / conflict list、comparison matrix、capability review batch 与会话 scratchpad 默认属于 **Requirement Analysis Workspace**，不构成 Requirement Authority。

当前 G2 的 transitional analysis / review evidence 由 Issue #155、PR Review 与 GitHub history 承载，因此不为“方便 AI”额外创建长期 `analysis/` 同步副本。未来确需 Repository 内 analysis artifact 时，必须显式标记 non-Authority、producer、promotion path 与退出条件；事实 promote 后应删除、归档或降级为 historical evidence。

## Archive

`archive/` 保存已经被当前 Product / Domain / Architecture / Specification Authority 吸收、取代或纠正的历史 Requirement Change 与 clarification source，只用于 traceability。

Archive 默认不参与 Fresh Context；只有当前 Authority 明确需要审计 lineage、来源或历史决策时才定向读取。历史正文以证据保真为先，不因归档而重写。

## 维护规则

- 新的长期 Requirement fact 先判断是否可进入现有 Product / Domain owner；只有真实 semantic owner 无法合理承担时才新增 owner；
- 不通过新增并列 Requirement Change 文件长期覆盖已有 owner；
- `index.md` 只维护 locator / relation，不复制规则正文；
- Requirement 变更先更新真实 fact owner，再最小同步 Index 与受影响引用；
- 未经 Authority promotion 的 analysis、实现事实、历史行为或 provisional default 不得进入 ordinary Fresh Context Requirement baseline。
