import { readFile } from 'node:fs/promises'
import http from 'node:http'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const host = process.env.HUMAN_REVIEW_HOST || '127.0.0.1'
const port = Number(process.env.HUMAN_REVIEW_PORT || 4173)

const packageJson = JSON.parse(await readFile(path.join(here, 'package.json'), 'utf8'))
const versions = {
  jodit: packageJson.dependencies.jodit,
  suneditor: packageJson.dependencies.suneditor,
}

const assetRoutes = {
  '/vendor/jodit.js': await firstExisting([
    'node_modules/jodit/es2021/jodit.min.js',
    'node_modules/jodit/build/jodit.min.js',
  ]),
  '/vendor/jodit.css': await firstExisting([
    'node_modules/jodit/es2021/jodit.min.css',
    'node_modules/jodit/es2021/jodit.css',
    'node_modules/jodit/build/jodit.min.css',
  ]),
  '/vendor/suneditor.js': await firstExisting([
    'node_modules/suneditor/dist/suneditor.min.js',
  ]),
  '/vendor/suneditor.css': await firstExisting([
    'node_modules/suneditor/dist/css/suneditor.min.css',
    'node_modules/suneditor/dist/css/suneditor.css',
  ]),
  '/vendor/suneditor-zh-cn.js': await firstExisting([
    'node_modules/suneditor/src/langs/zh_cn.js',
    'node_modules/suneditor/dist/lang/zh_cn.js',
    'node_modules/suneditor/dist/langs/zh_cn.js',
  ]),
}

const initialHtml = [
  '<p><strong>富文本编辑器人工验证。</strong> 请使用真实 Windows 中文输入法、Microsoft Word 与 WPS Writer 完成下面的检查。</p>',
  '<p>基础中文内容：吉林省高校毕业生就业服务。</p>',
  '<ul><li>测试列表中的中文输入</li><li>测试撤销与恢复</li></ul>',
  '<table border="1" cellpadding="4" cellspacing="0"><tbody><tr><td>表格单元格</td><td>请在这里输入中文</td></tr></tbody></table>',
].join('')

const pageHtml = buildHtml({ versions, initialHtml })

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url || '/', `http://${host}:${port}`)
    if (url.pathname === '/' || url.pathname === '/human-review') {
      res.writeHead(200, { 'content-type': 'text/html; charset=utf-8', 'cache-control': 'no-store' })
      res.end(pageHtml)
      return
    }

    const asset = assetRoutes[url.pathname]
    if (asset) {
      const bytes = await readFile(asset)
      const contentType = url.pathname.endsWith('.css')
        ? 'text/css; charset=utf-8'
        : 'text/javascript; charset=utf-8'
      res.writeHead(200, { 'content-type': contentType, 'cache-control': 'no-store' })
      res.end(bytes)
      return
    }

    if (url.pathname === '/health') {
      res.writeHead(200, { 'content-type': 'application/json; charset=utf-8' })
      res.end(JSON.stringify({ ok: true, versions }))
      return
    }

    res.writeHead(404, { 'content-type': 'text/plain; charset=utf-8' })
    res.end('Not found')
  } catch (error) {
    res.writeHead(500, { 'content-type': 'text/plain; charset=utf-8' })
    res.end(String(error?.stack || error))
  }
})

server.listen(port, host, () => {
  console.log(`Rich Text Editor V2 Human Review: http://${host}:${port}/`)
  console.log('Press Ctrl+C to stop.')
})

for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => server.close(() => process.exit(0)))
}

async function firstExisting(relativePaths) {
  for (const relativePath of relativePaths) {
    const absolutePath = path.join(here, relativePath)
    try {
      await readFile(absolutePath)
      return absolutePath
    } catch {
      // try the next package layout
    }
  }
  throw new Error(`Unable to locate required vendor asset. Tried: ${relativePaths.join(', ')}`)
}

