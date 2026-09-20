# HUI-URL-BASE-REFACTOR：慧就业地址公共部分抽取

状态：`READY TO INTEGRATE`

## 目标与范围

根据当前用户指令，仅修改 `frontend/public-site/src/sites/main/integrations/huiEmployment.ts`：把所有 `url` 共同使用的慧就业站点根地址抽取为常量，并进一步抽取 `HUI_EMPLOYMENT_PAGE_TARGETS` 共用的 `/moreActivities/2685` 页面路径，通过模板字符串拼接现有业务路径。

本执行单元不改变任何最终 URL、业务编号、renderer identity、iframe 行为或用户可见结果，也不调整测试中的独立预期值。

## 就绪结论

`readiness-check = PASS`。

- 当前分支基于 `main`，工作树在进入执行前无未提交变更；远程仓库没有 Open PR，与当前定位器 `NONE` 一致。
- 用户指令、慧就业集成技术契约与目标源码足以确定范围，不存在产品或架构决策缺口。
- 实现责任限定为两个常量和九处模板字符串；验证责任为最终 URL 静态复核、Public Frontend 构建与最终差异检查。

## 验收义务

- `url` 的公共站点根地址只有一个源码定义。
- `HUI_EMPLOYMENT_PAGE_TARGETS` 共用的页面基础路径只有一个源码定义。
- 九个目标地址均通过模板字符串拼接，求值结果与变更前完全一致。
- Public Frontend 构建通过，最终差异不包含无关修改。

## 当前验证证据

- `npm run build`：`PASS`，覆盖 Public 源码边界检查、`vue-tsc --noEmit` 与 Vite 生产构建。
- 最终源码差异复核：`PASS`，六个页面 URL 统一使用 `HUI_EMPLOYMENT_PAGE_BASE_URL`，原有 `/4/22`、`/4/23`、`/4/24`、`/4/25`、`/4/30` 与 `/5/27` 业务路径后缀保持不变。
- `node scripts/verify-docs-governance.mjs` 与 `git diff --check`：`PASS`。
