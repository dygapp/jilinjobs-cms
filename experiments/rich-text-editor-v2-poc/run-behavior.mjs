import { readFile, writeFile } from 'node:fs/promises'
import http from 'node:http'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { chromium } from '@playwright/test'

const here = path.dirname(fileURLToPath(import.meta.url))
const versions = {
  jodit: JSON.parse(await readFile(path.join(here, 'node_modules/jodit/package.json'), 'utf8')).version,
  suneditor: JSON.parse(await readFile(path.join(here, 'node_modules/suneditor/package.json'), 'utf8')).version,
}
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
  const src = candidate === 'suneditor' ? '/suneditor.js' : '/jodit.js'
  res.writeHead(200, { 'content-type': 'text/html; charset=utf-8' })
  res.end(`<!doctype html><meta charset="utf-8"><textarea id="editor"></textarea><script src="${src}"></script>`)
})
await new Promise(resolve => server.listen(0, '127.0.0.1', resolve))
const { port } = server.address()
const browser = await chromium.launch({ headless: true })
const results = { versions, gates: {} }
for (const candidate of ['jodit', 'suneditor']) {
  results.gates[candidate] = {
    p3OrdinaryAuthoring: await safe(() => p3(candidate)),
    p4aOfficeClipboardStructure: await safe(() => p4a(candidate)),
    p5CmsResourceAdapter: await safe(() => p5(candidate)),
  }
}
results.summary = Object.fromEntries(Object.entries(results.gates).map(([candidate, gates]) => [
  candidate,
  Object.values(gates).every(value => value?.pass === true),
]))
results.scope = {
  p4a: 'Automated Office-shaped clipboard structural precheck; real Windows Word/WPS paste remains pending.',
  p6: 'Real Windows Chinese IME remains pending and is not simulated by this workflow.',
}
await browser.close()
await new Promise(resolve => server.close(resolve))
await writeFile(path.join(here, 'behavior-results.json'), `${JSON.stringify(results, null, 2)}\n`)
console.log(JSON.stringify(results, null, 2))

async function p3(candidate) {
  const page = await newPage(candidate, '<p>已有正文</p>')
  const selector = candidate === 'jodit' ? '.jodit-wysiwyg[contenteditable="true"]' : '.se-wrapper-wysiwyg[contenteditable="true"]'
  const editable = page.locator(selector)
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
  await page.waitForTimeout(80)
  const typed = await output(page, candidate)
  await page.keyboard.press('Control+z')
  await page.waitForTimeout(80)
  const undone = await output(page, candidate)
  await page.keyboard.press('Control+Shift+z')
  await page.waitForTimeout(80)
  let redone = await output(page, candidate)
  if (!redone.includes('吉林省就业服务')) {
    await page.keyboard.press('Control+y')
    await page.waitForTimeout(80)
    redone = await output(page, candidate)
  }
  await page.close()
  return {
    pass: typed.includes('吉林省就业服务') && !undone.includes('吉林省就业服务') && redone.includes('吉林省就业服务'),
    typed: typed.includes('吉林省就业服务'),
    undone: !undone.includes('吉林省就业服务'),
    redone: redone.includes('吉林省就业服务'),
  }
}

const OFFICE_HTML = `
<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word">
<head><meta name=ProgId content=Word.Document><style>
p.MsoNormal{margin:0cm;font-family:"宋体";font-size:12.0pt;mso-style-name:正文;}
p.MsoListParagraph{margin-left:21.0pt;mso-list:l0 level1 lfo1;font-family:"宋体";}
table.MsoTableGrid{border-collapse:collapse;mso-table-layout-alt:fixed;}
</style></head>
<body><!--StartFragment-->
<p class=MsoNormal style='color:#1F4E79;mso-bidi-font-size:12.0pt'>吉林省高校毕业生就业服务</p>
<p class=MsoListParagraph style='mso-list:l0 level1 lfo1'>1. 第一项：就业手续办理</p>
<table class=MsoTableGrid border=1><tr><td><p class=MsoNormal>学校</p></td><td><p class=MsoNormal>人数</p></td></tr><tr><td><p class=MsoNormal>吉林大学</p></td><td><p class=MsoNormal>100</p></td></tr></table>
<!--EndFragment--></body></html>`

