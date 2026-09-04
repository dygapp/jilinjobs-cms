# EU-29 Party Source Discovery

## Evidence

- GitHub Actions workflow: `EU-29 Party Source Discovery` Run #1 / Run ID `33828006870`;
- Head: `5752f21dde59ec1f379483965d8f4ed2b1303477`;
- 原站：`https://24365.jl.smartedu.cn`；
- 对象：四个 `/plist.html?typeCode=...` 列表与已知 `/pdetail.html?content_id=278556458369024&typeCode=gzdt` 详情。

## 已证实的原站页面模型

1. 原站党建详情不是 SPA 数据装配模型。标题、栏目名、面包屑、信息来源、发布日期、正文、正文图片、页脚都已经存在于 Document HTML；页面脚本主要承担导航和分享等增强。
2. Run #1 未观察到用于列表/详情正文装配的 XHR / fetch，因此当前没有证据证明存在可复用的公开 JSON 内容 API。
3. 已采详情明确引用 `/webfile/theme2/js/jquery-1.11.1.min.js`、Bootstrap 4.6、自定义 `theme2` CSS/JS 与 jQuery 分享插件；这支持“后端模板输出 HTML + 传统前端脚本增强”的页面模型。
4. 用户补充原站整体技术栈存在 Layui 线索。当前这份 `pdetail.html` Raw Evidence 未出现 `layui` / `lay-*` 直接引用，因此 EU-29 将 Layui 作为原站整体技术线索，而不把它写成这张详情页已经直接证实的依赖。
5. Source Discovery Run #1 摘要字段 `initialHtmlContainsVisibleTitleCandidate=false` 来自早期启发式反推标题未命中；对 `party-detail-known-internal-initial.html` 的直接检查已证明正文在初始 HTML 中，因此该字段不是页面事实。

## Collector Contract

- 批量 Collector 使用 **HTTP 服务端 HTML 为主采集源**，Browser DOM / Network Evidence 用于抽样交叉验证；
- 原站模板、jQuery、Bootstrap、Layui 等页面实现技术不迁移到新站 Runtime，Importer 只迁移模板已输出后的业务内容；
- 先从栏目列表分页文本取得 source-reported `pageCount / total`；
- 遍历全部 source scope，逐页保存 Raw HTML；
- 仅从内容区 `ul.default-list > li.list-item > a.list-item-a` 提取记录，不把全站导航链接混入栏目内容；
- `/pdetail.html` / `/detail.html` + `content_id` 归为 INTERNAL；其他列表目标归为 EXTERNAL_LINK；
- INTERNAL 从 `.detail-content-title / .detail-content-title-tips / .rich-text-wrap` 提取结构化正文；
- 列表和详情同时存在的标题/日期必须 reconciliation；冲突进入 unresolved，不静默覆盖；
- 正文资源按真实媒体字节计算 SHA-256，保存 Snapshot，再把正文引用改写为 migration token；
- 采集结束必须用 source-reported total 与 parsed/unique count 对账。
