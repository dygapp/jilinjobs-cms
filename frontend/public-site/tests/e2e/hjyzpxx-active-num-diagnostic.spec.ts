import { test } from '@playwright/test'

const activeNums = [22, 23, 24, 25, 30]
const legacyBase = 'https://24365.jl.smartedu.cn/hjyzpxx.html'

type Evidence = Record<string, unknown>

function snippets(text: string, terms: string[]) {
  const rows: Array<{ term: string; snippet: string }> = []
  for (const term of terms) {
    let cursor = 0
    while (rows.filter(row => row.term === term).length < 8) {
      const index = text.indexOf(term, cursor)
      if (index < 0) break
      rows.push({
        term,
        snippet: text.slice(Math.max(0, index - 240), Math.min(text.length, index + term.length + 420)),
      })
      cursor = index + term.length
    }
  }
  return rows
}

test('diagnose legacy hjyzpxx activeNum protocol', async ({ browser }, testInfo) => {
  const all: Evidence[] = []
  const terms = ['activeNum', 'postMessage', 'contentWindow', 'addEventListener', 'message']

  for (const activeNum of activeNums) {
    const context = await browser.newContext()
    await context.addInitScript(() => {
      const target = window as Window & { __hjyzMessages?: unknown[] }
      target.__hjyzMessages = []
      window.addEventListener('message', event => {
        let data: unknown
        try {
          data = JSON.parse(JSON.stringify(event.data))
        } catch {
          data = String(event.data)
        }
        target.__hjyzMessages?.push({
          at: Date.now(),
          href: location.href,
          origin: event.origin,
          data,
        })
      })
    })

    const page = await context.newPage()
    const scriptEvidence: Array<Record<string, unknown>> = []
    const responsePromises: Promise<void>[] = []

    page.on('response', response => {
      if (response.request().resourceType() !== 'script') return
      responsePromises.push((async () => {
        try {
          const text = await response.text()
          const hits = snippets(text, terms)
          if (hits.length > 0) {
            scriptEvidence.push({
              url: response.url(),
              status: response.status(),
              hits,
            })
          }
        } catch (error) {
          scriptEvidence.push({
            url: response.url(),
            readError: String(error),
          })
        }
      })())
    })

    const requestedUrl = `${legacyBase}?activeNum=${activeNum}`
    const response = await page.goto(requestedUrl, { waitUntil: 'domcontentloaded', timeout: 60_000 })
    await page.waitForTimeout(8_000)
    await Promise.allSettled(responsePromises)

    const outer = await page.evaluate(() => {
      const scripts = Array.from(document.scripts).map((script, index) => ({
        index,
        src: script.src || null,
        inline: script.src ? null : (script.textContent || '').slice(0, 6000),
      }))
      return {
        href: location.href,
        title: document.title,
        scripts,
        iframes: Array.from(document.querySelectorAll('iframe')).map((frame, index) => ({
          index,
          src: frame.getAttribute('src'),
          id: frame.id || null,
          name: frame.getAttribute('name'),
          className: frame.className || null,
        })),
        activeLike: Array.from(document.querySelectorAll(
          '[class*="active"],[class*="selected"],[aria-selected="true"],.active'
        )).slice(0, 40).map(element => ({
          tag: element.tagName,
          id: element.id || null,
          className: element.getAttribute('class'),
          text: (element.textContent || '').replace(/\\s+/g, ' ').trim().slice(0, 300),
        })),
        messages: (window as Window & { __hjyzMessages?: unknown[] }).__hjyzMessages || [],
      }
    })

    const inlineHits = (outer.scripts as Array<{ index: number; src: string | null; inline: string | null }>)
      .filter(script => script.inline)
      .map(script => ({
        index: script.index,
        hits: snippets(script.inline || '', terms),
      }))
      .filter(script => script.hits.length > 0)

    const frameEvidence = []
    for (const frame of page.frames()) {
      let state: Record<string, unknown>
      try {
        state = await frame.evaluate(() => ({
          href: location.href,
          title: document.title,
          readyState: document.readyState,
          activeLike: Array.from(document.querySelectorAll(
            '[class*="active"],[class*="selected"],[aria-selected="true"],.ant-tabs-tab-active,.ant-menu-item-selected'
          )).slice(0, 80).map(element => ({
            tag: element.tagName,
            id: element.id || null,
            className: element.getAttribute('class'),
            ariaSelected: element.getAttribute('aria-selected'),
            text: (element.textContent || '').replace(/\\s+/g, ' ').trim().slice(0, 400),
          })),
          messages: (window as Window & { __hjyzMessages?: unknown[] }).__hjyzMessages || [],
          bodyTextSample: (document.body?.innerText || '').replace(/\\s+/g, ' ').trim().slice(0, 3000),
        }))
      } catch (error) {
        state = { href: frame.url(), evaluateError: String(error) }
      }
      frameEvidence.push(state)
    }

    all.push({
      activeNum,
      requestedUrl,
      status: response?.status() ?? null,
      outer,
      inlineHits,
      scriptEvidence,
      frames: frameEvidence,
    })

    await context.close()
  }

  const body = Buffer.from(JSON.stringify(all, null, 2))
  await testInfo.attach('hjyzpxx-active-num-evidence.json', {
    body,
    contentType: 'application/json',
  })

  console.log('HJYZPXX_ACTIVE_NUM_EVIDENCE_START')
  console.log(JSON.stringify(all, null, 2))
  console.log('HJYZPXX_ACTIVE_NUM_EVIDENCE_END')

  throw new Error('Intentional diagnostic stop after evidence capture')
})
