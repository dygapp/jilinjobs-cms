---
id: technical:human-review-runtime
type: technical-strategy
status: active
relations:
  verification:
    - docs/technical/verification-strategy.md
    - docs/technical/ci-verification-runtime.md
updated_at: 2026-09-28
---

# 人工评审运行时

## 1. 文档责任

本文持有 `jilinjobs-cms` 本地 **Human Review Runtime** 的长期 implementation HOW：如何从当前完整 Local Docker CI 已验证的 immutable project images 与 Review Baseline 启动、检查、重置和停止供人工测试的环境。

人工评审环境用于显式 Human Runtime Observation，不替代自动 CI，也不重新定义 Product Requirement、Feature Acceptance 或自动验证 claim。完整 Local Docker CI 的职责仍由 `docs/technical/ci-verification-runtime.md` 持有。

面向人工评审、演示和手工测试人员的简化操作步骤见 `docs/guides/human-review.md`。该 Guide 是本文的使用投影，不拥有或复制本 Runtime contract。

## 2. 统一入口

Repository-owned 唯一日常入口为：

```bash
bash scripts/human-review.sh start
bash scripts/human-review.sh status
bash scripts/human-review.sh reset
bash scripts/human-review.sh stop
```

普通人工评审不应再手工拼接 Backend / Frontend / Baseline image tag，也不应把 `restore-review-baseline.sh`、`start-review-runtime.sh` 和 `apply-human-review-fixture.sh` 当成新的并列用户入口。它们继续作为统一入口内部复用的底层 contract。

### start 启动

`start`：

1. 确认没有 Full Local Docker CI 正在占用固定端口；
2. 要求当前 worktree clean；
3. 读取 `.local-ci/evidence/latest.txt`；
4. 要求对应 `result.txt` 为 `PASS` 且 `subject` 等于当前 exact `HEAD`；
5. 校验 evidence 绑定的 Backend、Frontend、Review Baseline、Review Verification images 均存在；
6. 读取 Review Verification marker，确认 `sourceSubject` 等于当前 `HEAD`；
7. 启动 Human Review MySQL；
8. 恢复 verified Review Baseline；
9. 启动带 `admin` / `super` 短期模拟会话的隔离 Review Backend 与 Public/Admin Runtime；Admin 首次进入保持匿名，由测试人员在页面选择身份；
10. 默认注入人工评审 fixture；
11. 完成 HTTP health probe 后输出访问地址。

若当前同一 verified HEAD 的环境已经健康运行，`start` 只复用现有环境，不重置人工测试中的可写状态。

### reset 重置

`reset` 使用与 `start` 相同的 evidence / exact-HEAD 前置检查，但会重新恢复 verified baseline、重启 Backend / Frontend，并重新注入人工评审 fixture。

```bash
bash scripts/human-review.sh reset
```

### status 状态

`status` 只读取当前 Runtime 状态，不创建、重置或删除环境：

```bash
bash scripts/human-review.sh status
```

输出至少包括当前 Repository `head`、启动时的 `source_head`、真正完成 Full CI 的 `verified_source_subject`、`runtime_fingerprint`、evidence directory、fixture 状态、MySQL / Backend / Frontend container 状态，以及 `running`、`degraded` 或 `stopped`。

### stop 停止

`stop` 只回收人工评审可写 Runtime：

- `jilinjobs-human-review-mysql`；
- `cms-backend`；
- `cms-frontend`；
- `runtime-static/`；
- `runtime-uploads/`；
- `review-baseline-restore/`；
- `runtime-review-meta/`。

它不删除：

- `jilinjobs-cms-local/*` verified project images；
- Playwright / Gradle / Node / MySQL / Alpine 等基础镜像；
- `.local-ci/cache/**` dependency cache；
- `jilinjobs-cms-ci` BuildKit builder/cache；
- `.local-ci/evidence/**`。

因此停止人工评审环境不会破坏下一次快速启动或 CI cache。

## 3. Verified evidence 与 Runtime 等价边界

Human Review Runtime 必须来源于一次 **exact-commit Full Local Docker CI PASS**，但当前 Repository `HEAD` 不再要求与该 verified source commit 完全相同。

启动时使用两层身份：

- **verified source subject**：完整 Local Docker CI 真正完成验证的 exact commit；
- **Runtime fingerprint / equivalence**：当前 Backend、Frontend、Review Baseline 与人工评审运行控制输入是否仍与 verified source 等价。

当前 Runtime-relevant inputs 包括：

- Backend Runtime 输入；
- Public/Admin Frontend Runtime 输入；
- Review Baseline 的 Flyway、Site Package、canonical migration data 与 baseline prepare / restore 输入；
- `start-review-runtime.sh`、`apply-human-review-fixture.sh`、`verify-review-runtime.py` 等人工评审运行控制输入。

`docs/**`、Guide、Roadmap、Governance 等不参与 Runtime 的变化不会仅因为 Git `HEAD` 改变而使已有 verified images 失效；未提交的非 Runtime 变更同样允许存在。

