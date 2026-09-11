import { createHash } from 'node:crypto'
import { readFile, writeFile } from 'node:fs/promises'
import http from 'node:http'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { chromium } from '@playwright/test'

const here = path.dirname(fileURLToPath(import.meta.url))
const repoRoot = path.resolve(here, '../..')
const MARKER = 'JILINJOBS_RICH_TEXT_V2_POC_MARKER'
const partyPath = path.join(repoRoot, 'data-migrations/party/v1/articles/zhutijiaoyu-content-154659859759104/article.json')
const pagesPath = path.join(repoRoot, 'sites/jilinjobs/structure/pages.json')
const partyHtml = JSON.parse(await readFile(partyPath, 'utf8'))?.content?.bodyHtml
const pages = JSON.parse(await readFile(pagesPath, 'utf8'))
const teacherHtml = findObject(pages, value => value?.alias === 'teacher-library' && typeof value?.bodyHtml === 'string')?.bodyHtml
if (!partyHtml || !teacherHtml) throw new Error('Missing real corpus input')

const vendor = await readFile(path.join(here, 'node_modules/suneditor/dist/suneditor.min.js'))
const version = JSON.parse(await readFile(path.join(here, 'node_modules/suneditor/package.json'), 'utf8')).version
const server = http.createServer((req, res) => {
  if (req.url === '/vendor.js') {
    res.writeHead(200, { 'content-type': 'text/javascript; charset=utf-8' })
    res.end(vendor)
    return
  }
  res.writeHead(200, { 'content-type': 'text/html; charset=utf-8' })
  res.end('<!doctype html><meta charset="utf-8"><textarea id="editor"></textarea><script src="/vendor.js"></script>')
})
await new Promise(resolve => server.listen(0, '127.0.0.1', resolve))
const { port } = server.address()
const browser = await chromium.launch({ headless: true })
const results = { version, candidates: {} }
for (const [sampleName, html] of [['party', partyHtml], ['teacherLibrary', teacherHtml]]) {
  try {
    results.candidates[sampleName] = await roundTrip(sampleName, html)
  } catch (error) {
    results.candidates[sampleName] = { pass: false, infrastructureError: String(error?.stack || error) }
  }
}
results.elimination = Boolean(results.candidates.party?.pass && results.candidates.teacherLibrary?.pass)
await browser.close()
await new Promise(resolve => server.close(resolve))
await writeFile(path.join(here, 'suneditor-results.json'), `${JSON.stringify(results, null, 2)}\n`)
console.log(JSON.stringify(results, null, 2))

async function roundTrip(sampleName, html) {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1200 } })
  await page.goto(`http://127.0.0.1:${port}/`, { waitUntil: 'load' })
  await page.evaluate(value => {
    window.__pocEditor = SUNEDITOR.create('#editor', { value, buttonList: [['bold']], height: 'auto' })
  }, html)
  await page.waitForSelector('.se-wrapper-wysiwyg[contenteditable="true"]')
  await page.waitForTimeout(150)
  const initialOutput = await getOutput(page)
  const initialFacts = await facts(page, sampleName, initialOutput)
  await page.evaluate(marker => {
    window.__pocEditor.$.html.insert(`<p>${marker}</p>`, { selectInserted: false, skipCleaning: false })
    window.__pocEditor.$.history.push(false)
  }, MARKER)
  await page.waitForTimeout(100)
  const editedOutput = await getOutput(page)
  const editedFacts = await facts(page, sampleName, editedOutput)
  const markerPresent = editedOutput.includes(MARKER)
  await page.close()
  return {
    pass: markerPresent && gatePass(sampleName, initialFacts) && gatePass(sampleName, editedFacts),
    markerPresent,
    inputLength: html.length,
    initialOutputLength: initialOutput.length,
    editedOutputLength: editedOutput.length,
    inputSha256: sha256(html),
    initialOutputSha256: sha256(initialOutput),
    editedOutputSha256: sha256(editedOutput),
    initialFacts,
    editedFacts,
  }
}

