import { readFile } from 'node:fs/promises'
import http from 'node:http'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const host = process.env.HUMAN_REVIEW_HOST || '127.0.0.1'
const port = Number(process.env.HUMAN_REVIEW_PORT || 4173)
const packageJson = JSON.parse(await readFile(path.join(here, 'package.json'), 'utf8'))
const versions = { jodit: packageJson.dependencies.jodit, suneditor: packageJson.dependencies.suneditor }

const assetRoutes = {
  '/vendor/jodit.js': await firstExisting(['node_modules/jodit/es2021/jodit.min.js', 'node_modules/jodit/build/jodit.min.js']),
  '/vendor/jodit.css': await firstExisting(['node_modules/jodit/es2021/jodit.min.css', 'node_modules/jodit/es2021/jodit.css', 'node_modules/jodit/build/jodit.min.css']),
  '/vendor/suneditor.js': await firstExisting(['node_modules/suneditor/dist/suneditor.min.js']),
  '/vendor/suneditor.css': await firstExisting(['node_modules/suneditor/dist/suneditor.min.css', 'node_modules/suneditor/dist/css/suneditor.min.css', 'node_modules/suneditor/dist/css/suneditor.css']),
  '/vendor/suneditor-zh-cn.js': await firstExisting(['node_modules/suneditor/src/langs/zh_cn.js', 'node_modules/suneditor/dist/lang/zh_cn.js', 'node_modules/suneditor/dist/langs/zh_cn.js']),
}

