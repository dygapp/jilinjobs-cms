# Consumer-local Rule Discovery 入口（过渡 Locator）

> **TRANSITIONAL LOCATOR — Issue #153 Foundation Rebuild**
>
> 本文件不再拥有 Rule / Rule Discovery / Skill discovery 的规范正文。它仅在本轮 Foundation Rebuild 期间兼容旧 locator；Phase B 收口前必须删除。

当前 canonical owners：

- Rule semantic / granularity / Consumer specialization：`docs/architecture/rule.md`
- Rule Discovery runtime / signals / matching / locator-only / fail-closed：`docs/architecture/rule-discovery.md`
- Repository-local Rule root / Tool / ordinary invocation：`docs/project/project-capability-profile.md`
- Skill type contract：`docs/architecture/skill.md`
- Rule corpus：`docs/rules/**`
- Tool：`tools/rule-discovery/rule_discovery.py`

当前 Tool contract 仍以 Consumer-local 实现为准；本轮不会因为 upstream Architecture 已出现新能力就隐式扩大本地 Tool 行为。