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
