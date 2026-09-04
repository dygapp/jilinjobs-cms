# EU-30 — EU-29 → EU-30 Migration Upgrade Verification

## Purpose

验证 EU-30 对党建历史数据的增量修订不仅能在 Fresh DB 上导入，还能够从已经完成 EU-29 canonical 导入的 Runtime 数据库原地升级，重点覆盖 Party carousel position 2 从 `LINK` 转为 `ARTICLE` 时的动态 Article / Resource ID 解析。

## Verification workflow

- Workflow: `.github/workflows/eu30-migration-upgrade-verify.yml`
- Run: `33893308133` / #1
- Head: `34c21795775e6edf4771d887868b412e3ac25331`
- Result: PASS
- Evidence Artifact: `eu30-migration-upgrade-verification`
- Artifact ID: `9944806491`
- SHA-256: `d932a95fa88475332aeaeea97e9a246a21068c33b5cb59f01ea4b8f74d64d20d`

## Scenario

1. 从 `main` 取得 EU-29 已接受 canonical dataset；
2. 在同一 Fresh Runtime DB 中导入 EU-29：181 篇文章 + 4 条旧 LINK 轮播项；
3. 记录 position 2 的 Runtime identity；
4. 在同一数据库上导入当前 EU-30 candidate dataset；
5. 验证新增“主题教育”2 篇文章，原 181 篇保持映射；
6. 验证 position 2 原地由 LINK 更新为 ARTICLE；
7. 验证 article legacy identity 与 carousel image SHA 被解析成动态 Runtime IDs；
8. 再次执行当前 EU-30 import，验证升级后的幂等性。

## Observed result

### EU-29 baseline

- Article import: `total=181`, `created=181`, `skipped=0`, `conflicts=0`, `invalid=0`；
- Carousel import: `total=4`, `created=4`, `updated=0`；
- Position 2:
  - `list_item_id=8`；
  - `source_type=LINK`；
  - `article_id=NULL`；
  - `image_path=/static/migrated/party/carousel/...`；
  - `image_resource_id=NULL`；
  - image SHA-256 = `a00db48e094e24b778374ae621b6f150e235625c62f17efc860391223fac830b`。

### EU-30 upgrade on the same database

- Article import: `total=183`, `created=2`, `skipped=181`, `conflicts=0`, `invalid=0`；
- `zhutijiaoyu:content:154659859759104` resolved to Runtime `article_id=183`；
- Carousel import: `total=4`, `created=0`, `updated=1`, `skipped=3`, `conflicts=0`, `invalid=0`；
- Position 2:
  - remains `list_item_id=8`；
  - `source_type=ARTICLE`；
  - `article_id=183`；
  - article legacy mapping = `zhutijiaoyu:content:154659859759104`；
  - `image_path=NULL`；
  - `image_resource_id=210`；
  - Runtime Resource bytes SHA-256 remains `a00db48e094e24b778374ae621b6f150e235625c62f17efc860391223fac830b`。

### Second EU-30 import

- Articles: `created=0`, `skipped=183`, no conflict/invalid；
- Carousel: `created=0`, `updated=0`, `skipped=4`, no conflict/invalid。

## Conclusion

Party carousel position 2 的 `LINK → ARTICLE` 动态 ID 转换、Resource 落库、原列表项 identity 保留以及升级后幂等性均已得到独立 Runtime Evidence，未发现迁移实现缺陷。

Review Environment 当前在名为“导入 Party canonical 历史数据”的 step 失败，但实际退出发生在 Importer 执行前的旧 manifest `jq` guard；因此该失败应归类为 stale Review Workflow verification contract，而不是 position 2 migration failure。Review Workflow 后续仍存在“全部轮播项都必须使用 static imagePath”等旧 EU-29 断言，需要按当前 `LINK / ARTICLE` 混合模型修订。
