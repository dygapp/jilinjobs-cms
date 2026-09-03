import { expect, test, type Browser, type Page, type TestInfo } from '@playwright/test'
import { createHash } from 'node:crypto'
import { mkdir, writeFile } from 'node:fs/promises'
import path from 'node:path'

const enabled = process.env.PARTY_REFERENCE_EVIDENCE === 'true'
const origin = 'https://24365.jl.smartedu.cn'
const homeUrl = `${origin}/dyzj`
const listUrls = {
  gcsy: `${origin}/plist.html?typeCode=gcsy`,
  gzdt: `${origin}/plist.html?typeCode=gzdt`,
  dgdz: `${origin}/plist.html?typeCode=dgdz`,
  llxx: `${origin}/plist.html?typeCode=llxx`,
} as const
const internalDetailUrl = `${origin}/pdetail.html?content_id=278556458369024`

interface ElementSnapshot {
  tag: string
  className: string
  text: string
  rect: { x: number; y: number; width: number; height: number }
  style: Record<string, string>
}

async function capturePage(page: Page, url: string, name: string, testInfo: TestInfo) {
  await page.goto(url, { waitUntil: 'networkidle', timeout: 45_000 })
  await page.screenshot({ path: testInfo.outputPath(`${name}.png`), fullPage: true })
  await writeFile(testInfo.outputPath(`${name}.html`), await page.content(), 'utf8')

  const snapshot = await page.evaluate(() => {
    const normalize = (value: string | null | undefined) => (value ?? '').replace(/\s+/g, ' ').trim()
    const describe = (element: Element | null): ElementSnapshot | null => {
      if (!(element instanceof HTMLElement)) return null
      const rect = element.getBoundingClientRect()
      const style = getComputedStyle(element)
      return {
        tag: element.tagName.toLowerCase(),
        className: element.className,
        text: normalize(element.textContent).slice(0, 240),
        rect: { x: rect.x, y: rect.y, width: rect.width, height: rect.height },
        style: {
          display: style.display,
          position: style.position,
          width: style.width,
          maxWidth: style.maxWidth,
          height: style.height,
          margin: style.margin,
          padding: style.padding,
          color: style.color,
          backgroundColor: style.backgroundColor,
          backgroundImage: style.backgroundImage,
          border: style.border,
          borderRadius: style.borderRadius,
          boxShadow: style.boxShadow,
          fontFamily: style.fontFamily,
          fontSize: style.fontSize,
          fontWeight: style.fontWeight,
          lineHeight: style.lineHeight,
          textAlign: style.textAlign,
        },
      }
    }
    const exactText = (text: string) => {
      const candidates = Array.from(document.querySelectorAll('body *'))
        .filter(element => normalize(element.textContent) === text)
      return candidates.sort((a, b) => a.children.length - b.children.length)[0] ?? null
    }

    return {
      url: location.href,
      title: document.title,
      viewport: { width: innerWidth, height: innerHeight, devicePixelRatio },
      documentSize: {
        width: document.documentElement.scrollWidth,
        height: document.documentElement.scrollHeight,
      },
      body: describe(document.body),
      landmarks: {
        work: describe(exactText('工作动态')),
        garden: describe(exactText('学习园地')),
        rules: describe(exactText('党规党章')),
        study: describe(exactText('理论学习')),
      },
      links: Array.from(document.querySelectorAll<HTMLAnchorElement>('a[href]')).map(anchor => ({
        text: normalize(anchor.textContent),
        href: anchor.href,
        target: anchor.target,
      })),
      stylesheets: Array.from(document.querySelectorAll<HTMLLinkElement>('link[rel="stylesheet"][href]')).map(link => link.href),
      images: Array.from(document.images).map(image => ({
        src: image.currentSrc || image.src,
        alt: image.alt,
        naturalWidth: image.naturalWidth,
        naturalHeight: image.naturalHeight,
        renderedWidth: image.getBoundingClientRect().width,
        renderedHeight: image.getBoundingClientRect().height,
      })),
      resources: performance.getEntriesByType('resource').map(entry => entry.name),
    }
  })

  await writeFile(testInfo.outputPath(`${name}.json`), JSON.stringify(snapshot, null, 2), 'utf8')
  return snapshot
}

async function captureViewport(browser: Browser, viewport: { width: number; height: number }, suffix: string, testInfo: TestInfo) {
  const context = await browser.newContext({ viewport })
  const page = await context.newPage()
  try {
    return await capturePage(page, homeUrl, `party-home-${suffix}`, testInfo)
  } finally {
    await context.close()
  }
}

