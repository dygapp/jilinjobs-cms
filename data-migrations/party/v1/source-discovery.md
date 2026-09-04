# EU-29 Party Source Discovery

## Evidence

- GitHub Actions workflow: `EU-29 Party Source Discovery` Run #1 / Run ID `33828006870`;
- Head: `5752f21dde59ec1f379483965d8f4ed2b1303477`;
- 原站：`https://24365.jl.smartedu.cn`；
- 对象：四个 `/plist.html?typeCode=...` 列表与已知 `/pdetail.html?content_id=278556458369024&typeCode=gzdt` 详情。

## 结论

1. 四个列表页均在初始 Document HTML 中直接存在栏目内容和分页信息；未观察到用于内容装配的 XHR / fetch。
2. 已知 `pdetail.html` 详情页的初始 Document HTML 已直接包含 `.detail-content-title`、`.detail-content-title-tips` 与 `.rich-text-wrap`，其中包括标题、信息来源、发布日期、正文和正文图片。
3. Source Discovery Run #1 的摘要字段 `initialHtmlContainsVisibleTitleCandidate=false` 来自测试中“从全页 bodyText 反推标题”的启发式未命中，不能解释为正文不存在于初始 HTML；对上传的 `party-detail-known-internal-initial.html` 直接检查后已纠正该判断。
4. 当前没有证据证明存在可复用的公开 JSON 内容 API；因此 EU-29 批量 Collector 采用 **HTTP 服务端 HTML 为主采集源**，Browser DOM / Network Evidence 作为抽样交叉验证，而不是为每篇文章启动完整浏览器渲染。

## Collector Contract

- 先从栏目列表分页文本取得 source-reported `pageCount / total`；
- 遍历全部 source scope，逐页保存 Raw HTML；
- 仅从内容区 `ul.default-list > li.list-item > a.list-item-a` 提取记录，不把全站导航链接混入栏目内容；
- `/pdetail.html` / `/detail.html` + `content_id` 归为 INTERNAL；其他列表目标归为 EXTERNAL_LINK；
- INTERNAL 再从 `.detail-content-title / .detail-content-title-tips / .rich-text-wrap` 提取结构化正文；
- 列表和详情同时存在的标题/日期必须 reconciliation；冲突进入 unresolved，不静默覆盖；
- 正文资源按真实媒体字节计算 SHA-256，保存 Snapshot，再把正文引用改写为 migration token；
- 采集结束必须用 source-reported total 与 parsed/unique count 对账。
