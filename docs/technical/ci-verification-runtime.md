---
id: technical:ci-verification-runtime
type: technical-strategy
status: active
relations:
  verification:
    - docs/technical/verification-strategy.md
updated_at: 2026-09-28
---

# CI 验证运行时

## 1. 文档责任

本文持有 jilinjobs-cms 的 **CI execution HOW**：把 docs/technical/verification-strategy.md 定义的验证责任映射到当前可执行环境。

当前执行优先级：

1. **Local Docker CI**：项目默认自动化验证路径；
2. **GitHub Actions**：保留的远程备用路径，默认关闭自动执行；
3. **Human Review Environment**：只服务显式人工评审，不属于默认 CI；统一生命周期入口与 verified evidence 复用规则见 docs/technical/human-review-runtime.md。

本文不重新定义 Product Requirement、Feature Acceptance 或通用验证方法。哪些 claim 必须被证明仍由当前 Requirement / Specification / Technical Authority 与 verification-strategy.md 决定；更换执行环境不能降低这些责任。

## 2. 默认入口

默认完整验证：

~~~bash
bash scripts/local-ci.sh
~~~

等价显式形式：

~~~bash
bash scripts/local-ci.sh full
~~~

状态与项目级缓存：

~~~bash
bash scripts/local-ci.sh status
~~~

只清理本项目 Local CI runtime / image / builder cache：

~~~bash
bash scripts/local-ci.sh clean
~~~

full 成功才构成完整 Local Docker CI PASS。单独的 build、某个 test task、某个 browser suite 或 cache hit 都不能替代完整结果。

## 3. Host 与固定运行时

Local Docker CI 的宿主只承担编排和源码身份识别；Java / Gradle、Node、MySQL、Nginx、Playwright 的可执行环境进入容器。

宿主要求：

- Linux / WSL2；
- Docker daemon 与 Docker Buildx；
- Git、Bash、Python 3、curl、sha256sum；
- 当前用户可直接访问 Docker daemon；
- host network 的 3306、8080、5173 在执行期间可用。

当前固定运行时与现有项目 CI 对齐：

| Responsibility | Runtime |
| --- | --- |
| Backend build / test | Gradle 9.6.1 + JDK 21 |
| Frontend build | Node 24 |
| Database | MySQL 8.4 |
| Browser runtime | Playwright 1.61.0 noble |
| Combined frontend gateway | Nginx 1.29.8 alpine |
| Small immutable evidence image | Alpine 3.23 |

固定版本由 scripts/local-ci.sh 的默认值持有；如为诊断临时覆盖镜像，覆盖值属于本轮 execution evidence，不自动成为新的长期默认值。

### 3.1 国内依赖镜像

Local Docker CI 的依赖下载优先使用 Repository-owned 国内镜像配置，但镜像只改变 transport，不改变依赖版本、lockfile 或验证责任：

- Gradle plugin：backend/settings.gradle.kts 优先使用阿里云 gradle-plugin / public repository，并保留 Gradle Plugin Portal 与 Maven Central fallback；
- Backend dependency：backend/build.gradle.kts 优先使用阿里云 public repository，并保留 Maven Central fallback；
- Local Docker Node/npm：scripts/local-ci.sh 默认设置 npm registry 为 https://registry.npmmirror.com/，并要求 lockfile registry host 跟随当前 registry；
- 如需诊断镜像故障，可用 LOCAL_CI_NPM_REGISTRY=https://registry.npmjs.org/ 临时切回 npm 官方 registry；覆盖值属于本轮 execution evidence，不改变长期默认。

GitHub Actions 是远程备用路径，不因为 Local Docker 的 npm transport 默认值而被强制改写为国内 registry。

Docker / OCI 基础镜像 registry 与 npm package registry 是两层不同的 transport。Local Docker CI 不在 Dockerfile 中硬编码公共 Docker Hub mirror；宿主 Docker daemon 持有当前环境的 Registry Mirrors，scripts/local-ci.sh 会把该列表投影为忽略于 Git 的 .local-ci/buildkitd.toml，并用配置指纹维护项目专用 docker-container BuildKit builder。这样 Docker Engine 与 BuildKit 使用同一宿主网络政策，同时不把环境特定的公共 mirror 固化成 Repository Authority。

