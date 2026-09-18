---
id: rule:git-commit-governance
type: rule
status: active
scope:
  phases: []
  activities: []
  technologies: []
  artifacts: [git-commit]
  risks: []
---

# Git Commit 治理

创建、重写或准备接受 Git Commit 时，提交历史必须保持可读、可追溯并符合本 Consumer 的稳定语义。

## 提交说明（Commit Message）

默认格式：

```text
<type>(<scope>): <中文摘要>
```

必要时可以省略 Scope：

```text
<type>: <中文摘要>
```

规则：

- `type` / `scope` 使用小写英文；
- 摘要以中文主述，保留必要机器标识和技术专名；
- 摘要直接描述主要动作与对象，默认不以句号结尾；
- 普通 Commit 不要求 Body；只有摘要不足以解释关键 why、兼容边界、迁移约束或 trade-off 时才增加。

## Type

优先复用已经稳定使用的 Type：`feat`、`fix`、`refactor`、`test`、`docs`、`chore`、`ci`、`build`、`style`、`data`。只有形成新的长期责任语义时才增加 Type。

## Scope

Scope 表示稳定责任域，不表示单个文件或一次性任务编号。优先复用 `migration`、`public`、`party`、`admin`、`backend`、`config`、`resource`、`review`、`method`、`governance`、`project`、`repo` 等已有稳定域。

不要仅用 EU / Issue 编号替代真实责任域。

## 单一逻辑目的

一个 Commit 应表达一个主要逻辑目的。多个文件可以共同完成同一目的；互不相关的 Authority、实现或修复应按需要拆分。

临时执行期间可以存在中间 Commit，但 PR 进入 Review 前必须检查：

- Message 是否全部符合本 Rule；
- 临时、试探或明显过程性的 Commit 是否应重写 / squash；
- 最终历史是否仍有助于 Review 与 Fresh Context 恢复；
- 是否混入无关生成物、调试内容或未授权变化。

## 历史重写与 证据

重写 Branch history 后必须重新读取最终 Head / tree / diff。与旧 Head 绑定的 当前证据 不机械继承；是否复用由当前 证据 Claim 影响判断决定。

本 Rule 不授予 merge / release / deploy 权限。Squash merge 的最终标题仍应保持中文 Conventional Commit 语义。