# 当前正式工作

Current Ready Execution Unit：**EU-67 CMS 可信主体与隔离测试身份基础**。

当前工作 artifact：`eu67-cms-principal-foundation.md`。

- Status：`active`
- Readiness：`PASS`
- Branch：`codex/eu67-cms-principal-foundation`
- Base：`3f0e431ba1f0b3117eba9a939fb2976f6baa8df4`

EU-67 只建立 `cms-server` 的统一可信主体、受信转换接缝和隔离测试身份适配器，不实现 Admin 请求 / 方法授权、`401 / 403`、业务审计、前端身份页面或真实宿主协议。

Fresh Context 执行前必须重新读取本 locator、EU-67 artifact、其指向的 Current Authority，并核对 Branch / Head、工作树以及任务相关 GitHub native state。Readiness PASS 不自动授予 merge、release、deploy 或后续 Execution Unit 的执行权。

已完成 Unit 的历史证据按需从 `../archive/` 定向读取；稳定产品语义必须从当前 Requirement / Architecture / Specification / Design / Technical owner 恢复，而不是从完成态 Work artifact 反向建立第二份 Authority。
