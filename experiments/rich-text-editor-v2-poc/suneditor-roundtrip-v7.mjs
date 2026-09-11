import { readFile } from 'node:fs/promises'
import http from 'node:http'
import path from 'node:path'
import { Script } from 'node:vm'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const repoRoot = path.resolve(here, '../..')
const host = process.env.SUNEDITOR_ROUNDTRIP_HOST || '127.0.0.1'
const port = Number(process.env.SUNEDITOR_ROUNDTRIP_PORT || 4174)
const packageJson = JSON.parse(await readFile(path.join(here, 'package.json'), 'utf8'))
const version = packageJson.dependencies.suneditor

const partyArticleDir = path.join(repoRoot, 'data-migrations/party/v1/articles/zhutijiaoyu-content-154659859759104')
const partyAssetsDir = path.join(partyArticleDir, 'assets')
const siteAssetsDir = path.join(repoRoot, 'sites/jilinjobs/assets')
const partyArticle = JSON.parse(await readFile(path.join(partyArticleDir, 'article.json'), 'utf8'))
const pagesDocument = JSON.parse(await readFile(path.join(repoRoot, 'sites/jilinjobs/structure/pages.json'), 'utf8'))
const teacherPage = findObject(pagesDocument, (value) => value?.alias === 'teacher-library' && typeof value?.bodyHtml === 'string')

if (!partyArticle?.content?.bodyHtml) throw new Error('P1 Party fixture missing bodyHtml')
if (!teacherPage?.bodyHtml) throw new Error('P2 teacher-library fixture missing bodyHtml')

const realImage = '/assets/236413a1e5002810f6f9be856f85ac66b792ca3c44cd67c2a800ca2b1af99c93.png'
const fixtures = {
  p1: partyArticle.content.bodyHtml,
  p2: teacherPage.bodyHtml,
  minimal: `<p><strong><img alt="legacy-star-15" height="15" src="${realImage}" width="15"> legacy 15×15</strong></p><p><img alt="legacy-star-16" height="16" src="${realImage}" width="16"> legacy 16×16</p>`,
}

const vendorRoutes = {
  '/vendor/suneditor.js': await firstExisting(['node_modules/suneditor/dist/suneditor.min.js']),
  '/vendor/suneditor.css': await firstExisting([
    'node_modules/suneditor/dist/suneditor.min.css',
    'node_modules/suneditor/dist/css/suneditor.min.css',
    'node_modules/suneditor/dist/css/suneditor.css',
  ]),
  '/vendor/suneditor-zh-cn.js': await firstExisting([
    'node_modules/suneditor/src/langs/zh_cn.js',
    'node_modules/suneditor/dist/lang/zh_cn.js',
    'node_modules/suneditor/dist/langs/zh_cn.js',
  ]),
}

const clientPath = path.join(here, 'suneditor-roundtrip-v7-client.js')
const clientSource = await readFile(clientPath, 'utf8')
new Script(clientSource, { filename: 'suneditor-roundtrip-v7-client.js' })

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url || '/', `http://${host}:${port}`)

    if (url.pathname === '/' || url.pathname === '/roundtrip') {
      return send(res, 200, 'text/html; charset=utf-8', buildHtml())
    }

    if (url.pathname === '/roundtrip-client.js') {
      return send(res, 200, 'text/javascript; charset=utf-8', clientSource)
    }

    if (url.pathname === '/health') {
      return send(res, 200, 'application/json; charset=utf-8', JSON.stringify({
        ok: true,
        suneditor: version,
        harness: 7,
        clientSyntaxChecked: true,
      }))
    }

    if (url.pathname.startsWith('/fixture/')) {
      const name = url.pathname.slice('/fixture/'.length)
      if (name in fixtures) return send(res, 200, 'text/html; charset=utf-8', fixtures[name])
    }

    if (url.pathname.startsWith('/assets/')) {
      const relative = decodeURIComponent(url.pathname.slice('/assets/'.length))
      const file = await readFromRoots(relative, [partyAssetsDir, siteAssetsDir])
      if (file) return send(res, 200, mimeType(file.path), file.content)
    }

    if (url.pathname.startsWith('/static/')) {
      const relative = decodeURIComponent(url.pathname.slice('/static/'.length))
      const file = await readFromRoots(relative, [siteAssetsDir])
      if (file) return send(res, 200, mimeType(file.path), file.content)
    }

    const vendor = vendorRoutes[url.pathname]
    if (vendor) {
      return send(
        res,
        200,
        url.pathname.endsWith('.css') ? 'text/css; charset=utf-8' : 'text/javascript; charset=utf-8',
        await readFile(vendor),
      )
    }

    return send(res, 404, 'text/plain; charset=utf-8', `Not found: ${url.pathname}`)
  } catch (error) {
    return send(res, 500, 'text/plain; charset=utf-8', String(error?.stack || error))
  }
})

