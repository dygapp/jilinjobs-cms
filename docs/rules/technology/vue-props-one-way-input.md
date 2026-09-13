---
id: rule:vue-props-one-way-input
type: rule
status: active
scope:
  phases: [execute]
  activities: [implementation]
  technologies: [vue3]
  artifacts: [vue-sfc, code]
  risks: []
---

# Vue Props 保持单向输入

Props 是父组件传入的只读输入。组件不应直接修改 prop；需要可变本地状态时建立明确的本地状态并定义同步语义，需要双向模型时使用当前项目已经接受的 emit / model contract。

不得通过类型断言、对象别名或深层突变绕过单向数据流约束。