宿主若没有配置 Docker Hub registry mirror，BuildKit 按官方默认直接访问 Docker Hub；如当前网络无法可靠访问 Docker Hub，应先在宿主 Docker daemon 配置适用 mirror，再由本地 CI 自动继承。

Playwright 官方镜像位于 Microsoft Container Registry，不属于 Docker Hub registry-mirrors 的作用范围。当前国内执行环境默认使用 mcr.m.daocloud.io/playwright:v1.61.0-noble；如需诊断国内 mirror，可用 LOCAL_CI_PLAYWRIGHT_IMAGE=mcr.microsoft.com/playwright:v1.61.0-noble 临时切回官方 MCR。该覆盖只改变镜像 transport，不改变 Playwright 固定版本与验证责任。

## 4. Source subject 与 evidence binding

Local Docker CI 同时记录：

- 当前 HEAD；
- 当前 branch；
- 由实际 tracked / modified / untracked、且未被 .gitignore 排除的输入 bytes 计算的 worktree fingerprint；
- Backend / Frontend / Review Baseline / Review Verification 各自的 input fingerprint；
- 本轮 changed-file set；
- 本轮运行结果与 Docker resource snapshot。

因此 dirty worktree 的 source subject 表示为“HEAD + worktree fingerprint”，不得写成 exact-HEAD PASS；干净 worktree 的 source subject 直接绑定当前 exact HEAD。

本地 Docker 验证读取实际工作树而不是只读取 Git commit，因此已修改但未提交的 tracked 文件，以及未被 .gitignore 排除的 untracked 输入，都可以参加 build / test。Docker build context 本身也不要求文件先提交；是否进入 context 由实际文件系统与 .dockerignore 决定。本项目 Local Docker CI 额外以 worktree fingerprint 约束该行为，避免把 dirty input 的运行结果误标为目标提交证据。

最终 Integration / Completion 需要 exact-Head 证据时，应在目标提交形成后，以干净 worktree 对该 exact Head 再执行完整入口。

本地证据默认写入：

~~~text
.local-ci/evidence/<run-id>/
~~~

.local-ci/ 不进入 Git。成功运行保留最近三个 evidence directory；失败运行保留当前诊断，后续运行会继续执行有界回收。

## 5. 完整 Local Docker CI claim map

默认 full 按当前项目验证责任执行以下链路。

### 5.1 Authority / Design 静态验证

- scripts/verify-docs-governance.mjs；
- 当前固定 Google DESIGN.md CLI 对 docs/design/public-site/DESIGN.md 的 lint。

### 5.2 Backend 与长期运行契约

在容器化 Gradle 9.6.1 / JDK 21 中执行 clean build，并在 Fresh MySQL schema 上覆盖：

- Backend test；
- CMS Server bootJar；
- Content Migration bootJar；
- Site Package foundation；
- stable site structure；
- Runtime Site Package composition；
- bootstrap / baseline separation；
- stable assets；
- page content ownership / adoption；
- link open-mode migration；
- Content Migration application boundary；
- Generic article / page / list migration compatibility。

同时执行 Content Migration source boundary 检查，防止 site-specific migration implementation 回流到 Generic Migration application。

这些验证是原 Backend / Site Package / Generic Migration GitHub workflows 的 Local Docker 对应责任，不因为 Workflow 默认关闭而消失。

### 5.3 前端正式构建

Public 与 Admin 分别执行：

~~~text
npm ci
→ Vue-aware type-check / Vite formal build
~~~

一个前端的成功不能替代另一个前端。

### 5.4 Fresh integrated runtime 与完整 Browser verification

每轮完整验证重新创建 jilinjobs_cms 数据库，使用当前输入生成的 Backend runtime image 与 Public/Admin combined frontend image 启动真实组合：

~~~text
Fresh MySQL
→ current Backend Runtime
→ JilinJobs Site Package bootstrap
→ combined Nginx gateway
→ Public full Playwright
→ Admin full Playwright
~~~

Browser suite 仍由各前端当前 repository tests 持有；Local Docker 只提供执行环境，不维护第二份 case inventory。

### 5.5 Party canonical migration 专项验证

当变更触达当前 canonical workflow 原本负责的输入范围时，full 追加：

