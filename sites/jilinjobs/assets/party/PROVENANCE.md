# 中心党建稳定视觉资源来源

本目录保存经 2026-09-02～2026-09-03 原站 Browser Evidence 验证、且属于党员之家（**Party Members’ Home**）稳定页面视觉的工程资产。目录名 `party` 是已落地兼容性技术标识，不再作为“党员之家”的正式英文翻译。历史文章正文图片、附件和轮播成员属于运营内容，不进入本目录。

## Header Banner

- 原站页面：`https://24365.jl.smartedu.cn/dyzj`
- 原站资源地址：`https://24365.jl.smartedu.cn/webfile/theme2/img/party_banner.png`
- 原始文件名虽为 `.png`，实际媒体类型经 `file` / Pillow 验证为 **JPEG / JFIF**；因此版本化文件使用正确扩展名 `party-header-banner.jpg`。
- 原始尺寸：3072 × 512
- 原始文件大小：1,213,296 bytes
- 原始 SHA-256：`7444d50235d4c87a00d0221ac84551ea083c617bb8a15e58f58d002224bd27a3`
- 当前仓库资源：`party-header-banner.jpg`
- 当前 Git blob SHA：`926e7d67ae530e2e8135a6631ac0fda7b876b743`
- 处理：**原始字节直接入库，不做重采样、不做 WebP/AVIF 转码、不重新编码、不裁剪。** 页面正式运行只引用 `/static/party/party-header-banner.jpg`，不得依赖原站资源地址。

### 已否决的派生方案

早期 `party-header-banner.webp` 曾将原图缩放至 960 × 160 后放大显示，造成明显模糊；后续 3072 × 512 AVIF 虽保留像素尺寸，但 Human Visual Review 仍发现文字边缘毛刺。两种派生资源均已退出正式基线，不得再次用于页面模板。

## UI Markers

- `ic-title-yellow.png`
  - 原站文件：`ic_title_yellow@2x.png`
  - 原始 SHA-256：`187493b0b6ac2d1b673b28d22a355d4539e35b91b85cbab49b68cf9986b67b40`
  - 用途：高层声音等资讯列表前置黄色标记。
- `section-marker.png`
  - 原站文件：`mark_y@2x.png`
  - 原始 SHA-256：`7e11fddb8e600534593c2afeb9af20226c4850cef58196eff1bb72fcca4be457`
  - 用途：工作动态、学习园地等标题左侧竖向标记。

## 外部资源边界

公开站设计模板所需的稳定图片、图标、二维码、字体等展示资源必须进入本项目版本化静态基线或受控 CMS 静态资源，不允许模板直接依赖第三方资源地址。业务跳转链接、文章外链、外部平台入口不属于静态展示资源；开源 JS/CSS 依赖按项目依赖管理规则处理。

## Evidence Boundary

原站 CSS 同时显示 `html, body { min-width: 1200px }` 与 1200px 固定主内容宽度。该行为只作为旧站实现证据；新版按现行 Requirement 保留视觉比例与层级，但在窄屏正常响应式重排，不复制横向溢出。
