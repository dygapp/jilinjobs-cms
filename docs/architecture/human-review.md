---
id: architecture:human-review
type: architecture
status: active
---

# 人工评审架构

## 目标与边界

本架构定义普通 Consumer 软件项目中，Requirement、Specification、Architecture 与 Technical 内容如何转换为便于人工理解和判断的评审材料，以及人工反馈如何返回真实 语义所有者。

它不拥有具体产品事实，不创建新的通用 Method stage，也不替代 `skill:review-change`。人工评审用于帮助业务、产品、架构或工程人员理解并确认内容；独立变更复核用于判断 Repository change 是否符合当前 Authority、范围、规则与 Evidence。

## 默认评审介质

没有显式交付格式要求时，默认只形成结构化 Markdown 评审草稿。

评审草稿是临时投影，不是 canonical Authority。只在能够提高判断效率时生成流程图、状态图、关系图、矩阵、架构图等派生视图；这些视图必须能从 Current Authority 重新生成，不建立长期平行事实源。

## 人工反馈分类

人工反馈至少区分：

- **展示反馈**：措辞、排版、顺序、图形布局等，只修改评审投影；
- **语义修正**：改变已有长期事实，必须回写原真实 owner；
- **新增长期决定**：确认新的长期约束，必须进入适当 Requirement、Specification、Architecture 或 Technical owner；
- **未决问题**：保持 unresolved，不伪装成已确认事实。

一条反馈同时包含展示与语义变化时必须拆分处理。

## 权威回写闭环

标准闭环：

```text
人工反馈
→ 分类
→ 更新真实 owner
→ 重新读取并确认 owner 已持久化
→ 重新生成受影响评审草稿 / 派生视图
→ 核对投影与 Current Authority 一致
```

禁止：

- 只修改评审草稿而不修改 Authority；
- 让 DOCX / HTML 或图形成为事实修订入口却不回写；
- 在下游 Specification / Technical 中静默覆盖上游 Requirement；
- 让人工长期决定只停留在聊天、Issue comment 或评审草稿中。

人工已经决定但真实 owner 尚未完成并验证回写时，状态必须保持“待权威回写 / 评审未完成”。

## 交付投影

只有调用方明确要求最终格式时才进入交付投影。交付投影只能改变组织、导航、样式或展示方式，不得重新解释业务、架构或技术语义。

正式交付物只能基于已经实际回写并重新读取确认的 Current Authority；如果人工明确要求在未收敛状态输出中间版本，必须标识为草案 / 待确认。

## 完成条件

只有同时满足以下条件，人工评审才算完成：

1. 范围明确；
2. 必要人工判断已经处理或明确保持未决；
3. 已确认的长期语义变化都实际进入真实 owner；
4. 已重新读取 owner 并确认回写存在且无冲突；
5. 评审草稿已按最新 Current Authority 校准；
6. 不存在只保存在草稿、图形、会话或待执行动作中的 长期事实。

人工评审完成不授予 merge、release、deploy 或独立变更复核通过。

## 生命周期

### 产生与触发

评审草稿可以由当前 Method / Skill、已采用的 `skill:human-review` 或明确的人工作业请求产生。典型触发包括：当前责任判断需要人工集中确认、人工明确要求评审材料，或跨模块 / 多角色 / 长生命周期内容需要通过结构化视图暴露缺口。

人工评审能力不自行创造新的产品、架构或技术决策，也不因为能力存在而把普通低风险工作升级为固定人工门禁。

### 使用方

主要使用方包括目标项目的产品、业务、架构、工程与客户评审人员，以及需要根据人工决定更新真实 Requirement / Specification / Architecture / Technical owner 的后续 Agent。

### 持久化、更新与退出

评审草稿和临时派生视图默认属于 transitional / disposable artifact。只有确需跨上下文继续评审、审计、签署或显式交付时才持久化；即使持久化，也不获得产品或架构事实所有权。

真实 owner 更新后，必须重新读取并确认回写，再重新生成或校准评审草稿。可从 Current Authority 唯一再生的旧草稿 / 派生视图应退出当前评审范围，不要求与 Authority 长期双向同步。

### 取代与升级

新的评审投影取代旧草稿时，不通过并行维护两份 Current review truth 保留历史；必要历史由 Git / Issue / PR 或明确 historical evidence 承担。

出现 Product / Domain ambiguity、重大难逆 Architecture、安全 / 隐私 / 生产或不可逆数据风险、owner 无法唯一确定、或当前写入授权不足时，返回真实责任层 / 人工权威。当前请求若实际属于独立 Repository change review，则使用 `skill:review-change`。

## Consumer 本地适配

本 Consumer 不建立中央人工评审数据库、持久业务模型层或固定全项目评审模板。评审对象、章节和派生视图按当前责任选择；产品 / 业务术语继续由 Requirement / Domain owner 持有。
