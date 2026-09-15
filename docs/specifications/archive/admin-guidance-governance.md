# 管理端操作提示与解释责任治理规格说明（Specification）

## 1. Authority

上游需求：`docs/requirements/admin-guidance-governance.md`。

本文只定义本轮 WHAT / WHY 与验收边界。`docs/specifications/admin-site.md`、`docs/specifications/cms-core.md` 中未被本文明确修订的管理模型、字段、preset 保护和交互规则继续有效。

## 2. Expected Behavior

### 2.1 全局提示责任

当前八类 CMS 管理入口的页面标题、表单辅助文案和 Alert 必须只承担运营人员当前操作所需信息。

允许并应保留：

- 必填/格式校验；
- 操作结果、状态后果；
- 删除、替换、公开访问风险；
- 隐式无法表达的创建后不可修改限制；
- 受保护资源语义；
- 外链、NO_LINK 等直接影响当前行为的说明。

不得继续展示：

- Requirement / Method 名称或演进背景；
- Database、metadata 存储位置、部署配置职责；
- “随前端工程部署”等实现归属；
- “不决定公开页面布局/图片尺寸”等架构职责解释；
- 已被控件隐藏、disabled、标签或默认行为完全表达的重复提示。

### 2.2 稳定身份字段

本轮不改变 Backend 字段名、请求字段和数据库字段，只调整产品层标签：

| Technical field | User-facing label |
|---|---|
| Column / Page / PageGroup `alias` | 公开标识 |
| CmsList `code` | 列表标识 |
| NavigationLocation `code` | 位置标识 |
| AdvertisementSlot `code` | 展示位标识 |
| SiteProperty `key` | 属性标识 |

预置对象的不可修改说明使用“预置…标识不可修改”等简短操作语言，不使用“稳定站点身份”等架构解释。

普通 Runtime 对象的字段是否可编辑继续遵守现有 contract；本轮不扩大或缩小写权限。

### 2.3 Article

- `coverPolicy=NONE`：不渲染封面编辑控件，也不渲染额外“当前栏目不使用文章封面图片” Alert；
- `coverPolicy=REQUIRED`：继续显示“草稿可暂存，发布前必须设置封面”；
- 编辑已有文章时，内容类型控件继续不可修改，辅助文字收敛为“创建后不可更改”；
- “运营属性”只说明排序优先级：置顶 → 展示顺序 → 发布日期；
- EXTERNAL_LINK 提示继续说明公开访问直接跳转来源网站；
- INTERNAL 提示继续说明新建为草稿、普通编辑不改变当前发布状态。

### 2.4 Column / List

Column：

- 表格与 Dialog 中 `Alias` 改为“公开标识”；
- `coverPolicy` 选项不变；
- 删除“只约束文章是否具有封面数据，不决定公开页面图片位置/尺寸/布局”等设计说明。

List：

- Dialog 中 `Code` 改为“列表标识”；
- `imagePolicy` 选项与 Backend 约束不变；
- 删除“约束列表项是否需要图片，不决定公开页面如何展示”等设计说明；
- 列表项数据来源创建后不可修改的说明保留，但使用简洁限制语言。

### 2.5 Page

- Page / PageGroup `Alias` 标签改为“公开标识”；
- `INTERNAL_STATIC` 不再显示“随前端工程部署”“不允许上传任意 HTML/JS”等实现说明；字段本身和 `/` 开头校验足以表达当前输入要求；
- `EMBED_PLACEHOLDER` 允许保留一条简洁操作说明：当前只保存嵌入地址与占位说明，不直接加载第三方内容；不得出现“当前 Requirement”等 Method 文案；
- 删除确认中的公开地址风险继续保留。

### 2.6 Navigation / Advertisement

Navigation：

- NavigationLocation `Code` 改为“位置标识”；
- 导航条目 Dialog 中“导航位置”不再使用 disabled input，而以普通只读文本表达当前选择上下文；
- 目标类型、目标栏目/单页/链接、打开方式等现有字段不变。

Advertisement：

- AdvertisementSlot `Code` 改为“展示位标识”；
- `NO_LINK` 下辅助文案简化为“目标地址会保留；重新启用跳转后继续使用”；
- 删除展示位/内容的确认语义继续保留。

### 2.7 SiteProperty

