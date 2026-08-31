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

## 6. 静态资源边界

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

关键资源保护集合包括运行时基线、启用的 RESOURCE_PATH 属性、列表图片、宣传展示图片和导航图标。该机制仍不是完整引用图。

## 7. 权限边界

当前 Runtime 无认证授权。Controller/Service 不读取虚构用户身份；Admin 不做角色条件渲染。未来权限通过统一平台接入，届时在 API 边界增加权限控制，不改变当前核心对象语义。

## 8. Verification

CI 分为 Backend、Public Site、Admin Site、Integrated Browser。新的 CMS 模型必须包含 Backend 定向测试和跨 Admin→Public 的 Browser 证据。最终 Human Review 使用干净基线重新启动。
