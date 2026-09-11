---
id: technical-plan-rich-text-authoring
title: 富文本内容编辑 V2 技术计划
type: technical-plan
status: ready
version: "V2.0"
relations:
  upstream:
    - docs/requirements/rich-text-authoring.md
    - docs/specifications/rich-text-authoring.md
    - https://github.com/dygapp/jilinjobs-cms/issues/60
  evidence:
    - https://github.com/dygapp/jilinjobs-cms/pull/135
  execution_units:
    - docs/work/current/eu54-rich-text-v2-mature-editor-adoption.md
created_at: 2026-09-05
updated_at: 2026-09-11
---

# 富文本内容编辑 V2 技术计划

## 1. Planning Decision

B3 V2 不继续修补 V1 的自组 Tiptap + 窄 `RichTextHtmlPolicy` 组合，而是一次性收敛成熟 editor integration 与 shared HTML contract。

已完成的隔离 PoC / Human Review只作为技术选型 Evidence，不进入生产 main：PR #135 已关闭且未合并。Primary 选择 SunEditor 3.3.3，Jodit 4.15.0 保留 verified fallback。

## 2. Execution Unit

只形成一个纵向 Unit：

**EU-54 — Rich Text V2 Mature Editor Adoption**

同一 Unit 闭环：

1. Admin editor replacement；
2. Article managed image bridge；
3. Page RICH_TEXT shared editor consumption；
4. Backend/Public HTML policy correction；
5. real-corpus compatibility；
6. WPS / IME / Public Human Review。

不拆成多个串行微型 EU，以避免内容契约在中间状态不一致，并降低治理开销。

## 3. Admin Implementation

### 3.1 Dependency

在 `frontend/admin`：

- 添加并精确锁定 `suneditor@3.3.3`；
- 使用 SunEditor core + 官方 CSS / zh_cn language，仅用于 Admin editor；
- 删除所有不再被其他代码使用的 `@tiptap/**` dependencies；
- 更新 `package-lock.json`；
- 不引入第三方 SunEditor Vue wrapper。

### 3.2 Shared RichTextEditor

重写现有 `frontend/admin/src/modules/cms/components/RichTextEditor.vue` 为薄 lifecycle adapter：

- mount 时 create SunEditor；
- prop change 时只在外部 HTML 真正变化时同步；
- editor change 时 emit `update:modelValue`；
- unmount 时 destroy；
- 保持 `testId` 稳定验证入口；
- 保持可选 managed image callback 与 uploading state；
- 使用 editor 自带 toolbar / dialog / history / paste / table/image UI，不再在项目中重建完整 toolbar。

### 3.3 Proven config

以 PR #135 已验证配置为最小 compatibility baseline：

- `zh_cn`；
- `plugins: SUNEDITOR.plugins` 或等价完整 core plugin registration；
- table `align|cellpadding|cellspacing` compatibility；
- `td width|height` compatibility；
- 启用当前需求中的成熟 toolbar functions。

若新增 compatibility config，必须由 Repository real corpus failure 证明需要；不得为了“以后可能”扩张配置。

## 4. Consumer Integration

### 4.1 Article INTERNAL

`ArticleManagementView.vue` 保持 current domain ownership：

- `bodyHtml` 仍在 Article form；
- `uploadResource()` 仍创建 CMS Resource；
- thin adapter把 uploaded `src/alt`交给 editor；
- Article consumer继续维护 `bodyImageResourceIds`；
- save前继续按正文实际 managed resource URL reconcile association；
- cover / attachment现有流程不迁入 editor core。

在 SunEditor image操作中必须验证 width/height、alignment、replacement、alt 等普通内容编辑结果不会被 wrapper / Backend policy破坏。

### 4.2 Page RICH_TEXT

`PageManagementView.vue` 继续通过 `v-model=pageForm.bodyHtml` 使用 shared editor；不传 managed Resource provider，不新增 Page Resource API/schema。

`EMBED_PLACEHOLDER`、`INTERNAL_STATIC` 与未来 Structured / Engineering Page均不在本 Unit更改。

## 5. Backend / Public Implementation

### 5.1 Policy correction

保留 OWASP Java HTML Sanitizer dependency，但重构 `RichTextHtmlPolicy`：

