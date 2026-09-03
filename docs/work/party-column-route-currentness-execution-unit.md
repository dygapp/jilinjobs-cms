# Party Column Route Currentness Execution Unit

## Parent

- Roadmap Stage：中心党建正式页面与内容收敛
- Parent Unit：EU-29 Party Historical Content Migration & Final Review

## Authority

- `docs/requirements/information-publishing.md`：Party canonical route、栏目作用域和历史内容迁移边界；
- `docs/specifications/party.md` §5.2：四个真实党建栏目必须支持作用域分页、直接访问和刷新；
- `docs/work/party-convergence-execution-units.md` EU-29：迁入真实历史内容后必须完成 Party 入口、栏目、详情及 Main/Admin 回归；
- `docs/project/development-method.md`：Implementation Minimality、Surgical Diff Scope、Vue 3 + TypeScript Profile 与 Verification Profile；
- `docs/technical/verification-strategy.md`：Router/watcher 异步 UI 的 currentness 与 Browser Evidence。

## Observed Gap

`PartyColumnView.vue` 使用 route alias / page / size 的 `watch` 触发异步 `getPartyColumn()` 与 `listPublicArticles()`。当前实现没有使旧 watcher 工作失效：用户在分页请求尚未完成时通过浏览器历史返回到前一页，较慢的旧响应仍可在新路由状态完成后覆盖 `columnName / articles / total / page / loading / metadata`。

真实历史内容迁入后，栏目分页是 EU-29 最终回归的一部分；因此该问题属于当前 Parent Unit 的实际前端收敛责任，不是为了 Capability Adoption 人工创造的新 Feature。

## Goal

当 Party 栏目 route/query 在前一轮异步加载完成前发生变化时，只有当前 watcher 对应的请求可以提交可见状态和页面 metadata；旧请求完成后不得覆盖当前路由状态。

## Scope

- `frontend/public-site/src/sites/party/modules/content/PartyColumnView.vue`
- 一个针对该 race 的 Playwright regression test
- 与本 Unit 直接相关的 Consumer Authority / Evidence

## Non-goals

- 不建立通用请求取消框架、Composable、全局 request registry 或新依赖；
- 不修改 shared Article / Column API contract；
- 不扩展到未建立当前失败证据的相邻组件；
- 不实现 legacy URL redirect；
- 不实现 EU-29 历史内容采集/迁移机制本身；
- 不改变页面视觉、布局或产品行为口径。

## Implementation Plan

1. 先用 Browser E2E 受控延迟第二页响应，建立“page=1 较慢 → 浏览器返回 page=0 → 旧响应最后完成”的失败场景；
2. 使用 Vue watcher 自身 cleanup/currentness 语义使过期异步工作失效；
3. 只在 current watcher 下提交成功、失败、loading 与 metadata 状态；
4. 不为这一处问题抽取共享 abstraction；
5. 执行 Vue-aware type-check + Vite build、目标 Browser E2E、完整 CI，并做 Final Diff Scope Check。

## Acceptance

- 受控 race 中 URL 返回 page=0 后，较慢的 page=1 响应最终完成也不会覆盖 page=0 的文章与分页状态；
- 正常分页、直接访问和刷新行为保持；
- Public Site `npm run build` 通过，其中 `vue-tsc --noEmit` 与 `vite build` 均成功；
- 目标 Playwright regression 与完整当前 CI 通过；
- 无新的依赖、共享抽象、配置项、扩展点或与本 Unit 无关的 cleanup；
- 因无视觉 DOM/CSS 变化，本 Unit 不新增 Visual/Human Review 要求；EU-29 最终 Human Review 仍按 Parent Unit 原 Authority 执行。