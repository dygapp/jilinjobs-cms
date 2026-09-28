# 操作指南

`docs/guides/` 保存面向人的 **Consumer-local 操作指南**：帮助项目维护者、测试人员或评审人员完成明确的日常操作。

Guide 的职责是回答“怎么使用”，例如：

- 如何启动一个人工评审 / 演示环境；
- 从浏览器访问哪些入口；
- 如何恢复到稳定测试起点；
- 如何停止环境；
- 常见错误下一步该做什么。

Guide **不是新的 Authority 层**，不得重新拥有或复制：

- Product / Domain Requirement；
- Feature / surface Specification；
- Architecture / Design；
- Technical / Interface / Verification contract；
- Git / CI / Runtime 的瞬时 Evidence。

当操作步骤依赖长期技术语义时，Guide 必须指向真实 Technical owner，而不是维护第二份完整 contract。

## 当前指南

- `human-review.md` — 启动、使用、重置和停止人工评审 / 演示 / 手工测试环境；底层 Runtime contract 由 `docs/technical/human-review-runtime.md` 持有。
