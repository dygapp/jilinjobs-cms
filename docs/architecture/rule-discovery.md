---
id: architecture:rule-discovery
type: architecture
status: active
---

# Rule Discovery 架构

## 目标

Rule Discovery 只解决一个问题：从当前可观察的任务 / Repository 事实中，确定性筛出少量值得读取的 Consumer-local Rule locator，而不是把全量 Rule metadata / 正文交给模型。

```text
当前任务事实
→ 有界 task signals
→ Consumer-local Rule Discovery Tool
→ 扫描 Rule Front Matter
→ 确定性预筛选
→ 少量 {id,path} candidate locator
→ 读取候选正文
→ 语义适用性确认
```

Rule 的类型、粒度与 Consumer 本地特化由 `docs/architecture/rule.md` 持有；本文件只拥有 discovery、渐进披露与 失败关闭 contract。

## 任务信号

调用方必须显式提供五维：

```json
{
  "phases": [],
  "activities": [],
  "technologies": [],
  "artifacts": [],
  "risks": []
}
```

每维三态：

- 非空数组：当前事实可安全规范化的 known tokens；
- `[]`：known-empty；
- `null`：unknown / unsafe-to-canonicalize，不得用于排除候选。

每个非空数组最多 6 个 lowercase kebab-case token。不得用候选 Rule 名、同义词堆叠、未命中 metadata 或预期答案反向构造 signals。

Method phase token 只能来自当前选定 Method 的 规范语义所有者；不能安全确定时 `phases = null`。

## 确定性匹配

- Rule 某维为空 → 该维不限制；
- Task 某维为 `null` → 不用该维排除；
- Task 某维为 `[]` 且 Rule 该维有限制 → 排除；
- 双方都有 token 且受限维无交集 → 排除；
- 其余受限维均通过 → candidate。

不使用 score、priority、embedding、目录分类或模型置信度做隐式路由；候选按 id 稳定排序。

## 输出与渐进披露

成功结果只暴露 locator，例如：

```json
{"status":"ok","scanned":100,"candidate_count":1,"candidates":[{"id":"rule:example","path":"docs/rules/..."}]}
```

示例中的 `scanned` 不是本仓库当前 Rule 数量；实际 inventory 由 Rule corpus 与 Tool lint 机械得到，不由 Architecture 缓存。

普通运行时 只能从 `candidates[].path` 获得 Rule locator。不得通过目录树、`find`、`rg --files`、IDE index 或 Human README 枚举未命中 Rule。candidate 只是“值得读取”，最终适用性仍由正文语义确认。

## Consumer 本地适配

当前 Consumer Tool contract 由 Project Capability Profile 指向。当前 `docs/rules/**` 下每个 `.md` 都属于 discoverable Rule；本地 Tool 不提供 Rule-root `README.md` 保留例外，因此 Human navigation 继续由 `docs/README.md` 等 Rule root 外入口承担。只有显式修改本地 Tool contract 并完成验证后才可改变这一点。

## 失败关闭

metadata / schema / duplicate id / task signals / scan 完整性异常必须 失败关闭；不得跳过坏 Rule 后继续，也不得在 zero-candidate 后读取未命中 Rule 做 calibration。

## 责任检查点

Rule Discovery 本身属于 preflight infrastructure invocation：它只计算候选，不修改项目语义或授予后续动作权限，因此不要求先递归执行另一轮 Rule Discovery。

在首次向人工输出包含项目事实、状态判断、方案、复核结论或其他实质内容前，必须建立 `communication` responsibility checkpoint。若本次 task-level discovery 已覆盖 `activities=[communication]` 与 `artifacts=[human-facing-content]`，无需重复；仅表示“正在恢复 / 正在读取”、且不携带项目事实或判断的短进度消息可以先行。

当前 direct responsibility 建立后，在该责任首个受 Rule 约束的实质动作前必须完成 task-level discovery；有副作用动作始终属于这一边界。只读 Authority / Repository fact 恢复可以先行，但 read-only 工作不能因为没有副作用就绕过适用于其 communication / review / verification 输出的 Rule。

direct responsibility 切换，或 phase / activity / technology / artifact / risk 等关键事实发生足以改变候选集合的实质变化时，必须在下一次受 Rule 约束的实质动作前重新发现。

准备请求人工执行动作、提供输入、作出决定或充当系统 / 工具之间的中转时，属于新的 human escalation responsibility checkpoint；调用方应至少使用 `activities=[human-escalation]`、`risks=[human-intervention]` 重新发现适用 Rule。

## 本地与云端 transport

本地 checkout 直接调用 Consumer-local Tool。当前执行面没有适用 shell / worktree，但仍需要真实 task-level discovery 时，不得因此跳过；可以使用 Project Capability Profile 声明的 exact-SHA GitHub Actions transport。

云端 transport 必须：

- 显式接收 exact 40-character target SHA 与 task signals；
- checkout 目标 SHA 并验证 actual SHA 与 requested SHA 一致；
- 调用该 SHA 自身的 Consumer-local Rule Discovery Tool；
- 输出并保留 requested / actual SHA、signals 与 locator-only result；
- signals、SHA 或 discovery contract 无效时 失败关闭。

`pull_request` / `push(main)` 的 CI lint、deterministic tests 与固定 smoke 只验证 Tool / corpus，不替代当前 Agent 的 task-level discovery。

Skill discovery 与 Rule Discovery 分离；普通运行时 discovery 失败只在 Consumer-local state 内关闭，不自动访问 upstream。
