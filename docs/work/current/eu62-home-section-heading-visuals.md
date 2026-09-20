---
id: execution-unit:eu62-home-section-heading-visuals
type: execution-unit
status: active
readiness: PASS
controlling_issue: 193
base_sha: eff10d81abd1c6068d1032c077cb03adeda1908a
---

# EU-62 首页大板块与小板块标题视觉收敛

## 目标

修复人工评审发现的 Main 首页标题视觉偏差，恢复整行大板块与多列小板块的稳定视觉差异。

## 权威输入

- `docs/requirements/information-publishing.md` V6.1；
- `docs/specifications/public-site.md` V3.5；
- Issue #193 人工评审反馈；
- `method:review-feedback-cycle`。

## 范围

- “最新招聘”“网站导航”保持独占首页内容主轴一整行，并恢复大板块独立 section heading / 左侧竖向视觉标记；
- 通知公告、就业动态、快速导航、招聘公告使用 Label / Tab 标题轮廓；
- 标题文字使用强调色；3px 顶部强调色横条只覆盖 Label / Tab 自身宽度，不铺满整个卡片；Label / Tab 保留左右 1px 中性边线；
- 不恢复标题文字下划线；
- 可实现轻量顶部圆角，但不作为阻塞验收项；
- 保持网站导航 Tab / 链接正文 16px / 15px；
- 保持慧就业 iframe、直播课程 / 招聘公告并排关系与响应式无横向溢出。

## 人工复评修订

2026-09-20 人工截图复评明确指出当前“小板块整卡片通栏顶边”仍不通过，差异集中在三点：标题字体颜色、顶部颜色条宽度、Label / Tab 左右边线。该 Finding supersede 本单元此前“把 3px 强调色提升为 Panel 整体顶边”的实现解释与对应自动断言。

## 实现边界

优先只修改 `frontend/public-site/src/sites/main/styles/style.css` 与对应 Playwright。现有 `PublicHomeView.vue` 已提供足够结构，不为样式修复增加包装层或新的业务抽象。

## 验收义务

1. 桌面 1440px 下“最新招聘”“网站导航”分别与 `.home-content` 主轴同宽。
2. 大板块 `.section-title` 使用左侧竖向视觉标记；其 `h2` 不存在 3px top border。
3. 通知公告、就业动态、快速导航、招聘公告的标题文字均使用强调色；标题 Label / Tab 自身拥有 3px 顶部强调色横条与左右 1px 中性边线，且标签宽度小于 Header / Panel 宽度。
4. 小板块 Panel 不得使用 3px 通栏强调色顶边；`h2::after` 不恢复文字下划线。
5. 网站导航 16px Tab / 15px link baseline 保持。
6. 直播课程与招聘公告并排高度、首页无横向 overflow、现有慧就业行为不退化。
7. Public build / relevant Playwright PASS。
8. 自动验证后仍需人工视觉复评；自动测试不替代最终视觉观察。

## 完成条件

定向实现和验证通过后形成 PR；完成自动 Convergence 后停在人工视觉复评 Gate，不在缺少人工视觉结论时声明视觉验收完成。
