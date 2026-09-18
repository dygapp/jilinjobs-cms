---
id: architecture:rule
type: architecture
status: active
---

# Rule 架构

## Rule 定义

Rule 是当前 task facts 满足适用条件时施加的 policy、constraint、default、invariant 或 completion requirement。Rule 不要求形成完整 Procedure，也不依附某个 Skill。

## Rule 与 Skill

- Skill 回答“已经决定要做这件事，怎样稳定完成”；
- Rule 回答“当前条件成立时，必须 / 不得 / 默认怎样做，或完成前必须证明什么”。

Rule 可以约束 Method stage、Skill execution、direct Agent work、repository operation、verification 与 completion claim。

## 粒度

默认边界是：

> **一个可独立发现的具体任务或责任所需的有界规范语义集合。**

同一任务中由相同或高度重叠 task facts 触发、通常共同发现与消费的一组 policy 应优先聚合。只有拆分能稳定减少无关加载 / 错误激活，或存在不同 technology、artifact、risk、lifecycle、语义所有者 时才独立成 Rule。

一条 assertion 不等于一个 Rule 文件；正文大小也不是机械拆分指标。

## Consumer 本地特化

Consumer Rule corpus 由本仓库当前 Authority 自主 adopt / adapt / replace / reject，不与 upstream Rule 数量或目录镜像。Repository-specific commit、术语、审批、迁移、验证与技术 policy 优先由本地 Rule 持有。

## Metadata 与正文

Rule Front Matter 只负责 deterministic discovery metadata；Rule body 是规范语义 owner。metadata 不复制 decision logic、exception catalog、正文摘要或 completion details。目录只服务 human IA，不参与 matching。

具体 task signals、matching、locator-only 输出和 失败关闭 contract 由 `docs/architecture/rule-discovery.md` 单点拥有。