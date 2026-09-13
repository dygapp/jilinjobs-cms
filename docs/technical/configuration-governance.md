# 配置职责与硬编码治理

## 1. 目的

本文定义 `jilinjobs-cms` 对代码常量、CMS 运营数据、CMS metadata、Spring externalized configuration、Site Package 与 CI / Deployment variables 的长期责任边界。

核心原则：**硬编码本身不是缺陷。只有本应存在部署差异、站点差异或运营维护价值的值被错误固定，才需要配置化。禁止以消除字面常量为目标机械配置化。**

## 2. 配置责任分类

### 2.1 代码常量

适用于稳定领域契约、安全 / protocol 规则、页面模板 contract 与实现算法参数，例如：

- `ContentImagePolicy`、打开方式、内容类型等稳定 enum；
- HTTP / HTTPS URL safety rules；
- Code / Alias / Key 格式与长度；
- 上传文件真实签名 / allow-list 等安全边界；
- `MAIN / HOME_SHORTCUT / HOME_QUICK / HOME_CAROUSEL` 等页面模板稳定 identity；
- 没有运营价值的 UI / algorithm parameters。

### 2.2 CMS 运营数据 / SiteProperty

适用于管理员需要在 Runtime 维护的数据或低风险行为参数：

- 网站名称、联系方式、备案、版权；
- 可运营 Logo / Banner 等 Resource path；
- `CAROUSEL_INTERVAL_SECONDS / CAROUSEL_MAX_ITEMS`；
- 栏目、导航、单页、列表、宣传展示等普通 Runtime content。

SiteProperty 不是通用“系统设置”容器。DB connection、安全限制、基础设施地址、deployment parameters 不进入 CMS SiteProperty。

### 2.3 CMS metadata

适用于低频、结构性、通常由开发 / 部署维护、但不需要 DB lifecycle 的 CMS definitions。

Current `cms-metadata.yml` 至少负责 SiteProperty group definitions。只有真实需求证明需要独立 Runtime lifecycle 时才建立新的 DB management object。

### 2.4 Spring externalized configuration

适用于部署实例差异，例如：

- DB connection；
- Backend port；
- Runtime storage root；
- Site Package root；
- fixed protected-resource paths；
- 未来真实具有部署差异的 infrastructure parameters。

### 2.5 JilinJobs Site Package

适用于 JilinJobs 产品级、版本化且需要随 Repository 一致恢复的 stable site definition：

- `sites/jilinjobs/structure/**`；
- `sites/jilinjobs/bootstrap/**`；
- `sites/jilinjobs/assets/**`；
- package manifest / asset catalog / digest metadata。

Site Package 不是 Spring deployment configuration，也不是 ordinary operator Runtime data。

### 2.6 CI / Repository / Deployment Variables

GitHub Actions、Review Environment、FRP、临时域名 / proxy name、CI runner parameters 属于 CI / deployment responsibility，不进入 CMS。

## 3. StaticResource protection

Current `protectedResource` 表示资源不能通过 ordinary delete 移除。Backend 合并至少三类保护来源：

1. **固定部署保护**：Spring externalized configuration 声明的 fixed protected paths；
2. **Site Package stable asset protection**：`sites/jilinjobs/assets/**` manifest/catalog 声明的 stable `/static/**` target；
3. **Runtime direct reference protection**：当前 enabled SiteProperty `RESOURCE_PATH`、CmsList effective/override image、Advertisement image、Navigation iconPath 等直接引用。

不得把保护状态改为管理员手工维护的 `protected=true` 重要性标记。

资源进入/退出 Runtime direct reference 集合时应自动影响保护状态。Site Package stable target 在 package ownership 持续期间始终受保护。

普通 DELETE 必须由 Backend 最终拒绝 protected resource；明确 replace 继续按 current contract 允许。该机制不宣称扫描所有 Rich HTML / CSS / JS reference，因此对普通资源删除仍需保留风险提示。

## 4. 硬编码审计方法

Review 发现 literal value 时先判断：

1. 是否属于稳定领域 / security / protocol / template contract？→ code constant；
2. 是否需要管理员 Runtime 维护？→ CMS data / SiteProperty；
3. 是否属于低频结构 metadata？→ CMS metadata resource；
4. 是否具有部署实例差异？→ Spring externalized configuration；
5. 是否属于 JilinJobs stable product definition？→ Site Package；
6. 是否只服务 CI / Review / deployment？→ CI / Deployment Variables。

只有责任归属明确后才能配置化。

## 5. Current examples

| 项目 | 当前责任 | 处理 |
|---|---|---|
| 上传扩展名 / real media signature | 安全边界 | 代码常量 |
| `MAIN / HOME_SHORTCUT / HOME_QUICK / HOME_CAROUSEL` | stable template identity | 保持稳定 code |
| `CAROUSEL_INTERVAL_SECONDS / CAROUSEL_MAX_ITEMS` | 低风险运营行为 | SiteProperty |
| `sites/jilinjobs/assets/**` target / digest | stable product asset | Site Package manifest/catalog |
| `/static/uploads/**` | mutable Runtime content | CMS Runtime store |
| Review FRP server/domain/proxy | CI / deployment | Repository / Environment variables |
| DB URL / credentials / port | deployment instance | Spring / environment |

旧 `HOME_CAROUSEL_INTERVAL_SECONDS` 只属于 superseded history，不是 Current SiteProperty。

## 6. Review 要求

后续 Finding 不能只说“这里写死了”。必须说明：

- 值的真实变化来源与维护者；
- 应归入哪一 responsibility；
- 配置化后的 default / override / failure behavior；
- 是否错误扩大管理员可修改范围；
- 是否削弱 security / protocol / lifecycle boundary。

无法回答这些问题时，默认保留稳定代码常量而不是增加配置复杂度。