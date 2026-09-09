# Site Package 稳定静态资源

本目录属于 `sites/jilinjobs` Site Package，保存中心主站与中心党建已经确认的稳定静态资源源文件。

`manifest.json` 为资源投影合同：每个条目声明 package-root 内的 `source`、公开 `/static/**` `target` 与源文件 SHA-256。Backend 启动时在 Site Package 校验通过后，将缺失资源以 create-if-missing 语义投影到 `CMS_STATIC_ROOT`；已经存在的目标文件不会被启动过程覆盖，因此后台明确替换后的运营版本可跨重启保留。

Site Package target 属于静态资源 protected-path 合同：普通删除被拒绝，明确 replace 仍然允许。

`/static/uploads/**` 是可变 Runtime Store，不属于本目录，也不会被 Site Package 接管。历史文章正文及迁移资源继续由 `data-migrations/**` 管理。

`health/baseline.png` 仅用于证明稳定资源投影链路，不作为前台业务素材。

`pages/**` 保存 stable Page 正文直接引用的 Site Package 静态文件；例如 `pages/budget/**` 统一承载“预决算公开”附件，公开路径为 `/static/pages/budget/**`，不继承 Legacy CMS 的上传目录结构。
