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

脚本会自动选择一组与当前 Runtime 输入等价的已验证 Backend、Frontend 与 Review Baseline，不需要人工复制 Docker image tag。纯文档或 Guide 变化不会仅因为 Git `HEAD` 改变而使环境失效。

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

若本机启用了 HTTP 代理，loopback 请求可能被代理误转发；可显式直连本地地址启动（`reset` / `status` 同理）：

```bash
NO_PROXY=127.0.0.1,localhost,::1 no_proxy=127.0.0.1,localhost,::1 \
  bash scripts/human-review.sh start
```

打开 Admin 后先看到标有“仅限隔离测试环境”的身份入口：

- “以内容管理员登录”用于复核普通内容管理能力；该身份不显示“操作审计”，直接访问审计地址会得到禁止访问反馈；
- “以超级管理员登录”用于复核全部日常管理能力和操作审计；
- 顶栏“模拟身份失效”用于验证使用过程中身份过期，“退出”用于结束当前测试会话；
- 刷新页面保留当前 tab 内仍然有效的测试会话；退出、模拟失效、会话到期或重启 Runtime 后必须重新选择身份。

这些入口只存在于隔离 Review Runtime，不是正式账号或密码登录。页面显示的用户 ID 与角色来自 Backend 当前主体接口，不由浏览器自行填写。

## 2. 查看运行状态

```bash
bash scripts/human-review.sh status
```

主要关注：

- `verified_source_subject`：这套环境真正在哪个 commit 上完成完整 CI；
- `runtime_fingerprint`：当前环境对应的 Runtime 输入身份；
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

## 5. 哪些变化需要重新跑完整 CI

纯文档、Guide、Roadmap、Governance 等不影响 Runtime 的变化，不再要求仅因为 Git `HEAD` 改变就重新执行完整 CI。只要当前 Runtime 输入与已有 verified source commit 等价，可以直接：

```bash
bash scripts/human-review.sh start
```

如果 Backend、Frontend、Review Baseline、迁移数据或人工评审运行控制脚本等 Runtime-relevant input 发生变化，会看到类似：

```text
当前 Runtime-relevant inputs 与 verified source commit=<sha> 不一致
```

先执行：

```bash
bash scripts/local-ci.sh full
```

通过后再执行：

```bash
bash scripts/human-review.sh start
```

不要手工改 image tag 绕过这一检查。

未提交的非 Runtime 文档变化可以保留；未提交的 Runtime-relevant 新文件或修改同样会触发失败关闭。

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
