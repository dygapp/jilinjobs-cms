# Site Package Contracts

`sites/` 用于承载 Site Package contract 与后续具体站点 package。

EU-37 只建立通用 v1 contract 和测试 fixture，不在本目录提前提交不完整的 JilinJobs 正式 Site Definition。正式 `sites/jilinjobs/**` 将由后续 Current Site Baseline Migration Slice 在完成 V2 全量 classification audit 后创建。

当前边界：

- `schemas/**`：Generic CMS 可消费的 Site Package 数据契约；
- `backend/src/test/resources/site-packages/**`：仅用于 Foundation verification 的非产品 fixture；
- `site-baseline/static/**`：现阶段继续保留原物理路径，但语义 ownership 已由 Issue #77 Authority 归入 JilinJobs Site Package；
- `data-migrations/**`：Historical Content Migration，不承载 Site Definition。

Site Package v1 Foundation 当前只实现 `columns` structure，用于证明 stable alias、parent logical identity、`preset`、preflight 与幂等 provisioning。其他结构类型在后续 Slice 按已接受 Technical Plan 增量加入，不通过 speculative plugin framework 预实现。
