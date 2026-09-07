# 管理端工程分离与功能收敛技术计划（Technical Plan）

## 1. 目的

本文把 `docs/specifications/admin-frontend-convergence.md` 映射为跨 Execution Unit 持续有效的 HOW。

## 2. 目标前端结构

```text
frontend/
├── public-site/
│   ├── package.json
│   ├── vite.config.ts
│   ├── src/
│   └── tests/e2e/
└── admin/
    ├── package.json
    ├── vite.config.ts
    ├── src/
    └── tests/e2e/
```

后端继续保持单一 `backend` Spring Boot 工程。

当前不创建大型共享前端 package。拆分阶段允许两端暂时各自维护实际消费的 API transport 类型；只有拆分后出现稳定、明显并具有长期维护价值的重复时，再抽取最小共享包。

## 3. Runtime / URL

公开站 Vite base 为 `/`；管理端 Vite base 为 `/admin/`。

Review / E2E Nginx 使用一个监听端口：

```text
/admin/**  → /usr/share/nginx/html/admin/**
/**        → /usr/share/nginx/html/public/**
/api/**    → backend:8080
/static/** → backend:8080
```

管理端 Vue Router 使用 `createWebHistory('/admin/')`，内部路由为 `/articles`、`/columns` 等；外部 URL 保持 `/admin/articles` 等。

公开站保持既有 canonical URL 与 `page.html` 多入口构建接缝，不因管理端拆分改变 URL。

## 4. Admin Application Shell

管理端新增统一 `AdminLayout` / Root App：

- 左侧管理导航；
- 当前路由高亮；
- 顶部应用标题与“查看公开站”链接；
- 主内容区；
- 统一基础视觉变量、页面 header、卡片、工具栏、表单和空状态样式。

本阶段不引入新的 UI 框架，继续使用 Element Plus。

## 5. 管理模块收敛

### 5.1 栏目

保留现有树结构与 CRUD。补充必要的必填和 alias 提示，错误继续由后端规则作为最终约束。

### 5.2 导航

保留 HOME / COLUMN / PAGE / LINK / PLACEHOLDER 模型。管理 UI 按 target type 清晰显示对应字段；避免无关 target 字段残留影响保存。

### 5.3 文章

后端当前数据量和 API 仍适合先保持稳定。第一阶段列表筛选与分页在管理前端完成：一次加载当前后台文章列表后，按标题、栏目、状态、articleType 过滤，再对当前结果分页。

如果未来数据规模证明需要服务端分页，再作为独立 API 演进，不在当前物理拆分阶段提前重构。

编辑器继续使用轻量 contenteditable，不引入新的重量级编辑器依赖；目标是消除“原型不可用”的明显问题，而不是建设通用内容设计器。

### 5.4 固定页面 / 页面组

RICH_TEXT 页面使用轻量富文本编辑区；EMBED_PLACEHOLDER / INTERNAL_STATIC 显示 `embedUrl` / path 接缝字段并给出用途说明。

页面组仍保持当前模型，不新增独立业务模块。

### 5.5 网站配置

后端 `SiteConfigService` 注入 Jackson `ObjectMapper`，JSON 类型配置通过 `readTree` 实际解析，要求根节点为 object 或 array。

管理前端对 JSON 编辑值执行 `JSON.parse`，保存前阻止明显格式错误并显示可读反馈。RESOURCE_PATH 类型显示 `/static/**` 用途提示。

### 5.6 静态资源内容验证

扩展名白名单继续作为第一层约束；第二层使用实际内容验证：

- `png`：PNG signature；
- `jpg` / `jpeg`：JPEG SOI；
- `gif`：GIF87a / GIF89a；
- `webp`：RIFF + WEBP；
- `ico`：ICO header；
- `pdf`：`%PDF-`；
- `doc` / `xls`：OLE Compound File signature；
- `docx` / `xlsx`：ZIP signature，并对 OOXML ZIP 至少检查相应 `word/` 或 `xl/` entry。

上传内容与扩展名不匹配时拒绝，不通过修改扩展名绕过。

### 5.7 关键资源保护

`StaticResourceService` 读取 `cms_site_config` 当前配置，形成受保护路径集合：

- 固定 `health/baseline.png`；
- 所有 `value_type = RESOURCE_PATH` 且值属于 `/static/**` 的路径；
- `HOME_BANNERS` JSON 中 `image` 字段属于 `/static/**` 的路径。

删除前检查该集合，受保护资源返回验证错误；replace 上传允许，但由管理 UI 明确显示更高风险确认。

本阶段不扫描 CSS / JS / 富文本引用，因此不能把上述保护集合称为完整引用图。

## 6. Backend Test

新增至少：

- `PageServiceTest`：alias / group / render mode 基本规则；
- `SiteConfigServiceTest`：合法 JSON、非法 JSON、未知 key；
- `StaticResourceServiceTest`：真实文件签名、伪装扩展名、路径穿越、受保护删除、普通删除/恢复。

既有 Column / Article / Navigation tests 继续回归。

## 7. Frontend Test

### Public Site

迁移并保持现有公开站 E2E：首页、导航、栏目、文章、固定页、视觉/响应式/SEO 等。

### Admin

新增独立管理端 E2E：

- Admin Shell 导航；
- 栏目 / 导航页面可访问；
- 文章筛选、分页、类型切换和显式发布；
- Page render mode 字段；
- JSON 配置错误提示；
- 静态资源伪装上传失败、保护删除失败、普通资源替换/回收/恢复。

### Cross-boundary

至少保留：

```text
Admin create/update/publish
→ Public API / Public Page reflects current state
```

用于证明前端物理拆分没有破坏 CMS 核心业务闭环。

## 8. CI

CI 分层：

```text
Backend verify
Public frontend verify
Admin frontend verify
        ↓
Integrated browser verification
```

分别上传：

- `backend-jar`
- `public-site-dist`
- `admin-dist`
- `playwright-evidence`

E2E Nginx 同时挂载两个 dist。

## 9. Review Environment

Review Environment 同时构建 Backend、Public Site、Admin 三个产物。

自动 Browser Verification 完成并收集证据后：

1. 重建数据库；
2. 恢复版本化静态资源；
3. 重启 Backend；
4. 准备明确 Human Review Fixture；
5. 验证 `/`、`/admin/`、公开 API、管理 API 和关键静态资源；
6. 建立 FRP；
7. 验证外部 `http://review.cc-lotus.info/` 与 `http://review.cc-lotus.info/admin/`；
8. 进入 Human Admin Review。

## 10. 风险控制

- **公开站回归**：EU-14 只做结构迁移和 Router / build 分离，不同时重做公开页面；公开 E2E 继续作为回归网。
- **双前端契约漂移**：先接受小规模 transport duplication，不复制业务规则；后端仍是最终业务校验者。
- **API 过早重构**：文章筛选/分页先在 admin 前端完成，不为当前规模提前改变后端 contract。
- **媒体校验误杀**：仅对当前允许扩展名实施轻量 signature / parse；转换能力不进入本阶段。
- **关键资源保护误称完整引用检查**：只保护明确 site-config 引用和系统基线，UI 保留“非完整引用检查”提示。
- **证据漂移**：每次最终 Completion Claim 使用当前 Head Evidence；祖先证据复用必须按 Consumer-local descendant evidence 规则逐 Claim 判断。
