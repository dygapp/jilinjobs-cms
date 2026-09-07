# Site Packages

`sites/` 保存 Generic Site Package schema 与具体 Site 的版本化 package source。

- `schema/`：Site Package machine-readable schema；
- `jilinjobs/`：当前 JilinJobs Site Package。

Generic CMS / Site Package 四层边界以 `../docs/requirements/cms-site-package-boundary.md`、对应 Specification / Technical Authority 与 Issue #77 为准。Historical Content Migration 仍位于 `../data-migrations/**`，不因站点 package 存在而迁入本目录。
