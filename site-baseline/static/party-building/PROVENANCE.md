# 中心党建稳定视觉资源来源

本目录只保存经 2026-09-02～2026-09-03 原站 Browser Evidence 验证、且属于党员之家（**Party Members’ Home**）稳定页面视觉的工程资产。目录名 `party-building` 是已落地兼容性技术标识，不再作为“党员之家”的正式英文翻译。历史文章正文图片、附件和首页轮播成员属于运营内容，不进入本目录。

## Header Banner

- 原站：`https://24365.jl.smartedu.cn/dyzj`
- 原始资源：`https://24365.jl.smartedu.cn/webfile/theme2/img/party_banner.png`
- 原始尺寸：3072 × 512
- 原始 SHA-256：`7444d50235d4c87a00d0221ac84551ea083c617bb8a15e58f58d002224bd27a3`
- 当前仓库资源：`party-header-banner.avif`
- 当前仓库尺寸：3072 × 512
- 当前仓库 SHA-256：`0dff03d5a9e826fac83ee4e36a0db201bb08e42f67aa35cb13a9feca08c7ae3e`
- 当前 Git blob SHA：`914cb30e8d5e0bc81febd55faddac8fa726982bf`
- 处理：保持原始 3072 × 512 像素尺寸，只转换为 AVIF 以降低版本化资源体积；不重新设计、不裁剪、不加入非原站素材。
- Human Review 修正：早期 `party-header-banner.webp` 曾缩放至 960 × 160，再在 Desktop 以 320px 高展示，产生明显上采样模糊。该派生方案已被 2026-09-03 Human Visual Review 否决；正式页面不得再使用低分辨率版本。

## UI Markers

- `ic-title-yellow.png`
  - 原站文件：`ic_title_yellow@2x.png`
  - 原始 SHA-256：`187493b0b6ac2d1b673b28d22a355d4539e35b91b85cbab49b68cf9986b67b40`
  - 用途：高层声音等资讯列表前置黄色标记。
- `section-marker.png`
  - 原站文件：`mark_y@2x.png`
  - 原始 SHA-256：`7e11fddb8e600534593c2afeb9af20226c4850cef58196eff1bb72fcca4be457`
  - 用途：工作动态、学习园地等标题左侧竖向标记。

## Evidence Boundary

原站 CSS 同时显示 `html, body { min-width: 1200px }` 与 1200px 固定主内容宽度。该行为只作为旧站实现证据；新版按现行 Requirement 保留视觉比例与层级，但在窄屏正常响应式重排，不复制横向溢出。
