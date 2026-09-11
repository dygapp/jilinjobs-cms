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

const partyArticlePath = path.join(repoRoot, 'data-migrations/party/v1/articles/zhutijiaoyu-content-154659859759104/article.json')
const pagesPath = path.join(repoRoot, 'sites/jilinjobs/structure/pages.json')
const partyArticle = JSON.parse(await readFile(partyArticlePath, 'utf8'))
const pagesDocument = JSON.parse(await readFile(pagesPath, 'utf8'))
const teacherPage = findObject(pagesDocument, candidate => candidate?.alias === 'teacher-library' && typeof candidate?.bodyHtml === 'string')

if (!partyArticle?.content?.bodyHtml) throw new Error(`P1 Party fixture missing bodyHtml: ${partyArticlePath}`)
if (!teacherPage?.bodyHtml) throw new Error(`P2 teacher-library fixture missing bodyHtml: ${pagesPath}`)

const fixtures = {
  p1: partyArticle.content.bodyHtml,
  p2: teacherPage.bodyHtml,
  minimal: '<p><strong><img alt="legacy-star" height="15" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==" width="15"> legacy 15×15</strong></p><p><img alt="legacy-16" height="16" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==" width="16"> legacy 16×16</p>',
}

const assetRoutes = {
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
      res.writeHead(200, { 'content-type': 'text/html; charset=utf-8', 'cache-control': 'no-store' })
      res.end(buildHtml())
      return
    }
    if (url.pathname === '/health') {
      res.writeHead(200, { 'content-type': 'application/json; charset=utf-8' })
      res.end(JSON.stringify({ ok: true, suneditor: version, fixtures: ['p1', 'p2', 'minimal'] }))
      return
    }
    const asset = assetRoutes[url.pathname]
    if (asset) {
      res.writeHead(200, {
        'content-type': url.pathname.endsWith('.css') ? 'text/css; charset=utf-8' : 'text/javascript; charset=utf-8',
        'cache-control': 'no-store',
      })
      res.end(await readFile(asset))
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
  console.log(`SunEditor ${version} HTML round-trip harness: http://${host}:${port}/`)
  console.log('Press Ctrl+C to stop.')
})

for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => server.close(() => process.exit(0)))

