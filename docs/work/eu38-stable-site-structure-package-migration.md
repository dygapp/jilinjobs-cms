# EU-38 — Stable Site Structure Package Migration

## 1. Identity

- Identifier：`EU-38`
- Source：GitHub Issue #77 / Slice B current audit
- Requirement：`docs/requirements/cms-site-package-boundary.md`
- Specification：`docs/specifications/cms-site-package-boundary.md`
- Technical Plan：`docs/technical/cms-site-package-boundary.md`
- Execute baseline：`main@2b2d924a16c2f9df5f781b0a794a17c4f84d6b88`
- Baseline Post-Integration CI：#741 / run `34019950248`，Backend / Public / Admin / Integrated Browser 全部 PASS
- Implementation PR：#81
- Status：**CONVERGING — final exact-head verification pending**

## 2. Intent

把当前 V2 中具有既有稳定 identity 的 JilinJobs preset 结构正式表达为 Site Package 数据，并证明：

- Fresh V1 Generic Schema + Site Package 可以恢复同一稳定结构；
- Legacy V1+V2 Database 上重复 apply 不产生结构漂移；
- operator-created `preset=false` 数据不被接管；
- 现有 V2、NavigationItem 与运营成员保持不变。

## 3. Included stable domains

本 Unit 只迁移已存在稳定 identity 的结构域：

1. Column → `alias`；
2. PageGroup → `alias`；
3. Page → `groupAlias + alias`；
4. NavigationLocation → `code`；
5. SiteConfig → `config_key`；
6. CmsList definition → `code`；
7. AdvertisementSlot → `code`。

## 4. Explicit non-goals

- 不删除、改写或迁空 `V2__current_preset_data.sql`；
- 不接管 `cms_navigation` / NavigationItem；
- 不把 `cms_list_item` 迁入 Site Package；
- 不把 `cms_advertisement` 迁入 Site Package；
- 不移动 `site-baseline/static/**`；
- 不改变 Public Renderer、Admin 产品行为、Gateway 或 canonical routes；
- 不处理 Party canonical migration compatibility；
- 不进入 Issue #60 / E1～E3；
- 不拆 Repository，不引入 plugin / tenant / multi-site framework。

## 5. Slice rationale

Slice B current audit 证明完整 V2 不能安全作为一个执行单元整体迁移：

- 上述七类结构具有现有 stable identity，可在不改 schema 的前提下独立 reconcile；
- `cms_navigation` 当前没有独立 stable code，必须后续单独解决 logical identity；
- `cms_list_item` 与 `cms_advertisement` 是当前运营成员，不属于本 Unit 的 stable preset structure；
- 因此 EU-38 先完成可证明、可回滚的 stable structure ownership migration，避免把 Navigation identity 与 Flyway baseline responsibility 混入同一 Diff。

## 6. Acceptance obligations

- `sites/jilinjobs/manifest.json` 显式声明所有 included structure files 与 SHA-256；
- 每个 included structure domain 都有机器可审计 JSON Schema；
- loader 校验 path / digest / duplicate / field / preset contract；
- provisioner 对七类 stable domain 支持 create / reconcile / unchanged；
- existing `preset=false` 同 identity 冲突必须拒绝；
- Fresh V1-only DB first apply 创建当前正式 stable structure；
- second apply 全部 unchanged；
- representative preset update 可以 reconcile，并可由正式 package 恢复；
- Legacy V1+V2 DB apply 后 structural snapshot 与 Fresh V1-only + Site Package 等价；
- Legacy Navigation / ListItem / Advertisement operational snapshot 不被改写；
- `V2__current_preset_data.sql` 保持 byte-for-byte 未修改；
- EU-37 Foundation regression PASS；
- Backend full test/build PASS；
- Public/Admin build 与 Integrated Browser regression PASS；
- exact-head verification PASS 后才能进入 Ready to Integrate；
- 合并后 `main` Post-Integration Verification PASS 后才能声明 COMPLETED。

## 7. Verification path

专项 MySQL verifier：

```text
Fresh path
Flyway V1
→ apply JilinJobs Site Package
→ second apply
→ representative reconcile / restore
→ structural snapshot

Legacy path
Flyway V1 + V2
→ capture operational members
→ apply JilinJobs Site Package
→ second apply
→ compare structural snapshot with Fresh path
→ prove operational members unchanged
```

Repository regression 继续由现有 CI 承担；Site Package 专项验证由 `.github/workflows/site-package-verification.yml` 承担，并只在 Site Package / provisioning / migration 相关路径变化时触发。

## 8. Execute / Convergence Evidence

PR #81 首轮 Head `262ffc60a7b50cc126aee40c21e7e49c6dcefa31` 已取得 Current Evidence：

- Site Package Verification #1 / run `34022487759`：PASS；
  - EU-37 Foundation regression：PASS；
  - EU-38 `verifyStableSiteStructure`：PASS；
  - Fresh V1-only first apply 创建 58 个 stable preset objects；
  - second apply 全部 unchanged；
  - representative SiteConfig reconcile / restore：PASS；
  - `preset=false` ownership conflict：PASS；
  - Legacy V1+V2 apply / second apply：PASS；
  - Fresh 与 Legacy structural snapshot 等价；
  - legacy Navigation / ListItem / Advertisement operational snapshot 保持不变。
- Repository CI #742 / run `34022487697`：PASS；
  - Backend full test / bootJar：PASS；
  - EU-37 Foundation verifier：PASS；
  - Public build：PASS；
  - Admin build：PASS；
  - Integrated Public Browser：PASS；
  - Integrated Admin Browser：PASS。
- Final changed-file audit 未发现 `V2__current_preset_data.sql`、NavigationItem、ListItem、Advertisement、Public/Admin 产品源码或其他 Slice B/C/D 越界修改。

随后仅对专项 workflow 的触发 paths 与本 Work Evidence 做收口性调整，因此 Head 已改变；上述 Evidence 不能替代最终 exact-head verification。

## 9. Stop / return-to-planning conditions

若实现必须：

- 给 NavigationItem 新增数据库 stable identity 字段；
- 删除 / 重写 V2；
- 把运营成员变成 preset structure；
- 修改 Public/Admin 产品行为；
- 搬迁全部静态资源；
- 引入 multi-site / plugin framework；

则停止扩大 EU-38，返回 Issue #77 Planning。
