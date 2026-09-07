# EU-30 — EU-29 → EU-30 Migration Upgrade Verification

## Status

**CURRENT — latest code-state upgrade verification PASS**

## Purpose

验证 EU-30 对党建历史数据的增量修订不仅能在 Fresh DB 上导入，还能够从已经完成 EU-29 canonical 导入的 Runtime 数据库原地升级，重点覆盖 Party carousel position 2 从 `LINK` 转为 `ARTICLE` 时的稳定 legacy identity、动态 Article / Resource ID 解析、原列表项 identity 保留、幂等以及意外 fingerprint 漂移拒绝。

## Verification workflow

- Workflow: `.github/workflows/eu30-migration-upgrade-verify.yml`
- Latest evidence Run: `33930893093` / #32
- Evidence Head: `821a8a34f0f6063c747e6872d6091ed29014c299`
- Result: PASS
- Evidence Artifact: `eu30-migration-upgrade-verification`
- Artifact ID: `9958531784`
- SHA-256: `6f92d2dafddbada822a4bd5ea7e4a89cc8cc4611d73f2064f8e3ad12a81ce4c9`

后继提交仅修改 EU-30 Authority / Evidence 文档时，可以按 `docs/technical/verification-strategy.md` 的 Descendant Commit Evidence Reuse 规则对本 Upgrade Claim 复用该 Run；一旦 Migration Dataset、Importer、Flyway、Upgrade Workflow、相关 Fixture 或 Runtime 配置继续变化，则必须重新取得 Upgrade Evidence。

## Pinned baseline

专项验证**不再从移动的 `main` 分支推导 EU-29**。EU-29 已接受 canonical runtime 基线固定为：

`59c855f55899cd613fdee059b27db762ffa3b092`

Workflow materialize 该 commit 中的 `data-migrations/party/v1`，并在导入前验证：

- manifest `status=accepted-canonical`；
- accepted articles = 181；
- carousel items = 4；
- `party-carousel:position:2` 仍为 LINK；
- position 2 accepted source fingerprint = `c2ad182b8b2dc981a3cbe3b0153a1e3e47604c1f01dd43e6d25971e1deed10dc`。

这避免 EU-30 合并后 `main` 前进导致“EU-29 baseline”语义漂移。

## Scenario

1. 从固定 commit `59c855f...` materialize EU-29 accepted canonical dataset；
2. 在 Fresh Runtime DB 中导入 EU-29：181 篇文章 + 4 条旧 LINK 轮播项；
3. 记录 position 2 的 Runtime list item identity、旧 fingerprint 和图片 SHA；
4. 在**同一数据库**上导入当前 EU-30 candidate dataset；
5. 验证新增主题教育 2 篇，原 181 篇保持映射；
6. 验证 position 2 原列表项 ID 不变，原地由 LINK 更新为 ARTICLE；
7. 验证 article legacy identity 与 carousel image SHA 被解析成当前 Runtime article/resource IDs；
8. 再次执行 EU-30 import，验证升级后的幂等性；
9. 人为把 position 2 mapping fingerprint 修改为一个非 accepted EU-29 / 非 EU-30 的未知值；
10. 再次执行 Carousel Importer，要求非零退出，并只把 position 2 报告为 `CONFLICT`，证明一次性兼容口不能覆盖未来未知漂移。

## Observed result

### EU-29 baseline import

- Article import: `total=181`, `created=181`, `skipped=0`, `conflicts=0`, `invalid=0`；
- Carousel import: `total=4`, `created=4`, `updated=0`, `skipped=0`, `conflicts=0`, `invalid=0`；
- Position 2:
  - Runtime list item ID 被记录用于升级后 identity 对账；
  - `source_type=LINK`；
  - `article_id=NULL`；
  - `image_path=/static/migrated/party/carousel/...`；
  - `image_resource_id=NULL`；
  - source fingerprint = `c2ad182b8b2dc981a3cbe3b0153a1e3e47604c1f01dd43e6d25971e1deed10dc`；
  - image SHA-256 = `a00db48e094e24b778374ae621b6f150e235625c62f17efc860391223fac830b`。

### EU-30 upgrade on the same database

- Article import: `total=183`, `created=2`, `skipped=181`, `conflicts=0`, `invalid=0`；
- `zhutijiaoyu:content:154659859759104` 创建并获得当前 Runtime article ID；
- Carousel import: `total=4`, `created=0`, `updated=1`, `skipped=3`, `conflicts=0`, `invalid=0`；
- Position 2:
  - list item ID 与 EU-29 导入前记录完全相同；
  - `source_type=ARTICLE`；
  - article legacy mapping = `zhutijiaoyu:content:154659859759104`；
  - `image_path=NULL`；
  - `image_resource_id` 为动态 Runtime Resource ID；
  - source fingerprint = `f8b5d8df87021373803639b174bf88e46ae6cef7f2599a205763b5887c78be84`；
  - Runtime Resource bytes SHA-256 = `a00db48e094e24b778374ae621b6f150e235625c62f17efc860391223fac830b`。

Runtime ID 只用于该 Run 内部 reconciliation，不作为 durable canonical identity 写回仓库。

### Second EU-30 import

- Articles: `total=183`, `created=0`, `skipped=183`, `conflicts=0`, `invalid=0`；
- Carousel: `total=4`, `created=0`, `updated=0`, `skipped=4`, `conflicts=0`, `invalid=0`。

### Unexpected fingerprint negative test

Workflow 将 position 2 mapping fingerprint 改为 64 位 `f`，随后再次执行当前 Carousel Importer：

- 进程必须非零退出；
- report: `total=4`, `conflicts=1`, `invalid=0`；
- `party-carousel:position:2` status = `CONFLICT`；
- 其他条目不被误报为 conflict。

因此当前兼容逻辑只允许：

```text
position 2
+ incoming sourceType=ARTICLE
+ existing fingerprint = exact accepted EU-29 fingerprint
+ incoming fingerprint = exact EU-30 correction fingerprint
→ one-time UPDATE
```

其他 fingerprint drift 不允许静默覆盖。

## Conclusion

当前 Evidence 已独立证明：

- EU-29 accepted runtime 可以在原数据库上升级到 EU-30 candidate；
- position 2 的 LINK → ARTICLE 转换保留原列表项 identity；
- Article / Resource Runtime ID 从稳定 legacy identity 和 canonical bytes 动态解析；
- 升级后再次执行完全幂等；
- 未知 fingerprint 漂移被显式拒绝，不会利用 position 2 兼容口静默覆盖。

早期文档中关于 Review Environment 在旧 manifest guard 失败的说明只属于历史 Stale Verification Contract。当前 Review Workflow 已按 EU-30 acceptedSnapshot + candidateExtension、五栏目迁移作用域和 `[LINK, ARTICLE, LINK, LINK]` Runtime 模型修订；其最终状态由最新 Head Review Environment Evidence 单独承担，不再混入本 Upgrade Verification 的结论。
