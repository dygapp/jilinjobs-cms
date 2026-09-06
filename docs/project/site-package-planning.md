# CMS Core / Site Package / Public Renderer 规划状态

## 当前结论

在 Issue #60 / E1～E3 主站正式内容工作前，先完成 Issue #77 的 CMS Core / JilinJobs Site Package / Historical Migration / Replaceable Public Renderer 四层边界收敛。

该前置调整的核心原因：当前 EU-36 已完成 Public source responsibility isolation，但 `V2__current_preset_data.sql` 仍将大量吉林就业站点实例数据与 CMS Core baseline 绑定；如果直接进入 E1～E3，会继续扩大该绑定并增加后续迁移成本。

## 当前阶段要做

1. 以 `docs/requirements/cms-site-package-boundary.md` 固化产品 / 架构意图；
2. 以 `docs/specifications/cms-site-package-boundary.md` 固化四层稳定 contract；
3. 以 `docs/technical/cms-site-package-boundary.md` 固化 Site Package / Provisioning 实施方向与 planning slices；
4. 集成上述 Planning Authority 后执行 `slice-work → readiness-check`，逐个形成可验证 Candidate Execution Unit；
5. 完成 Site Package 收敛后重新进入 Issue #60 / E1～E3。

## 暂不做

- 不选择或重写 Public Frontend 技术栈；
- 不拆 Git Repository；
- 不引入 Git Submodule；
- 不做 Docs / Code 分仓；
- 不实现多 Repository Workspace composition；
- 不为形式统一进行全仓目录重排；
- 不把 Site Definition 并入 `data-migrations/**`；
- 不提前执行 E1 / E2 / E3。

## 后续独立评估

Site Package 边界形成并取得真实集成证据后，再单独执行 Repository Split Readiness Assessment。届时再判断 Public Renderer、Site Package、Docs / Project Authority 是否适合独立 Repository，以及 exact commit composition、cross-repo contract、Fresh Context 和 CI Evidence 应如何治理。

该评估当前不是 Execution Unit，也不预设最终一定拆仓。