async function getOutput(page) {
  return page.evaluate(() => {
    if (typeof window.__pocEditor.getContents === 'function') return window.__pocEditor.getContents()
    return window.__pocEditor.$.html.get()
  })
}

async function facts(page, sampleName, html) {
  return page.evaluate(({ sampleName, html }) => {
    const parsed = new DOMParser().parseFromString(html, 'text/html')
    const preview = document.createElement('div')
    preview.style.cssText = 'position:fixed;left:-20000px;top:0;visibility:hidden;width:1600px;'
    preview.innerHTML = html
    document.body.appendChild(preview)
    const px = value => {
      if (value == null || value === '') return null
      const match = String(value).match(/-?\d+(?:\.\d+)?/)
      return match ? Number(match[0]) : null
    }
    const dimension = (element, name) => element ? (px(element.getAttribute(name)) ?? px(element.style?.[name])) : null
    if (sampleName === 'party') {
      const img = parsed.querySelector('img')
      const previewImg = preview.querySelector('img')
      const rect = previewImg?.getBoundingClientRect()
      const result = {
        textContainsKnownHeadline: parsed.body.textContent.includes('中共中央政治局召开会议'),
        imageCount: parsed.querySelectorAll('img').length,
        firstImageWidth: dimension(img, 'width'),
        firstImageHeight: dimension(img, 'height'),
        firstImageStrongAncestor: Boolean(img?.closest('strong')),
        renderedWidth: rect ? Math.round(rect.width * 100) / 100 : null,
        renderedHeight: rect ? Math.round(rect.height * 100) / 100 : null,
      }
      preview.remove()
      return result
    }
    const table = parsed.querySelector('table')
    const firstTd = parsed.querySelector('td')
    const img = parsed.querySelector('img')
    const previewImg = preview.querySelector('img')
    const result = {
      textContainsFangZhanren: parsed.body.textContent.includes('方占仁'),
      textContainsLiJunkai: parsed.body.textContent.includes('李军凯'),
      tableExists: Boolean(table),
      tableAlign: table?.getAttribute('align') || null,
      tableCellpadding: table?.getAttribute('cellpadding') || null,
      tableCellspacing: table?.getAttribute('cellspacing') || null,
      tableWidth: dimension(table, 'width'),
      firstTdWidth: dimension(firstTd, 'width'),
      firstTdHeight: dimension(firstTd, 'height'),
      firstImageWidth: dimension(img, 'width'),
      firstImageHeight: dimension(img, 'height'),
      firstImageFloat: img?.style?.cssFloat || null,
      renderedImageWidth: previewImg ? Math.round(previewImg.getBoundingClientRect().width * 100) / 100 : null,
      renderedImageHeight: previewImg ? Math.round(previewImg.getBoundingClientRect().height * 100) / 100 : null,
    }
    preview.remove()
    return result
  }, { sampleName, html })
}

function gatePass(sampleName, f) {
  if (sampleName === 'party') return f.textContainsKnownHeadline && f.imageCount > 0 && f.firstImageWidth === 15 && f.firstImageHeight === 15 && f.firstImageStrongAncestor && f.renderedWidth === 15 && f.renderedHeight === 15
  return f.textContainsFangZhanren && f.textContainsLiJunkai && f.tableExists && f.tableWidth === 1170 && f.firstTdWidth === 200 && f.firstTdHeight === 325 && f.firstImageWidth === 200 && f.firstImageHeight === 266 && f.firstImageFloat === 'left' && f.renderedImageWidth === 200 && f.renderedImageHeight === 266
}

function findObject(value, predicate) {
  if (value && typeof value === 'object') {
    if (predicate(value)) return value
    for (const child of (Array.isArray(value) ? value : Object.values(value))) {
      const found = findObject(child, predicate)
      if (found) return found
    }
  }
  return null
}
const sha256 = value => createHash('sha256').update(value).digest('hex')
