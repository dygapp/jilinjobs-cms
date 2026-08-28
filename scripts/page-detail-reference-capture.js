const fs = require('fs')
const path = require('path')
const { chromium } = require('playwright')

const OUT = path.resolve('page-detail-reference')
const TARGETS = [
  { key: 'column-notice', url: 'https://24365.jl.smartedu.cn/list.html?typeCode=notice', markers: ['通知公告'] },
  { key: 'article-notice', url: 'https://24365.jl.smartedu.cn/detail.html?content_id=132750149451776&typeCode=notice', markers: ['通知公告', '信息来源', '发布时间'] },
  { key: 'page-about', url: 'https://24365.jl.smartedu.cn/about', markers: ['关于我们'] },
  { key: 'guide-archive', url: 'https://24365.jl.smartedu.cn/danganguanli', markers: ['档案管理', '业务指南'] },
  { key: 'guide-party', url: 'https://24365.jl.smartedu.cn/liudongdangyuan', markers: ['流动党员', '业务指南'] },
]

fs.mkdirSync(OUT, { recursive: true })

const tidy = value => String(value || '').replace(/\s+/g, ' ').trim()

async function settle(page) {
  await page.waitForTimeout(2200)
  await page.evaluate(async () => {
    await new Promise(resolve => {
      let moved = 0
      const step = 700
      const timer = setInterval(() => {
        window.scrollBy(0, step)
        moved += step
        if (moved >= document.documentElement.scrollHeight) {
          clearInterval(timer)
          window.scrollTo(0, 0)
          setTimeout(resolve, 700)
        }
      }, 90)
    })
  })
}

