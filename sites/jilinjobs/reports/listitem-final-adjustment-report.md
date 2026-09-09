# Main ListItem 最终调整报告

## Status

- Owner: JilinJobs Site Package
- Date: 2026-09-09
- Source occurrences audited: **96**
- Adjusted: **72**（人工明确确认 2；Authority / 官方站点核验 70）
- Unchanged after review: **24**
- Historical Migration input: **NO**
- Runtime stable ListItem provisioning input: **NO — capability gap remains**

本报告固化 EU-50 对 Main 三组稳定站点链接 ListItem 的最终内容审核结论。HTTP / redirect / title 探测只用于发现异常候选；网络失败、403/429 等状态本身不构成自动改写、删除或丢弃数据的依据。只有人工明确确认，或能够由当前官方 Authority 建立更高置信度的名称/官网/继任服务平台，才进入调整结果。

## Summary

| List | Audited | Adjusted | Unchanged |
| --- | ---: | ---: | ---: |
| `SITE_RELATED` | 5 | 3 | 2 |
| `SITE_REGIONAL_GRADUATES` | 31 | 31 | 0 |
| `SITE_JILIN_UNIVERSITIES` | 60 | 38 | 22 |
| **Total** | **96** | **72** | **24** |

## Human-confirmed corrections

| List | Source order | Source | Accepted target |
| --- | ---: | --- | --- |
| `SITE_REGIONAL_GRADUATES` | 18 | 湖北省高等院校毕业生就业信息网 — `http://jyzdzx.scedu.net/` | 湖北省高等院校毕业生就业信息网 — `https://www.hbbys.com.cn/` |
| `SITE_REGIONAL_GRADUATES` | 22 | 四川省高等院校毕业生就业信息网 — `http://jyzdzx.scedu.net/` | 四川大学生就业服务平台 — `https://bigdata.scbdc.edu.cn:8888/` |

Legacy Source 中湖北、四川错误共享同一 URL。两条 source occurrence 均保留 provenance，并形成两个不同的 Site Package 内容决策；同时关闭 `DUPLICATE_LIST_LINK_REQUIRES_REVIEW` handoff observation。

## `SITE_RELATED` adjustments

1. `#2` 国家24365大学生就业服务平台 → **国家大学生就业服务平台**；`https://www.ncss.cn/`
2. `#3` 学历认证 → `https://www.chsi.com.cn/xlrz/paper/report/gdjyxl.action`
3. `#5` 吉林省教育厅 → `https://jyt.jl.gov.cn/`

## `SITE_REGIONAL_GRADUATES` adjustments