async function firstExisting(relativePaths) {
  for (const relativePath of relativePaths) {
    const absolutePath = path.join(here, relativePath)
    try { await readFile(absolutePath); return absolutePath } catch {}
  }
  throw new Error(`Unable to locate required vendor asset. Tried: ${relativePaths.join(', ')}`)
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

function scriptJson(value) {
  return JSON.stringify(value).replaceAll('</script', '<\\/script')
}

function buildHtml() {
  const fixturesJson = scriptJson(fixtures)
  const versionJson = scriptJson(version)
  return `<!doctype html>
<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>SunEditor HTML Round-trip Harness</title>
<link rel="stylesheet" href="/vendor/suneditor.css">
<style>
:root{font-family:"Microsoft YaHei","PingFang SC",Arial,sans-serif;color:#1f2328;background:#f6f8fa}*{box-sizing:border-box}body{margin:0}main{max-width:1500px;margin:auto;padding:24px}h1{margin:0 0 8px;font-size:26px}h2{font-size:20px;margin:0 0 12px}.muted{color:#59636e}.panel{background:#fff;border:1px solid #d0d7de;border-radius:10px;padding:16px;margin:16px 0}.notice{line-height:1.7}.fixture-grid{display:grid;grid-template-columns:1fr;gap:14px}.fixture{border:1px solid #d8dee4;border-radius:8px;padding:12px}.fixture-head{display:flex;gap:12px;align-items:center;justify-content:space-between;margin-bottom:8px}.fixture-title{font-weight:700}.actions{display:flex;gap:8px;flex-wrap:wrap;margin:10px 0}button{border:1px solid #8c959f;background:#fff;border-radius:6px;padding:8px 12px;cursor:pointer}button.primary{background:#0969da;color:#fff;border-color:#0969da}textarea.raw{width:100%;min-height:170px;resize:vertical;padding:10px;border:1px solid #8c959f;border-radius:6px;font-family:Consolas,"Courier New",monospace;font-size:12px;line-height:1.5;background:#fff}.editor-shell{min-height:360px}.output-grid{display:grid;grid-template-columns:1fr 1fr;gap:14px}.output-grid textarea{min-height:220px}.options{display:flex;gap:18px;align-items:center;flex-wrap:wrap}.status{font-weight:700}.ok{color:#1a7f37}.warn{color:#9a6700}.error{color:#cf222e;font-weight:700}pre{max-height:360px;overflow:auto;white-space:pre-wrap;word-break:break-all;background:#f6f8fa;padding:10px;border-radius:6px;font-size:12px}@media(max-width:900px){.output-grid{grid-template-columns:1fr}}
</style></head><body><main>
<h1>SunEditor ${version} — HTML Round-trip Harness V2</h1>
<div class="muted">独立实验页面；不连接 CMS API、不写数据库。P1/P2 输入框直接读取仓库真实 canonical HTML。</div>
<section class="panel notice"><h2>验证说明</h2><ol><li>三个“原始 HTML”区域都是普通 textarea，可直接粘贴、修改任意 HTML。</li><li>点击某个“加载到 SunEditor”后，立即读取一次当前序列化 HTML 和图片尺寸。</li><li>可切换 <b>v2Migration</b>，点击“按当前模式重建编辑器”，再加载相同 HTML 比较。</li><li>手工新增图片并设为 16×16 后，执行“读取当前 HTML → 保存浏览器快照 → 从快照重新加载”，验证尺寸是否保持。</li></ol></section>
<section class="panel"><h2>原始 HTML 输入</h2><div class="fixture-grid">
<div class="fixture"><div class="fixture-head"><span class="fixture-title">P1 — 当前自动验证失败的 Party canonical HTML</span><span class="muted" id="p1-meta"></span></div><textarea class="raw" id="source-p1" spellcheck="false"></textarea><div class="actions"><button class="primary" data-load="p1">加载到 SunEditor</button><button data-reset="p1">恢复仓库原始值</button></div></div>
<div class="fixture"><div class="fixture-head"><span class="fixture-title">P2 — teacher-library canonical HTML</span><span class="muted" id="p2-meta"></span></div><textarea class="raw" id="source-p2" spellcheck="false"></textarea><div class="actions"><button class="primary" data-load="p2">加载到 SunEditor</button><button data-reset="p2">恢复仓库原始值</button></div></div>
<div class="fixture"><div class="fixture-head"><span class="fixture-title">自定义 / 最小复现 HTML</span><span class="muted" id="minimal-meta"></span></div><textarea class="raw" id="source-minimal" spellcheck="false"></textarea><div class="actions"><button class="primary" data-load="minimal">加载到 SunEditor</button><button data-reset="minimal">恢复 15×15 / 16×16 最小样本</button></div></div>
</div></section>
<section class="panel"><div class="options"><h2 style="margin:0">SunEditor</h2><label><input type="checkbox" id="v2-migration"> 启用官方 <code>v2Migration</code></label><button id="rebuild-editor">按当前模式重建编辑器</button><span id="editor-mode" class="status"></span></div><div id="startup-error" class="error"></div><div class="editor-shell"><textarea id="suneditor-editor"></textarea></div><div class="actions"><button class="primary" id="capture-html">读取当前 HTML</button><button id="save-snapshot">保存浏览器快照</button><button id="reload-output">从当前序列化 HTML 重新加载</button><button id="reload-snapshot">从浏览器快照重新加载</button><button id="clear-snapshot">清除浏览器快照</button><button id="copy-output">复制当前序列化 HTML</button></div><div id="action-status" class="muted"></div></section>
<section class="panel"><h2>Round-trip 输出</h2><div class="output-grid"><div><div class="fixture-head"><b>当前序列化 HTML</b><span class="muted" id="output-meta"></span></div><textarea class="raw" id="current-html" spellcheck="false"></textarea></div><div><div class="fixture-head"><b>浏览器保存快照（localStorage）</b><span class="muted" id="snapshot-meta"></span></div><textarea class="raw" id="snapshot-html" spellcheck="false"></textarea></div></div></section>
<section class="panel"><h2>图片尺寸诊断</h2><div class="muted">重点看 width / height / style / data-se-size / 实际 bounding box。</div><pre id="diagnostics"></pre></section>
</main><script src="/vendor/suneditor.js"></script><script src="/vendor/suneditor-zh-cn.js"></script><script>
const FIXTURES=${fixturesJson};const VERSION=${versionJson};const STORAGE_KEY='jilinjobs.suneditor.roundtrip.snapshot.v2';let editor=null;
function status(t){document.querySelector('#action-status').textContent=t}
function getSunHtml(){if(!editor)return '';return typeof editor.getContents==='function'?editor.getContents():editor.$.html.get()}
function setSunHtml(v){if(!editor){status('SunEditor 尚未成功初始化');return}if(typeof editor.setContents==='function')editor.setContents(v);else editor.$.html.set(v);setTimeout(updateDiagnostics,120)}
function destroyEditor(){if(!editor)return;try{editor.destroy()}catch{}editor=null}
function replaceEditorNode(){const old=document.querySelector('#suneditor-editor');const fresh=document.createElement('textarea');fresh.id='suneditor-editor';old.replaceWith(fresh)}
function buildEditor(){
  destroyEditor();replaceEditorNode();document.querySelector('#startup-error').textContent='';const migration=document.querySelector('#v2-migration').checked;
  try{
    editor=SUNEDITOR.create('#suneditor-editor',{
      plugins:SUNEDITOR.plugins,
      lang:window.SUNEDITOR_LANG&&window.SUNEDITOR_LANG.zh_cn,
      height:'430px',
      v2Migration:migration,
      buttonList:[['undo','redo'],['bold','italic','underline','strike'],['list','table'],['link','image']],
      attributeWhitelist:{table:'align|cellpadding|cellspacing',img:'width|height|align'},
      tagStyles:{td:'width|height','img|video|iframe':'width|height|float'},
    });
    document.querySelector('#editor-mode').textContent='v2Migration='+migration;document.querySelector('#editor-mode').className='status '+(migration?'warn':'ok');
    setSunHtml(document.querySelector('#current-html').value||FIXTURES.minimal);status('SunEditor 初始化成功');
  }catch(error){document.querySelector('#startup-error').textContent='SunEditor 初始化失败：'+(error?.message||error);console.error(error)}
}
function capture(){const html=getSunHtml()||'';document.querySelector('#current-html').value=html;document.querySelector('#output-meta').textContent=html.length+' chars';updateDiagnostics();status('已读取当前 HTML');return html}
function snapshotValue(){return localStorage.getItem(STORAGE_KEY)||''}
function refreshSnapshotBox(){const v=snapshotValue();document.querySelector('#snapshot-html').value=v;document.querySelector('#snapshot-meta').textContent=v?v.length+' chars':'无快照'}
function loadCase(name){const v=document.querySelector('#source-'+name).value;document.querySelector('#current-html').value=v;setSunHtml(v);setTimeout(()=>{capture();status('已加载 '+name+' 原始 HTML')},180)}
function resetCase(name){document.querySelector('#source-'+name).value=FIXTURES[name];updateSourceMeta(name);status('已恢复 '+name+' 原始值')}
function updateSourceMeta(name){const v=document.querySelector('#source-'+name).value||'';document.querySelector('#'+name+'-meta').textContent=v.length+' chars'}
function updateDiagnostics(){
  const out=document.querySelector('#diagnostics');if(!editor){out.textContent='SunEditor 尚未初始化';return}
  const surface=document.querySelector('.se-wrapper-wysiwyg[contenteditable="true"]');if(!surface){out.textContent='未找到可编辑 surface';return}
  const rows=[...surface.querySelectorAll('img')].map((img,i)=>{const r=img.getBoundingClientRect();return {index:i+1,alt:img.getAttribute('alt'),src:img.getAttribute('src'),width:img.getAttribute('width'),height:img.getAttribute('height'),style:img.getAttribute('style'),dataSeSize:img.getAttribute('data-se-size'),renderedWidth:Math.round(r.width*100)/100,renderedHeight:Math.round(r.height*100)/100}});out.textContent=rows.length?JSON.stringify(rows,null,2):'当前内容没有图片'
}
for(const name of ['p1','p2','minimal']){const input=document.querySelector('#source-'+name);input.value=FIXTURES[name];input.addEventListener('input',()=>updateSourceMeta(name));updateSourceMeta(name)}
document.querySelectorAll('[data-load]').forEach(b=>b.addEventListener('click',()=>loadCase(b.dataset.load)));document.querySelectorAll('[data-reset]').forEach(b=>b.addEventListener('click',()=>resetCase(b.dataset.reset)));
document.querySelector('#rebuild-editor').onclick=buildEditor;document.querySelector('#capture-html').onclick=capture;
document.querySelector('#save-snapshot').onclick=()=>{const v=capture();localStorage.setItem(STORAGE_KEY,v);refreshSnapshotBox();status('已保存浏览器快照')};
document.querySelector('#reload-output').onclick=()=>{setSunHtml(document.querySelector('#current-html').value);setTimeout(()=>{capture();status('已从当前序列化 HTML 重新加载')},180)};
document.querySelector('#reload-snapshot').onclick=()=>{const v=snapshotValue();if(!v){status('当前没有浏览器快照');return}document.querySelector('#current-html').value=v;setSunHtml(v);setTimeout(()=>{capture();status('已从浏览器快照重新加载')},180)};
document.querySelector('#clear-snapshot').onclick=()=>{localStorage.removeItem(STORAGE_KEY);refreshSnapshotBox();status('已清除浏览器快照')};
document.querySelector('#copy-output').onclick=async()=>{await navigator.clipboard.writeText(document.querySelector('#current-html').value);status('已复制当前序列化 HTML')};
refreshSnapshotBox();buildEditor();window.__roundtrip={getSunHtml,setSunHtml,capture,editor:()=>editor,fixtures:FIXTURES,version:VERSION};
</script></body></html>`
}
