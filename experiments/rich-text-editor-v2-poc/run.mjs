import { createHash } from 'node:crypto'
import { readFile, writeFile } from 'node:fs/promises'
import http from 'node:http'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { chromium } from '@playwright/test'

const here = path.dirname(fileURLToPath(import.meta.url))
const repoRoot = path.resolve(here, '../..')
const MAIN_SHA = '6069e493c5a330ab3a53f55fd31cccc5b14b043d'
const MARKER = 'JILINJOBS_RICH_TEXT_V2_POC_MARKER'

const partyPath = path.join(repoRoot, 'data-migrations/party/v1/articles/zhutijiaoyu-content-154659859759104/article.json')
const pagesPath = path.join(repoRoot, 'sites/jilinjobs/structure/pages.json')

const partyArticle = JSON.parse(await readFile(partyPath, 'utf8'))
const pages = JSON.parse(await readFile(pagesPath, 'utf8'))
const partyHtml = partyArticle?.content?.bodyHtml
const teacherPage = findObject(pages, value => value?.alias === 'teacher-library' && typeof value?.bodyHtml === 'string')
const teacherHtml = teacherPage?.bodyHtml

if (!partyHtml) throw new Error(`Missing Party bodyHtml: ${partyPath}`)
if (!teacherHtml) throw new Error(`Missing teacher-library bodyHtml: ${pagesPath}`)

const versions = {
  playwright: JSON.parse(await readFile(path.join(here, 'node_modules/@playwright/test/package.json'), 'utf8')).version,
  jodit: JSON.parse(await readFile(path.join(here, 'node_modules/jodit/package.json'), 'utf8')).version,
  suneditor: JSON.parse(await readFile(path.join(here, 'node_modules/suneditor/package.json'), 'utf8')).version,
}

const vendorFiles = {
  '/vendor/jodit.js': path.join(here, 'node_modules/jodit/es2021/jodit.min.js'),
  '/vendor/suneditor.js': path.join(here, 'node_modules/suneditor/dist/suneditor.min.js'),
}

const server = http.createServer(async (req, res) => {
  try {
    if (req.url in vendorFiles) {
      const bytes = await readFile(vendorFiles[req.url])
      res.writeHead(200, { 'content-type': 'text/javascript; charset=utf-8' })
      res.end(bytes)
      return
    }
    if (req.url === '/jodit') {
      res.writeHead(200, { 'content-type': 'text/html; charset=utf-8' })
      res.end('<!doctype html><meta charset="utf-8"><textarea id="editor"></textarea><script src="/vendor/jodit.js"></script>')
      return
    }
    if (req.url === '/suneditor') {
      res.writeHead(200, { 'content-type': 'text/html; charset=utf-8' })
      res.end('<!doctype html><meta charset="utf-8"><textarea id="editor"></textarea><script src="/vendor/suneditor.js"></script>')
      return
    }
    res.writeHead(404)
    res.end('not found')
  } catch (error) {
    res.writeHead(500)
    res.end(String(error?.stack || error))
  }
})

await new Promise(resolve => server.listen(0, '127.0.0.1', resolve))
const { port } = server.address()
const browser = await chromium.launch({ headless: true })

const results = {
  purpose: 'Planning evidence only; no production implementation authority',
  baseline: MAIN_SHA,
  versions,
  samples: {
    party: { path: path.relative(repoRoot, partyPath), sha256: sha256(partyHtml) },
    teacherLibrary: { path: path.relative(repoRoot, pagesPath), alias: 'teacher-library', sha256: sha256(teacherHtml) },
  },
  candidates: {},
}

for (const editor of ['jodit', 'suneditor']) {
  results.candidates[editor] = {}
  for (const [sampleName, html] of [['party', partyHtml], ['teacherLibrary', teacherHtml]]) {
    try {
      results.candidates[editor][sampleName] = await runRoundTrip(editor, sampleName, html)
    } catch (error) {
      results.candidates[editor][sampleName] = {
        pass: false,
        infrastructureError: String(error?.stack || error),
      }
    }
  }
}

results.elimination = {
  jodit: Boolean(results.candidates.jodit.party?.pass && results.candidates.jodit.teacherLibrary?.pass),
  suneditor: Boolean(results.candidates.suneditor.party?.pass && results.candidates.suneditor.teacherLibrary?.pass),
}
results.scope = {
  covered: ['P1 Party inline image', 'P2 teacher-library complex Page HTML'],
  pending: ['P3 ordinary authoring', 'P4 real Word/WPS paste', 'P5 CMS Resource adapter', 'P6 real Windows Chinese IME'],
}

await browser.close()
await new Promise(resolve => server.close(resolve))
await writeFile(path.join(here, 'results.json'), `${JSON.stringify(results, null, 2)}\n`)
console.log(JSON.stringify(results, null, 2))