- canonical Party dataset structure / resource digest；
- Fresh Site Package provisioning；
- 第一次 Generic Content Migration；
- Runtime DB projection 与 resource bytes；
- 第二次 migration idempotency；
- expected article / list mapping counts。

### 5.6 EU-29 → current migration 兼容性验证

当变更触达当前 migration-upgrade workflow 原本负责的输入范围时，full 追加：

- pinned accepted EU-29 snapshot materialization；
- accepted old runtime import；
- current Generic canonical dataset upgrade；
- list item Runtime identity preservation；
- current re-run idempotency；
- unexpected mapping fingerprint drift 的 conflict rejection。

该路径只消费 Repository / Git 已接受的 pinned source，不重新激活 historical source discovery。

如需诊断或主动重验 specialized paths，可设置：

~~~bash
LOCAL_CI_FORCE_SPECIALIZED=true bash scripts/local-ci.sh
~~~

这只是扩大本轮验证范围，不改变长期 trigger responsibility。

## 6. Reusable artifact 与缓存

本地缓存只降低重复构建和依赖下载成本，不能跳过当前 claim 需要的 tests。

### 6.1 依赖与构建缓存

- Gradle dependency/build cache：.local-ci/cache/gradle；
- npm cache：.local-ci/cache/npm；
- BuildKit：专用 builder jilinjobs-cms-ci。

不执行 daemon-global prune，也不借清理本项目缓存删除其他项目镜像、volume 或 cache。

Backend 的 .gradle / .kotlin / build 输出和 Frontend 的 node_modules / dist 属于可再生本地中间产物，不参与 Repository source identity；对应目录必须保持 ignore，并允许由项目级 clean / Local CI cleanup 重建。

### 6.2 单元测试执行环境

Backend 单元测试默认继续使用 Local Docker 中固定的 Gradle 9.6.1 / JDK 21，不建立第二套宿主机原生 Gradle CI path。容器启动开销相对编译、依赖解析和测试链路较小，而统一 runtime、持久依赖缓存、可控中间产物和 Fresh Runtime 隔离更有价值。

开发者可以把宿主机原生编译作为个人快速诊断，但它不替代默认 Local Docker CI，也不单独形成 Completion evidence。

### 6.3 组件镜像身份

Local runtime image 使用真实 input bytes 的 fingerprint，而不是 monorepo commit SHA 作为唯一身份：

- Backend image；
- Content Migration image；
- combined Frontend runtime image；
- Review Data Baseline image；
- Review Verification marker。

Component image cache hit 只允许复用 matching immutable build/runtime input。Backend test、formal frontend build、Fresh integrated runtime 和完整 Browser claims 仍按完整入口重新执行。

### 6.4 Review Data Baseline 复用

Review Baseline 继续复用既有 Repository-owned 生成链：

~~~text
Generic schema
→ Site Definition
→ stable assets
→ accepted canonical Historical Migration
→ bounded Main review subset
→ logical DB dump + runtime-static + runtime-uploads
~~~

Baseline fingerprint 至少覆盖 Backend identity、schema、Site Package、canonical migration bytes 与 baseline preparation contract。

Baseline 是可再生测试数据起点，不是 ordinary database backup，也不是新的 Historical Migration Authority。

### 6.5 Review Verification marker 生成

只有当前完整运行已经证明：

- Backend formal verification；
- Public / Admin formal build；
- Fresh integrated Public / Admin Browser verification；
- matching Review Baseline 可恢复；
- restored canonical Review Runtime probe；
- Party migration runtime browser probe；

才生成 matching local Review Verification marker。

Marker 只证明对应自动化 fingerprint；不替代 Human Runtime Observation。

## 7. Fresh writable state 与运行隔离

每个完整运行必须获得新的 writable verification state：

- MySQL database 按 claim 显式 drop / create；
- canonical / upgrade runtime 文件位于本轮 .local-ci/work/<run-id>；
- integrated runtime 使用本轮独立 static / upload directory；
- Review Baseline restore 在 probe 前重新恢复；
- run exit 时回收本项目命名 container 与本轮临时目录。

当前 Local Docker CI 是单宿主单 slot 模型，通过 .local-ci/lock 防止两个完整 run 同时争用固定 host ports。

Main historical migration source discovery 仍保持 FROZEN / explicit reactivation only；默认 Local Docker CI 不访问 Legacy Source，也不重新执行 Main source discovery。

