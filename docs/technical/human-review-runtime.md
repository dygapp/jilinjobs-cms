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
9. 启动 Backend 与 Public/Admin Runtime；
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

输出至少包括当前 Repository `HEAD`、Runtime `source_head`、evidence directory、fixture 状态、MySQL / Backend / Frontend container 状态，以及 `running`、`degraded` 或 `stopped`。

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

## 3. Verified evidence 边界

人工评审默认只允许启动当前 **clean exact HEAD** 的完整 Local Docker CI PASS。

如果 `start` / `reset` 报告最新完整 CI subject 与当前 HEAD 不一致，应先执行：

```bash
bash scripts/local-ci.sh full
```

通过后再启动人工评审环境。

该约束防止人工观察误绑定旧 Backend / Frontend / Baseline image。dirty worktree 虽然允许进入 Local Docker CI 的开发验证，但默认不作为 Human Review Runtime 的 source subject。

如需要显式选择某个 evidence directory，可设置：

```bash
HUMAN_REVIEW_EVIDENCE_DIR=.local-ci/evidence/<run-id> \
  bash scripts/human-review.sh start
```

该 override 仍必须满足 PASS、当前 exact HEAD 与 Review Verification marker 的绑定检查，不能绕过 source identity。

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
