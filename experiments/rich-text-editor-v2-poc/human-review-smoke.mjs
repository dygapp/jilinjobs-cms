import { spawn } from 'node:child_process'
import vm from 'node:vm'
import { chromium } from '@playwright/test'

const port = 4174
const url = `http://127.0.0.1:${port}/`
const child = spawn(process.execPath, ['human-review.mjs'], {
  cwd: new URL('.', import.meta.url),
  env: { ...process.env, HUMAN_REVIEW_PORT: String(port) },
  stdio: ['ignore', 'pipe', 'pipe'],
})

let output = ''
child.stdout.on('data', chunk => { output += chunk.toString() })
child.stderr.on('data', chunk => { output += chunk.toString() })

try {
  await waitForServer(url)
  const html = await (await fetch(url)).text()
  const inlineScripts = [...html.matchAll(/<script(?:\s[^>]*)?>([\s\S]*?)<\/script>/gi)]
    .map(match => match[1])
    .filter(Boolean)
  const reviewScript = inlineScripts.at(-1) || ''
  try {
    new vm.Script(reviewScript, { filename: 'human-review-inline.js' })
  } catch (error) {
    throw new Error(`Human Review inline script is invalid before browser startup:\n${error?.stack || error}\n--- script ---\n${reviewScript}`)
  }

  const browser = await chromium.launch({ headless: true })
  try {
    const page = await browser.newPage({ viewport: { width: 1500, height: 1000 } })
    const diagnostics = { consoleErrors: [], pageErrors: [], failedRequests: [] }
    page.on('console', message => {
      if (message.type() === 'error') diagnostics.consoleErrors.push(message.text())
    })
    page.on('pageerror', error => diagnostics.pageErrors.push(String(error?.stack || error)))
    page.on('requestfailed', request => diagnostics.failedRequests.push(`${request.url()} :: ${request.failure()?.errorText || 'unknown'}`))

    await page.goto(url, { waitUntil: 'load' })
    try {
      await page.waitForSelector('.jodit-wysiwyg[contenteditable="true"]', { timeout: 5000 })
      await page.waitForSelector('.se-wrapper-wysiwyg[contenteditable="true"]', { timeout: 5000 })
    } catch (error) {
      const startupFacts = await page.evaluate(() => ({
        readyState: document.readyState,
        title: document.title,
        hasJoditGlobal: typeof window.Jodit !== 'undefined',
        hasSunEditorGlobal: typeof window.SUNEDITOR !== 'undefined',
        hasSunChineseLanguage: Boolean(window.SUNEDITOR_LANG?.zh_cn),
        hasHumanReview: Boolean(window.__humanReview),
        joditEditableCount: document.querySelectorAll('.jodit-wysiwyg').length,
        sunEditableCount: document.querySelectorAll('.se-wrapper-wysiwyg').length,
        scriptSources: Array.from(document.scripts).map(script => script.src || '[inline]'),
      }))
      throw new Error(`Human Review editors did not initialize: ${error}\nDiagnostics=${JSON.stringify({ startupFacts, ...diagnostics }, null, 2)}`)
    }

    const facts = await page.evaluate(() => ({
      title: document.title,
      language: document.documentElement.lang,
      sunChineseLanguageLoaded: Boolean(window.SUNEDITOR_LANG?.zh_cn),
      joditLanguage: window.__humanReview?.jodit?.o?.language || null,
      joditValue: window.__humanReview?.jodit?.value || '',
      sunValue: window.__humanReview?.sunValue?.() || '',
      reviewSelectCount: document.querySelectorAll('[data-result]').length,
    }))

    if (facts.title !== 'Rich Text Editor V2 Human Review') throw new Error(`Unexpected title: ${facts.title}`)
    if (facts.language !== 'zh-CN') throw new Error(`Unexpected page language: ${facts.language}`)
    if (!facts.sunChineseLanguageLoaded) throw new Error('SunEditor zh_cn language was not loaded')
    if (facts.joditLanguage !== 'zh_cn') throw new Error(`Jodit zh_cn language was not configured: ${facts.joditLanguage}`)
    if (!facts.joditValue.includes('吉林省高校毕业生就业服务')) throw new Error('Jodit sample content missing')
    if (!facts.sunValue.includes('吉林省高校毕业生就业服务')) throw new Error('SunEditor sample content missing')
    if (facts.reviewSelectCount !== 8) throw new Error(`Expected 8 result selects, got ${facts.reviewSelectCount}`)

    await page.evaluate(() => {
      window.__humanReview.jodit.s.insertHTML('<p>SMOKE中文输入</p>')
      window.__humanReview.jodit.synchronizeValues()
      window.__humanReview.suneditor.$.html.insert('<p>SMOKE中文输入</p>', { selectInserted: false, skipCleaning: false })
    })
    await page.waitForTimeout(80)

    const after = await page.evaluate(() => ({
      jodit: window.__humanReview.jodit.value,
      suneditor: window.__humanReview.sunValue(),
    }))
    if (!after.jodit.includes('SMOKE中文输入')) throw new Error('Jodit smoke insertion missing')
    if (!after.suneditor.includes('SMOKE中文输入')) throw new Error('SunEditor smoke insertion missing')

    console.log(JSON.stringify({ pass: true, facts, diagnostics }, null, 2))
  } finally {
    await browser.close()
  }
} finally {
  child.kill('SIGTERM')
  await new Promise(resolve => {
    const timer = setTimeout(resolve, 1000)
    child.once('exit', () => {
      clearTimeout(timer)
      resolve()
    })
  })
}

async function waitForServer(target) {
  const deadline = Date.now() + 10_000
  let lastError
  while (Date.now() < deadline) {
    try {
      const response = await fetch(target)
      if (response.ok) return
      lastError = new Error(`HTTP ${response.status}`)
    } catch (error) {
      lastError = error
    }
    await new Promise(resolve => setTimeout(resolve, 100))
  }
  throw new Error(`Human Review server did not become ready: ${lastError}\n${output}`)
}
