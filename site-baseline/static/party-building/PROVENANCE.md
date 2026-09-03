# 中心党建稳定视觉资源来源

本目录保存经 2026-09-02～2026-09-03 原站 Browser Evidence 验证的中心党建稳定视觉资源与来源说明。`party-building` 是既有兼容性技术目录名。

## Header Banner

原站：`https://24365.jl.smartedu.cn/dyzj`

原始 Banner：

- URL：`https://24365.jl.smartedu.cn/webfile/theme2/img/party_banner.png`
- 原始尺寸：3072 × 512
- 原始 SHA-256：`7444d50235d4c87a00d0221ac84551ea083c617bb8a15e58f58d002224bd27a3`
- Reference Evidence 文件：`original-resources/21-party_banner.png`
- 注意：原站 URL/文件名扩展名为 `.png`，但 Reference Evidence 对原始字节识别为 JFIF/JPEG。该事实不影响资源真实性；不得仅因扩展名再次转码。

### Human Review 结论

早期曾产生：

- 960×160 WebP：因低分辨率上采样被 Human Review 否决；
- 3072×512 AVIF：虽然保留像素尺寸，但二次有损编码仍在标题文字边缘产生可见毛刺，被 2026-09-03 Human Review 否决。

因此当前正式页面直接使用上述原站原始资源 URL，停止对 Banner 进行 WebP/AVIF 二次有损转码。仓库内既有 `party-header-banner.avif` 仅保留为本 PR 历史派生证据，不再是正式页面 Banner 依赖，也不得被 Review fixture 用作轮播图片。

如果后续为了消除外部运行时依赖而把原始资源纳入版本化静态包，必须 byte-for-byte 保存原站原始文件，并验证 SHA-256 仍为 `7444d502...`；不得重新编码后声称为原始资源。

Banner 是纯视觉内容，不承担导航：正式 DOM 使用非链接容器与 `<img>`，不包裹 `<a>`。

## UI Markers

- `ic-title-yellow.png`
  - 原站文件：`ic_title_yellow@2x.png`
  - SHA-256：`187493b0b6ac2d1b673b28d22a355d4539e35b91b85cbab49b68cf9986b67b40`
  - 用途：高层声音列表黄色标记。
- `section-marker.png`
  - 原站文件：`mark_y@2x.png`
  - SHA-256：`7e11fddb8e600534593c2afeb9af20226c4850cef58196eff1bb72fcca4be457`
  - 用途：工作动态、学习园地标题左侧标记。

## Evidence Boundary

原站 `html, body { min-width: 1200px }` 与固定 1200px 内容宽度只作为历史证据；新版保留可证明的视觉关系并实现窄屏响应式，不复制横向溢出。

历史文章正文图片、附件和中心党建轮播成员属于运营内容，由 EU-29 迁移，不进入本稳定视觉资源目录。
