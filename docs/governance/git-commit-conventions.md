# Git Commit 规范

## 责任与来源

本文是 `jilinjobs-cms` 的 Git Commit Message **Current owner**。创建、整理或重写 Commit 前必须读取本文。

本规范参考当前 adopted exact tag 下的 `guide:git-commit-conventions`，并继承本仓库此前已经采用的 Consumer-local 约定。Provider Guide 只提供候选方法；本文一经纳入 Repository Authority 后，由本仓库自己拥有，后续 `agentic-dev` 升级不会自动覆盖或改变本规范。

历史版本可保留在 archive 中用于 provenance，但不得替代本文参与普通 Fresh Context。

## 基本格式

统一采用：

```text
<type>(<scope>): <中文摘要>
```

当没有可靠的稳定 Scope 时允许省略：

```text
<type>: <中文摘要>
```

强制规则：

- `type` 使用小写英文；
- `scope` 使用小写英文，只表示稳定责任域；
- 摘要以自然中文为主要叙述语言；
- Docker、CI、API、Git、文件名、代码标识等必要机器标识或正式名称可以保留；
- 摘要直接说明主要动作与对象，避免“更新文件”“misc fixes”一类无法恢复目的的表达；
- 默认不在摘要末尾添加句号；
- 不允许只有英文摘要。

例如：

```text
ci: 将默认验证迁移到本地 Docker CI
ci(review): 将评审证据绑定到源提交
docs(governance): 固化 Git Commit 规范
fix(public): 修正栏目切换后的菜单状态
refactor(migration): 收敛内容迁移实现边界
```

以下写法不符合本项目规范：

```text
ci: migrate default verification to local Docker
ci: bind review evidence to source subject
update docs
fix migration issue
```

## Type 类型

当前稳定 Type 为：

| Type | 用途 |
|---|---|
| `feat` | 新增用户可见能力、业务能力或明确工程能力 |
| `fix` | 修复实现、配置、迁移、验证或运行缺陷 |
| `refactor` | 重构结构或实现，预期不改变既定外部语义 |
| `test` | 新增或调整测试、fixture、验证或回归证据 |
| `docs` | 仅改变 Requirement、Specification、Technical、Governance、Roadmap 等文档 |
| `chore` | 必要仓库维护或清理，且没有更准确的 Type |
| `ci` | CI、Review Workflow、自动化验证或相关运行配置 |
| `build` | 构建系统、依赖解析、打包或产物生成 |
| `style` | 不改变行为的格式或纯视觉样式整理 |
| `data` | Consumer-owned canonical / baseline 数据变化 |

不为了与 Provider 清单保持形式一致而机械增加 Type。只有本仓库出现明确、持续的责任语义时才扩展该集合。

## Scope 范围

Scope 表示稳定责任域，不表示单个文件、临时 Branch、Issue / EU 编号、人员名或一次性实现细节。

当前可优先复用的稳定 Scope 包括：

- `migration`
- `public`
- `party`
- `admin`
- `backend`
- `config`
- `resource`
- `review`
- `method`
- `governance`
- `project`
- `repo`

确有其他长期稳定责任域时可以使用新的 Scope。跨多个范围且没有可靠主范围时，应省略 Scope，而不是使用 `misc`、`all` 或临时组合。

## 单一逻辑目的

一个 Commit 只表达一个主要逻辑目的。

为完成同一目的必须同步变化的 Authority、实现、测试、迁移和必要文档可以进入同一 Commit；无关修复、独立重构、顺手清理和全局格式化应拆开。

提交前应能回答：

1. 这个 Commit 完成或推进什么单一目的；
2. 每个 staged change 为什么属于该目的；
3. 移除其中任一变化后，目标、正确性或验证是否会不完整；
4. 是否混入可以独立理解、验证或回退的另一项变化。

## Commit 生命周期与 History Convergence

Git history 用于表达长期有意义的逻辑变化，不用于逐步记录 Agent 的执行过程。实现、测试、Readiness / Gate 状态、debug、修复、Review finding 处理和重测默认属于 **Working State**；不因为某个执行步骤进入终态就机械创建 Commit。

