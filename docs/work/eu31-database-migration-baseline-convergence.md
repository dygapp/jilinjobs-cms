# EU-31 数据库迁移基线收敛

## 分配说明

EU-31 在 EU-30 完成后的规划与 Readiness Review 后重新分配，不继承历史未使用的 EU 编号草案。

## 关联规划

- Issue #59：EU-30 后续规划候选
- Issue #60：EU-30 收口后的下一执行切片评估

## 权威输入

- `docs/requirements/database-migration-baseline-convergence.md`
- `docs/specifications/database-migration-baseline-convergence.md`
- `docs/technical/database-migration-baseline-convergence.md`
- `docs/technical/verification-strategy.md`
- `data-migrations/party/v1/**`

## 目标

将 V1～V20 开发阶段 Flyway 增量迁移历史收敛为当前正式 Schema 与当前预置数据基线，使 Fresh DB 可以直接得到 EU-30 后正式模型，同时保留历史 Party 数据迁移能力。

## 范围

- 当前有效 Flyway migration；
- migration baseline 相关 Authority 文档；
- Fresh DB 与 canonical migration 验证。

## 非目标

- 产品功能变化；
- 前端/UI调整；
- Party canonical 数据修改；
- importer 语义修改；
- 生产环境原位升级方案设计。

## 实施切片

1. 使用当前正式 Schema 和 preset 数据替代开发期 Flyway transcript。
2. 验证 Backend/Flyway Fresh DB 启动。
3. 验证当前 canonical Runtime Dataset、Party carousel 导入以及幂等能力。
4. 执行 CI 与最终 Diff Scope Review。

## 验证证据

- Canonical Migration Verification：通过。
- EU-30 Migration Upgrade Verification：通过。
- CI：通过。
- PR 文件范围检查通过，未修改 Party migration corpus、importer 或产品行为代码。

## 验收标准

- Fresh MySQL 数据库直接包含当前正式 Schema。
- 不保留已经废弃的开发期结构。
- 历史 Party migration corpus、stable identity、fingerprint、legacy mapping 和 importer 能力保持。
- EU-29 → EU-30 migration-only compatibility 独立验证通过。
- PR 不包含无关产品修改。

## 文档规范说明

本项目 Authority、Specification、Execution Unit 等长期维护文档统一使用中文编写。技术专有名词（如 Flyway、Fresh DB、Canonical Migration Verification）保留英文名称。