const initialHtml = [
  '<p><strong>富文本编辑器人工验证。</strong> 请使用真实 Windows 中文输入法、Microsoft Word 与 WPS Writer 完成下面的检查。</p>',
  '<p>基础中文内容：吉林省高校毕业生就业服务。</p>',
  '<ul><li>测试列表中的中文输入</li><li>测试撤销与恢复</li></ul>',
  '<table border="1" cellpadding="4" cellspacing="0"><tbody><tr><td>表格单元格</td><td>请在这里输入中文</td></tr></tbody></table>',
].join('')

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url || '/', `http://${host}:${port}`)
    if (url.pathname === '/' || url.pathname === '/human-review') {
      res.writeHead(200, { 'content-type': 'text/html; charset=utf-8', 'cache-control': 'no-store' })
      res.end(buildHtml())
      return
    }
    if (url.pathname === '/health') {
      res.writeHead(200, { 'content-type': 'application/json; charset=utf-8' })
      res.end(JSON.stringify({ ok: true, versions }))
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
  console.log(`Rich Text Editor V2 Human Review: http://${host}:${port}/`)
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

function buildHtml() {
  const initialJson = JSON.stringify(initialHtml)
  const versionJson = JSON.stringify(versions)
  const rows = ['H1 Windows 中文 IME', 'H2 Microsoft Word 粘贴', 'H3 WPS Writer 粘贴', 'H4 粘贴后继续编辑']
    .map((label, index) => `<tr><td>${label}</td><td><select data-result="jodit-${index + 1}"><option>待验证</option><option>PASS</option><option>FAIL</option></select></td><td><select data-result="suneditor-${index + 1}"><option>待验证</option><option>PASS</option><option>FAIL</option></select></td><td><input data-note="${index + 1}" placeholder="记录异常或体验差异"></td></tr>`)
    .join('')
  return `<!doctype html>
<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Rich Text Editor V2 Human Review</title>
<link rel="stylesheet" href="/vendor/jodit.css"><link rel="stylesheet" href="/vendor/suneditor.css">
<style>
:root{font-family:"Microsoft YaHei","PingFang SC",Arial,sans-serif;color:#1f2328;background:#f6f8fa}*{box-sizing:border-box}body{margin:0}main{max-width:1680px;margin:auto;padding:24px}h1{margin:0 0 8px;font-size:26px}h2{margin:0;font-size:20px}.muted{color:#59636e}.notice,.review-table,.editor-card{background:#fff;border:1px solid #d0d7de;border-radius:10px}.notice{padding:16px 20px;margin:18px 0;line-height:1.65}.actions{display:flex;gap:10px;flex-wrap:wrap;margin:16px 0}button{border:1px solid #8c959f;background:#fff;border-radius:6px;padding:8px 12px;cursor:pointer}.editors{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1fr);gap:18px}.editor-card{padding:16px;min-width:0}.editor-heading{display:flex;justify-content:space-between;align-items:baseline;margin-bottom:12px}details{margin-top:12px}pre{max-height:220px;overflow:auto;white-space:pre-wrap;word-break:break-all;background:#f6f8fa;padding:10px;border-radius:6px;font-size:12px}.review-table{margin-top:20px;overflow:hidden}.matrix{width:100%;border-collapse:collapse}.matrix th,.matrix td{padding:10px;border-bottom:1px solid #d8dee4;text-align:left;vertical-align:top}.matrix th{background:#f6f8fa}select,input{width:100%;min-width:110px;padding:7px;border:1px solid #8c959f;border-radius:5px;background:#fff}#copy-status{color:#1a7f37}@media(max-width:1050px){.editors{grid-template-columns:1fr}}
</style></head><body><main>
<h1>Rich Text Editor V2 — Human Review</h1><div class="muted">隔离 PoC：Jodit ${versions.jodit} / SunEditor ${versions.suneditor}；不连接 CMS、不写入项目数据。</div>
<section class="notice"><strong>人工只验证真实桌面体验：</strong><ol><li><b>H1 Windows 中文 IME：</b>微软拼音连续输入、选词、退格、中英文切换；在粗体、列表、表格中继续输入。</li><li><b>H2 Microsoft Word 粘贴：</b>中文标题、段落、粗体/颜色、编号列表、3×3 表格和图片。</li><li><b>H3 WPS Writer 粘贴：</b>同一内容从 WPS 粘贴，观察列表、表格、字体和图片。</li><li><b>H4 粘贴后继续编辑：</b>插字、删除、改粗体、编辑表格、连续 Undo/Redo 3–5 次。</li></ol><p><b>PASS 重点：</b>不丢字、不重复字、光标不跳、正文/列表/表格稳定。图片只记录现象；未接 CMS Resource uploader 不单独判 FAIL。</p></section>
<div class="actions"><button id="reset-sample">恢复相同测试内容</button><button id="clear-both">清空两个编辑器</button></div>
<section class="editors"><article class="editor-card"><div class="editor-heading"><h2>Jodit</h2><span class="muted">${versions.jodit}</span></div><textarea id="jodit-editor"></textarea><details><summary>实时 HTML <span id="jodit-length"></span></summary><pre id="jodit-output"></pre></details></article><article class="editor-card"><div class="editor-heading"><h2>SunEditor</h2><span class="muted">${versions.suneditor}</span></div><textarea id="suneditor-editor"></textarea><details><summary>实时 HTML <span id="suneditor-length"></span></summary><pre id="suneditor-output"></pre></details></article></section>
<section class="review-table"><table class="matrix"><thead><tr><th>人工 Gate</th><th>Jodit</th><th>SunEditor</th><th>备注</th></tr></thead><tbody>${rows}</tbody></table><div class="actions" style="padding:0 12px 12px;margin-bottom:0"><button id="copy-result">复制人工验证结果</button><span id="copy-status"></span></div></section>
</main><script src="/vendor/jodit.js"></script><script src="/vendor/suneditor.js"></script><script src="/vendor/suneditor-zh-cn.js"></script><script>
const INITIAL_HTML=${initialJson};const VERSIONS=${versionJson};
const jodit=Jodit.make('#jodit-editor',{value:INITIAL_HTML,language:'zh_cn',height:430,askBeforePasteFromWord:false,defaultActionOnPasteFromWord:'insert_as_html',defaultActionOnPaste:'insert_as_html'});
const suneditor=SUNEDITOR.create('#suneditor-editor',{value:INITIAL_HTML,plugins:SUNEDITOR.plugins,lang:window.SUNEDITOR_LANG&&window.SUNEDITOR_LANG.zh_cn,height:'430px',buttonList:[['undo','redo'],['bold','italic','underline','strike'],['list','table'],['link','image']],attributeWhitelist:{table:'align|cellpadding|cellspacing'},tagStyles:{td:'width|height'}});
function sunValue(){return typeof suneditor.getContents==='function'?suneditor.getContents():suneditor.$.html.get()}function setSunValue(value){if(typeof suneditor.setContents==='function')suneditor.setContents(value);else suneditor.$.html.set(value)}
function updateOutputs(){const a=jodit.value||'',b=sunValue()||'';document.querySelector('#jodit-output').textContent=a;document.querySelector('#suneditor-output').textContent=b;document.querySelector('#jodit-length').textContent='('+a.length+' chars)';document.querySelector('#suneditor-length').textContent='('+b.length+' chars)'}
document.querySelector('#reset-sample').onclick=()=>{jodit.value=INITIAL_HTML;setSunValue(INITIAL_HTML);updateOutputs()};document.querySelector('#clear-both').onclick=()=>{jodit.value='<p><br></p>';setSunValue('<p><br></p>');updateOutputs()};
document.querySelector('#copy-result').onclick=async()=>{const labels=['H1 Windows 中文 IME','H2 Microsoft Word 粘贴','H3 WPS Writer 粘贴','H4 粘贴后继续编辑'];const lines=['Rich Text Editor V2 Human Review','Jodit: '+VERSIONS.jodit+' / SunEditor: '+VERSIONS.suneditor,''];labels.forEach((label,index)=>{const n=index+1,a=document.querySelector('[data-result="jodit-'+n+'"]').value,b=document.querySelector('[data-result="suneditor-'+n+'"]').value,note=document.querySelector('[data-note="'+n+'"]').value.trim();lines.push(label+': Jodit='+a+', SunEditor='+b+(note?', 备注='+note:''))});await navigator.clipboard.writeText(lines.join(String.fromCharCode(10)));const s=document.querySelector('#copy-status');s.textContent='已复制';setTimeout(()=>s.textContent='',1500)};
window.__humanReview={jodit,suneditor,sunValue,setSunValue,updateOutputs,versions:VERSIONS};setInterval(updateOutputs,400);updateOutputs();
</script></body></html>`
}
