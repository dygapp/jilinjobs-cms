# 技术权威文档（Technical Authority）

`docs/technical/` 根目录只保存当前仍需要跨 Execution Unit 持续协调的 implementation HOW、Interface Contract 与 Verification Strategy。

## Current Technical owners

- `backend-service.md` — Backend multi-project / application composition 与跨 Feature implementation contract；
- `http-interface-contract.md` — Backend ↔ Admin/Public Frontend 稳定 HTTP compatibility、wire projection 与 technology-substitution seam；
- `admin-frontend.md` — Admin application / module 与共享 authoring implementation contract；
- `public-site-frontend.md` — 当前 Public Renderer implementation adapter 与 source ownership contract；
- `rich-text-authoring.md` — Rich Text 跨 consumer integration HOW；
- `verification-strategy.md` — 跨 Feature verification layering、Runtime composition 与 Evidence contract。

HTTP endpoint / wire compatibility 只由 `http-interface-contract.md` 长期持有；Backend / Admin / Public Technical owner 只说明各自如何消费或实现该 contract，不复制第二份 endpoint / DTO inventory。

Technical Authority 不拥有 Product Requirement、Domain semantics 或长期 Architecture State；也不因历史上曾存在 Requirement / Specification / Technical 同名三件套而保留完成态 Planning 正文。

具体 package version、源码文件清单、Gradle task、active migration inventory、测试数量、当前 Workflow run 与 Execution Unit Evidence 由 Repository implementation、Work lifecycle 或 GitHub 原生状态持有。

## Archive

`archive/` 保存已完成、被取代或已经被长期 owner 接管的 Technical Plan / convergence record，仅用于 traceability。Archive 默认不参与 Fresh Context。

当一个 Feature Technical Plan 完成后，如果其中仍有跨 Feature 长期 HOW，应折回上述 canonical Technical owner；其余完成态 Planning / READY / EU / exact implementation snapshot 退出根级 Current Authority。