server.listen(port, host, () => {
  console.log(`SunEditor ${version} legacy-image round-trip harness V7: http://${host}:${port}/`)
  console.log('Browser client syntax pre-check: PASS')
})

for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => server.close(() => process.exit(0)))
}

function send(res, status, contentType, body) {
  res.writeHead(status, {
    'content-type': contentType,
    'cache-control': 'no-store',
  })
  res.end(body)
}

async function firstExisting(relativePaths) {
  for (const relativePath of relativePaths) {
    const absolutePath = path.join(here, relativePath)
    try {
      await readFile(absolutePath)
      return absolutePath
    } catch {}
  }
  throw new Error(`Unable to locate required vendor asset. Tried: ${relativePaths.join(', ')}`)
}

async function readFromRoots(relativePath, roots) {
  if (!relativePath || relativePath.includes('\0')) return null

  for (const root of roots) {
    const resolvedRoot = path.resolve(root)
    const candidate = path.resolve(resolvedRoot, relativePath)
    if (candidate !== resolvedRoot && !candidate.startsWith(`${resolvedRoot}${path.sep}`)) continue
    try {
      return { path: candidate, content: await readFile(candidate) }
    } catch {}
  }

  return null
}

function mimeType(filePath) {
  switch (path.extname(filePath).toLowerCase()) {
    case '.png': return 'image/png'
    case '.jpg':
    case '.jpeg': return 'image/jpeg'
    case '.gif': return 'image/gif'
    case '.webp': return 'image/webp'
    case '.svg': return 'image/svg+xml'
    default: return 'application/octet-stream'
  }
}

function findObject(value, predicate) {
  if (!value || typeof value !== 'object') return null
  if (predicate(value)) return value

  const children = Array.isArray(value) ? value : Object.values(value)
  for (const child of children) {
    const found = findObject(child, predicate)
    if (found) return found
  }

  return null
}

