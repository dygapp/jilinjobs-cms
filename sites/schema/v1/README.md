# Site Package v1 Schemas

本目录定义 `sites/**` Site Package v1 的机器可审计数据契约。

EU-37 首先建立 `manifest + columns` Foundation；EU-38 在不改变 `schemaVersion=1` 的前提下增加可选 structure types：

- `page-groups`
- `pages`
- `navigation-locations`
- `site-config`
- `lists`
- `advertisement-slots`

Manifest 中实际声明的 structure file 才参与 package apply；因此只包含 `columns` 的 Foundation package 仍保持 v1 兼容。

本版本故意不定义：

- NavigationItem；
- CmsListItem；
- Advertisement member；
- Historical Content；
- Runtime upload；
- multi-site / plugin metadata。

这些对象没有因 EU-38 被提升为稳定 Site Structure Authority。
