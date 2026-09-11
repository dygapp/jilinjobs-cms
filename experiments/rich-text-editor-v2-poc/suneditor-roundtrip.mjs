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

const partyArticlePath = path.join(
  repoRoot,
  'data-migrations/party/v1/articles/zhutijiaoyu-content-154659859759104/article.json',
)
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

for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => server.close(() => process.exit(0)))
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

function scriptJson(value) {
  return JSON.stringify(value).replaceAll('</script', '<\\/script')
}

function buildHtml() {
  const fixturesJson = scriptJson(fixtures)
  const versionJson = scriptJson(version)
  return `<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>SunEditor HTML Round-trip Harness</title>
<link rel="stylesheet" href="/vendor/suneditor.css">
<style>
:root{font-family:"Microsoft YaHei","PingFang SC",Arial,sans-serif;color:#1f2328;background:#f6f8fa}*{box-sizing:border-box}body{margin:0}main{max-width:1500px;margin:auto;padding:24px}h1{margin:0 0 8px;font-size:26px}h2{font-size:20px;margin:0 0 12px}.muted{color:#59636e}.panel{background:#fff;border:1px solid #d0d7de;border-radius:10px;padding:16px;margin:16px 0}.notice{line-height:1.7}.fixture-grid{display:grid;grid-template-columns:1fr;gap:14px}.fixture{border:1px solid #d8dee4;border-radius:8px;padding:12px}.fixture-head{display:flex;gap:12px;align-items:center;justify-content:space-between;margin-bottom:8px}.fixture-title{font-weight:700}.actions{display:flex;gap:8px;flex-wrap:wrap;margin:10px 0}button{border:1px solid #8c959f;background:#fff;border-radius:6px;padding:8px 12px;cursor:pointer}button.primary{background:#0969da;color:#fff;border-color:#0969da}textarea.raw{width:100%;min-height:170px;resize:vertical;padding:10px;border:1px solid #8c959f;border-radius:6px;font-family:Consolas,"Courier New",monospace;font-size:12px;line-height:1.5;background:#fff}.editor-shell{min-height:360px}.output-grid{display:grid;grid-template-columns:1fr 1fr;gap:14px}.output-grid textarea{min-height:220px}.options{display:flex;gap:18px;align-items:center;flex-wrap:wrap}.status{font-weight:700}.ok{color:#1a7f37}.warn{color:#9a6700}pre{max-height:360px;overflow:auto;white-space:pre-wrap;word-break:break-all;background:#f6f8fa;padding:10px;border-radius:6px;font-size:12px}@media(max-width:900px){.output-grid{grid-template-columns:1fr}}
</style>
</head>
<body>
<main>
<h1>SunEditor ${version} — HTML Round-trip Harness</h1>
<div class="muted">独立实验页面；不连接 CMS API、不写数据库。P1/P2 文本框直接读取仓库当前真实 canonical HTML。</div>

<section class="panel notice">
<h2>建议验证顺序</h2>
<ol>
<li>先点击 <b>P1 自动失败样本 → 加载到 SunEditor</b>，再点“读取当前 HTML”，观察第一张图片是否从 <code>15×15</code> 变为 <code>auto×auto</code>。</li>
<li>可勾选 <b>v2Migration</b> 后“重建编辑器”，再次加载同一 P1 HTML，对比官方兼容模式。</li>
<li>手工添加图片并设为 <b>16×16px</b>，点“读取当前 HTML” → “保存浏览器快照” → “从快照重新加载”，确认尺寸是否保持。</li>
<li>三个“原始 HTML”都是普通 textarea，可直接粘贴或修改任意 HTML 后加载。</li>
</ol>
</section>

<section class="panel">
<h2>原始 HTML 输入</h2>
<div class="fixture-grid">
  <div class="fixture">
    <div class="fixture-head"><span class="fixture-title">P1 — 当前自动验证失败的 Party canonical HTML</span><span class="muted" id="p1-meta"></span></div>
    <textarea class="raw" id="source-p1" spellcheck="false"></textarea>
    <div class="actions"><button class="primary" data-load="p1">加载到 SunEditor</button><button data-reset="p1">恢复仓库原始值</button></div>
  </div>
  <div class="fixture">
    <div class="fixture-head"><span class="fixture-title">P2 — teacher-library canonical HTML</span><span class="muted" id="p2-meta"></span></div>
    <textarea class="raw" id="source-p2" spellcheck="false"></textarea>
    <div class="actions"><button class="primary" data-load="p2">加载到 SunEditor</button><button data-reset="p2">恢复仓库原始值</button></div>
  </div>
  <div class="fixture">
    <div class="fixture-head"><span class="fixture-title">自定义 / 最小复现 HTML</span><span class="muted" id="minimal-meta"></span></div>
    <textarea class="raw" id="source-minimal" spellcheck="false"></textarea>
    <div class="actions"><button class="primary" data-load="minimal">加载到 SunEditor</button><button data-reset="minimal">恢复 15×15 / 16×16 最小样本</button></div>
  </div>
</div>
</section>

<section class="panel">
<div class="options">
  <h2 style="margin:0">SunEditor</h2>
  <label><input type="checkbox" id="v2-migration"> 启用 SunEditor 官方 <code>v2Migration</code></label>
  <button id="rebuild-editor">按当前模式重建编辑器</button>
  <span id="editor-mode" class="status"></span>
</div>
<div class="editor-shell"><textarea id="suneditor-editor"></textarea></div>
<div class="actions">
  <button class="primary" id="capture-html">读取当前 HTML</button>
  <button id="save-snapshot">保存浏览器快照</button>
  <button id="reload-output">从“当前序列化 HTML”重新加载</button>
  <button id="reload-snapshot">从浏览器快照重新加载</button>
  <button id="clear-snapshot">清除浏览器快照</button>
  <button id="copy-output">复制当前序列化 HTML</button>
</div>
<div id="action-status" class="muted"></div>
</section>

<section class="panel">
<h2>Round-trip 输出</h2>
<div class="output-grid">
  <div><div class="fixture-head"><b>当前序列化 HTML</b><span class="muted" id="output-meta"></span></div><textarea class="raw" id="current-html" spellcheck="false"></textarea></div>
  <div><div class="fixture-head"><b>浏览器保存快照（localStorage）</b><span class="muted" id="snapshot-meta"></span></div><textarea class="raw" id="snapshot-html" spellcheck="false"></textarea></div>
</div>
</section>

<section class="panel">
<h2>图片尺寸诊断</h2>
<div class="muted">每次“读取当前 HTML”或重新加载后自动刷新。重点看 <code>width</code> / <code>height</code> / <code>style</code> / <code>data-se-size</code> 与实际 bounding box。</div>
<pre id="diagnostics"></pre>
</section>
</main>
<script src="/vendor/suneditor.js"></script>
<script src="/vendor/suneditor-zh-cn.js"></script>
<script>
const FIXTURES=${fixturesJson};
const VERSION=${versionJson};
const STORAGE_KEY='jilinjobs.suneditor.roundtrip.snapshot.v1';
let editor=null;

function getSunHtml(){
  if(!editor)return '';
  return typeof editor.getContents==='function'?editor.getContents():editor.$.html.get();
}
function setSunHtml(value){
  if(!editor)return;
  if(typeof editor.setContents==='function')editor.setContents(value);else editor.$.html.set(value);
  setTimeout(updateDiagnostics,80);
}
function destroyEditor(){
  if(!editor)return;
  try{editor.destroy()}catch{}
  editor=null;
}
function buildEditor(){
  destroyEditor();
  const old=document.querySelector('#suneditor-editor');
  const fresh=document.createElement('textarea');
  fresh.id='suneditor-editor';
  old.replaceWith(fresh);
  const migration=document.querySelector('#v2-migration').checked;
  editor=SUNEDITOR.create('#suneditor-editor',{
    plugins:SUNEDITOR.plugins,
    lang:window.SUNEDITOR_LANG&&window.SUNEDITOR_LANG.zh_cn,
    height:'430px',
    v2Migration:migration,
    buttonList:[['undo','redo'],['bold','italic','underline','strike'],['font','fontSize','fontColor','hiliteColor'],['align','list','blockquote','horizontalRule'],['table','link','image'],['removeFormat','codeView','fullScreen']],
    attributeWhitelist:{table:'align|cellpadding|cellspacing',img:'width|height|align'},
    tagStyles:{td:'width|height','img|video|iframe':'width|height|float'},
  });
  document.querySelector('#editor-mode').textContent='v2Migration='+migration;
  document.querySelector('#editor-mode').className='status '+(migration?'warn':'ok');
  setSunHtml(document.querySelector('#current-html').value||FIXTURES.minimal);
}
function capture(){
  const html=getSunHtml()||'';
  document.querySelector('#current-html').value=html;
  document.querySelector('#output-meta').textContent=html.length+' chars';
  updateDiagnostics();
  status('已读取当前 HTML');
  return html;
}
function snapshotValue(){return localStorage.getItem(STORAGE_KEY)||''}
function refreshSnapshotBox(){
  const value=snapshotValue();
  document.querySelector('#snapshot-html').value=value;
  document.querySelector('#snapshot-meta').textContent=value?value.length+' chars':'无快照';
}
function status(text){document.querySelector('#action-status').textContent=text}
function loadCase(name){
  const value=document.querySelector('#source-'+name).value;
  document.querySelector('#current-html').value=value;
  setSunHtml(value);
  capture();
  status('已加载 '+name+' 原始 HTML');
}
function resetCase(name){
  document.querySelector('#source-'+name).value=FIXTURES[name];
  updateSourceMeta(name);
  status('已恢复 '+name+' 仓库/内置原始值');
}
function updateSourceMeta(name){
  const value=document.querySelector('#source-'+name).value||'';
  document.querySelector('#'+name+'-meta').textContent=value.length+' chars';
}
function updateDiagnostics(){
  if(!editor)return;
  const surface=document.querySelector('.se-wrapper-wysiwyg[contenteditable="true"]');
  const images=surface?Array.from(surface.querySelectorAll('img')):[];
  const rows=images.map((img,index)=>{
    const rect=img.getBoundingClientRect();
    return {
      index:index+1,
      alt:img.getAttribute('alt'),
      width:img.getAttribute('width'),
      height:img.getAttribute('height'),
      dataSeSize:img.getAttribute('data-se-size'),
      style:img.getAttribute('style'),
      rendered:Math.round(rect.width)+'×'+Math.round(rect.height),
      src:(img.getAttribute('src')||'').slice(0,120),
    };
  });
  document.querySelector('#diagnostics').textContent=JSON.stringify({count:rows.length,images:rows},null,2);
}

for(const name of ['p1','p2','minimal']){
  document.querySelector('#source-'+name).value=FIXTURES[name];
  updateSourceMeta(name);
  document.querySelector('#source-'+name).addEventListener('input',()=>updateSourceMeta(name));
}
document.querySelectorAll('[data-load]').forEach(button=>button.addEventListener('click',()=>loadCase(button.dataset.load)));
document.querySelectorAll('[data-reset]').forEach(button=>button.addEventListener('click',()=>resetCase(button.dataset.reset)));
document.querySelector('#rebuild-editor').onclick=()=>{const html=document.querySelector('#current-html').value||getSunHtml()||FIXTURES.minimal;document.querySelector('#current-html').value=html;buildEditor();setSunHtml(html);capture();status('已按当前 v2Migration 模式重建')};
document.querySelector('#capture-html').onclick=capture;
document.querySelector('#save-snapshot').onclick=()=>{const html=capture();localStorage.setItem(STORAGE_KEY,html);refreshSnapshotBox();status('已保存到浏览器 localStorage；刷新页面后仍可恢复')};
document.querySelector('#reload-output').onclick=()=>{setSunHtml(document.querySelector('#current-html').value);capture();status('已从当前序列化 HTML 重新加载')};
document.querySelector('#reload-snapshot').onclick=()=>{const value=document.querySelector('#snapshot-html').value||snapshotValue();if(!value){status('没有可加载的浏览器快照');return}setSunHtml(value);document.querySelector('#current-html').value=value;capture();status('已从浏览器快照重新加载')};
document.querySelector('#clear-snapshot').onclick=()=>{localStorage.removeItem(STORAGE_KEY);refreshSnapshotBox();status('已清除浏览器快照')};
document.querySelector('#copy-output').onclick=async()=>{const html=capture();await navigator.clipboard.writeText(html);status('已复制当前序列化 HTML')};
refreshSnapshotBox();
document.querySelector('#current-html').value=FIXTURES.minimal;
buildEditor();
loadCase('minimal');
window.__suneditorRoundtrip={get editor(){return editor},getSunHtml,setSunHtml,capture,fixtures:FIXTURES,version:VERSION};
</script>
</body>
</html>`
}
