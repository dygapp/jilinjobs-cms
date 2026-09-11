import { readFile, writeFile } from 'node:fs/promises'
import http from 'node:http'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { chromium } from '@playwright/test'

const here = path.dirname(fileURLToPath(import.meta.url))
const OFFICE_HTML = `<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word"><head><meta name=ProgId content=Word.Document><style>p.MsoNormal{margin:0cm;font-family:"宋体";font-size:12.0pt;mso-style-name:正文;}p.MsoListParagraph{margin-left:21.0pt;mso-list:l0 level1 lfo1;font-family:"宋体";}table.MsoTableGrid{border-collapse:collapse;mso-table-layout-alt:fixed;}</style></head><body><!--StartFragment--><p class=MsoNormal style='color:#1F4E79;mso-bidi-font-size:12.0pt'>吉林省高校毕业生就业服务</p><p class=MsoListParagraph style='mso-list:l0 level1 lfo1'>1. 第一项：就业手续办理</p><table class=MsoTableGrid border=1><tr><td><p class=MsoNormal>学校</p></td><td><p class=MsoNormal>人数</p></td></tr><tr><td><p class=MsoNormal>吉林大学</p></td><td><p class=MsoNormal>100</p></td></tr></table><!--EndFragment--></body></html>`
const vendorFiles = {
  '/jodit.js': path.join(here, 'node_modules/jodit/es2021/jodit.min.js'),
  '/suneditor.js': path.join(here, 'node_modules/suneditor/dist/suneditor.min.js'),
}
const server = http.createServer(async (req, res) => {
  if (req.url in vendorFiles) {
    res.writeHead(200, { 'content-type': 'text/javascript; charset=utf-8' })
    res.end(await readFile(vendorFiles[req.url]))
    return
  }
  const candidate = req.url === '/suneditor' ? 'suneditor' : 'jodit'
  res.writeHead(200, { 'content-type': 'text/html; charset=utf-8' })
  res.end(`<!doctype html><meta charset="utf-8"><textarea id="editor"></textarea><script src="/${candidate}.js"></script>`)
})
await new Promise(resolve => server.listen(0, '127.0.0.1', resolve))
const { port } = server.address()
const browser = await chromium.launch({ headless: true })
const results = { gates: {} }
for (const candidate of ['jodit', 'suneditor']) {
  results.gates[candidate] = {
    p3OrdinaryAuthoring: await safe(() => p3(candidate)),
    p4aOfficeClipboardStructure: await safe(() => p4a(candidate)),
  }
}
results.summary = Object.fromEntries(Object.entries(results.gates).map(([candidate, gates]) => [candidate, Object.values(gates).every(value => value?.pass === true)]))
results.scope = {
  p4a: 'Office-shaped automated clipboard precheck only; real Windows Word/WPS paste remains pending.',
  p6: 'Real Windows Chinese IME remains pending.',
}
await browser.close()
await new Promise(resolve => server.close(resolve))
await writeFile(path.join(here, 'behavior-final-results.json'), `${JSON.stringify(results, null, 2)}\n`)
console.log(JSON.stringify(results, null, 2))

async function p3(candidate) {
  const page = await newPage(candidate, '<p>已有正文</p>')
  const editable = page.locator(selector(candidate))
  await editable.evaluate(node => {
    node.focus()
    const range = document.createRange()
    range.selectNodeContents(node)
    range.collapse(false)
    const selection = window.getSelection()
    selection.removeAllRanges()
    selection.addRange(range)
  })
  await page.keyboard.insertText('吉林省就业服务')
  await page.waitForTimeout(700)
  const typed = await output(page, candidate)
  await page.evaluate(candidate => {
    if (candidate === 'jodit') window.__pocEditor.history.undo()
    else window.__pocEditor.$.history.undo()
  }, candidate)
  await page.waitForTimeout(120)
  const undone = await output(page, candidate)
  await page.evaluate(candidate => {
    if (candidate === 'jodit') window.__pocEditor.history.redo()
    else window.__pocEditor.$.history.redo()
  }, candidate)
  await page.waitForTimeout(120)
  const redone = await output(page, candidate)
  await page.close()
  return {
    pass: typed.includes('吉林省就业服务') && !undone.includes('吉林省就业服务') && redone.includes('吉林省就业服务'),
    typed: typed.includes('吉林省就业服务'),
    undone: !undone.includes('吉林省就业服务'),
    redone: redone.includes('吉林省就业服务'),
  }
}

async function p4a(candidate) {
  const page = await newPage(candidate, '<p>粘贴位置：</p>', true)
  const editable = page.locator(selector(candidate))
  await editable.click()
  await editable.evaluate((node, html) => {
    const data = new DataTransfer()
    data.setData('text/html', html)
    data.setData('text/plain', '吉林省高校毕业生就业服务\n1. 第一项：就业手续办理\n学校 人数\n吉林大学 100')
    node.dispatchEvent(new ClipboardEvent('paste', { clipboardData: data, bubbles: true, cancelable: true }))
  }, OFFICE_HTML)
  await page.waitForTimeout(300)
  const html = await output(page, candidate)
  const facts = await page.evaluate(value => {
    const doc = new DOMParser().parseFromString(value, 'text/html')
    return {
      hasTitle: doc.body.textContent.includes('吉林省高校毕业生就业服务'),
      hasListText: doc.body.textContent.includes('第一项：就业手续办理'),
      hasTable: Boolean(doc.querySelector('table')),
      hasSchool: doc.body.textContent.includes('吉林大学'),
      hasCount: doc.body.textContent.includes('100'),
      msoClassCount: doc.querySelectorAll('[class*="Mso"]').length,
      hasScript: Boolean(doc.querySelector('script')),
    }
  }, html)
  await page.close()
  return {
    pass: facts.hasTitle && facts.hasListText && facts.hasTable && facts.hasSchool && facts.hasCount && !facts.hasScript,
    facts,
    outputLength: html.length,
  }
}

async function newPage(candidate, value, officePaste = false) {
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } })
  await page.goto(`http://127.0.0.1:${port}/${candidate}`, { waitUntil: 'load' })
  if (candidate === 'jodit') {
    await page.evaluate(({ value, officePaste }) => {
      window.__pocEditor = Jodit.make('#editor', {
        value,
        toolbar: false,
        buttons: [],
        askBeforePasteFromWord: false,
        defaultActionOnPasteFromWord: officePaste ? 'insert_as_html' : null,
        defaultActionOnPaste: officePaste ? 'insert_as_html' : 'insert_clear_html',
      })
    }, { value, officePaste })
  } else {
    await page.evaluate(value => {
      window.__pocEditor = SUNEDITOR.create('#editor', {
        value,
        buttonList: [['bold']],
        height: 'auto',
        attributeWhitelist: { table: 'align|cellpadding|cellspacing' },
        tagStyles: { td: 'width|height' },
      })
    }, value)
  }
  await page.waitForSelector(selector(candidate))
  await page.waitForTimeout(150)
  return page
}

function selector(candidate) {
  return candidate === 'jodit' ? '.jodit-wysiwyg[contenteditable="true"]' : '.se-wrapper-wysiwyg[contenteditable="true"]'
}

async function output(page, candidate) {
  return page.evaluate(candidate => {
    if (candidate === 'jodit') return window.__pocEditor.value
    if (typeof window.__pocEditor.getContents === 'function') return window.__pocEditor.getContents()
    return window.__pocEditor.$.html.get()
  }, candidate)
}

async function safe(fn) {
  try { return await fn() }
  catch (error) { return { pass: false, infrastructureError: String(error?.stack || error) } }
}
