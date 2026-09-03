---
id: requirement-party-building-positioning
title: 中心党建业务定位补充需求
type: business-requirement
status: confirmed
version: "V1.0"
classification:
  - l1-06
  - l2-29
relations:
  upstream:
    - docs/requirements/information-publishing.md
  downstream:
    - docs/specifications/party-building.md
    - docs/technical/party-building-frontend.md
created_at: 2026-09-03
updated_at: 2026-09-03
---

# 中心党建业务定位补充需求

## 1. Authority

本文固化 2026-09-03 Human Review 对中心党建业务定位、Banner 和轮播名称的明确修正。

当 `docs/requirements/information-publishing.md` V4.8 或其历史版本说明中出现“中心党建独立站点”“中心党建首页”“党员之家首页”等与本文冲突的**当前业务语义**时，以本文为准。历史架构演进事实可以保留，但不得再据此把中心党建产品定位成第二个网站或独立首页。

## 2. 业务定位

1. “中心党建”是中心主站信息架构下的特殊栏目/专题页面。
2. 主导航中的“中心党建”进入 `/party/`，该地址称为“中心党建入口页”，不称为“党建首页”。
3. `/party/**`、`party.html`、独立 App/Router 和红色内容主题属于前端隔离实现；它们不改变中心党建属于主站的业务关系。
4. 中心党建与主站的主导航、Footer 应使用共享组件，结构与交互保持一致，仅主题色不同。

## 3. Banner

1. Banner 使用原站资源 `https://24365.jl.smartedu.cn/webfile/theme2/img/party_banner.png`。
2. 不对该资源进行 WebP/AVIF 二次有损转码后再作为正式 Banner。
3. Banner 只显示图片/其中已有文字，不承担导航功能；DOM 不得把 Banner 包装成 `<a>`。

## 4. 中心党建轮播

1. 产品名称统一为“中心党建轮播”，不使用“中心党建首页轮播”。
2. 当前稳定 CmsList code 统一为 `PARTY_CAROUSEL`。
3. 已执行 V14 中的 `PARTY_HOME_CAROUSEL` 通过后续 Migration 原地重命名，不修改历史 Migration，也不更换列表 ID。
4. 代码、测试、Review fixture、Specification、Technical Plan、Execution Units 的当前语义不得继续使用 `HOME`/“首页”指代中心党建入口页或轮播。

## 5. Acceptance

- `/party/` Banner 使用原站资源且不可点击；
- 管理端列表显示“中心党建轮播”；
- 公共 API 使用 `PARTY_CAROUSEL`；
- 当前 Party 专项文档明确“主站特殊栏目/专题页面”的业务定位；
- 技术隔离与业务定位不再混为同一概念。
