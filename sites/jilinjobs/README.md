# JilinJobs Site Package

`sites/jilinjobs/` 是 JilinJobs 稳定版本化站点定义（Site Definition）的 Repository owner。它描述随代码版本一起恢复的稳定站点结构、明确的一次性初始默认值与稳定站点资源，不承担 Generic CMS Schema、Historical Content Migration 或 ordinary Runtime 数据的长期 ownership。

## 1. 目录责任

- `manifest.json`：当前 package identity、schema version 与实际 component inventory；当前支持哪些 structure component 以 manifest 和实现为准，本 README 不维护第二份清单。
- `structure/**`：由当前 manifest 声明并受 stable identity / reconcile contract 管理的站点结构与明确接受的稳定内容。
- `bootstrap/**`：只保存需要在 Fresh Site 或明确 adoption 场景执行的一次性初始默认值；成功应用后对应数据进入 ordinary operator-managed Runtime lifecycle，普通 reconcile 不重放、覆盖或 resurrect 它们。
- `assets/**`：稳定站点资源的版本化 source、integrity metadata 与 Runtime projection 输入。

## 2. Ownership 边界

以下内容不由 Site Package 持有：

- Generic CMS Schema / capability evolution → Backend Generic schema owner；
- Historical Article / legacy provenance / fingerprint / canonical migration dataset → `../../data-migrations/**`；
- operator-created / operator-edited ordinary Runtime content → Runtime CMS Data；
- `/static/uploads/**` 等 mutable Runtime uploads → Runtime storage；
- Feature-local renderer / frontend implementation → 对应 Specification / Technical / code。

Historical Source 中发现的 Page、List 或其他信息只有在当前 Product / Domain / Architecture Authority 明确选择 Site Definition ownership 后，才能形成新的 versioned Site Definition change；Source discovery 本身不自动授予该 ownership。

## 3. Runtime composition

当前长期组合语义为：

```text
Generic CMS schema ready
→ stable Site Definition reconcile
→ stable asset projection
→ optional one-time initial Runtime defaults
→ optional Historical Canonical Migration
→ ordinary Runtime
```

`structure/**` 与 `bootstrap/**` 的区别是 lifecycle，不是“重要 / 不重要”：stable structure 可以按稳定 identity reconcile；bootstrap 数据完成初始化后由 ordinary Runtime owner 接管。

README 不维护“未来必须把某类 bootstrap 数据迁移为 stable structure”的隐含 Roadmap。若未来产品或架构需要改变 ownership，必须通过新的 Requirement / Architecture / Planning Authority 明确建立，而不是从历史 capability gap 描述继承。

## 4. Canonical Authority

- CMS Domain 与 stable identity / ownership semantics：`../../docs/requirements/cms-domain.md`
- Site Definition / Runtime / Historical Migration 长期架构边界：`../../docs/architecture/cms-architecture.md`
- 跨 Feature 验证策略：`../../docs/technical/verification-strategy.md`
- 当前 package component inventory、version、digest 与具体格式：本目录 manifest / structure / bootstrap / assets 及对应 implementation

本 README 是 workspace boundary / locator，不复制 Requirement、Architecture 或当前 GitHub execution state。