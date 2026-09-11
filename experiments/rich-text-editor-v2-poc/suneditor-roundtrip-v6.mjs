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
const partyAssetsDir = path.join(partyArticleDir, 'assets')
const siteAssetsDir = path.join(repoRoot, 'sites/jilinjobs/assets')
const partyArticle = JSON.parse(await readFile(path.join(partyArticleDir, 'article.json'), 'utf8'))
const pagesDocument = JSON.parse(await readFile(path.join(repoRoot, 'sites/jilinjobs/structure/pages.json'), 'utf8'))
const teacherPage = findObject(pagesDocument, value => value?.alias === 'teacher-library' && typeof value?.bodyHtml === 'string')
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

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url || '/', `http://${host}:${port}`)
    if (url.pathname === '/' || url.pathname === '/roundtrip') return send(res, 200, 'text/html; charset=utf-8', buildHtml())
    if (url.pathname === '/health') return send(res, 200, 'application/json; charset=utf-8', JSON.stringify({ ok: true, suneditor: version, harness: 6 }))
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
    if (vendor) return send(res, 200, url.pathname.endsWith('.css') ? 'text/css; charset=utf-8' : 'text/javascript; charset=utf-8', await readFile(vendor))
    send(res, 404, 'text/plain; charset=utf-8', `Not found: ${url.pathname}`)
  } catch (error) {
    send(res, 500, 'text/plain; charset=utf-8', String(error?.stack || error))
  }
})

server.listen(port, host, () => {
  console.log(`SunEditor ${version} legacy-image round-trip harness V6: http://${host}:${port}/`)
  console.log('V6 applies legacy dimensions through SunEditor Figure/ImageSizeService APIs.')
})
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => server.close(() => process.exit(0)))

