# Main ListItem 调整报告

## 范围

本报告固化 EU-50 当前 Main 稳定站点链接 ListItem 的人工确认调整结果，归属 **JilinJobs Site Package**。

它不是 Historical Migration Dataset，也不是当前 Runtime ListItem provisioning 输入。Site Package v1 尚未具备 stable `list-items` provision/reconcile 能力；该能力仍需单独的 Site Package Authority / Execution Unit。

## 当前结果

- 审计列表：`SITE_REGIONAL_GRADUATES`、`SITE_RELATED`、`SITE_JILIN_UNIVERSITIES`
- Legacy Source 唯一站点链接候选：95
- 因重复 URL 身份折叠而恢复的 Source occurrence：1
- 调整后参与链接审计的 occurrence：96
  - `SITE_REGIONAL_GRADUATES`：31
  - `SITE_RELATED`：5
  - `SITE_JILIN_UNIVERSITIES`：60
- 保持 Legacy Source URL 不变：94
- 人工确认调整：2
- 已解决 handoff observation：1（`HOME_SITE_LINK_DUPLICATE_URL`）

`HOME_CAROUSEL` 不在本次站点链接审计范围内。EU-50 handoff 中的 ListItem candidate count 也恰好为 96，但它包含 `HOME_CAROUSEL`，同时区域就业站只保留了重复 URL 去重后的 30 条，因此两个 “96” 不是同一数据集合。

## 人工确认调整

| 列表 | 顺序 | 标题 | Legacy Source URL | 最终确认 URL | 决策 |
| --- | ---: | --- | --- | --- | --- |
| `SITE_REGIONAL_GRADUATES` | 18 | 湖北省高等院校毕业生就业信息网 | `http://jyzdzx.scedu.net/` | `https://www.hbbys.com.cn/` | 人工确认替换错误的重复 URL |
| `SITE_REGIONAL_GRADUATES` | 22 | 四川省高等院校毕业生就业信息网 | `http://jyzdzx.scedu.net` | `https://bigdata.scbdc.edu.cn:8888/` | 恢复被重复 URL identity 折叠的 occurrence，并采用人工确认 URL |

这两个明确不同的最终目标共同解决：

- identity：`LIST:SITE_REGIONAL_GRADUATES:HOME_SITE_LINK_DUPLICATE_URL`
- code：`HOME_SITE_LINK_DUPLICATE_URL`
- 原 classification：`DUPLICATE_LIST_LINK_REQUIRES_REVIEW`
- resolution：`SITE_PACKAGE_LIST_LINK_DUPLICATE_RESOLVED_BY_HUMAN_DECISION`

## 网络审计证据

ListItem Link Audit Run `34323764980` / Run #2（Head `547ac9453fd4ca6b85949810f3991d02f172e02f`）结果：

- Total：96
- HTTP/curl 2xx/3xx successful：30
- Probe failure / non-2xx/3xx：66
- Redirected / final URL changed：25

Artifact：

- ID：`10092940455`
- Name：`eu50-listitem-link-audit-547ac9453fd4ca6b85949810f3991d02f172e02f`
- Digest：`sha256:b0cfd4549c700f0b84edf19bb620b2ebc4c2102b46f7a8dc727ee9d841b5fa16`

湖北、四川两条人工确认目标在该次 GitHub-hosted runner 探测中均发生 timeout（HTTP `000`）。该结果只表示一次网络可达性观测，**不推翻人工确认的数据调整决策**；同理，其他 94 条不会因为一次探测失败、重定向或非 2xx/3xx 状态而被静默改写或删除。

## Source provenance

冻结 Source Evidence：

- Run：`34303771704`
- Artifact ID：`10086056781`
- Artifact：`eu50-main-retry-attempt-3-a96cee22449508f92f3c89789f99aad477286a66`
- Digest：`sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`
- Source Head：`a96cee22449508f92f3c89789f99aad477286a66`

机器可读决策见同目录 `listitem-adjustment-report.json`。