async function runRoundTrip(editorName, sampleName, html) {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1200 } })
  await page.goto(`http://127.0.0.1:${port}/${editorName}`, { waitUntil: 'load' })

  if (editorName === 'jodit') {
    await page.evaluate(value => {
      window.__pocEditor = Jodit.make('#editor', {
        toolbar: false,
        buttons: [],
        minHeight: 220,
      })
      window.__pocEditor.value = value
    }, html)
    await page.waitForSelector('.jodit-wysiwyg[contenteditable="true"]')
    await page.waitForTimeout(100)
  } else {
    await page.evaluate(value => {
      window.__pocEditor = SUNEDITOR.create('#editor', {
        value,
        buttonList: [['bold']],
        height: 'auto',
      })
    }, html)
    await page.waitForSelector('.se-wrapper-wysiwyg[contenteditable="true"]')
    await page.waitForTimeout(150)
  }

  const initialOutput = await getOutput(page, editorName)
  const initialFacts = await extractFacts(page, sampleName, initialOutput)

  if (editorName === 'jodit') {
    await page.evaluate(marker => {
      window.__pocEditor.s.insertHTML(`<p>${marker}</p>`)
      window.__pocEditor.synchronizeValues()
    }, MARKER)
  } else {
    await page.evaluate(marker => {
      window.__pocEditor.$.html.insert(`<p>${marker}</p>`, { selectInserted: false, skipCleaning: false })
      window.__pocEditor.$.history.push(false)
    }, MARKER)
  }
  await page.waitForTimeout(100)

  const editedOutput = await getOutput(page, editorName)
  const editedFacts = await extractFacts(page, sampleName, editedOutput)
  const markerPresent = editedOutput.includes(MARKER)
  const pass = markerPresent && gatePass(sampleName, initialFacts) && gatePass(sampleName, editedFacts)

  await page.close()
  return {
    pass,
    markerPresent,
    inputLength: html.length,
    initialOutputLength: initialOutput.length,
    editedOutputLength: editedOutput.length,
    initialOutputSha256: sha256(initialOutput),
    editedOutputSha256: sha256(editedOutput),
    initialFacts,
    editedFacts,
  }
}

async function getOutput(page, editorName) {
  return page.evaluate(name => {
    if (name === 'jodit') return window.__pocEditor.value
    return window.__pocEditor.$.html.getContents()
  }, editorName)
}

async function extractFacts(page, sampleName, html) {
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
    const dimension = (element, name) => {
      if (!element) return null
      return px(element.getAttribute(name)) ?? px(element.style?.[name])
    }

    if (sampleName === 'party') {
      const img = parsed.querySelector('img')
      const previewImg = preview.querySelector('img')
      const rect = previewImg?.getBoundingClientRect()
      const facts = {
        textContainsKnownHeadline: parsed.body.textContent.includes('中共中央政治局召开会议'),
        imageCount: parsed.querySelectorAll('img').length,
        firstImageWidth: dimension(img, 'width'),
        firstImageHeight: dimension(img, 'height'),
        firstImageStrongAncestor: Boolean(img?.closest('strong')),
        firstImageAnchorAncestor: Boolean(img?.closest('a')),
        renderedWidth: rect ? Math.round(rect.width * 100) / 100 : null,
        renderedHeight: rect ? Math.round(rect.height * 100) / 100 : null,
      }
      preview.remove()
      return facts
    }

    const table = parsed.querySelector('table')
    const firstTd = parsed.querySelector('td')
    const img = parsed.querySelector('img')
    const previewImg = preview.querySelector('img')
    const facts = {
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
    return facts
  }, { sampleName, html })
}

function gatePass(sampleName, facts) {
  if (sampleName === 'party') {
    return facts.textContainsKnownHeadline
      && facts.imageCount > 0
      && facts.firstImageWidth === 15
      && facts.firstImageHeight === 15
      && facts.firstImageStrongAncestor
      && facts.renderedWidth === 15
      && facts.renderedHeight === 15
  }

  return facts.textContainsFangZhanren
    && facts.textContainsLiJunkai
    && facts.tableExists
    && facts.tableWidth === 1170
    && facts.firstTdWidth === 200
    && facts.firstTdHeight === 325
    && facts.firstImageWidth === 200
    && facts.firstImageHeight === 266
    && facts.firstImageFloat === 'left'
    && facts.renderedImageWidth === 200
    && facts.renderedImageHeight === 266
}

function findObject(value, predicate) {
  if (value && typeof value === 'object') {
    if (predicate(value)) return value
    const children = Array.isArray(value) ? value : Object.values(value)
    for (const child of children) {
      const found = findObject(child, predicate)
      if (found) return found
    }
  }
  return null
}

function sha256(value) {
  return createHash('sha256').update(value).digest('hex')
}