只有当前 claim 确实需要 exact-Head Evidence、跨上下文 durable handoff，或一个已经形成稳定逻辑边界的变化需要成为长期 subject 时，才形成 **Candidate Commit**。Candidate 仍按“单一逻辑目的”组织，而不是按 Gate、测试轮次或工具调用组织。

Candidate 尚未进入共享历史，并且当前授权允许历史整理时，同一逻辑目的内的后续修复可以收敛回 candidate。任何导致 Candidate SHA 变化的整理都会使旧 exact-Head Evidence 失效，必须按本文“历史重写与 Evidence”及当前 Verification Authority 重新取得受影响证据。

在 push 或集成前执行 **History Convergence**：检查 checkpoint、WIP、Readiness、Gate、Evidence 记录、测试 / debug / Review 修复等过程性 Commit 是否只是同一逻辑目的的中间状态；如果是，并且当前 Repository policy 与授权允许，应收敛为最少必要 logical commits。已经进入共享历史或当前规则不允许改写的历史，不为了整洁重新改写。

一个 Execution Unit、Issue、PR 或 Feature 不要求与 Commit 一一对应。窄而完整的变化通常自然形成一个 Commit；存在多个真正可以独立理解、验证和回退的逻辑目的时则保留多个 Commit，不机械合并。

涉及多个 Git Repository 时，逐仓确认 Repository identity、branch、HEAD、staged / unstaged / untracked 状态、commit policy 与授权，并分别完成 Candidate Commit 和 History Convergence。父仓或项目根工作树干净不能证明被忽略的独立组件仓已经提交，也不得从项目根统一改写组件仓历史。若父仓以 `160000` gitlink / submodule 显式跟踪组件，组件 Candidate SHA 变化后必须同步核对父仓 gitlink、组合 SHA 与绑定 Evidence。

## Summary 与 Body

Summary 应简短、独立可读，并以中文明确说明动作和对象。

普通 Commit 默认不要求 Body。只有标题不足以解释重要原因、兼容性边界、迁移方式、风险或 Trade-off 时才增加 Body；Body 同样以中文主述，不复制完整实施日志或逐项复述 Diff。

## 提交闭环

创建 Commit 时按以下顺序执行：

1. 确认当前 Repository、branch、HEAD、working tree、授权边界和本文；
2. 选定单一逻辑目的，只 stage 属于该目的的文件或 hunk；
3. 检查 staged diff，排除 secret、调试产物、临时日志、无关格式化和其他责任的变化；
4. 执行与本次声明匹配的必要验证，并确认验证输入与准备提交的内容一致；
5. 按本文格式创建 Commit；
6. Commit 后重新读取 HEAD、Commit Message、Commit Diff 与 working tree，确认实际结果；
7. 在 push 或集成前再次确认新增 Commit Message 均符合本文。

Commit 成功不自动授权 push、PR、merge、release 或历史改写；这些操作继续服从根 `AGENTS.md`。

## 历史重写与 Evidence

尚未进入共享远端历史的本地 Commit，在当前授权范围内可以通过 amend / rebase 整理 Message 或逻辑边界。

已经推送到共享 `main` 或其他共享分支的历史，不得只为修正 Message 擅自 force-push；必须先取得明确的人类授权并重新核验远端状态、协作影响与恢复方案。

任何改变 Commit SHA 的历史重写都会使旧 Head 绑定的 exact-Head Evidence 失效。重写后必须重新读取最终 Head / tree / diff，并按当前 Verification Authority 重新取得受影响证据。

## 机器校验

Repository 提供：

```text
python3 scripts/verify-git-commit-message.py
```

默认校验相对 `origin/main` 的新增 Commit；如果没有可用的新提交范围，则至少校验当前 `HEAD`。默认 Local Docker CI 也执行同一检查。

机器校验只负责可机械判断的 Message 格式、Type、Scope 形态和中文摘要；单一逻辑目的、Scope 是否真正稳定、Body 是否充分等语义责任仍由提交者按照本文判断。
