# CMS 总体技术架构（Technical Plan）

## 1. 目的

本文描述 CMS Core、Public Site、Admin Site 与 Backend 的长期技术边界，并以 Issue #77 / Issue #92 已接受的 Generic CMS Core、JilinJobs Site Package、Historical Migration、Replaceable Public Renderer 四层责任作为当前数据与生命周期解释基线。

## 2. 应用关系

```text
Admin Site SPA
      │
      ▼
 CMS Backend ───── Database / Runtime Static Store
      │
      ▼
Public Site SPA
```

Public Site 与 Admin Site 是同级前端应用；Backend 是共享 CMS 业务模型和持久化的服务实现，不定义第二套产品事实。JilinJobs Site Package 与 Historical Migration 当前仍可与 Backend 同仓，但不因此并入 Generic CMS Core 的数据 Authority。

## 3. 源码结构

```text
frontend/
├── public-site/
└── admin/
backend/
sites/jilinjobs/
data-migrations/
```

公开站 base `/`；管理端 base `/admin/`；`/api/**` 和 Runtime `/static/**` 由 Backend 提供。

## 4. Backend 模块边界

按领域对象保持最小模块：column、content/article、page、navigation、listing、advertisement、siteproperty、resource/staticresource。

领域对象间只通过明确查询接口或 ID 关联。产品界面使用“宣传展示”，Backend 为兼容已有表/API 可继续保留 advertisement 技术命名，不为纯命名制造破坏性迁移。

当前 Generic CMS Core 与 Server / Historical Migration application boundary 仍按 Issue #92 Phase 2 单独规划；不得从本文件的领域 package 划分直接推导新的 Gradle module、Clean Architecture 或 Repository split。

## 5. 数据 Authority

运行时同一数据只允许一个业务来源：

- 导航名称、目标、排序及可选业务图标 → navigation；
- 轮播/友情链接等通用有序数据 → listing；
- 宣传展示位/展示内容 → advertisement 技术模块；
- 站点属性 → siteproperty；
- 页面稳定结构/NCSS 固定集成 → Public Site 工程；
- JilinJobs stable structure → `sites/jilinjobs/structure/**`；
- Fresh JilinJobs one-time operational defaults → `sites/jilinjobs/bootstrap/**`；
- JilinJobs stable Site asset source → `sites/jilinjobs/assets/**`；
- 历史文章、外链、正文/附件资源、历史运营列表成员 → `data-migrations/**`。

CmsList 只描述数据，不配置页面展示模式；Public Site 根据页面设计决定读取哪些列表字段和如何渲染。Navigation 图标属于条目数据，Public Site 不按数组位置推导。

当前 Backend Flyway 只承担 Generic CMS Schema evolution / site-neutral provisioning capability。EU-41 已完成一次受控 development baseline replacement；当前 active lineage 为 `V1__current_cms_schema.sql` + `V2__site_provisioning_schema_capabilities.sql`，后续 Generic Schema change 从 V3 恢复 append-only。历史已接受的旧 migration 仍用于追溯，但不得作为当前 Site data lifecycle Authority。

## 6. 配置与元数据 Authority

配置治理以 `docs/technical/configuration-governance.md` 为长期规则。项目不建立一个无边界的“系统设置”容器，而是按责任区分：

- 稳定领域契约、安全规则、页面模板稳定 Code / Alias → 代码常量；
- 站点管理员需要运行期维护的数据和低风险行为参数 → CMS 运营数据 / 网站属性；
- 低频结构定义 → `cms-metadata.yml` 等 CMS 资源元数据；
- 数据库、存储、端口等实例差异 → Spring 外部化配置；
- JilinJobs stable Site package root / assets → `sites/jilinjobs/**` 版本化 Authority + Runtime composition 配置；
- GitHub Actions、FRP、Review URL 等 → CI / Deployment Variables。

“存在硬编码”本身不构成架构缺陷。只有证明某值存在部署差异、站点差异或运营维护价值后，才允许提升为配置；配置化不得削弱安全边界或把工程责任错误暴露给 Admin。

## 7. 静态资源边界

静态资源统一由 Backend Runtime `/static/**` 服务。稳定 Site asset 的版本化 source 与运行时上传目录分离：

```text
sites/jilinjobs/assets/**        # 稳定 Site asset 唯一版本化 source owner
        ↓ Site Package asset projection
/static/
├── brand/                       # stable target
├── footer/                      # stable target
├── health/                      # stable target
├── home/                        # stable target
├── icons/                       # stable target / 内置站点图标
└── uploads/                     # CMS mutable Runtime 上传
    ├── displays/
    ├── lists/
    ├── site-properties/
    └── navigation-icons/
```

Site Package asset manifest 负责 stable source / `/static/**` target / SHA-256 integrity；projection 仅 create missing targets，不覆盖 existing regular target。`/static/uploads/**` 明确不属于 stable Site asset ownership。

Admin 的展示内容、列表项、RESOURCE_PATH 网站属性和导航图标统一复用图片资源选择/上传组件；Backend 继续负责真实媒体签名校验、路径安全、替换和回收。引用变化不自动删除旧文件。

受保护资源集合由“Spring 配置声明的固定部署路径 + Site Package stable targets + 当前 CMS 运行时直接引用”共同形成。固定路径不得散落硬编码在 Service；运行时引用必须动态计算。该机制仍不是完整引用图。

Admin 图片辨识统一复用 AdaptiveImagePreview；透明/浅色图片的对比背景由项目组件处理，放大 Viewer 复用 Element Plus，不建立第二套自研图片查看器。

## 8. 权限边界

当前 Runtime 无认证授权。Controller/Service 不读取虚构用户身份；Admin 不做角色条件渲染。未来权限通过统一平台接入，届时在 API 边界增加权限控制，不改变当前核心对象语义。

## 9. Verification

CI 分为 Backend、Public Site、Admin Site、Integrated Browser。新的 CMS 模型必须包含 Backend 定向测试和跨 Admin→Public 的 Browser 证据。配置边界调整必须验证默认配置和可覆盖配置；图片公共组件调整必须执行相关 Admin Browser 回归。

Fresh JilinJobs Runtime 的当前组合顺序为：Generic Flyway → stable Site Package reconcile → 按场景显式 one-time bootstrap → stable asset projection → optional Historical Canonical Migration → Runtime verification。测试代码不得重新创建 Site stable structure，也不得通过第二份静态 baseline 绕过 Site Package asset projection。最终 Human Review 使用同一版本化 Site Package 恢复干净基线后启动。
