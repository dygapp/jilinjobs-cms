---
name: human-review
description: 为 Consumer 软件项目准备结构化人工评审材料，分类人工反馈，并把确认后的长期语义回写真正的 Requirement、Specification、Architecture 或 Technical owner；默认只形成 Markdown 评审草稿，不替代独立变更复核或 merge 授权。
metadata:
  jilinjobs-cms-id: skill:human-review
  jilinjobs-cms-type: skill
  jilinjobs-cms-status: active
---

# human-review

## 输入

- 当前评审目标、范围与受众；
- 直接相关的 当前权威内容；
- 必要的上下游关系；
- 当前未决歧义、冲突或待确认事项；
- 当前写入授权；
- 可选的显式交付目标。

## 过程

1. 确认当前对象属于普通 Consumer 软件项目，并明确评审目标与范围。
2. 只读取当前评审直接需要的真实 语义所有者，不为了“完整”默认扫描全库、archive 或无关实现。
3. 生成结构化 Markdown 评审草稿；只保留当前判断真正需要的事实、流程、边界、验收、差异和待确认项。
4. 只有明显提升理解或判断效率时才形成临时图形、矩阵或其他派生视图。
5. 将反馈分类为展示反馈、语义修正、新增长期决定或未决问题。
6. 有写入授权时，把长期语义变化回写真正 owner，并重新读取验证；没有写入授权时，输出最小待执行回写动作，状态保持“待权威回写 / 评审未完成”。
7. 基于重新读取后的 当前权威内容 重新校准评审草稿，确认没有长期事实只存在于草稿、图形或会话中。
8. 只有显式要求最终格式时才生成交付投影；交付物不取得新的事实所有权。

## 输出与退出

默认输出：

- 结构化 Markdown 评审草稿；
- 已发现的缺口、冲突和待确认项；
- 人工反馈分类；
- 已完成并重新读取验证的 Authority 回写，或明确的待回写动作。

只有长期语义已经实际回写、重新读取确认且评审草稿已校准时，才能声明人工评审完成。

以下情况返回真实责任层或 人工权威：

- Product / Domain ambiguity；
- 高影响、难逆的架构选择超出当前授权；
- 安全、隐私、生产或不可逆数据风险；
- 语义所有者 无法唯一确定；
- 当前请求实际属于独立 Repository change review，应使用 `review-change`。

人工评审完成不等于独立变更复核通过，也不授予 integration / merge / release / deploy 权限。
