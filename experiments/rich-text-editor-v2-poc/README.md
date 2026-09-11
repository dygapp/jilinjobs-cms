# Rich Text Editor V2 PoC

该目录只承载 Rich Text Authoring V2 的隔离 Planning Evidence，不是生产实现，也不授予新的 Execute Authority。

当前候选：

- Jodit 4.15.0
- SunEditor 3.3.3

自动化 PoC 使用仓库真实 Party Article 与 Main `teacher-library` Page HTML，验证历史 HTML round-trip、普通中文编辑、Office-shaped clipboard 结构与 CMS Resource thin adapter。真实 Windows 中文 IME、Microsoft Word 和 WPS Writer 剪贴板行为必须由人工桌面环境验证。

## Human Review

Windows 本地执行：

```bash
git fetch origin
git switch experiment/rich-text-editor-v2-poc
cd experiments/rich-text-editor-v2-poc
npm install
npm run human-review
```

浏览器打开：

```text
http://127.0.0.1:4173/
```

页面会同屏加载 Jodit / SunEditor，并提供相同初始内容、实时 HTML 输出、结果表和“复制人工验证结果”按钮。

只需完成四项人工 Gate：

1. **H1 Windows 中文 IME**：使用真实 Windows 中文输入法连续输入、选词、退格、中英文切换，并在粗体、列表、表格中继续输入。
2. **H2 Microsoft Word 粘贴**：粘贴含中文标题、段落、粗体/颜色、编号列表、3×3 表格和图片的真实 Word 内容。
3. **H3 WPS Writer 粘贴**：使用同一份内容从 WPS Writer 再粘贴一次。
4. **H4 粘贴后继续编辑**：插字、删除、改粗体、编辑表格，并连续 Undo/Redo 3～5 次。

PASS 重点：不丢字、不重复字、光标不异常跳转、正文/列表/表格保持可用。PoC 未接入真实 CMS Resource uploader，因此 Word/WPS 图片未自动进入 CMS Resource 不单独判 FAIL，只记录实际现象。

完成后在页面下方选择 PASS / FAIL、填写必要备注，点击“复制人工验证结果”，将结果粘贴回当前讨论即可。

## SunEditor HTML Round-trip Harness

用于单独验证 SunEditor 3.3.3 的原始 HTML 加载、序列化、浏览器保存及重新加载行为，尤其用于复现 P1 legacy image `width` / `height` 被归一化为 `auto` 的问题。

Windows 本地执行：

```bash
git fetch origin
git switch experiment/rich-text-editor-v2-poc
cd experiments/rich-text-editor-v2-poc
npm install
npm run suneditor-roundtrip
```

浏览器打开：

```text
http://127.0.0.1:4174/
```

页面包含三个普通 HTML 文本框，可直接粘贴或修改原始 HTML：

1. **P1 Party canonical HTML**：直接读取 `data-migrations/party/v1/articles/zhutijiaoyu-content-154659859759104/article.json` 的真实 `bodyHtml`；这是当前 EU-54 自动验证中稳定复现 `15×15 → auto×auto` 的样本。
2. **P2 `teacher-library` canonical HTML**：直接读取 `sites/jilinjobs/structure/pages.json` 的真实 `bodyHtml`。
3. **自定义 / 最小复现 HTML**：内置一张 `15×15` 和一张 `16×16` legacy image，可任意替换为其他 HTML。

Round-trip 页面支持：

- 从任一普通 textarea 加载 HTML 到 SunEditor；
- 读取并显示 SunEditor 当前序列化 HTML；
- 将当前 HTML 保存到浏览器 `localStorage`；
- 从当前序列化 HTML 或保存快照重新加载；
- 刷新页面后继续恢复浏览器快照；
- 显示每张图片的 `width`、`height`、`style`、`data-se-size` 和实际渲染尺寸；
- 切换 SunEditor 官方 `v2Migration` 后重建编辑器，对比兼容模式行为。

建议先复现 P1，再手工添加图片并设置为 `16×16px`，执行“读取当前 HTML → 保存浏览器快照 → 从快照重新加载”，把“当前序列化 HTML”和图片尺寸诊断结果一并反馈。

## Automated Evidence Boundary

当前自动化 Gate 已覆盖：

- P1 Party inline image historical HTML；
- P2 `teacher-library` complex Page HTML；
- P3 ordinary Chinese authoring + undo/redo；
- P4a Office-shaped clipboard structural precheck；
- P5 CMS Resource thin adapter；
- Human Review page smoke initialization。

P4a 不替代真实 Windows Microsoft Word / WPS Writer 剪贴板验证；Headless Chromium 也不替代真实 Windows 中文 IME，因此 H1～H4 仍是最终人工 Gate。

## Boundary

- 不连接 CMS API；
- 不写数据库；
- 不修改 Admin / Backend / Public / Site Package；
- 不运行或重新激活 Main Historical Migration；
- PR #135 保持 Draft，不应作为生产实现合并。