1. 北京市高等院校毕业生就业信息网 → **北京毕业生就业创业服务平台** — `https://fuwu.rsj.beijing.gov.cn/bjdkhy/bysjycy/`
2. 天津市高等院校毕业生就业信息网 → **天津公共就业服务网智慧招聘频道** — `https://www.cnthr.com/`
3. 河北省高等院校毕业生就业信息网 → **河北高校毕业生招聘专区** — `https://rst.hebei.gov.cn/ggzp/ww/a/b/wwab_gxbyszp.html`
4. 山西省高等院校毕业生就业信息网 → **山西省公共招聘网** — `https://sxjy.rst.shanxi.gov.cn/sxscwebui/`
5. 内蒙古高等院校毕业生就业信息网 → `https://zph.nmrc.com.cn/`
6. 山东省高等院校毕业生就业信息网 → `https://www.sdgxbys.cn/col/sdjyfwpt/index.html`
7. 江苏省高等院校毕业生就业信息网 → **江苏省人才服务云平台** — `https://www.jssrcfwypt.org.cn/rcfwypt/?areaCode=320000`
8. 安徽省高等院校毕业生就业信息网 → **安徽公共招聘网高校毕业生专区** — `https://www.ahggzp.gov.cn/ww/b/a/wwba_graduates.html`
9. 浙江省高等院校毕业生就业信息网 → **浙江人才服务网** — `https://www.zjrc.com/zjrcw/#/activityList`
10. 福建省高等院校毕业生就业信息网 → **福建省毕业生就业创业公共服务网** — `https://220.160.52.58/`
11. 上海市高等院校毕业生就业信息网 → **乐业上海第一站** — `https://jobs.rsj.sh.gov.cn/ggzp-shrs/index.html#/`
12. 辽宁省高等院校毕业生就业信息网 → **辽宁省大学生就业创业服务平台** — `http://bys.lnrc.com.cn/index.do`
13. 吉林省高等院校毕业生就业信息网 → **吉林人才网** — `https://www.jlrc.com.cn/ww/index.html`
14. 黑龙江省高等院校毕业生就业信息网 → **黑龙江省大学生就业创业服务平台** — `https://www.hljbys.org.cn/`
15. 广东省高等院校毕业生就业信息网 → **广东公共求职招聘服务平台高校毕业生专区** — `https://ggfw.hrss.gd.gov.cn/recruitment/internet/main/#/graduateZone`
16. 广西省高等院校毕业生就业信息网 → **广西毕业生就业服务平台** — `https://bys.gxrc.com/`
17. 海南省高等院校毕业生就业信息网 → **海南省公共招聘网** — `https://zhaopin.hainan.gov.cn/#/recruit/home`
18. 湖北省高等院校毕业生就业信息网 → `https://www.hbbys.com.cn/`（人工确认）
19. 湖南省高等院校毕业生就业信息网 → **湖南大学生就业服务平台** — `https://employment.hunan.smartedu.cn/`
20. 河南省高等院校毕业生就业信息网 → **河南省大学生就业服务平台** — `https://hnbysjy.jyt.henan.gov.cn/zp/`
21. 江西省高等院校毕业生就业信息网 → **江西人才服务网** — `https://www.jxrcfw.com/`
22. 四川省高等院校毕业生就业信息网 → **四川大学生就业服务平台** — `https://bigdata.scbdc.edu.cn:8888/`（人工确认）
23. 云南省高等院校毕业生就业信息网 → `https://www.ynhr.com/index.php?m=home&c=gxfw&a=index`
24. 贵州省高等院校毕业生就业信息网 → `https://gzggzpw.gzsrs.cn/app/graduates/graduates.shtml`
25. 西藏自治区高等院校毕业生就业信息网 → `https://www.xzggjyfw.cn/`
26. 重庆市高等院校毕业生就业信息网 → `https://ggfw.rlsbj.cq.gov.cn/cqjy/`
27. 宁夏高等院校毕业生就业信息网 → `http://www.nxjob.cn/wcms/nxjob/byszp/`
28. 新疆高等院校毕业生就业信息网 → `https://www.xjggjy.com/`
29. 青海省高等院校毕业生就业信息网 → `https://qhrsggfw.org.cn/qhrst/index/qhrs/talant/index.jspx`
30. 陕西省高等院校毕业生就业信息网 → `https://job.snhrm.com/app/article/content/newArticleAction.shtml`
31. 甘肃省高等院校毕业生就业信息网 → `https://www.gszhaopin.com/`

## `SITE_JILIN_UNIVERSITIES` adjustments

1. `#1` 吉林大学 → `https://www.jlu.edu.cn/`
2. `#2` 东北师范大学 → `https://www.nenu.edu.cn/`
3. `#4` 延边大学 → `https://www.ybu.edu.cn/`
4. `#5` 吉林农业大学 → `https://www.jlau.edu.cn/`
5. `#7` 长春工业大学 → `https://www.ccut.edu.cn/`
6. `#8` 东北电力大学 → `https://www.neepu.edu.cn/`
7. `#12` 吉林外国语大学 → `https://www.jisu.edu.cn/`
8. `#13` 长春大学 → `https://www.ccu.edu.cn/`
9. `#14` 吉林建筑大学 → `https://www.jlju.edu.cn/`
10. `#15` 吉林化工学院 → **吉林化工大学** — `https://www.jluct.edu.cn/`
11. `#16` 长春工程学院 → `https://www.ccit.edu.cn/`
12. `#17` 吉林农业科技学院 → `https://www.jlnku.edu.cn/`
13. `#18` 吉林医药学院 → `https://www.jlmu.cn/`
14. `#19` 长春师范大学 → `https://www.ccsfu.edu.cn/`
15. `#21` 吉林工程技术师范学院 → `https://www.jlenu.edu.cn/`
16. `#22` 白城师范学院 → `https://www.bcnu.edu.cn/`
17. `#23` 吉林工商学院 → `https://www.jlbtc.edu.cn/`
18. `#25` 吉林艺术学院 → `https://www.jlart.edu.cn/`
19. `#26` 吉林警察学院 → `https://www.jljcxy.com/`
20. `#27` 吉林动画学院 → `https://www.jlai.edu.cn/`
21. `#28` 吉林省教育学院 → `https://www.jlsjyxy.com.cn/`
22. `#29` 吉林广播电视大学 → **吉林开放大学**；URL 保持 `http://www.jlrtvu.jl.cn/`
23. `#30` 吉林经济职业技术学院 → **吉林省经济管理干部学院** — `https://www.jlemcc.edu.cn/`
24. `#31` 长春汽车工业高等专科学校 → **长春汽车职业技术大学** — `https://www.caii.edu.cn/`
25. `#35` 吉林交通职业技术学院 → `https://www.jljy.edu.cn/`
26. `#38` 吉林铁道职业技术学院 → **吉林铁道职业技术大学** — `https://www.jty.edu.cn/`
27. `#39` 吉林农业工程职业技术学院 → **吉林工程职业学院** — `https://www.jlevc.cn/`
28. `#40` 吉林司法警官高等职业学院 → **吉林司法警官职业学院** — `https://www.jlsfjy.cn/`
29. `#43` 长春职业技术学院 → **长春职业技术大学** — `https://www.cvit.edu.cn/`
30. `#45` 白城职业技术学院 → `https://www.bcvit.cn/`
31. `#48` 长春东方职业学院 → **长春医药职业学院** — `https://www.dfzyxy.net/`
32. `#49` 长春信息职业技术学院 → **长春信息技术职业学院** — `https://www.citpc.edu.cn/`
33. `#50` 吉林科技职业技术学院 → `https://www.jilinkj.com/`
34. `#51` 东北师范大学人文学院 → **长春人文学院** — `https://www.ccrw.edu.cn/`
35. `#53` 长春大学旅游学院 → `https://www.tccu.edu.cn/`
36. `#54` 长春理工大学光电信息学院 → **长春电子科技学院** — `https://www3.changdian2001.com/`
37. `#57` 吉林师范大学博达学院 → `https://www.bdxy.com.cn/`
38. `#60` 长春建筑学院 → `https://www.jladi.edu.cn/`

