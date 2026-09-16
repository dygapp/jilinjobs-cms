# Party Canonical Migration 运行组合边界

Party Canonical Migration 继续遵循仓库根 `data-migrations/README.md` 的 Historical Content Migration 边界；本文件只补充 JilinJobs Consumer 当前长期稳定的 Party canonical import 组合关系，不维护具体 Backend task、class、环境变量或 active schema migration 文件清单。

## 运行准备

当前正式组合顺序为：

```text
Generic schema ready
→ JilinJobs Site Definition reconcile
→ Party Canonical Migration import
```

Party Canonical Dataset 只引用稳定 Column alias、List code 等 Site Definition identity，不接管 Site structure definition、one-time bootstrap 或 stable Site asset ownership。

Generic schema evolution 与 JilinJobs instance data 必须保持分离；实际 active migration corpus 由当前 Backend Repository implementation 自己拥有并恢复，不在本 README 建立第二份 migration inventory。

具体 canonical import invocation、application wiring、Site Package root 注入方式与 implementation class / task 名同样由当前 Repository implementation 持有。替换 Backend technology 时，只要继续满足上述稳定组合顺序、Site Definition ownership 与 canonical migration contract，就不要求复制现有 Spring / Gradle wiring。

Canonical Migration Verification、migration upgrade verification 与 Review Runtime 如需执行 Party import，必须复用同一 ownership 顺序，不另建第二套 provisioning 逻辑。Fresh Site 的普通运营默认数据如需建立，由独立 one-time Site bootstrap lifecycle 负责；Canonical historical import 本身不依赖或接管该 bootstrap。

Party 具体 dataset、stable source identity、fingerprint、compatibility、provenance、Article / ListItem historical scope 与 accepted records 继续由 `v1/**` canonical assets 持有；本 README 不复制其数量、digest 或 current record inventory。
