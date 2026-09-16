---
id: method:architecture-clarification
type: method
status: active
---

# 架构澄清方法

## 目标与进入条件

本 Method 只处理跨多个当前或预期 Feature 持续成立、长期、高返工成本或高成本难逆，并且不解决就会阻塞可靠 Specification / Technical Planning 的 systemic architecture driver。

它不是 Big Design Up Front，也不负责完成整个系统的技术设计。只有 shared capability / contract、core data / system structural boundary、security / integration / deployment topology、高成本难逆结构决定，或多个局部实现已经暴露出 shared architecture candidate 时才进入。

单 Feature 普通实现选择、低影响可逆设计、类 / 函数 / 文件组织、施工顺序与普通 framework usage detail 留在 `method:ai-development` 的 Technical Planning / JIT execution。

本 Method 不创造 Product Requirement。若暴露业务多解、Requirement conflict、Product Boundary 或 data semantics 缺失，返回真实 Requirement owner；系统性 Requirement Baseline gap 由 local selector 进入 `method:requirement-baseline-establishment`。

## 与其他 Method 的关系

```text
Requirement Baseline Ready
        ↓
architecture blocker?
   ├─ no → AI Development
   └─ yes
        ↓
Architecture Clarification
        ↓
Architecture Context Ready
        ↓
AI Development
```

Feature Technical Planning 与本 Method 必须更新同一个长期 Architecture owner，不形成平行 Authority。

## 生命周期

```text
Establish Architecture Drivers
→ Clarify Architecture
→ Architecture Convergence
→ Architecture Context Ready
```

stable phase identities：

- Establish Architecture Drivers → `establish-architecture-drivers`
- Clarify Architecture → `clarify-architecture`
- Architecture Convergence → `architecture-convergence`

## Establish Architecture Drivers

每个候选 driver 至少证明：

- 影响范围确实跨多个 Feature；
- 会形成长期结构事实；
- 错误选择返工成本显著；
- 不解决会阻塞可靠 Specification / Planning；
- 最终有明确 Architecture owner；
- 没有把未解决 Product Requirement 伪装成 Architecture decision。

不能满足这些条件的内容返回正确责任层。

## Clarify Architecture

依据当前 Requirement Baseline、Repository facts、已有 Architecture Context 与成熟 engineering evidence 形成最小充分长期结构决定。可能更新 Architecture constraints、capability / shared contract boundary、core data / system boundary、security / integration / deployment topology、durable Architecture State；只有 decision 背景与主要 trade-off 具有长期历史价值时才形成 ADR。

Seed Architecture 只提前解决高杠杆结构问题；后续实现中的 repeated structural pain / shared pattern 可以作为 Evolutionary Architecture evidence 重新进入本 Method。

如果 Architecture 分析暴露多个合理业务解释、Requirement conflict、产品责任边界或业务 data semantics 未定义，则返回 Requirement owner，不以“技术上更合理”创造 Product Requirement。

## Architecture Convergence

只有同时满足以下条件才能声明 `Architecture Context Ready`：

- 当前 blocking systemic drivers 已处理；
- 每个长期 Architecture fact 有明确 owner；
- Requirement / Architecture 边界清楚；
- Feature-local 可逆 HOW 未被过度提升；
- shared capability / contract / topology 可以被 Fresh Context Feature Agent 定位；
- ADR 只持有真正需要历史理由的 decision；
- remaining item 明确为 non-blocking 或等待 evolutionary evidence；
- 高影响变更需要的独立 review 已完成；
- 新增或重大修改的长期 Architecture artifact 有明确 producer / trigger / consumer / persistence / update / supersede / escalation boundary。

真正存在 blocker 时保持 NOT READY。

## Return contract

完成后只返回：

```text
Architecture Context Ready
```

它不等于 all architecture designed、Specification / Technical Plan / Execution Unit 已创建，也不授予 Execute / Integrate Authority。后续具体 Feature / change 必须重新按 local selector 进入 `method:ai-development` 或其他适用 Method。

## Artifact lifecycle

Durable：Architecture Context / constraints、shared capability / contract boundary、current durable Architecture State、条件性的 ADR。

Transitional / disposable：candidate matrix、option comparison、exploratory diagram、spike / benchmark interpretation、review scratchpad。可从长期 Architecture Authority 唯一再生的表达不建立平行长期 owner。