async function downloadSameOriginResources(page: Page, testInfo: TestInfo) {
  const resources = await page.evaluate(() => Array.from(new Set([
    ...Array.from(document.querySelectorAll<HTMLLinkElement>('link[rel="stylesheet"][href]')).map(link => link.href),
    ...Array.from(document.images).map(image => image.currentSrc || image.src),
    ...performance.getEntriesByType('resource').map(entry => entry.name),
  ])).filter(Boolean))

  const outputDir = testInfo.outputPath('original-resources')
  await mkdir(outputDir, { recursive: true })
  const manifest: Array<Record<string, unknown>> = []
  let savedImages = 0

  for (const resourceUrl of resources) {
    let parsed: URL
    try {
      parsed = new URL(resourceUrl)
    } catch {
      continue
    }
    if (parsed.origin !== origin) continue

    const response = await page.request.get(resourceUrl, { timeout: 20_000 }).catch(() => null)
    if (!response || !response.ok()) {
      manifest.push({ url: resourceUrl, status: response?.status() ?? null, saved: false })
      continue
    }

    const contentType = response.headers()['content-type'] ?? ''
    const body = await response.body()
    const isCss = contentType.includes('text/css') || parsed.pathname.endsWith('.css')
    const isImage = contentType.startsWith('image/')
    const shouldSave = isCss || (isImage && savedImages < 24 && body.length <= 3 * 1024 * 1024)
    const hash = createHash('sha256').update(body).digest('hex')

    let filename: string | null = null
    if (shouldSave) {
      if (isImage) savedImages += 1
      const basename = path.basename(parsed.pathname) || 'resource'
      filename = `${String(manifest.length + 1).padStart(2, '0')}-${basename.replace(/[^a-zA-Z0-9._-]/g, '_')}`
      await writeFile(path.join(outputDir, filename), body)
    }
    manifest.push({
      url: resourceUrl,
      status: response.status(),
      contentType,
      sizeBytes: body.length,
      sha256: hash,
      saved: shouldSave,
      filename,
    })
  }

  await writeFile(path.join(outputDir, 'manifest.json'), JSON.stringify(manifest, null, 2), 'utf8')
  return manifest
}

test('采集中心党建原站 Desktop / Mobile / DOM / CSS / 静态资源证据', async ({ browser }, testInfo) => {
  test.skip(!enabled, '仅中心党建收敛分支显式启用原站 Reference Evidence 采集')
  test.setTimeout(180_000)

  const desktopContext = await browser.newContext({ viewport: { width: 1440, height: 1000 } })
  const desktopPage = await desktopContext.newPage()
  try {
    const home = await capturePage(desktopPage, homeUrl, 'party-home-desktop', testInfo)
    expect(home.title).toContain('党员之家')

    const expectedTypeCodes = Object.keys(listUrls)
    const listLinks = home.links.filter(link => link.href.includes('/plist.html?typeCode='))
    for (const typeCode of expectedTypeCodes) {
      expect(listLinks.some(link => new URL(link.href).searchParams.get('typeCode') === typeCode), `原站首页缺少 ${typeCode} 栏目入口`).toBeTruthy()
    }

    const externalLinks = home.links.filter(link => {
      try { return new URL(link.href).origin !== origin && /^https?:/.test(link.href) } catch { return false }
    })
    expect(externalLinks.length).toBeGreaterThan(0)

    await downloadSameOriginResources(desktopPage, testInfo)

    for (const [typeCode, url] of Object.entries(listUrls)) {
      const listSnapshot = await capturePage(desktopPage, url, `party-list-${typeCode}-desktop`, testInfo)
      expect(listSnapshot.url).toContain(`typeCode=${typeCode}`)
    }

    const detail = await capturePage(desktopPage, internalDetailUrl, 'party-detail-internal-desktop', testInfo)
    expect(detail.url).toContain('/pdetail.html?content_id=')
    expect(detail.links.some(link => link.href.includes('/plist.html?typeCode=gzdt')) || detail.landmarks.work !== null).toBeTruthy()
  } finally {
    await desktopContext.close()
  }

  const mobile = await captureViewport(browser, { width: 390, height: 844 }, 'mobile', testInfo)
  expect(mobile.viewport.width).toBe(390)
})