function buildHtml() {
  return `<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>SunEditor Legacy Image Round-trip V7</title>
<link rel="stylesheet" href="/vendor/suneditor.css">
<style>
:root{font-family:"Microsoft YaHei","PingFang SC",Arial,sans-serif;color:#1f2328;background:#f6f8fa}*{box-sizing:border-box}body{margin:0}main{max-width:1500px;margin:auto;padding:24px}h1{margin:0 0 8px;font-size:26px}h2{font-size:20px}.muted{color:#59636e}.panel{background:#fff;border:1px solid #d0d7de;border-radius:10px;padding:16px;margin:16px 0}.fixture{border:1px solid #d8dee4;border-radius:8px;padding:12px;margin:12px 0}.head{display:flex;justify-content:space-between;gap:12px;align-items:center}.actions{display:flex;gap:8px;flex-wrap:wrap;margin:10px 0;align-items:center}button{border:1px solid #8c959f;background:#fff;border-radius:6px;padding:8px 12px;cursor:pointer}button.primary{background:#0969da;color:#fff;border-color:#0969da}textarea.raw{width:100%;min-height:160px;resize:vertical;padding:10px;border:1px solid #8c959f;border-radius:6px;font-family:Consolas,"Courier New",monospace;font-size:12px;line-height:1.5}.outputs{display:grid;grid-template-columns:1fr 1fr;gap:14px}.outputs textarea{min-height:220px}.error{color:#cf222e;font-weight:700}pre{max-height:520px;overflow:auto;white-space:pre-wrap;word-break:break-all;background:#f6f8fa;padding:10px;border-radius:6px;font-size:12px}@media(max-width:900px){.outputs{grid-template-columns:1fr}}
</style>
</head>
<body>
<main>
<h1>SunEditor ${version} — Legacy Image Round-trip V7</h1>
<div class="muted">V7 将浏览器脚本拆成独立文件，服务启动前先完成 JavaScript syntax check；图片尺寸 bridge 继续通过 SunEditor Figure / ImageSizeService API 验证。</div>
<section class="panel"><b>本轮判定：</b>页面首先必须正常初始化并填充三个 HTML 输入框；然后最小样本两张真实图片应实际显示为 15×15 / 16×16，并且可以点击弹出图片工具面板。</section>
<section class="panel">
<h2>原始 HTML 输入</h2>
<div class="fixture"><div class="head"><b>P1 — Party canonical HTML</b><span id="p1-meta" class="muted"></span></div><textarea id="source-p1" class="raw"></textarea><div class="actions"><button class="primary" data-load="p1">加载到 SunEditor</button><button data-reset="p1">恢复原始值</button></div></div>
<div class="fixture"><div class="head"><b>P2 — teacher-library canonical HTML</b><span id="p2-meta" class="muted"></span></div><textarea id="source-p2" class="raw"></textarea><div class="actions"><button class="primary" data-load="p2">加载到 SunEditor</button><button data-reset="p2">恢复原始值</button></div></div>
<div class="fixture"><div class="head"><b>自定义 / 最小复现 HTML</b><span id="minimal-meta" class="muted"></span></div><textarea id="source-minimal" class="raw"></textarea><div class="actions"><button class="primary" data-load="minimal">加载到 SunEditor</button><button data-reset="minimal">恢复真实图片 15×15 / 16×16 样本</button></div></div>
</section>
<section class="panel">
<div class="actions"><label><input type="checkbox" id="bridge" checked> <b>Legacy image bridge</b></label><label><input type="checkbox" id="v2-migration"> v2Migration</label><button id="rebuild">按当前模式重建编辑器</button><span id="mode"></span></div>
<div id="startup-error" class="error"></div>
<textarea id="editor"></textarea>
<div class="actions"><button class="primary" id="capture">读取当前 HTML</button><button id="save">保存模拟 CMS HTML 快照</button><button id="reload-current">从模拟 CMS HTML 重新加载</button><button id="reload-snapshot">从浏览器快照重新加载</button><button id="clear">清除快照</button><button id="copy">复制模拟 CMS HTML</button></div>
<div id="status" class="muted"></div>
</section>
<section class="panel"><h2>Round-trip 输出</h2><div class="outputs"><div><b>SunEditor 内部序列化 HTML</b><textarea id="internal" class="raw"></textarea></div><div><b>模拟 CMS 保存 HTML</b><textarea id="app-html" class="raw"></textarea></div></div><div class="fixture"><b>浏览器保存快照</b><textarea id="snapshot" class="raw"></textarea></div></section>
<section class="panel"><h2>Bridge 事件</h2><pre id="events-log"></pre></section>
<section class="panel"><h2>图片诊断</h2><pre id="diagnostics"></pre></section>
</main>
<script src="/vendor/suneditor.js"></script>
<script src="/vendor/suneditor-zh-cn.js"></script>
<script src="/roundtrip-client.js"></script>
</body>
</html>`
}