- 表格和定义 Dialog 的 `Key` 改为“属性标识”；
- 删除页面顶部关于 Database / 上传安全限制由工程配置负责的 Alert；
- 属性分组导航不显示“来自 CMS 资源 metadata”等实现来源说明；
- 定义 Dialog 的分组 Select 不显示“来自部署资源配置、不在数据库维护”等实现说明；
- 编辑既有定义时的 Alert 收敛为“属性值请通过列表中的‘编辑值’修改”；
- 值编辑 Dialog 中属性 `description` 属于真实运营说明，继续显示。

### 2.8 StaticResource

- 受保护 Tag/Tooltip、删除和替换确认继续存在；
- 页面级风险说明改为用户后果语言：受保护资源不能直接删除；其他资源在替换或删除前需要确认没有页面使用；系统不会自动检查全部引用；
- 删除/替换确认继续明确可能影响公开页面；不得因文案治理移除安全门槛。

## 3. Acceptance Obligations

| ID | Acceptance Obligation | Required Evidence |
|---|---|---|
| AG-01 | 八类管理页面不再展示 Requirement / Database / metadata / 前端工程 / 页面布局职责等实现说明 | Browser / Source Evidence |
| AG-02 | alias/code/key 技术字段保持 contract，但关键用户标签改为中文业务标识 | Browser Evidence |
| AG-03 | Article NONE 不显示封面 Alert；REQUIRED、外链和发布状态操作信息仍存在 | Browser Evidence |
| AG-04 | Column/List 图片策略保留数据约束且移除页面布局职责解释 | Browser Evidence |
| AG-05 | Page 特殊呈现模式只显示当前操作语义，不显示实现/Method 背景 | Browser Evidence |
| AG-06 | SiteProperty definition/value 分工不变，同时移除 metadata/部署/数据库解释 | Browser Evidence |
| AG-07 | StaticResource 的保护、删除、替换风险提示继续存在 | Browser Evidence |
| AG-08 | Backend API、CMS 数据模型、preset protection、Public Site 行为无变化 | Diff Scope Review + Existing CI |

## 4. Verification Strategy

新增 Admin Playwright 回归，至少覆盖：

1. 栏目与单页的“公开标识”、列表“列表标识”、导航位置“位置标识”、展示位“展示位标识”、网站属性“属性标识”；
2. 文章在 `coverPolicy=NONE` 栏目下不存在 `article-cover-disabled` Alert；切换到 REQUIRED 栏目后 REQUIRED 提示仍可见；
3. Page INTERNAL_STATIC / EMBED_PLACEHOLDER 不出现“前端工程 / Requirement / HTML/JS”实现说明，后者仍有简洁“当前不直接加载第三方内容”操作说明；
4. SiteProperty 页面不存在 metadata / Database / 部署来源解释，但编辑已有定义仍明确从“编辑值”维护值；
5. StaticResource 页面仍有受保护风险说明，删除/替换入口和既有保护回归继续通过；
6. Existing Admin E2E、Backend、Public frontend 与 Integrated Browser Verification 全量通过。

## 5. Scope Control

本工作只允许修改 Admin 用户可见文案、标签、只读呈现和与之直接对应的 Browser E2E。不得借机：

- 修改 Backend DTO / Service / Mapper；
- 修改 Flyway；
- 改变 Public Site；
- 调整权限；
- 重构富文本编辑器；
- 建立新的 shared guidance abstraction，除非实施中出现至少两个真实消费者且当前重复已造成维护问题。

## 6. Readiness Inputs

当前 `main@4e668645f039fdff35c5b1aa58b6255681d14481` 已确认：

- 八类 Admin CMS 页面均位于 `frontend/admin/src/modules/cms/views/admin/`；
- 需治理内容全部属于模板文案/呈现，未发现必须同步改变 Backend contract 的前置依赖；
- `docs/specifications/admin-site.md` 明确要求产品界面不得要求管理员理解数据库/API/模块装配术语，同时保留 REQUIRED 封面、静态资源保护等真实操作语义；
- 现有 Admin E2E 已覆盖 preset protection、列表图片要求、文章封面要求、静态资源安全等回归，可作为 AG-08 的既有基线；
- 本工作不存在跨多个 Execution Unit 的长期 HOW 协调需求，因此不需要独立持久化 Technical Plan。
