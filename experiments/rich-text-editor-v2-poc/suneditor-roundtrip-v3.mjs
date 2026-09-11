import { readFile } from 'node:fs/promises'
import http from 'node:http'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const repoRoot = path.resolve(here, '../..')
const host = process.env.SUNEDITOR_ROUNDTRIP_HOST || '127.0.0.1'
const port = Number(process.env.SUNEDITOR_ROUNDTRIP_PORT || 4174)
const packageJson = JSON.parse(await readFile(path.join(here, 'package.json'), 'utf8'))
const version = packageJson.dependencies.suneditor

const partyArticleDir = path.join(repoRoot, 'data-migrations/party/v1/articles/zhutijiaoyu-content-154659859759104')
const partyArticlePath = path.join(partyArticleDir, 'article.json')
const partyAssetsDir = path.join(partyArticleDir, 'assets')
const siteAssetsDir = path.join(repoRoot, 'sites/jilinjobs/assets')
const pagesPath = path.join(repoRoot, 'sites/jilinjobs/structure/pages.json')

const partyArticle = JSON.parse(await readFile(partyArticlePath, 'utf8'))
const pagesDocument = JSON.parse(await readFile(pagesPath, 'utf8'))
const teacherPage = findObject(pagesDocument, value => value?.alias === 'teacher-library' && typeof value?.bodyHtml === 'string')
if (!partyArticle?.content?.bodyHtml) throw new Error(`P1 Party fixture missing bodyHtml: ${partyArticlePath}`)
if (!teacherPage?.bodyHtml) throw new Error(`P2 teacher-library fixture missing bodyHtml: ${pagesPath}`)

const fixtures = {
  p1: partyArticle.content.bodyHtml,
  p2: teacherPage.bodyHtml,
  minimal: '<p><strong><img alt="legacy-star" height="15" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==" width="15"> legacy 15×15</strong></p><p><img alt="legacy-16" height="16" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==" width="16"> legacy 16×16</p>',
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

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url || '/', `http://${host}:${port}`)

    if (url.pathname === '/' || url.pathname === '/roundtrip') {
      send(res, 200, 'text/html; charset=utf-8', buildHtml())
      return
    }

    if (url.pathname === '/health') {
      send(res, 200, 'application/json; charset=utf-8', JSON.stringify({ ok: true, suneditor: version }))
      return
    }

    if (url.pathname.startsWith('/fixture/')) {
      const name = url.pathname.slice('/fixture/'.length)
      if (name in fixtures) {
        send(res, 200, 'text/html; charset=utf-8', fixtures[name])
        return
      }
    }

    if (url.pathname.startsWith('/assets/')) {
      const relative = decodeURIComponent(url.pathname.slice('/assets/'.length))
      const file = await readFromRoots(relative, [partyAssetsDir, siteAssetsDir])
      if (file) {
        send(res, 200, mimeType(file.path), file.content)
        return
      }
    }

    if (url.pathname.startsWith('/static/')) {
      const relative = decodeURIComponent(url.pathname.slice('/static/'.length))
      const file = await readFromRoots(relative, [siteAssetsDir])
      if (file) {
        send(res, 200, mimeType(file.path), file.content)
        return
      }
    }

    const vendor = vendorRoutes[url.pathname]
    if (vendor) {
      send(
        res,
        200,
        url.pathname.endsWith('.css') ? 'text/css; charset=utf-8' : 'text/javascript; charset=utf-8',
        await readFile(vendor),
      )
      return
    }

    send(res, 404, 'text/plain; charset=utf-8', `Not found: ${url.pathname}`)
  } catch (error) {
    send(res, 500, 'text/plain; charset=utf-8', String(error?.stack || error))
  }
})

server.listen(port, host, () => {
  console.log(`SunEditor ${version} HTML round-trip harness V3: http://${host}:${port}/`)
  console.log(`P1 /assets/* -> ${partyAssetsDir}`)
  console.log(`P2 /static/* -> ${siteAssetsDir}`)
  console.log('Press Ctrl+C to stop.')
})
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => server.close(() => process.exit(0)))

