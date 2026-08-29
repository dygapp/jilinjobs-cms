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

建议按领域对象保持最小模块：column、content/article、page、navigation、list、advertisement、siteproperty、resource/staticresource。

领域对象间只通过明确查询接口或 ID 关联。前端 transport 类型可暂时分别维护；不为了消除少量重复创建大型共享前端 package。

## 5. 数据 Authority

运行时同一数据只允许一个业务来源：

- 导航 → navigation；
- 轮播/友情链接 → list；
- 广告 → advertisement；
- 站点属性 → siteproperty；
- 页面稳定结构/NCSS 固定集成 → Public Site 工程。

Flyway 只新增 migration，不回改已执行历史 migration。

## 6. 权限边界

当前 Runtime 无认证授权。Controller/Service 不读取虚构用户身份；Admin 不做角色条件渲染。未来权限通过统一平台接入，届时在 API 边界增加权限控制，不改变当前核心对象语义。

## 7. Verification

CI 分为 Backend、Public Site、Admin Site、Integrated Browser。新的 CMS 模型必须包含 Backend 定向测试和跨 Admin→Public 的 Browser 证据。最终 Human Review 使用干净基线重新启动。