async function collect(page, target) {
  return page.evaluate(({ markers }) => {
    const clean = value => String(value || '').replace(/\s+/g, ' ').trim()
    const visible = el => {
      const rect = el.getBoundingClientRect()
      const style = getComputedStyle(el)
      return rect.width > 0 && rect.height > 0 && style.display !== 'none' && style.visibility !== 'hidden' && style.opacity !== '0'
    }
    const rectOf = el => {
      const rect = el.getBoundingClientRect()
      return {
        x: Math.round(rect.x),
        y: Math.round(rect.y + window.scrollY),
        width: Math.round(rect.width),
        height: Math.round(rect.height),
      }
    }
    const sample = el => {
      const style = getComputedStyle(el)
      return {
        tag: el.tagName.toLowerCase(),
        id: el.id || '',
        className: typeof el.className === 'string' ? el.className : '',
        text: clean(el.textContent).slice(0, 220),
        rect: rectOf(el),
        style: {
          display: style.display,
          position: style.position,
          color: style.color,
          backgroundColor: style.backgroundColor,
          backgroundImage: style.backgroundImage,
          fontFamily: style.fontFamily,
          fontSize: style.fontSize,
          fontWeight: style.fontWeight,
          lineHeight: style.lineHeight,
          textAlign: style.textAlign,
          borderTop: style.borderTop,
          borderBottom: style.borderBottom,
          borderRadius: style.borderRadius,
          margin: style.margin,
          padding: style.padding,
        },
      }
    }
    const structuralSelectors = [
      'main', 'article', 'section', 'nav', 'footer', 'h1', 'h2', 'h3', 'h4',
      '[class*="bread"]', '[class*="location"]', '[class*="position"]',
      '[class*="title"]', '[class*="list"]', '[class*="news"]', '[class*="article"]',
      '[class*="content"]', '[class*="detail"]', '[class*="page"]', '[class*="pagination"]',
      '[class*="tab"]', '[class*="guide"]', '[class*="menu"]', '[class*="nav"]'
    ]
    const structural = Array.from(document.querySelectorAll(structuralSelectors.join(',')))
      .filter(visible)
      .map(sample)
      .filter(item => item.rect.width >= 120 && item.rect.height >= 18)
      .slice(0, 240)

    const markerMatches = []
    for (const marker of markers) {
      const matches = Array.from(document.querySelectorAll('body *'))
        .filter(el => visible(el) && clean(el.textContent).includes(marker))
        .sort((a, b) => clean(a.textContent).length - clean(b.textContent).length)
        .slice(0, 8)
      markerMatches.push({ marker, elements: matches.map(sample) })
    }

    const images = Array.from(document.images).filter(visible).map(img => ({
      src: img.currentSrc || img.src,
      alt: img.alt || '',
      naturalWidth: img.naturalWidth,
      naturalHeight: img.naturalHeight,
      rect: rectOf(img),
    }))
    const backgrounds = []
    for (const el of Array.from(document.querySelectorAll('body *'))) {
      if (!visible(el)) continue
      const bg = getComputedStyle(el).backgroundImage
      if (!bg || bg === 'none' || !bg.includes('url(')) continue
      for (const match of bg.matchAll(/url\(["']?(.*?)["']?\)/g)) {
        if (match[1]) backgrounds.push({ src: new URL(match[1], location.href).href, rect: rectOf(el) })
      }
    }

    const links = Array.from(document.querySelectorAll('a')).filter(visible).map(a => ({
      text: clean(a.textContent).slice(0, 100),
      href: a.href,
      rect: rectOf(a),
    })).filter(item => item.text).slice(0, 200)

    return {
      url: location.href,
      title: document.title,
      viewport: { width: innerWidth, height: innerHeight },
      document: { width: document.documentElement.scrollWidth, height: document.documentElement.scrollHeight },
      body: sample(document.body),
      structural,
      markerMatches,
      images,
      backgrounds,
      links,
      stylesheets: Array.from(document.querySelectorAll('link[rel="stylesheet"]')).map(link => link.href).filter(Boolean),
    }
  }, { markers: target.markers })
}

async function capture(page, target, width, height, suffix) {
  await page.setViewportSize({ width, height })
  const response = await page.goto(target.url, { waitUntil: 'domcontentloaded', timeout: 60000 })
  if (!response || !response.ok()) throw new Error(`${target.key} HTTP ${response && response.status()}`)
  await settle(page)
  const data = await collect(page, target)
  await page.screenshot({ path: path.join(OUT, `${target.key}-${suffix}.png`), fullPage: true })
  fs.writeFileSync(path.join(OUT, `${target.key}-${suffix}.json`), JSON.stringify(data, null, 2))
  if (suffix === 'desktop') fs.writeFileSync(path.join(OUT, `${target.key}.html`), await page.content())
  return data
}

;(async () => {
  const browser = await chromium.launch({ headless: true })
  const context = await browser.newContext({ ignoreHTTPSErrors: true })
  const page = await context.newPage()
  const summary = []

  for (const target of TARGETS) {
    const desktop = await capture(page, target, 1440, 1000, 'desktop')
    const mobile = await capture(page, target, 390, 844, 'mobile')
    summary.push([
      `${target.key}: ${desktop.url}`,
      `  desktop document=${desktop.document.width}x${desktop.document.height}, structural=${desktop.structural.length}, images=${desktop.images.length}`,
      `  mobile document=${mobile.document.width}x${mobile.document.height}, structural=${mobile.structural.length}, images=${mobile.images.length}`,
      ...desktop.markerMatches.map(m => `  marker ${m.marker}: ${m.elements.slice(0, 3).map(e => `${e.tag}.${e.className || '-'} ${e.rect.width}x${e.rect.height}@${e.rect.x},${e.rect.y} ${e.style.fontSize}/${e.style.color}`).join(' | ')}`),
    ].join('\n'))
  }

  fs.writeFileSync(path.join(OUT, 'summary.txt'), summary.join('\n\n') + '\n')
  console.log(summary.join('\n\n'))
  await browser.close()
})().catch(error => {
  console.error(error)
  process.exit(1)
})