function send(res, status, contentType, body) {
  res.writeHead(status, { 'content-type': contentType, 'cache-control': 'no-store' })
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
  for (const child of (Array.isArray(value) ? value : Object.values(value))) {
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
<title>SunEditor HTML Round-trip Harness V3</title>
<link rel="stylesheet" href="/vendor/suneditor.css">
<style>
:root{font-family:"Microsoft YaHei","PingFang SC",Arial,sans-serif;color:#1f2328;background:#f6f8fa}*{box-sizing:border-box}body{margin:0}main{max-width:1500px;margin:auto;padding:24px}h1{margin:0 0 8px;font-size:26px}h2{font-size:20px;margin:0 0 12px}.muted{color:#59636e}.panel{background:#fff;border:1px solid #d0d7de;border-radius:10px;padding:16px;margin:16px 0}.fixture{border:1px solid #d8dee4;border-radius:8px;padding:12px;margin:12px 0}.head{display:flex;justify-content:space-between;gap:12px;align-items:center}.actions{display:flex;gap:8px;flex-wrap:wrap;margin:10px 0}button{border:1px solid #8c959f;background:#fff;border-radius:6px;padding:8px 12px;cursor:pointer}button.primary{background:#0969da;color:#fff;border-color:#0969da}textarea.raw{width:100%;min-height:160px;resize:vertical;padding:10px;border:1px solid #8c959f;border-radius:6px;font-family:Consolas,"Courier New",monospace;font-size:12px;line-height:1.5}.outputs{display:grid;grid-template-columns:1fr 1fr;gap:14px}.outputs textarea{min-height:220px}.error{color:#cf222e;font-weight:700}pre{max-height:420px;overflow:auto;white-space:pre-wrap;word-break:break-all;background:#f6f8fa;padding:10px;border-radius:6px;font-size:12px}@media(max-width:900px){.outputs{grid-template-columns:1fr}}
</style>
</head>
<body><main>
<h1>SunEditor ${version} — HTML Round-trip Harness V3</h1>
<div class="muted">P1 相对路径 /assets/* 和 P2 /static/* 已映射到仓库真实资源。</div>
<section class="panel"><b>建议：</b>先加载 P1，确认图片显示，再点击“读取当前 HTML”；随后比较 v2Migration=false/true。三个原始 HTML 区域都是普通 textarea，可直接粘贴任意 HTML。</section>
<section class="panel"><h2>原始 HTML 输入</h2>
<div class="fixture"><div class="head"><b>P1 — Party canonical HTML</b><span id="p1-meta" class="muted"></span></div><textarea id="source-p1" class="raw"></textarea><div class="actions"><button class="primary" data-load="p1">加载到 SunEditor</button><button data-reset="p1">恢复原始值</button></div></div>
<div class="fixture"><div class="head"><b>P2 — teacher-library canonical HTML</b><span id="p2-meta" class="muted"></span></div><textarea id="source-p2" class="raw"></textarea><div class="actions"><button class="primary" data-load="p2">加载到 SunEditor</button><button data-reset="p2">恢复原始值</button></div></div>
<div class="fixture"><div class="head"><b>自定义 / 最小复现 HTML</b><span id="minimal-meta" class="muted"></span></div><textarea id="source-minimal" class="raw"></textarea><div class="actions"><button class="primary" data-load="minimal">加载到 SunEditor</button><button data-reset="minimal">恢复 15×15 / 16×16 样本</button></div></div>
</section>
<section class="panel"><div class="actions"><label><input type="checkbox" id="v2-migration"> v2Migration</label><button id="rebuild">按当前模式重建编辑器</button><span id="mode"></span></div><div id="startup-error" class="error"></div><textarea id="editor"></textarea><div class="actions"><button class="primary" id="capture">读取当前 HTML</button><button id="save">保存浏览器快照</button><button id="reload-current">从当前序列化 HTML 重新加载</button><button id="reload-snapshot">从浏览器快照重新加载</button><button id="clear">清除快照</button><button id="copy">复制当前序列化 HTML</button></div><div id="status" class="muted"></div></section>
<section class="panel"><h2>Round-trip 输出</h2><div class="outputs"><div><b>当前序列化 HTML</b><textarea id="current" class="raw"></textarea></div><div><b>浏览器保存快照</b><textarea id="snapshot" class="raw"></textarea></div></div></section>
<section class="panel"><h2>图片尺寸诊断</h2><pre id="diagnostics"></pre></section>
</main>
<script src="/vendor/suneditor.js"></script><script src="/vendor/suneditor-zh-cn.js"></script>
<script>
const STORAGE_KEY='jilinjobs.suneditor.roundtrip.v3';let editor=null;const original={};
const q=s=>document.querySelector(s);const setStatus=t=>q('#status').textContent=t;
async function fetchFixture(name){const r=await fetch('/fixture/'+name);if(!r.ok)throw new Error(await r.text());return r.text()}
async function initFixtures(){for(const name of ['p1','p2','minimal']){original[name]=await fetchFixture(name);const box=q('#source-'+name);box.value=original[name];box.oninput=()=>meta(name);meta(name)}}
function meta(name){q('#'+name+'-meta').textContent=(q('#source-'+name).value||'').length+' chars'}
function getHtml(){if(!editor)return '';return typeof editor.getContents==='function'?editor.getContents():editor.$.html.get()}
function setHtml(value){if(!editor)return;if(typeof editor.setContents==='function')editor.setContents(value);else editor.$.html.set(value);setTimeout(diagnostics,250)}
function destroy(){if(!editor)return;try{editor.destroy()}catch{}editor=null}
function rebuild(){destroy();const old=q('#editor');const fresh=document.createElement('textarea');fresh.id='editor';old.replaceWith(fresh);q('#startup-error').textContent='';const migration=q('#v2-migration').checked;try{editor=SUNEDITOR.create('#editor',{plugins:SUNEDITOR.plugins,lang:window.SUNEDITOR_LANG&&window.SUNEDITOR_LANG.zh_cn,height:'430px',v2Migration:migration,buttonList:[['undo','redo'],['bold','italic','underline','strike'],['list','table'],['link','image']],attributeWhitelist:{table:'align|cellpadding|cellspacing',img:'width|height|align'},tagStyles:{td:'width|height','img|video|iframe':'width|height|float'}});q('#mode').textContent='v2Migration='+migration;setHtml(q('#current').value||original.minimal||'');setStatus('SunEditor 初始化成功')}catch(error){q('#startup-error').textContent='SunEditor 初始化失败：'+(error?.message||error);console.error(error)}}
function capture(){const value=getHtml();q('#current').value=value;diagnostics();setStatus('已读取当前 HTML');return value}
function diagnostics(){const surface=q('.se-wrapper-wysiwyg[contenteditable="true"]');if(!surface){q('#diagnostics').textContent='未找到可编辑 surface';return}const rows=[...surface.querySelectorAll('img')].map((img,index)=>{const r=img.getBoundingClientRect();return{index:index+1,src:img.getAttribute('src'),loadState:img.complete?(img.naturalWidth>0?'loaded':'broken'):'loading',width:img.getAttribute('width'),height:img.getAttribute('height'),style:img.getAttribute('style'),dataSeSize:img.getAttribute('data-se-size'),naturalWidth:img.naturalWidth,naturalHeight:img.naturalHeight,renderedWidth:Math.round(r.width*100)/100,renderedHeight:Math.round(r.height*100)/100}});q('#diagnostics').textContent=rows.length?JSON.stringify(rows,null,2):'当前没有图片'}
function load(name){const value=q('#source-'+name).value;q('#current').value=value;setHtml(value);setTimeout(()=>{capture();setStatus('已加载 '+name)},350)}
function reset(name){q('#source-'+name).value=original[name];meta(name)}
q('#rebuild').onclick=rebuild;q('#capture').onclick=capture;q('#save').onclick=()=>{const value=capture();localStorage.setItem(STORAGE_KEY,value);q('#snapshot').value=value;setStatus('已保存浏览器快照')};q('#reload-current').onclick=()=>{setHtml(q('#current').value);setTimeout(capture,350)};q('#reload-snapshot').onclick=()=>{const value=localStorage.getItem(STORAGE_KEY)||'';if(!value){setStatus('没有浏览器快照');return}q('#current').value=value;setHtml(value);setTimeout(capture,350)};q('#clear').onclick=()=>{localStorage.removeItem(STORAGE_KEY);q('#snapshot').value='';setStatus('已清除快照')};q('#copy').onclick=async()=>{await navigator.clipboard.writeText(q('#current').value);setStatus('已复制')};document.querySelectorAll('[data-load]').forEach(b=>b.onclick=()=>load(b.dataset.load));document.querySelectorAll('[data-reset]').forEach(b=>b.onclick=()=>reset(b.dataset.reset));
(async()=>{try{await initFixtures();q('#snapshot').value=localStorage.getItem(STORAGE_KEY)||'';q('#current').value=original.minimal;rebuild()}catch(error){q('#startup-error').textContent='页面初始化失败：'+(error?.message||error);console.error(error)}})();
</script></body></html>`
}
