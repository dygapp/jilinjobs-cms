---
id: architecture:rule-discovery
type: architecture
status: active
---

# Rule Discovery 架构

## 目标

Rule Discovery 只解决一个问题：从当前可观察 task / repository facts 中，确定性筛出少量值得读取的 Consumer-local Rule locator，而不是把全量 Rule metadata / 正文交给模型。

```text
current task facts
→ bounded task signals
→ Consumer-local Rule Discovery Tool
→ scan Rule Front Matter
→ deterministic prefilter
→ small {id,path} candidate set
→ read candidate bodies
→ semantic applicability confirmation
```

Rule 的类型、粒度与 Consumer specialization 由 `docs/architecture/rule.md` 持有；本文件只拥有 discovery / progressive disclosure / fail-closed contract。

## Task signals

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

Method phase token 只能来自当前 selected Method canonical owner；不能安全确定时 `phases = null`。

## Deterministic matching

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

ordinary runtime 只能从 `candidates[].path` 获得 Rule locator。不得通过目录树、`find`、`rg --files`、IDE index 或 Human README 枚举未命中 Rule。candidate 只是“值得读取”，最终适用性仍由正文语义确认。

## Consumer-local adaptation

当前 Consumer Tool contract 由 Project Capability Profile 指向。当前 `docs/rules/**` 下每个 `.md` 都属于 discoverable Rule；本地 Tool 暂不提供 Rule-root `README.md` 保留例外，因此 Human navigation 继续由 `docs/README.md` 等 Rule root 外入口承担。只有显式修改本地 Tool contract并完成验证后才可改变这一点。

## Fail-closed

metadata / schema / duplicate id / task signals / scan 完整性异常必须 fail closed；不得跳过坏 Rule 后继续，也不得在 zero-candidate 后读取未命中 Rule 做 calibration。

## Responsibility checkpoint

当前 direct responsibility 建立后，在该责任首个有副作用动作前必须完成 task-level discovery。direct responsibility 切换，或 phase / activity / technology / artifact / risk 等关键事实发生足以改变候选集合的实质变化时，必须在下一次有副作用动作前重新发现。

为恢复 Authority / Repository facts 所需的只读读取可以先行。CI lint、deterministic tests 和固定 smoke 只验证 Tool / corpus，不替代当前 Agent 的 live discovery。

Skill discovery 与 Rule Discovery 分离；ordinary runtime discovery 失败只在 Consumer-local state 内关闭，不自动访问 upstream。