async function p4a(candidate) {
  const page = await newPage(candidate, '<p>粘贴位置：</p>', true)
  const selector = candidate === 'jodit' ? '.jodit-wysiwyg[contenteditable="true"]' : '.se-wrapper-wysiwyg[contenteditable="true"]'
  const editable = page.locator(selector)
  await editable.click()
  await editable.evaluate((node, html) => {
    const data = new DataTransfer()
    data.setData('text/html', html)
    data.setData('text/plain', '吉林省高校毕业生就业服务\n1. 第一项：就业手续办理\n学校 人数\n吉林大学 100')
    node.dispatchEvent(new ClipboardEvent('paste', { clipboardData: data, bubbles: true, cancelable: true }))
  }, OFFICE_HTML)
  await page.waitForTimeout(200)
  const html = await output(page, candidate)
  const facts = await page.evaluate(value => {
    const doc = new DOMParser().parseFromString(value, 'text/html')
    return {
      hasTitle: doc.body.textContent.includes('吉林省高校毕业生就业服务'),
      hasListText: doc.body.textContent.includes('第一项：就业手续办理'),
      hasTable: Boolean(doc.querySelector('table')),
      hasSchool: doc.body.textContent.includes('吉林大学'),
      hasCount: doc.body.textContent.includes('100'),
      hasScript: Boolean(doc.querySelector('script')),
    }
  }, html)
  await page.close()
  return { pass: facts.hasTitle && facts.hasListText && facts.hasTable && facts.hasSchool && facts.hasCount && !facts.hasScript, facts, outputLength: html.length }
}

async function p5(candidate) {
  const page = await newPage(candidate, '<p>资源适配：</p>')
  const managedImage = { id: 9001, url: '/api/admin/resources/9001/content', alt: 'managed-image.png' }
  const attachment = { id: 9002, url: '/api/admin/resources/9002/content', name: '就业材料.pdf' }
  if (candidate === 'jodit') {
    await page.evaluate(({ managedImage, attachment }) => {
      const adapter = {
        selectImage: async () => managedImage,
        selectAttachment: async () => attachment,
      }
      return Promise.all([adapter.selectImage(), adapter.selectAttachment()]).then(([image, file]) => {
        window.__pocEditor.s.insertHTML(`<p><img src="${image.url}" alt="${image.alt}" width="120" height="80"></p><p><a href="${file.url}">${file.name}</a></p>`)
        window.__pocEditor.synchronizeValues()
      })
    }, { managedImage, attachment })
  } else {
    await page.evaluate(({ managedImage, attachment }) => {
      const adapter = {
        selectImage: async () => managedImage,
        selectAttachment: async () => attachment,
      }
      return Promise.all([adapter.selectImage(), adapter.selectAttachment()]).then(([image, file]) => {
        window.__pocEditor.$.html.insert(`<p><img src="${image.url}" alt="${image.alt}" width="120" height="80"></p><p><a href="${file.url}">${file.name}</a></p>`, { selectInserted: false, skipCleaning: false })
      })
    }, { managedImage, attachment })
  }
  await page.waitForTimeout(100)
  const html = await output(page, candidate)
  const facts = await page.evaluate(value => {
    const doc = new DOMParser().parseFromString(value, 'text/html')
    const img = doc.querySelector('img[src="/api/admin/resources/9001/content"]')
    const link = doc.querySelector('a[href="/api/admin/resources/9002/content"]')
    return {
      imagePresent: Boolean(img),
      imageAlt: img?.getAttribute('alt') || null,
      imageWidth: img?.getAttribute('width') || img?.style.width || null,
      imageHeight: img?.getAttribute('height') || img?.style.height || null,
      attachmentPresent: Boolean(link),
      attachmentText: link?.textContent || null,
    }
  }, html)
  await page.close()
  return {
    pass: facts.imagePresent && facts.imageAlt === 'managed-image.png' && facts.attachmentPresent && facts.attachmentText === '就业材料.pdf',
    facts,
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
    await page.waitForSelector('.jodit-wysiwyg[contenteditable="true"]')
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
    await page.waitForSelector('.se-wrapper-wysiwyg[contenteditable="true"]')
  }
  await page.waitForTimeout(120)
  return page
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
