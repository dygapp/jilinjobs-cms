# Historical Content Migration Workspace

`data-migrations/` 承担历史运营内容迁移资产，与 Flyway、`site-baseline` 和运行时数据库保持职责分离。

## 边界

- Flyway：稳定数据库结构、预置栏目/列表容器等结构基线。
- `site-baseline/static/**`：稳定工程视觉资源。
- `data-migrations/**`：历史文章、外链、正文资源、附件、运营列表成员的采集证据、标准化快照、导入工具和迁移报告。
- CMS Runtime Database：迁移后的运行时数据，不作为历史迁移源文件。

本目录按未来可独立迁出仓库的方式组织；当前 EU-29 仍与 `jilinjobs-cms` 同仓，以便 CMS Schema、Importer 与 Runtime Verification 在同一 CI 中闭环。

## 数据流

```text
Legacy Website / Export / API
        ↓
Raw Evidence
        ↓
Normalized Migration Snapshot
        ↓
Validator / Reconciliation
        ↓
Importer
        ↓
Column / Article / Resource / CmsList
```

外部原站只用于显式 Collect / Discovery；稳定 CI 应优先验证已固化 Snapshot，不能让普通回归测试依赖原站实时可用性。

## 目录约定

```text
data-migrations/
├── README.md
├── schemas/
│   └── article.schema.json
└── party/
    └── v1/
        └── manifest.json
```

后续生成的正式 Party Snapshot 采用 NDJSON + 二进制资源：

```text
party/v1/
├── manifest.json
├── articles.ndjson
├── resources.ndjson
├── carousel.json
├── assets/
└── reports/
```

`articles.ndjson` 一行一篇文章，便于流式处理、局部失败报告和后续全站规模扩展。正文保持 HTML，不为了迁移机械拆成段落模型。

## 追溯与幂等原则

每条内容至少保留：

- `sourceSystem`；
- legacy `content_id / typeCode / detail path`；
- 原始 URL；
- 目标栏目 alias 与 Article Type；
- `sourceFingerprint`；
- 可可靠取得的资源 URL、媒体类型、大小与 SHA-256。

Importer 必须先判断 legacy identity + fingerprint，再创建 Runtime 数据：

- 不存在：创建并记录映射；
- 已存在且 fingerprint 相同：跳过；
- 已存在但 fingerprint 不同：作为冲突停止该条自动覆盖并进入报告。

不根据缺失字段猜值；例如原站没有发布日期时保留 `null`。

## EU-29 采集顺序

1. Source Discovery：比较 HTTP 初始 HTML、Browser 最终 DOM、XHR/fetch 与资源请求；
2. 优先使用已证明可靠的结构化数据源；
3. 若无结构化内容 API，以服务端 HTML 为主采集源，Browser DOM/Network 作为校验；
4. 遍历四个 Party 栏目的完整分页边界，不把首页 Top-N 或单页展示数当作内容全集；
5. 输出 discovered / internal / external / unresolved reconciliation；
6. 标准化 Snapshot 固化后，再执行 Fresh DB Import + 二次 Import 幂等验证 + Runtime Browser Review。