## 8. 磁盘与清理边界

成功运行结束时：

- 回收临时 container；
- 删除本轮 writable work directory；
- 只保留当前 input identity 仍可能复用的项目镜像；
- evidence 只保留最近三次；
- 保留项目专用 dependency / BuildKit cache 以加速下一轮。

需要回收项目缓存时使用：

~~~bash
bash scripts/local-ci.sh clean
~~~

该命令的 ownership 仅限 jilinjobs-cms Local CI namespace。禁止把 docker system prune -a --volumes 一类 daemon-global destructive cleanup 固化为项目验证步骤。

## 9. GitHub Actions 备用路径

.github/workflows/** 继续保留，作为远程 fallback 与 GitHub-native 验证实现，不删除其现有 build、artifact、GHCR、browser、migration 和 review-runtime 编排。

### 9.1 默认关闭

普通自动 CI job 统一受 Repository Actions variable 控制：

~~~text
JILINJOBS_GITHUB_CI_ENABLED == "true"
~~~

变量缺失、空值或其他值都表示自动 GitHub CI **关闭**。因此 merge 后不需要额外执行“禁用 workflow”操作即可得到 Local Docker default。

恢复自动 GitHub CI 时，只需在 GitHub Repository Actions variables 中把该变量设置为字符串 true；不需要重写 workflow。

### 9.2 显式备用执行

原本已具有 workflow_dispatch 的完整 CI、canonical migration、EU-30 migration upgrade 与 docs governance 允许显式手工 / Agent dispatch，即使自动 CI gate 关闭。

只支持自动事件的 focused workflows 保留原事件定义；需要恢复时通过同一个 repository variable 一次性启用。

eu29-source-discovery.yml 保持其 explicit high-cost source-discovery 语义，不受 default CI gate 驱动。

review-environment.yml 保持 workflow_dispatch / 显式 human-review activation 语义，不因 Local Docker 成为默认 CI 而自动激活或取消。

### 9.3 GitHub fast / full 语义

如果切回 GitHub Actions：

- Fast CI 仍只形成 bounded development-feedback evidence；
- Backend GHCR fingerprint 继续表示 Backend build-input identity，而 Repository SHA 表示 source provenance；
- GHCR cache hit 只能节省 build，不得省略目标 claim 需要的 Runtime / tests；
- Full CI 仍从目标 source 执行 Backend formal verification、双前端 build、Fresh MySQL、完整 Browser 与 Review Runtime cache chain；
- Registry lookup 仍必须区分 NOT_FOUND 与 auth / network / unknown failure；
- Review Frontend / Baseline / Verification marker 继续按 fingerprint immutable reuse；
- Human Review Environment 只消费 matching verified runtime，不把 cache hit 改称新的自动化证据。

Actions 与 Local Docker 是同一 Consumer verification responsibility 的两种执行映射；不得把某个 Workflow 是否运行本身提升为 Feature Acceptance。

## 10. 变更与维护

改变以下任一项时，需要同时检查本文、scripts/local-ci.sh、相关 runtime scripts 与 fallback workflows 是否仍一致：

- Backend / Frontend build-input boundary；
- 固定 toolchain / image version；
- Fresh DB / Runtime composition；
- Site Package / Migration verification responsibility；
- Browser runtime；
- Review Baseline / Review Verification fingerprint；
- GitHub fallback gate；
- Local CI cache / cleanup namespace。

新增 focused GitHub workflow 时，若它承载默认 automated verification responsibility，必须同时决定其 Local Docker claim mapping 以及是否纳入 JILINJOBS_GITHUB_CI_ENABLED gate，不能只在远程路径偷偷增加新的唯一验证责任。

## 11. 完成证据

Local Docker 完整验证的最小证据包括：

- source subject；
- component fingerprints；
- changed-file set 与 specialized-path decision；
- command logs / browser results；
- PASS / FAIL；
- 运行前后 Docker resource state；
- cleanup 后无遗留本轮 runtime container。

GitHub fallback 的完成证据继续绑定 repository / event / exact commit / Run / Job / conclusion。

无论使用哪条执行路径，Completion claim 都必须满足 docs/technical/verification-strategy.md 的 claim / subject / currentness 要求；执行环境切换本身不能把“未运行”解释为“已通过”。
