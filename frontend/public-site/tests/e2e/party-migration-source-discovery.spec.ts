import { expect, test, type Page, type Response, type TestInfo } from '@playwright/test'
import { createHash } from 'node:crypto'
import { writeFile } from 'node:fs/promises'

const enabled = process.env.PARTY_MIGRATION_DISCOVERY === 'true'
const origin = 'https://24365.jl.smartedu.cn'
const knownInternal = {
  contentId: '278556458369024',
  typeCode: 'gzdt',
  url: `${origin}/pdetail.html?content_id=278556458369024&typeCode=gzdt`,
}
const listUrls = {
  gcsy: `${origin}/plist.html?typeCode=gcsy`,
  gzdt: `${origin}/plist.html?typeCode=gzdt`,
  dgdz: `${origin}/plist.html?typeCode=dgdz`,
  llxx: `${origin}/plist.html?typeCode=llxx`,
} as const

type NetworkRecord = {
  url: string
  resourceType: string
  status: number
  contentType: string
  bodySha256?: string
  bodyExcerpt?: string
}

async function inspect(page: Page, url: string, name: string, testInfo: TestInfo) {
  const candidates: Response[] = []
  page.on('response', response => {
    const resourceType = response.request().resourceType()
    if (resourceType === 'xhr' || resourceType === 'fetch') candidates.push(response)
  })

  const documentResponse = await page.goto(url, { waitUntil: 'networkidle', timeout: 45_000 })
  if (!documentResponse) throw new Error(`未取得 Document Response: ${url}`)
  const initialHtml = await documentResponse.text()
  const finalHtml = await page.content()

  const dom = await page.evaluate(() => {
    const normalize = (value: string | null | undefined) => (value ?? '').replace(/\s+/g, ' ').trim()
    return {
      title: document.title,
      articleTitle: normalize(document.querySelector('.detail-content-title')?.textContent),
      articleBodyText: normalize(document.querySelector('.rich-text-wrap')?.textContent).slice(0, 20_000),
      bodyText: normalize(document.body.textContent).slice(0, 20_000),
      anchors: Array.from(document.querySelectorAll<HTMLAnchorElement>('a[href]')).map(anchor => ({
        text: normalize(anchor.textContent),
        href: anchor.href,
        target: anchor.target,
        parentTag: anchor.parentElement?.tagName.toLowerCase() ?? '',
        parentClass: anchor.parentElement?.className ?? '',
        grandParentTag: anchor.parentElement?.parentElement?.tagName.toLowerCase() ?? '',
        grandParentClass: anchor.parentElement?.parentElement?.className ?? '',
      })),
      scripts: Array.from(document.querySelectorAll<HTMLScriptElement>('script')).map(script => ({
        src: script.src,
        inlineExcerpt: script.src ? '' : normalize(script.textContent).slice(0, 500),
      })),
      images: Array.from(document.images).map(image => ({
        src: image.currentSrc || image.src,
        alt: image.alt,
        naturalWidth: image.naturalWidth,
        naturalHeight: image.naturalHeight,
      })),
    }
  })

  const network: NetworkRecord[] = []
  for (const response of candidates) {
    const contentType = response.headers()['content-type'] ?? ''
    const record: NetworkRecord = {
      url: response.url(),
      resourceType: response.request().resourceType(),
      status: response.status(),
      contentType,
    }
    if (/json|text|javascript/.test(contentType)) {
      const body = await response.text().catch(() => '')
      if (body) {
        record.bodySha256 = createHash('sha256').update(body).digest('hex')
        record.bodyExcerpt = body.slice(0, 4000)
      }
    }
    network.push(record)
  }

  await writeFile(testInfo.outputPath(`${name}-initial.html`), initialHtml, 'utf8')
  await writeFile(testInfo.outputPath(`${name}-final.html`), finalHtml, 'utf8')
  await writeFile(testInfo.outputPath(`${name}-discovery.json`), JSON.stringify({ url, dom, network }, null, 2), 'utf8')
  return { initialHtml, finalHtml, dom, network }
}

test('EU-29 Source Discovery：比较初始 HTML、最终 DOM 与 XHR/fetch', async ({ page }, testInfo) => {
  test.skip(!enabled, '仅 EU-29 source-discovery 分支显式启用实时原站侦察')
  test.setTimeout(180_000)

  const summary: Record<string, unknown> = { lists: {}, detail: {} }
  for (const [typeCode, url] of Object.entries(listUrls)) {
    const result = await inspect(page, url, `party-list-${typeCode}`, testInfo)
    const contentLinks = result.dom.anchors.filter(link => link.grandParentClass.includes('default-list') || link.parentClass.includes('list-item'))
    summary.lists[typeCode] = {
      initialHtmlHasTypeCode: result.initialHtml.includes(`typeCode=${typeCode}`),
      contentLinks: contentLinks.length,
      xhrOrFetch: result.network.length,
      samples: contentLinks.slice(0, 5),
    }
    expect(result.initialHtml).toContain(`typeCode=${typeCode}`)
  }

  const detail = await inspect(page, knownInternal.url, 'party-detail-known-internal', testInfo)
  const structuredCandidates = detail.network.filter(record => {
    const excerpt = record.bodyExcerpt ?? ''
    return excerpt.includes(knownInternal.contentId) || (detail.dom.articleTitle.length >= 4 && excerpt.includes(detail.dom.articleTitle))
  })
  const initialContainsArticle = detail.dom.articleTitle.length >= 4
    && detail.initialHtml.includes(detail.dom.articleTitle)
    && detail.initialHtml.includes('class="rich-text-wrap"')

  summary.detail = {
    url: knownInternal.url,
    contentId: knownInternal.contentId,
    articleTitle: detail.dom.articleTitle,
    initialHtmlContainsRenderedArticle: initialContainsArticle,
    xhrOrFetchCount: detail.network.length,
    structuredContentCandidates: structuredCandidates,
  }
  await writeFile(testInfo.outputPath('source-discovery-summary.json'), JSON.stringify(summary, null, 2), 'utf8')

  console.log(`EU29_SOURCE_DISCOVERY ${JSON.stringify(summary)}`)
  expect(detail.dom.title.length).toBeGreaterThan(0)
  expect(detail.dom.articleTitle.length).toBeGreaterThan(0)
  expect(detail.dom.articleBodyText.length).toBeGreaterThan(0)
  expect(initialContainsArticle).toBeTruthy()
})