function send(res, status, contentType, body) {
  res.writeHead(status, { 'content-type': contentType, 'cache-control': 'no-store' })
  res.end(body)
}
async function firstExisting(relativePaths) {
  for (const relativePath of relativePaths) {
    const absolutePath = path.join(here, relativePath)
    try { await readFile(absolutePath); return absolutePath } catch {}
  }
  throw new Error(`Unable to locate required vendor asset. Tried: ${relativePaths.join(', ')}`)
}
async function readFromRoots(relativePath, roots) {
  if (!relativePath || relativePath.includes('\0')) return null
  for (const root of roots) {
    const resolvedRoot = path.resolve(root)
    const candidate = path.resolve(resolvedRoot, relativePath)
    if (candidate !== resolvedRoot && !candidate.startsWith(`${resolvedRoot}${path.sep}`)) continue
    try { return { path: candidate, content: await readFile(candidate) } } catch {}
  }
  return null
}
function mimeType(filePath) {
  switch (path.extname(filePath).toLowerCase()) {
    case '.png': return 'image/png'
    case '.jpg': case '.jpeg': return 'image/jpeg'
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
<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>SunEditor Legacy Image Round-trip V6</title>
<link rel="stylesheet" href="/vendor/suneditor.css"><style>
:root{font-family:"Microsoft YaHei","PingFang SC",Arial,sans-serif;color:#1f2328;background:#f6f8fa}*{box-sizing:border-box}body{margin:0}main{max-width:1500px;margin:auto;padding:24px}h1{margin:0 0 8px;font-size:26px}h2{font-size:20px}.muted{color:#59636e}.panel{background:#fff;border:1px solid #d0d7de;border-radius:10px;padding:16px;margin:16px 0}.fixture{border:1px solid #d8dee4;border-radius:8px;padding:12px;margin:12px 0}.head{display:flex;justify-content:space-between;gap:12px;align-items:center}.actions{display:flex;gap:8px;flex-wrap:wrap;margin:10px 0;align-items:center}button{border:1px solid #8c959f;background:#fff;border-radius:6px;padding:8px 12px;cursor:pointer}button.primary{background:#0969da;color:#fff;border-color:#0969da}textarea.raw{width:100%;min-height:160px;resize:vertical;padding:10px;border:1px solid #8c959f;border-radius:6px;font-family:Consolas,"Courier New",monospace;font-size:12px;line-height:1.5}.outputs{display:grid;grid-template-columns:1fr 1fr;gap:14px}.outputs textarea{min-height:220px}.error{color:#cf222e;font-weight:700}pre{max-height:520px;overflow:auto;white-space:pre-wrap;word-break:break-all;background:#f6f8fa;padding:10px;border-radius:6px;font-size:12px}@media(max-width:900px){.outputs{grid-template-columns:1fr}}
</style></head><body><main>
<h1>SunEditor ${version} — Legacy Image Round-trip V6</h1>
<div class="muted">V6 不再直接修改图片 CSS，而是通过 SunEditor 自己的 Figure / ImageSizeService 写入尺寸状态。</div>
<section class="panel"><b>本轮判定：</b>最小样本两张真实图片应实际显示为 15×15 / 16×16，可点击弹出图片面板；保存模拟 CMS HTML 并重新加载后仍满足相同条件。</section>
<section class="panel"><h2>原始 HTML 输入</h2>
<div class="fixture"><div class="head"><b>P1 — Party canonical HTML</b><span id="p1-meta" class="muted"></span></div><textarea id="source-p1" class="raw"></textarea><div class="actions"><button class="primary" data-load="p1">加载到 SunEditor</button><button data-reset="p1">恢复原始值</button></div></div>
<div class="fixture"><div class="head"><b>P2 — teacher-library canonical HTML</b><span id="p2-meta" class="muted"></span></div><textarea id="source-p2" class="raw"></textarea><div class="actions"><button class="primary" data-load="p2">加载到 SunEditor</button><button data-reset="p2">恢复原始值</button></div></div>
<div class="fixture"><div class="head"><b>自定义 / 最小复现 HTML</b><span id="minimal-meta" class="muted"></span></div><textarea id="source-minimal" class="raw"></textarea><div class="actions"><button class="primary" data-load="minimal">加载到 SunEditor</button><button data-reset="minimal">恢复真实图片 15×15 / 16×16 样本</button></div></div>
</section>
<section class="panel"><div class="actions"><label><input type="checkbox" id="bridge" checked> <b>Legacy image bridge</b></label><label><input type="checkbox" id="v2-migration"> v2Migration</label><button id="rebuild">按当前模式重建编辑器</button><span id="mode"></span></div><div id="startup-error" class="error"></div><textarea id="editor"></textarea><div class="actions"><button class="primary" id="capture">读取当前 HTML</button><button id="save">保存模拟 CMS HTML 快照</button><button id="reload-current">从模拟 CMS HTML 重新加载</button><button id="reload-snapshot">从浏览器快照重新加载</button><button id="clear">清除快照</button><button id="copy">复制模拟 CMS HTML</button></div><div id="status" class="muted"></div></section>
<section class="panel"><h2>Round-trip 输出</h2><div class="outputs"><div><b>SunEditor 内部序列化 HTML</b><textarea id="internal" class="raw"></textarea></div><div><b>模拟 CMS 保存 HTML</b><textarea id="app-html" class="raw"></textarea></div></div><div class="fixture"><b>浏览器保存快照</b><textarea id="snapshot" class="raw"></textarea></div></section>
<section class="panel"><h2>Bridge 事件</h2><pre id="events-log"></pre></section>
<section class="panel"><h2>图片诊断</h2><pre id="diagnostics"></pre></section>
</main><script src="/vendor/suneditor.js"></script><script src="/vendor/suneditor-zh-cn.js"></script><script>
const STORAGE_KEY='jilinjobs.suneditor.roundtrip.v6';let editor=null;const original={};let pendingStates=[];let applyCount=0;const logs=[];const loadBound=new WeakSet();
const q=s=>document.querySelector(s);const setStatus=t=>q('#status').textContent=t;
function log(message){logs.push(new Date().toISOString().slice(11,23)+' '+message);if(logs.length>30)logs.shift();q('#events-log').textContent=logs.join('\n')}
async function fetchFixture(name){const r=await fetch('/fixture/'+name);if(!r.ok)throw new Error(await r.text());return r.text()}
async function initFixtures(){for(const name of ['p1','p2','minimal']){original[name]=await fetchFixture(name);const box=q('#source-'+name);box.value=original[name];box.oninput=()=>meta(name);meta(name)}}
function meta(name){q('#'+name+'-meta').textContent=(q('#source-'+name).value||'').length+' chars'}
function dimension(value){const v=(value||'').trim();if(/^\d+(?:\.\d+)?$/.test(v))return v+'px';if(/^\d+(?:\.\d+)?(?:px|%)$/i.test(v))return v;return ''}
function attrDimension(value){const v=dimension(value);return v.endsWith('px')?v.slice(0,-2):v}
function imageStates(raw){const t=document.createElement('template');t.innerHTML=raw;return [...t.content.querySelectorAll('img')].map(img=>({src:img.getAttribute('src')||'',alt:img.getAttribute('alt')||'',width:dimension(img.style.width)||dimension(img.getAttribute('width')),height:dimension(img.style.height)||dimension(img.getAttribute('height')),float:(img.style.float||img.getAttribute('align')||'').toLowerCase()}))}
function prepareForEditor(raw){if(!q('#bridge').checked)return raw;const t=document.createElement('template');t.innerHTML=raw;t.content.querySelectorAll('img').forEach(img=>{if(img.closest('.se-component'))return;const wrapper=document.createElement('span');const float=(img.style.float||img.getAttribute('align')||'').toLowerCase();const align=float==='left'||float==='right'||float==='center'?float:'none';wrapper.className='se-component se-inline-component se-image-container __se__float-'+align;img.replaceWith(wrapper);wrapper.appendChild(img)});return t.innerHTML}
function surface(){return editor?.$?.frameContext?.get?.('wysiwyg')||q('.se-wrapper-wysiwyg[contenteditable="true"]')}
function bindNativeLoads(){const s=surface();if(!s)return;[...s.querySelectorAll('img')].forEach(img=>{if(loadBound.has(img))return;loadBound.add(img);img.addEventListener('load',()=>{log('native img load: '+(img.getAttribute('alt')||img.src));applyLegacyStatesViaApi('native-load')},{once:true})})}
function applyLegacyStatesViaApi(reason){if(!q('#bridge').checked||!editor||pendingStates.length===0)return;const imagePlugin=editor.$?.plugins?.image;const s=surface();if(!imagePlugin?.figure||!imagePlugin?.sizeService||!s){log(reason+': image API unavailable');return}const images=[...s.querySelectorAll('img')];let applied=0;for(let index=0;index<images.length;index++){const img=images[index];const state=pendingStates[index];if(!state)continue;try{imagePlugin.figure.open(img,{nonResizing:true,nonSizeInfo:true,nonBorder:true,figureTarget:false,infoOnly:true});imagePlugin.sizeService.applySize(state.width||'auto',state.height||'auto');const component=img.closest('.se-component.se-image-container');if(component){component.classList.remove('__se__float-none','__se__float-left','__se__float-center','__se__float-right');const align=state.float==='left'||state.float==='right'||state.float==='center'?state.float:'none';component.classList.add('__se__float-'+align)}applied++}catch(error){log(reason+' image '+(index+1)+' ERROR '+(error?.message||error))}}applyCount++;log(reason+': API applied '+applied+'/'+images.length+' (#'+applyCount+')');diagnostics()}
function normalizeForApp(html){const t=document.createElement('template');t.innerHTML=html;t.content.querySelectorAll('.se-component.se-image-container').forEach(component=>{const img=component.querySelector('img');if(!img)return;const clone=img.cloneNode(true);const data=(clone.getAttribute('data-se-size')||'').split(',');const w=dimension(clone.style.width)||dimension(data[0])||dimension(clone.getAttribute('width'));const h=dimension(clone.style.height)||dimension(data[1])||dimension(clone.getAttribute('height'));if(w)clone.setAttribute('width',attrDimension(w));else clone.removeAttribute('width');if(h)clone.setAttribute('height',attrDimension(h));else clone.removeAttribute('height');for(const name of [...clone.getAttributeNames()])if(name.startsWith('data-se-'))clone.removeAttribute(name);clone.style.removeProperty('width');clone.style.removeProperty('height');clone.style.removeProperty('float');if(component.classList.contains('__se__float-left'))clone.style.float='left';if(component.classList.contains('__se__float-right'))clone.style.float='right';if(!clone.getAttribute('style'))clone.removeAttribute('style');component.replaceWith(clone)});return t.innerHTML}
function getHtml(){if(!editor)return '';return typeof editor.getContents==='function'?editor.getContents():editor.$.html.get()}
function setRawHtml(raw){if(!editor)return;pendingStates=imageStates(raw);const value=prepareForEditor(raw);log('setRawHtml: '+pendingStates.length+' legacy image state(s)');if(typeof editor.setContents==='function')editor.setContents(value);else editor.$.html.set(value);bindNativeLoads();try{editor.$?.pluginManager?.checkFileInfo?.(true);log('checkFileInfo(true) completed')}catch(error){log('checkFileInfo ERROR '+(error?.message||error))}applyLegacyStatesViaApi('post-set');requestAnimationFrame(()=>applyLegacyStatesViaApi('raf'))}
function destroy(){if(!editor)return;try{editor.destroy()}catch{}editor=null}
function rebuild(){destroy();const old=q('#editor');const fresh=document.createElement('textarea');fresh.id='editor';old.replaceWith(fresh);q('#startup-error').textContent='';const migration=q('#v2-migration').checked;try{editor=SUNEDITOR.create('#editor',{plugins:SUNEDITOR.plugins,lang:window.SUNEDITOR_LANG&&window.SUNEDITOR_LANG.zh_cn,height:'430px',v2Migration:migration,buttonList:[['undo','redo'],['bold','italic','underline','strike'],['list','table'],['link','image']],attributeWhitelist:{table:'align|cellpadding|cellspacing',img:'width|height|align'},tagStyles:{td:'width|height',img:'width|height|float'},events:{onImageLoad:()=>{log('onImageLoad');requestAnimationFrame(()=>applyLegacyStatesViaApi('onImageLoad'))}}});q('#mode').textContent='bridge='+q('#bridge').checked+', v2Migration='+migration;setRawHtml(q('#app-html').value||original.minimal||'');setStatus('SunEditor 初始化成功')}catch(error){q('#startup-error').textContent='SunEditor 初始化失败：'+(error?.message||error);console.error(error)}}
function capture(){const internal=getHtml();const app=normalizeForApp(internal);q('#internal').value=internal;q('#app-html').value=app;diagnostics();setStatus('已读取当前 HTML');return app}
function diagnostics(){const s=surface();if(!s){q('#diagnostics').textContent='未找到可编辑 surface';return}const rows=[...s.querySelectorAll('img')].map((img,index)=>{const r=img.getBoundingClientRect();const c=getComputedStyle(img);const component=img.closest('.se-component.se-image-container');const cr=component?.getBoundingClientRect();return{index:index+1,alt:img.getAttribute('alt'),src:img.getAttribute('src'),width:img.getAttribute('width'),height:img.getAttribute('height'),style:img.getAttribute('style'),dataSeSize:img.getAttribute('data-se-size'),componentTag:component?.tagName||null,componentClass:component?.className||null,componentStyle:component?.getAttribute('style')||null,naturalWidth:img.naturalWidth,naturalHeight:img.naturalHeight,computedWidth:c.width,computedHeight:c.height,renderedWidth:Math.round(r.width*100)/100,renderedHeight:Math.round(r.height*100)/100,componentRenderedWidth:cr?Math.round(cr.width*100)/100:null,componentRenderedHeight:cr?Math.round(cr.height*100)/100:null}});q('#diagnostics').textContent=rows.length?JSON.stringify(rows,null,2):'当前没有图片'}
function load(name){setRawHtml(q('#source-'+name).value);requestAnimationFrame(()=>{capture();setStatus('已加载 '+name)})}
function reset(name){q('#source-'+name).value=original[name];meta(name)}
q('#rebuild').onclick=rebuild;q('#capture').onclick=capture;q('#save').onclick=()=>{const app=capture();localStorage.setItem(STORAGE_KEY,app);q('#snapshot').value=app;setStatus('已保存模拟 CMS HTML 快照')};q('#reload-current').onclick=()=>setRawHtml(q('#app-html').value);q('#reload-snapshot').onclick=()=>{const raw=localStorage.getItem(STORAGE_KEY)||'';if(!raw){setStatus('没有浏览器快照');return}q('#app-html').value=raw;setRawHtml(raw)};q('#clear').onclick=()=>{localStorage.removeItem(STORAGE_KEY);q('#snapshot').value='';setStatus('已清除快照')};q('#copy').onclick=async()=>{await navigator.clipboard.writeText(q('#app-html').value);setStatus('已复制模拟 CMS HTML')};
document.querySelectorAll('[data-load]').forEach(b=>b.onclick=()=>load(b.dataset.load));document.querySelectorAll('[data-reset]').forEach(b=>b.onclick=()=>reset(b.dataset.reset));
(async()=>{await initFixtures();q('#snapshot').value=localStorage.getItem(STORAGE_KEY)||'';q('#app-html').value=original.minimal;rebuild();window.__roundtripV6={capture,getHtml,setRawHtml,applyLegacyStatesViaApi,normalizeForApp}})().catch(error=>{q('#startup-error').textContent=String(error?.stack||error);console.error(error)})
</script></body></html>`
}