## Unchanged after review

以下 24 条没有建立足够高置信度的错误/继任证据，因此保持 Legacy Source 值。HTTP 探测结果只作为审计观察，不单独作为内容调整 Authority。

### `SITE_JILIN_UNIVERSITIES` — 22

- `#3` 长春理工大学 — `http://www.cust.edu.cn/`（audit 403）
- `#6` 北华大学 — `http://www.beihua.edu.cn/`
- `#9` 长春中医药大学 — `http://www.ccucm.edu.cn//`
- `#10` 吉林师范大学 — `http://www.jlnu.edu.cn/`
- `#11` 吉林财经大学 — `http://www.jlufe.edu.cn/`
- `#20` 通化师范学院 — `http://www.thnu.edu.cn/`（audit final HTTPS 但 429）
- `#24` 吉林体育学院 — `http://www.jlsu.edu.cn/index.html`
- `#32` 长春金融高等专科学校 — `http://www.cjgz.edu.cn/`
- `#33` 长春医学高等专科学校 — `http://www.ccmc.edu.cn/`
- `#34` 白城医学高等专科学校 — `http://www.bcyz.cn/`
- `#36` 吉林电子信息职业技术学院 — `http://www.jltc.edu.cn/`
- `#37` 吉林工业职业技术学院 — `http://www.jvcit.edu.cn/`
- `#41` 四平职业大学 — `http://www.spvu.edu.cn/`
- `#42` 辽源职业技术学院 — `http://www.lyvtc.cn/`
- `#44` 松原职业技术学院 — `http://www.sypt.cn/`
- `#46` 长白山职业技术学院 — `http://www.cbsvtc.com.cn/`
- `#47` 延边职业技术学院 — `http://www.ybvtc.com/`
- `#52` 长春光华学院 — `http://www.ghu.edu.cn/`
- `#55` 长春工业大学人文信息学院 — `http://www.ccutchi.com/`
- `#56` 长春科技学院 — `http://www.jlaudev.com.cn/`
- `#58` 长春财经学院 — `http://www.ccufe.edu.cn/`
- `#59` 吉林建筑科技学院 — `http://www.jluat.edu.cn/`

### `SITE_RELATED` — 2

- `#1` 中国高等教育学生信息网 — `https://www.chsi.com.cn/`
- `#4` 全国征兵网 — `https://www.gfbzb.gov.cn/`

## Evidence

- Frozen Source Evidence: Run `34303771704`, artifact `10086056781`, digest `sha256:66118e4f21bf7644db1c97e2a631eee5d4902410f167606286e1280293620494`。
- ListItem Link Audit: Run `34323764980`, artifact `10092940455`, digest `sha256:b0cfd4549c700f0b84edf19bb620b2ebc4c2102b46f7a8dc727ee9d841b5fa16`。
- 单条调整的 accepted target 与决策已固化在本报告；外部核验在 EU-50 审核过程中完成。当前 `listitem-adjustment-report.json` 仅保存两条人工确认与 duplicate observation resolution，不代表全部 72 条调整。

## Runtime note

当前 Runtime 不会因为本报告自动新增、修改或恢复这些 ListItems。stable `list-items` provisioning/reconcile 仍是单独的 Site Package capability gap；后续实现应消费本报告中的最终 title/URL 决策，而不是重新从 Legacy Source 猜测。
