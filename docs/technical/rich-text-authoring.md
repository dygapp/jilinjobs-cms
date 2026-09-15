---
id: technical-rich-text-authoring
title: 富文本编辑集成技术约束
type: technical-authority
status: current
relations:
  specification:
    - docs/specifications/rich-text-authoring.md
  domain:
    - docs/requirements/cms-domain.md
updated_at: 2026-09-15
---

# 富文本编辑集成技术约束

## 1. 责任

本文只保留跨多个 Rich Text consumer 需要共同遵守的当前 integration HOW；具体 editor dependency / version、toolbar config、源码实现和 package lock 由 Repository implementation 自己拥有。

如果未来 Rich Text 变更只涉及单一 Feature-local、低风险实现，不需要为了保持本文件“同步”而复制所有实现细节。

## 2. Single editor runtime

Admin Runtime 同一时刻只采用一个成熟 Rich Text editor implementation，通过 CMS-local thin adapter 提供统一 `modelValue` / lifecycle / optional resource callback contract。

Thin adapter 只负责：

- create / destroy；
- external HTML 初始化与真实变化同步；
- value change output；
- minimal project configuration / locale；
- stable test hook；
- optional managed-image bridge。

selection、history、paste、table、font、format、dialog 等成熟通用能力由 editor core 负责，不在项目 adapter 中重新实现。

当前具体 editor 与版本从 `frontend/admin` package / lockfile 恢复，不在 Product Requirement 或本文件复制版本号。

## 3. Article integration

Article consumer 继续拥有 Article form、`bodyHtml`、cover、attachment 与 body-image association。

Editor adapter 不成为 Article Domain owner。

Managed image flow：

```text
Admin consumer
→ CMS Resource upload
→ editor inserts public-compatible managed URL / metadata
→ consumer reconciles current body resource references
→ Article save
```

从正文移除 URL 只解除当前正文引用，不自动删除 Resource 本体。

## 4. Page integration

RICH_TEXT Page 使用同一 Rich Text adapter编辑 whole-body `bodyHtml`。

Structured Page 只在 schema 明确允许的 item body 中复用 Rich Text capability；whole-page Structured content 不回退成并行 arbitrary `bodyHtml`。

Page 不因共享 editor 而自动获得 Article 的 resource association model。

## 5. HTML policy

Backend write boundary 与 Public defensive read 使用共享 responsibility 的 parser-based HTML policy：

- compatibility-first；
- 保持 accepted formatting / blocks / links / images / tables / presentation；
- 显式拒绝 active / document-level dangerous content；
- 不把当前 editor toolbar schema复制成 Backend dialect；
- valid real corpus 与 hostile corpus 同时驱动 tests。

Public read defense不回写 DB。

## 6. Public independence

Public Renderer 消费 accepted HTML contract，不加载 editor authoring chrome 作为正常显示前提。

Public projection负责把 managed resource identity转换为公开可消费的 contract；Public client不应知道 Admin-only endpoint。

## 7. Existing data

- ordinary editor replacement 不触发 full-database rewrite；
- operator 未编辑的 historical bytes 不因技术升级被批量 canonicalize；
- 管理员实际保存时允许进入当前 editor / sanitizer 的正常 canonicalization；
- 已发生的历史 presentation loss 没有 exact evidence 时不猜测修复；
- bounded repair 必须保护 operator divergence。

## 8. Verification

技术变更根据实际风险至少选择：

- Admin build / type-check；
- shared adapter lifecycle；
- Article / Page consumer regression；
- Chinese IME / paste / history；
- managed image + attachment；
- valid representative corpus；
- hostile payload；
- Backend → DB/API → Public browser chain；
- Public independence from editor chrome。

有真实 authoring UX / presentation 变化时，再追加 bounded Human Review。

## 9. Stop conditions

出现以下情况时停止堆叠 bespoke patch，回到 Specification / Technical decision：

- mature editor 的合理配置仍无法保持 accepted corpus；
- Public 必须依赖 editor UI runtime / chrome；
- Article managed resource association 不稳定；
- wrapper 破坏正常 IME / paste / history；
- Backend 必须复制 editor internal model 才能工作。
