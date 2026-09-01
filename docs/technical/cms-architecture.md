# CMS 总体技术架构（Technical Plan）

## 1. 目的

本文描述 CMS Core、Public Site、Admin Site 与 Backend 的长期技术边界。

## 2. 应用关系

```text
Admin Site SPA
      │
      ▼
 CMS Backend ───── Database / Static Assets
      │
      ▼
Public Site SPA
```

Public Site 与 Admin Site 是同级前端应用；Backend 是共享 CMS 业务模型和持久化的服务实现，不定义第二套产品事实。

## 3. 源码结构

```text
frontend/
├── public-site/
└── admin/
backend/
```

公开站 base `/`；管理端 base `/admin/`；`/api/**` 和 `/static/**` 由 Backend 提供。

## 4. Backend 模块边界

按领域对象保持最小模块：column、content/article、page、navigation、listing、advertisement、siteproperty、resource/staticresource。

领域对象间只通过明确查询接口或 ID 关联。产品界面使用“宣传展示”，Backend 为兼容已有表/API 可继续保留 advertisement 技术命名，不为纯命名制造破坏性迁移。

## 5. 数据 Authority

运行时同一数据只允许一个业务来源：

- 导航名称、目标、排序及可选业务图标 → navigation；
- 轮播/友情链接等通用有序数据 → listing；
- 宣传展示位/展示内容 → advertisement 技术模块；
- 站点属性 → siteproperty；
- 页面稳定结构/NCSS 固定集成 → Public Site 工程。

CmsList 只描述数据，不配置页面展示模式；Public Site 根据页面设计决定读取哪些列表字段和如何渲染。Navigation 图标属于条目数据，Public Site 不按数组位置推导。

Flyway 只新增 migration，不回改已执行历史 migration。

## 6. 配置与元数据 Authority

配置治理以 `docs/technical/configuration-governance.md` 为长期规则。项目不建立一个无边界的“系统设置”容器，而是按责任区分：

- 稳定领域契约、安全规则、页面模板稳定 Code / Alias → 代码常量；
- 站点管理员需要运行期维护的数据和低风险行为参数 → CMS 运营数据 / 网站属性；
- 低频结构定义 → `cms-metadata.yml` 等 CMS 资源元数据；
- 数据库、存储、端口、部署基线资源等实例差异 → Spring 外部化配置；
- GitHub Actions、FRP、Review URL 等 → CI / Deployment Variables。

“存在硬编码”本身不构成架构缺陷。只有证明某值存在部署差异、站点差异或运营维护价值后，才允许提升为配置；配置化不得削弱安全边界或把工程责任错误暴露给 Admin。

## 7. 静态资源边界

静态资源统一由 Backend `/static/**` 服务。版本化工程基线目录与运行时上传目录分离：

```text
/static/
├── brand/          # 工程基线
├── footer/         # 工程基线
├── health/         # 工程基线
├── home/           # 工程基线
├── icons/          # 工程基线/内置站点图标
└── uploads/        # CMS Runtime 上传
    ├── displays/
    ├── lists/
    ├── site-properties/
    └── navigation-icons/
```

Admin 的展示内容、列表项、RESOURCE_PATH 网站属性和导航图标统一复用图片资源选择/上传组件；Backend 继续负责真实媒体签名校验、路径安全、替换和回收。引用变化不自动删除旧文件。

受保护资源集合由“Spring 配置声明的固定部署/工程基线 + 当前 CMS 运行时直接引用”共同形成。固定基线路径不得散落硬编码在 Service；运行时引用必须动态计算。该机制仍不是完整引用图。

Admin 图片辨识统一复用 AdaptiveImagePreview；透明/浅色图片的对比背景由项目组件处理，放大 Viewer 复用 Element Plus，不建立第二套自研图片查看器。

## 8. 权限边界

当前 Runtime 无认证授权。Controller/Service 不读取虚构用户身份；Admin 不做角色条件渲染。未来权限通过统一平台接入，届时在 API 边界增加权限控制，不改变当前核心对象语义。

## 9. Verification

CI 分为 Backend、Public Site、Admin Site、Integrated Browser。新的 CMS 模型必须包含 Backend 定向测试和跨 Admin→Public 的 Browser 证据。配置边界调整必须验证默认配置和可覆盖配置；图片公共组件调整必须执行相关 Admin Browser 回归。最终 Human Review 使用干净基线重新启动。
