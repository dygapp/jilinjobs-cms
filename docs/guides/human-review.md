# 人工评审与演示环境指南

本指南用于快速启动一个可供 **人工评审、页面演示和手工测试** 的本地环境。

统一入口：

```bash
bash scripts/human-review.sh start
```

底层 verified evidence、exact-HEAD、端口、缓存和与 Local Docker CI 的互斥规则由 `docs/technical/human-review-runtime.md` 持有；本指南只说明日常如何使用。

## 1. 启动环境

在 Repository 根目录执行：

```bash
bash scripts/human-review.sh start
```

脚本会自动选择当前 `HEAD` 对应的已验证 Backend、Frontend 与 Review Baseline，不需要人工复制 Docker image tag。

启动成功后会显示：

```text
Public:  http://127.0.0.1:5173/
Admin:   http://127.0.0.1:5173/admin/
Backend: http://127.0.0.1:8080/
```

Windows + WSL2 环境通常可以直接在 Windows 浏览器中打开：

```text
http://localhost:5173/
http://localhost:5173/admin/
```

默认会注入便于人工识别的评审测试数据。

## 2. 查看运行状态

```bash
bash scripts/human-review.sh status
```

主要关注：

- `source_head` 是否等于当前 Repository `HEAD`；
- `status=running`；
- MySQL、Backend、Frontend 三个 container 是否均为 `running`。

如果显示 `status=degraded`，优先执行一次：

```bash
bash scripts/human-review.sh reset
```

## 3. 恢复测试初始状态

人工测试修改过内容、上传资源或改变数据库状态后，需要恢复到稳定起点时执行：

```bash
bash scripts/human-review.sh reset
```

`reset` 会：

1. 恢复已验证的 Review Baseline；
2. 重启 Backend 与 Public/Admin；
3. 重新注入人工评审测试数据；
4. 等待环境健康后再返回。

如果只希望查看 canonical baseline，不需要人工评审 fixture：

```bash
HUMAN_REVIEW_APPLY_FIXTURE=false \
  bash scripts/human-review.sh reset
```

## 4. 停止环境

人工测试或演示结束后：

```bash
bash scripts/human-review.sh stop
```

`stop` 会停止当前人工评审 Runtime 并删除可写测试状态，但会保留：

- 已验证的项目 Docker images；
- Playwright、Gradle、Node、MySQL、Alpine 等基础镜像；
- Gradle / npm dependency cache；
- BuildKit builder/cache；
- Local Docker CI evidence。

因此下次启动通常不需要重新下载或重新构建基础环境。

## 5. 源码变化后无法启动

如果看到类似：

```text
最新完整 CI subject=<old-sha>，与当前 HEAD=<new-sha> 不一致
```

说明当前源码提交还没有对应的完整 Local Docker CI PASS。

先执行：

```bash
bash scripts/local-ci.sh full
```

通过后再执行：

```bash
bash scripts/human-review.sh start
```

不要手工改 image tag 绕过这一检查。

## 6. Full CI 与人工环境冲突

Human Review Runtime 与 Local Docker CI 共用本机固定端口，不能同时运行。

如果准备执行完整 CI，先停止人工环境：

```bash
bash scripts/human-review.sh stop
bash scripts/local-ci.sh full
```

反过来，如果 Full CI 正在运行，等待它结束后再启动人工环境。

## 7. 常用操作速查

| 目标 | 命令 |
|---|---|
| 启动人工评审 / 演示环境 | `bash scripts/human-review.sh start` |
| 查看状态 | `bash scripts/human-review.sh status` |
| 恢复稳定测试起点 | `bash scripts/human-review.sh reset` |
| 停止并清理可写 Runtime | `bash scripts/human-review.sh stop` |
| 源码变化后重新取得完整 Evidence | `bash scripts/local-ci.sh full` |

## 8. 什么时候看 Technical 文档

普通人工测试只需要本 Guide。

以下问题再读取 `docs/technical/human-review-runtime.md`：

- 为什么必须匹配 exact `HEAD`；
- Evidence 和 verification marker 如何绑定；
- Review Baseline 的组成；
- 固定端口与 host network 设计；
- Human Review 与 Local Docker CI 为什么必须互斥；
- 哪些 Runtime 状态、images 和 caches 会被保留或删除；
- 如何显式选择特定 evidence directory。