- 从成熟库的标准 formatting / blocks / tables / links / images / styles policy出发；
- 加入 real corpus证明必要的普通 presentation attributes/styles；
- 保持 parser-based URL/CSS处理；
- active content维持明确禁止；
- policy目标是“标准 CMS HTML + 最低 active-content 防护”，不是“SunEditor输出 schema复制品”。

至少修复 V1 已知损失：image `width/height`，并覆盖 P2 需要的 table/cell/image presentation以及 accepted text styles。

### 5.2 Boundaries

`ArticleService` / `PageService` current write + Public defensive read结构保持，避免另建第二 sanitizer pipeline。External Article / non-RICH_TEXT Page继续绕开富文本处理。

Public frontend不加载 SunEditor editor chrome CSS。若新内容出现产品 class，先验证其普通 HTML / inline presentation在现有 `.rich-content` 下是否充分；不得默认把 editor产品 CSS变成 Public contract。

## 6. Existing-data Strategy

- 不执行全库 rewrite；
- 不修改 Main frozen migration assets/workflows；
- Fresh Runtime / canonical import用新的 Backend policy证明 Party star dimensions不再丢失；
- 对已经被 V1 sanitizer写坏的持久记录，不猜测修复；若 Review Environment需要保留现库，则只允许基于 exact old normalized baseline + canonical source的 bounded repair，operator-diverged记录必须停止并报告。

该 bounded repair只有在实际环境证明需要时才实现；不得预先泛化为 Generic migration rewrite机制。

## 7. Verification Sequence

### Gate A — Dependency / wrapper

- Admin install/build/type PASS；
- SunEditor 中文 UI实际初始化；
- Tiptap dependencies删除且无残余 import；
- Article/Page均只消费一个 shared editor。

### Gate B — Editor behavior

- ordinary Chinese authoring；
- headings / styles / list / table / link / image；
- save / reopen / continue editing；
- undo / redo；
- WPS-shaped automated paste/history。

### Gate C — Resource

- upload managed image；
- image URL / alt / dimensions；
- remove image后 association reconcile；
- attachment regression。

### Gate D — HTML compatibility / active-content

使用 Repository real corpus而不是只用 editor-generated fixtures：

- Party 15 / 16 px star；
- `teacher-library` table/cell/image/float；
- ordinary Article/Page；
- hostile API payload。

验证链必须覆盖：editor load/save → Backend write policy → DB/admin read → Public API → browser render。

### Gate E — Full exact-head verification

执行受影响的 Backend / Admin / Public / Integrated Browser / Party suites和当前标准 CI。Evidence必须对应 implementation PR exact Head。

### Gate F — Bounded Human Review

在 Review Environment完成：

1. Windows 中文 IME；
2. 真实 WPS paste与继续编辑；
3. 一次 paste一次 Undo；
4. Article managed image尺寸/对齐；
5. Party star显示；
6. Page RICH_TEXT ordinary edit/save/public render。

Microsoft Word真实 paste不是 blocker；有环境时可补充 Evidence，但不得伪报。

Human Review PASS 后才允许 integration；merge 后执行 Post-Integration CI并完成 EU-54 closure。

## 8. Stop / Escalation

若同一问题连续修正仍没有增加信息，或出现 Specification Stop Condition，必须停止机械重试并显式报告：症状、精确 Head、已试路径、最新证据、建议下一决策。

特别是以下情况不得通过继续堆 bespoke code解决：

- 需要复制完整 editor content model 到 Backend；
- 需要 Public整体加载 SunEditor editor UI CSS；
- resource association不可稳定；
- accepted corpus出现无法解释的语义损失。

## 9. Integration / Closure

Planning Authority集成后重新对最新 main执行 `readiness-check`；只有 PASS 才授予 EU-54 Execute Authority。

Implementation完成后：

- implementation PR exact-head gates PASS；
- bounded Human Review PASS；
- squash integration；
- Post-Integration verification PASS；
- EU-54 Work artifact归档；
- Current Ready Execution Unit恢复 NONE；
- Issue #60追加最终 Current Evidence。

下一业务候选（包括 Page Content Architecture / `就业派遣` 特殊页）必须重新 Planning，不继承 EU-54 authority。