如果任何 tracked / untracked Runtime-relevant input 相对 verified source commit 发生变化，`start` / `reset` 必须失败关闭并要求：

```bash
bash scripts/local-ci.sh full
```

通过后再启动人工评审环境。

Review Verification marker 继续保留原始 `sourceSubject`，用于回答“这组 immutable project images 在哪个 commit 上完成完整验证”；它不被重写成当前仅 Runtime 等价但未重新执行 Full CI 的 `HEAD`。

新 Evidence 同时记录 `review_runtime_fingerprint`。旧 Evidence 没有该字段时，可以通过既有 Backend / Frontend / Baseline fingerprints、verified source commit 与当前运行控制输入完成兼容性判定；一旦生成新版 Evidence，后续同时校验显式 Runtime fingerprint。

如需要显式选择某个 evidence directory，可设置：

```bash
HUMAN_REVIEW_EVIDENCE_DIR=.local-ci/evidence/<run-id> \
  bash scripts/human-review.sh start
```

该 override 仍必须满足 PASS、verified source exact commit 与 Review Verification marker 的绑定检查，不能绕过 source identity。当前工作分支可以位于另一个 Runtime-equivalent `HEAD`。

## 4. 人工评审 fixture

默认：

```text
HUMAN_REVIEW_APPLY_FIXTURE=true
```

统一入口在 baseline 恢复后调用 `scripts/apply-human-review-fixture.sh`，补充容易人工识别的管理端 / 公开站测试内容，例如人工评审通知、就业动态、招聘公告与轮播项。

如果只需要观察 canonical baseline，可显式关闭：

```bash
HUMAN_REVIEW_APPLY_FIXTURE=false \
  bash scripts/human-review.sh reset
```

fixture 只属于当前可写人工评审数据库状态，不进入 canonical dataset，也不成为 Product Requirement。

## 5. 访问地址与固定端口

Human Review Runtime 使用 host network，与 Local Docker CI 使用相同固定端口：

| Responsibility | Address |
|---|---|
| Public | `http://127.0.0.1:5173/` |
| Admin | `http://127.0.0.1:5173/admin/` |
| Backend | `http://127.0.0.1:8080/` |
| MySQL | `127.0.0.1:3306` |

Windows + WSL2 通常可以从 Windows 浏览器直接访问：

```text
http://localhost:5173/
http://localhost:5173/admin/
```

如果宿主端口已被其他服务占用，先释放对应端口；不要通过任意替换端口来改变当前 Review Runtime contract。

Review Frontend 通过运行时挂载的 `/review-environment.json` 显示测试身份入口；该 marker 只改变界面能力发现，不授予身份。登录由 Review BootJar 的 `/api/review/identity/sessions` 创建有界短期会话，正式 Server 不包含该 endpoint 或 verifier。代理层不得为普通 Browser 请求静默注入超级身份；自动化测试必须显式携带其 Review credential。

## 6. 与 Local Docker CI 的互斥关系

Full Local Docker CI 与 Human Review Runtime 都使用 host network 的 `3306`、`8080`、`5173`，因此是单宿主单 slot。

- `human-review.sh start/reset` 检测到当前 Full CI lock 或 Local CI Runtime container 时失败关闭；
- `local-ci.sh full` 检测到 Human Review Runtime container 时失败关闭，并要求先执行：

```bash
bash scripts/human-review.sh stop
```

不要同时启动两套 Runtime，也不要依赖 Docker container name 覆盖解决冲突。

## 7. 底层脚本责任

统一入口复用以下既有脚本：

| Script | Responsibility |
|---|---|
| `scripts/restore-review-baseline.sh` | 校验 baseline manifest / digest，恢复 DB、runtime-static 与 runtime-uploads |
| `scripts/start-review-runtime.sh` | 启动 Backend 与 Public/Admin，等待 readiness |
| `scripts/apply-human-review-fixture.sh` | 注入非 canonical 的人工评审 fixture |

这些脚本是 implementation building block；日常人工评审使用 `scripts/human-review.sh`。

## 8. 常见操作

首次启动或已有当前 verified images：

```bash
bash scripts/human-review.sh start
```

查看是否健康：

```bash
bash scripts/human-review.sh status
```

人工修改数据后恢复初始评审状态：

```bash
bash scripts/human-review.sh reset
```

结束评审：

```bash
bash scripts/human-review.sh stop
```

源码已经提交但当前 HEAD 没有 matching Full CI evidence：

```bash
bash scripts/local-ci.sh full
bash scripts/human-review.sh start
```

## 9. Evidence 与人工结论

Human Review Runtime 的自动 source identity 来自 matching Local Docker CI evidence；它只证明“人工当前观察的 Runtime 基于哪个已验证 source subject”。

人工视觉、交互或业务观察结论如果需要跨会话持续成立，应写回真实 Requirement / Specification / Design / Issue / Review owner。Runtime state、fixture 和 `.local-ci/**` 本身都不是长期 Product Authority。
