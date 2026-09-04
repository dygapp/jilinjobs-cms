# Historical Content Migration Workspace

`data-migrations/` 承担历史运营内容迁移资产，与 Flyway、`site-baseline` 和运行时数据库保持职责分离。

本 README 是 Consumer Repository 内历史内容迁移数据组织的长期约定。Party / EU-29 是首个落地场景；后续主站历史文章、外链、运营列表等迁移应优先沿用本约定，而不是为每个站点重新设计一套 Snapshot 结构。

## 1. 边界

- Flyway：稳定数据库结构、预置栏目 / 列表容器等结构基线。
- `site-baseline/static/**`：稳定工程视觉资源。
- `data-migrations/**`：历史文章、外链、正文资源、附件、运营列表成员的采集证据、标准化迁移数据、导入工具和迁移报告。
- CMS Runtime Database：迁移后的运行时数据，不作为历史迁移源文件。

本目录按未来可独立迁出仓库的方式组织；迁移数据仍与 `jilinjobs-cms` 同仓，以便 CMS Schema、Importer 与 Runtime Verification 在同一 CI 中闭环。

## 2. 数据流

```text
Legacy Website / Export / API
        ↓
Raw Evidence
        ↓
Canonical Migration Dataset
        ↓
Validator / Reconciliation
        ↓
Importer
        ↓
Column / Article / Resource / CmsList
```

外部原站只用于显式 Collect / Discovery；稳定 CI 应验证已固化的 Canonical Migration Dataset，不能让普通回归测试依赖原站实时可用性。

GitHub Actions Artifact、ZIP、tar 等只允许作为采集、传输或临时评审容器，不作为长期权威数据结构。

## 3. Canonical Dataset 原则

历史迁移数据在仓库中长期只维护一套 **Canonical Migration Dataset**。

不长期维护：

```text
baseline.zip
increment-01.zip
increment-02.zip
```

也不把每轮采集结果作为一个新的完整 Snapshot 目录永久叠加。

增量由 Git 本身表达：

- 新增内容：新增对应迁移单元并更新索引；
- 内容变化：更新对应迁移单元并更新 fingerprint；
- 资源变化：只新增或替换该迁移单元实际引用的资源；
- Git commit / diff 记录每次迁移数据变化。

因此“基线”和“增量”是采集 / 校验过程中的概念，不是两套永久存储形态。

## 4. 文章级自包含迁移单元

文章是历史内容迁移的最小可独立管理、校验和导入单元。

推荐结构：

```text
data-migrations/
├── README.md
├── schemas/
│   └── article.schema.json
└── <site>/
    └── v1/
        ├── manifest.json
        ├── index.ndjson
        ├── articles/
        │   ├── <stable-id-1>/
        │   │   ├── article.json
        │   │   └── assets/
        │   │       ├── <sha256>.jpg
        │   │       └── <sha256>.png
        │   └── <stable-id-2>/
        │       ├── article.json
        │       └── assets/
        ├── lists/
        │   └── <list-code>/
        │       ├── index.json
        │       └── items/
        └── reports/
```

其中：

- `<site>` 可以是 `party`、后续主站对应 scope 等；
- `<stable-id>` 来自稳定 migration identity，不依赖运行时数据库 ID；
- 每篇文章一个目录；
- `article.json` 保存文章标准化数据、来源、fingerprint 和资源清单；
- `assets/` 只保存该文章实际引用的正文图片 / 附件等资源；
- 文章目录应能够脱离原站独立校验和导入。

单篇文章使用 `.json`，而不是单行 `.ndjson`。NDJSON 保留给需要逐行维护和扫描的全局索引。

## 5. `index.ndjson` 职责

`index.ndjson` 是轻量迁移目录，不承载文章正文。

每条索引至少应能够定位：

- stable migration identity / `legacyKey`；
- 文章目录；
- legacy `content_id / typeCode / detail path`（若存在）；
- 原始 URL；
- 目标栏目 alias；
- Article Type；
- `sourceFingerprint`；
- 当前 source observation 状态；
- `firstSeenAt / lastSeenAt` 等必要追踪信息。

Collector 后续增量扫描应优先通过 identity + fingerprint 比较得到：

```text
NEW
UNCHANGED
CHANGED
MISSING_FROM_SOURCE
```

只有 `NEW / CHANGED` 才需要重新抓取详情和资源。

`MISSING_FROM_SOURCE` 不等于删除。原站暂时不可访问、内容下架和真实删除是不同事实；在证据不足时应保留迁移单元并记录 source observation 状态，不静默执行 `git rm`。

## 6. 文章资源组织

文章引用的图片、附件等资源跟随文章目录管理，不再为全部历史文章建立一个持续增长的统一资源 ZIP。

示例：

```text
articles/gzdt-content-154659859759104/
├── article.json
└── assets/
    ├── 7c3f...jpg
    └── b8a1...pdf
```

`article.json` 中的每个资源至少保留：

- resource role（如 `BODY_IMAGE`、`ATTACHMENT`）；
- 原始 URL；
- migration-relative path；
- MIME / content type；
- size；
- SHA-256。

