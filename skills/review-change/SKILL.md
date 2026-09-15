---
name: review-change
description: 对最终 Repository 变更执行独立复核，检查当前 Authority、范围、契约、适用 Rules 与证据是否一致，并输出可执行 findings 或有界通过结论；不替代实现验证、人工产品验收或 merge 授权。
metadata:
  jilinjobs-cms-id: skill:review-change
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# review-change

## 输入

- 当前目标基线与拟接受变更；
- 当前 Repository Authority；
- 精确 diff / changed files；
- 当前验证证据；
- 当前 review responsibility 下已发现并确认适用的 Rules。

## 过程

1. 重新读取当前 Authority 与最终变更，不依赖作者说明或旧 Review 结论。
2. 明确 review claim：判断当前变更是否可安全接受，而不是重新设计目标。
3. 在 review responsibility 的首个有副作用动作前完成当前 Rule Discovery；只读取并应用真实命中的 Rules。
4. 检查 Authority consistency、semantic regression、scope、授权边界、证据、artifact lifecycle 与必要的技术专项约束。
5. 记录可执行 findings，至少区分 blocking / medium / low；每项 finding 指向具体事实、影响与建议修复边界。
6. 修复后重新读取最终变更；旧 review / verification 只在能证明未受影响时复用。
7. 无未解决 blocking / medium finding 时，可以给出 bounded pass conclusion。

## 输出与退出

输出 findings 或有界通过结论，以及仍需验证 / 人工判断的剩余边界。

以下情况升级：

- Repository Authority 自身冲突；
- 需要改变 Product Goal、Scope、User-visible Behavior、重大 Architecture 或 Security / Privacy boundary；
- 需要人工承担的高影响、难逆或 Integration 决定。

Review 通过不等于 merge、release 或 deploy 授权。