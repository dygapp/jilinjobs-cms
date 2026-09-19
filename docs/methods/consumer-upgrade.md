---
id: method:consumer-upgrade
type: method
status: active
---

# Consumer 升级方法（Consumer Upgrade Method）

## 目标

用于 Existing Consumer 显式评估新的 `agentic-dev` upstream baseline，并决定是否改变 Consumer-local capability。upstream 新 commit、release 或 Project state 不自动改变本仓库；普通运行时 不触发隐式 upgrade。

## 生命周期

```text
Restore Current Consumer
→ Select Candidate Upstream Baseline
→ Evaluate Capability Delta
→ Apply Local Decisions
→ Refresh Local Capability Instance
→ Targeted Revalidation
→ Close New Evaluated Baseline
```

## 恢复当前 Consumer（Restore Current Consumer）

从 Consumer-local Project Knowledge、Method / Architecture / Skills / Rules、evaluated baseline 与 local adaptation 恢复当前 canonical state。若当前 规范语义所有权 本身无法可靠恢复，应先进入独立治理 / clarification work，而不是用 upgrade 覆盖结构性问题。

## 选择候选上游基线（Select Candidate Upstream Baseline）

选择精确固定 upstream ref，只读取本次升级直接相关 reusable capability assets 与必要 Evidence。upstream Project Knowledge 只作为 provenance / context，不是同步对象。

## 评估能力差异（Evaluate Capability Delta）

对相关 Method / Architecture / Skill / Rule / Tool contract 逐项给出 `retain / adopt / adapt / replace / reject`，比较语义责任而不是目录或文件 diff。

## 应用本地决策（Apply Local Decisions）

只把已接受决定写入 Consumer-local 规范语义所有者；迁移 / replace 时必须让旧 owner 明确退出，避免双 Authority。

## 刷新本地能力实例（Refresh Local Capability Instance）

如果 accepted delta 改变 Method selection、Skill entry、Rule root / Tool、Human / Agent entry 或其他 local instance pointer，更新 `docs/project/project-capability-profile.md`。不得复制 upstream Project Capability Profile。

如果 accepted delta 改变 Tool contract、runtime assumption、Rule Discovery contract、verification behavior 或 executable path requirement，还必须重新评估并刷新受影响的 Consumer-local executable instance，包括 obligation、canonical locator、direct execution path、automated alternate path、result / Evidence recovery 与 fail-closed behavior。不得只更新 upstream provenance 或 Tool source 而保留已经失真的运行路径。

## 定向重新验证（Targeted Revalidation）

按真实 semantic delta 选择当前验证，至少覆盖实际受影响的：

- Method transition、Skill behavior、Rule Discovery 与 local policy；
- local capability profile / runtime entry 与 ordinary-runtime upstream decoupling；
- direct execution path 与 automated alternate path；
- result / Evidence recovery、exact subject 与 terminal state；
- declared path 不可用或 locator 破坏时的 fail-closed behavior；
- Fresh Runtime 仅依赖 Consumer-local Authority 恢复受影响路径的行为；
- 受影响工程 / 产品行为。

旧 Evidence 不支撑已经变化的新 claim。

## 关闭新的已评估基线（Close New Evaluated Baseline）

记录新的 exact evaluated upstream baseline、关键 disposition 与当前 Evidence。完成条件：Consumer-local owners 自洽，普通运行时 不依赖 upstream，upstream Project state 未泄漏成 Consumer Authority。