function buildHtml({ versions, initialHtml }) {
  const initialJson = JSON.stringify(initialHtml)
  const versionJson = JSON.stringify(versions)
  return `<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Rich Text Editor V2 Human Review</title>
  <link rel="stylesheet" href="/vendor/jodit.css">
  <link rel="stylesheet" href="/vendor/suneditor.css">
  <style>
    :root { font-family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif; color: #1f2328; background: #f6f8fa; }
    * { box-sizing: border-box; }
    body { margin: 0; }
    main { max-width: 1680px; margin: 0 auto; padding: 24px; }
    h1 { margin: 0 0 8px; font-size: 26px; }
    h2 { margin: 0; font-size: 20px; }
    .muted { color: #59636e; }
    .notice, .review-table, .editor-card { background: #fff; border: 1px solid #d0d7de; border-radius: 10px; }
    .notice { padding: 16px 20px; margin: 18px 0; line-height: 1.65; }
    .notice ol { margin-bottom: 0; }
    .actions { display: flex; gap: 10px; flex-wrap: wrap; margin: 16px 0; }
    button { border: 1px solid #8c959f; background: #fff; border-radius: 6px; padding: 8px 12px; cursor: pointer; }
    button:hover { background: #f3f4f6; }
    .editors { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 18px; }
    .editor-card { padding: 16px; min-width: 0; }
    .editor-heading { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
    .editor-host { min-height: 430px; }
    details { margin-top: 12px; }
    pre { margin: 8px 0 0; padding: 10px; max-height: 220px; overflow: auto; white-space: pre-wrap; word-break: break-all; background: #f6f8fa; border-radius: 6px; font-size: 12px; }
    .review-table { margin-top: 20px; overflow: hidden; }
    table.matrix { width: 100%; border-collapse: collapse; }
    .matrix th, .matrix td { padding: 10px; border-bottom: 1px solid #d8dee4; text-align: left; vertical-align: top; }
    .matrix th { background: #f6f8fa; }
    .matrix tr:last-child td { border-bottom: 0; }
    select, input[type="text"] { width: 100%; min-width: 110px; padding: 7px; border: 1px solid #8c959f; border-radius: 5px; background: #fff; }
    #copy-status { margin-left: 8px; color: #1a7f37; }
    @media (max-width: 1050px) { .editors { grid-template-columns: 1fr; } }
  </style>
</head>
<body>
<main>
  <h1>Rich Text Editor V2 — Human Review</h1>
  <div class="muted">隔离 PoC，仅用于 Jodit ${versions.jodit} 与 SunEditor ${versions.suneditor} 的人工选型验证；不连接 CMS、不写入任何项目数据。</div>

  <section class="notice">
    <strong>只验证自动化无法可靠替代的真实桌面体验：</strong>
    <ol>
      <li><b>H1 Windows 中文 IME：</b>微软拼音连续输入、候选词选择、退格修改、中英文切换，并在粗体、列表、表格单元格中继续输入。</li>
      <li><b>H2 Microsoft Word 粘贴：</b>真实 Word 内容至少包含中文标题、普通段落、粗体/颜色、编号列表、3×3 表格和一张图片。</li>
      <li><b>H3 WPS Writer 粘贴：</b>用同一份内容从 WPS 再粘贴一次，重点观察列表、表格、中文字体和图片。</li>
      <li><b>H4 粘贴后继续编辑：</b>在粘贴内容中间插字、删除一段、改粗体、编辑表格，再连续 Undo/Redo 3–5 次。</li>
    </ol>
    <p><b>判定重点：</b>不丢字、不重复字、光标不跳、正文/列表/表格结构稳定。Word/WPS 图片行为只记录现象，不因未接 CMS Resource uploader 单独判 FAIL。</p>
  </section>

  <div class="actions">
    <button id="reset-sample" type="button">恢复相同测试内容</button>
    <button id="clear-both" type="button">清空两个编辑器</button>
  </div>

  <section class="editors">
    <article class="editor-card">
      <div class="editor-heading"><h2>Jodit</h2><span class="muted">${versions.jodit}</span></div>
      <textarea id="jodit-editor"></textarea>
      <details><summary>实时 HTML 输出 <span id="jodit-length"></span></summary><pre id="jodit-output"></pre></details>
    </article>

    <article class="editor-card">
      <div class="editor-heading"><h2>SunEditor</h2><span class="muted">${versions.suneditor}</span></div>
      <textarea id="suneditor-editor"></textarea>
      <details><summary>实时 HTML 输出 <span id="suneditor-length"></span></summary><pre id="suneditor-output"></pre></details>
    </article>
  </section>

  <section class="review-table">
    <table class="matrix">
      <thead><tr><th>人工 Gate</th><th>Jodit</th><th>SunEditor</th><th>备注</th></tr></thead>
      <tbody>
        ${['H1 Windows 中文 IME','H2 Microsoft Word 粘贴','H3 WPS Writer 粘贴','H4 粘贴后继续编辑'].map((label, index) => `
          <tr>
            <td>${label}</td>
            <td><select data-result="jodit-${index + 1}"><option>待验证</option><option>PASS</option><option>FAIL</option></select></td>
            <td><select data-result="suneditor-${index + 1}"><option>待验证</option><option>PASS</option><option>FAIL</option></select></td>
            <td><input type="text" data-note="${index + 1}" placeholder="记录异常或体验差异"></td>
          </tr>`).join('')}
      </tbody>
    </table>
    <div class="actions" style="padding: 0 12px 12px; margin-bottom: 0;">
      <button id="copy-result" type="button">复制人工验证结果</button><span id="copy-status"></span>
    </div>
  </section>
</main>

<script src="/vendor/jodit.js"></script>
<script src="/vendor/suneditor.js"></script>
<script src="/vendor/suneditor-zh-cn.js"></script>
<script>
  const INITIAL_HTML = ${initialJson};
  const VERSIONS = ${versionJson};

  const jodit = Jodit.make('#jodit-editor', {
    value: INITIAL_HTML,
    language: 'zh_cn',
    height: 430,
    askBeforePasteFromWord: false,
    defaultActionOnPasteFromWord: 'insert_as_html',
    defaultActionOnPaste: 'insert_as_html'
  });

  const suneditor = SUNEDITOR.create('#suneditor-editor', {
    value: INITIAL_HTML,
    lang: window.SUNEDITOR_LANG && window.SUNEDITOR_LANG.zh_cn,
    height: '430px',
    buttonList: [
      ['undo', 'redo'],
      ['bold', 'italic', 'underline', 'strike'],
      ['list', 'table'],
      ['link', 'image']
    ],
    attributeWhitelist: { table: 'align|cellpadding|cellspacing' },
    tagStyles: { td: 'width|height' }
  });

  function sunValue() {
    if (typeof suneditor.getContents === 'function') return suneditor.getContents();
    return suneditor.$.html.get();
  }

  function setSunValue(value) {
    if (typeof suneditor.setContents === 'function') suneditor.setContents(value);
    else suneditor.$.html.set(value);
  }

  function updateOutputs() {
    const joditHtml = jodit.value || '';
    const sunHtml = sunValue() || '';
    document.querySelector('#jodit-output').textContent = joditHtml;
    document.querySelector('#suneditor-output').textContent = sunHtml;
    document.querySelector('#jodit-length').textContent = '(' + joditHtml.length + ' chars)';
    document.querySelector('#suneditor-length').textContent = '(' + sunHtml.length + ' chars)';
  }

  document.querySelector('#reset-sample').addEventListener('click', () => {
    jodit.value = INITIAL_HTML;
    setSunValue(INITIAL_HTML);
    updateOutputs();
  });

  document.querySelector('#clear-both').addEventListener('click', () => {
    jodit.value = '<p><br></p>';
    setSunValue('<p><br></p>');
    updateOutputs();
  });

  document.querySelector('#copy-result').addEventListener('click', async () => {
    const labels = ['H1 Windows 中文 IME','H2 Microsoft Word 粘贴','H3 WPS Writer 粘贴','H4 粘贴后继续编辑'];
    const lines = [
      'Rich Text Editor V2 Human Review',
      'Jodit: ' + VERSIONS.jodit + ' / SunEditor: ' + VERSIONS.suneditor,
      ''
    ];
    labels.forEach((label, index) => {
      const n = index + 1;
      const joditResult = document.querySelector('[data-result="jodit-' + n + '"]').value;
      const sunResult = document.querySelector('[data-result="suneditor-' + n + '"]').value;
      const note = document.querySelector('[data-note="' + n + '"]').value.trim();
      lines.push(label + ': Jodit=' + joditResult + ', SunEditor=' + sunResult + (note ? ', 备注=' + note : ''));
    });
    await navigator.clipboard.writeText(lines.join('\n'));
    const status = document.querySelector('#copy-status');
    status.textContent = '已复制';
    setTimeout(() => { status.textContent = ''; }, 1500);
  });

  window.__humanReview = { jodit, suneditor, sunValue, setSunValue, updateOutputs, versions: VERSIONS };
  setInterval(updateOutputs, 400);
  updateOutputs();
</script>
</body>
</html>`
}
