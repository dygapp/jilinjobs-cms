# JilinJobs Site Package

`sites/jilinjobs/` 是 JilinJobs Site 的稳定版本化 package source：

- `manifest.json`：package identity / version / component manifest；
- `structure/`：stable Site structure 与 stable identity；
- `bootstrap/`：Fresh Site one-time operational defaults；
- `assets/`：stable Site asset source 与 integrity metadata。

`structure` 可按当前 Site Package contract provision / reconcile；bootstrap 成功后其 Runtime data 转为 operator-managed data，普通 restart / reconcile / repeated bootstrap 不得覆盖或 resurrect；stable assets 投影到既有 `/static/**` targets，CMS Runtime uploads 仍属于 `/static/uploads/**`。

历史 Article、正文资源、附件、历史列表成员与 provenance 继续由 `../../data-migrations/**` 承担，不进入 stable Site Package。

详细 Authority：`../../docs/requirements/cms-site-package-boundary.md`、`../../docs/specifications/cms-site-package-boundary.md`、`../../docs/technical/cms-site-package-boundary.md`。