正文中的标准化资源引用应使用迁移单元内部相对路径，例如：

```html
<img src="assets/7c3f....jpg">
```

不得让 Canonical Dataset 依赖原站 URL 或未来 CMS Runtime URL 才能恢复正文资源。

Importer 负责：

1. 读取 `article.json`；
2. 校验本地资源字节、大小和 SHA-256；
3. 写入 CMS Resource / Static Resource；
4. 把 migration-relative reference 改写为运行时公开 URL；
5. 再创建或核对文章 Runtime 数据。

完全相同的资源可以在不同文章目录中以相同 SHA 文件名出现；是否进一步做跨文章物理去重属于存储优化，不应破坏文章级自包含边界。

## 7. 运营列表 / 轮播

轮播和其他运营列表成员也应采用与文章类似的可独立管理单元，而不是长期依赖一个整包资源目录。

推荐结构：

```text
lists/PARTY_CAROUSEL/
├── index.json
└── items/
    └── <stable-id>/
        ├── item.json
        └── assets/
            └── <sha256>.jpg
```

每个列表项保存自身标题、目标 URL、open mode、排序、legacy identity、fingerprint 与实际引用资源。

若某个列表项未来建立到迁移文章的 canonical 关系，应通过稳定 identity 表达，不通过临时 Runtime 数据库 ID 建立长期迁移依赖。

## 8. 追溯与幂等原则

每条迁移内容至少保留：

- `sourceSystem`；
- stable migration identity / legacy identity；
- 原始 URL；
- 可取得的 legacy `content_id / typeCode / detail path`；
- 目标栏目 alias / list code；
- Article Type / item type；
- `sourceFingerprint`；
- 可可靠取得的资源 URL、媒体类型、大小与 SHA-256。

Importer 必须先判断 legacy identity + fingerprint，再创建 Runtime 数据：

- 不存在：创建并记录映射；
- 已存在且 fingerprint 相同：跳过；
- 已存在但 fingerprint 不同：作为冲突停止该条自动覆盖并进入报告。

不根据缺失字段猜值；例如原站没有发布日期时保留 `null`。

## 9. Collect / Review / CI 边界

### Collect Mode

允许联系原站，输出 Raw Evidence 和 Canonical Dataset 的候选变化。

### Review Mode

必须基于冻结的候选数据进行 Fresh DB Import、Reconciliation 和 Runtime Browser Review。ZIP 可以作为 Actions Artifact 的运输容器，但评审对象是解包后的逻辑数据集。

### Stable CI

不得依赖原站在线。应从仓库中的 Canonical Dataset 恢复 Fresh Runtime，验证：

- Schema / format；
- 资源完整性和 SHA-256；
- Importer 首次导入；
- 二次导入幂等；
- reconciliation；
- Runtime Browser 行为。

## 10. Party / EU-29 兼容说明

EU-29 初始候选 Snapshot 曾采用：

```text
articles.ndjson
resources.ndjson
carousel.json
assets/
```

并通过 GitHub Actions Artifact ZIP 在 Review Workflow 中传输。这是当前实验阶段形成的候选格式，不定义后续长期存储规范。

EU-29 正式长期资产收口时，应迁移为本 README 定义的 Canonical Dataset：

```text
party/v1/
├── manifest.json
├── index.ndjson
├── articles/<stable-id>/article.json
├── articles/<stable-id>/assets/**
├── lists/PARTY_CAROUSEL/**
└── reports/**
```

迁移格式变化不得改变已经冻结的人工作证据含义：原始 legacy identity、来源 URL、正文、资源字节、SHA-256、文章数量、列表项顺序等必须可对账。

## 11. 后续主站迁移要求

后续主站历史内容迁移应默认复用本 README：

1. 先建立 Main-site scope 的 `manifest.json + index.ndjson`；
2. 每篇文章采用独立 self-contained migration unit；
3. 正文图片 / 附件跟随文章目录；
4. 新增 / 修改内容直接更新 Canonical Dataset，不创建新的完整 ZIP 基线；
5. 统一使用 stable identity + fingerprint 做增量判定；
6. Collect、Import、Reconciliation、Runtime Verification 沿用同一数据契约；
7. 只有出现本约定无法覆盖的新内容类型时，才扩展本 README / schema，不为单个迁移阶段另起一套临时规则。

## 12. Source Discovery 基本顺序

1. 比较 HTTP 初始 HTML、Browser 最终 DOM、XHR / fetch 与资源请求；
2. 优先使用已证明可靠的结构化数据源；
3. 若无结构化内容 API，以服务端 HTML 为主采集源，Browser DOM / Network Evidence 作为校验；
4. 遍历目标栏目的完整分页边界，不把首页 Top-N 或单页展示数当作内容全集；
5. 输出 discovered / internal / external / unresolved reconciliation；
6. Canonical Dataset 候选冻结后，再执行 Fresh DB Import + 二次 Import 幂等验证 + Runtime Browser Review。
