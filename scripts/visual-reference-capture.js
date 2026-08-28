const fs = require('fs')
const path = require('path')
const crypto = require('crypto')
const { chromium } = require('playwright')

const TARGET = 'https://24365.jl.smartedu.cn/index'
const OUT = path.resolve('visual-reference')
const ASSET_DIR = path.join(OUT, 'assets')

fs.mkdirSync(ASSET_DIR, { recursive: true })

const safeName = (url, index, contentType = '') => {
  let base = 'asset'
  try {
    const parsed = new URL(url)
    base = path.basename(parsed.pathname) || 'asset'
  } catch (_) {}
  base = base.replace(/[^a-zA-Z0-9._-]+/g, '-')
  if (!/\.[a-zA-Z0-9]{2,5}$/.test(base)) {
    const ext = contentType.includes('png') ? '.png'
      : contentType.includes('jpeg') || contentType.includes('jpg') ? '.jpg'
      : contentType.includes('gif') ? '.gif'
      : contentType.includes('webp') ? '.webp'
      : contentType.includes('svg') ? '.svg'
      : '.bin'
    base += ext
  }
  const hash = crypto.createHash('sha1').update(url).digest('hex').slice(0, 8)
  return `${String(index).padStart(3, '0')}-${hash}-${base}`
}

async function autoScroll(page) {
  await page.evaluate(async () => {
    await new Promise(resolve => {
      let total = 0
      const distance = 700
      const timer = setInterval(() => {
        window.scrollBy(0, distance)
        total += distance
        if (total >= document.body.scrollHeight) {
          clearInterval(timer)
          window.scrollTo(0, 0)
          setTimeout(resolve, 1200)
        }
      }, 120)
    })
  })
}

async function captureViewport(page, name, width, height) {
  await page.setViewportSize({ width, height })
  await page.goto(TARGET, { waitUntil: 'domcontentloaded', timeout: 60000 })
  await page.waitForTimeout(2500)
  await autoScroll(page)
  await page.screenshot({ path: path.join(OUT, `${name}.png`), fullPage: true })
}

;(async () => {
  const browser = await chromium.launch({ headless: true })
  const context = await browser.newContext({ ignoreHTTPSErrors: true, viewport: { width: 1440, height: 1000 } })
  const page = await context.newPage()

  await page.goto(TARGET, { waitUntil: 'domcontentloaded', timeout: 60000 })
  await page.waitForTimeout(3000)
  await autoScroll(page)

  const manifest = await page.evaluate(() => {
    const rectOf = (el) => {
      const r = el.getBoundingClientRect()
      return { x: Math.round(r.x), y: Math.round(r.y + window.scrollY), width: Math.round(r.width), height: Math.round(r.height) }
    }
    const visible = (el) => {
      const r = el.getBoundingClientRect()
      const s = getComputedStyle(el)
      return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden' && s.opacity !== '0'
    }
    const contextOf = (el) => {
      let node = el
      const parts = []
      for (let i = 0; node && i < 4; i += 1, node = node.parentElement) {
        const id = node.id ? `#${node.id}` : ''
        const cls = node.className && typeof node.className === 'string'
          ? '.' + node.className.trim().split(/\s+/).filter(Boolean).slice(0, 3).join('.')
          : ''
        if (node.tagName) parts.push(`${node.tagName.toLowerCase()}${id}${cls}`)
      }
      return parts.join(' > ')
    }
    const images = Array.from(document.images).filter(visible).map((img, index) => ({
      index,
      src: img.currentSrc || img.src,
      alt: img.alt || '',
      naturalWidth: img.naturalWidth,
      naturalHeight: img.naturalHeight,
      rect: rectOf(img),
      context: contextOf(img),
    }))
    const backgrounds = []
    for (const el of Array.from(document.querySelectorAll('body *'))) {
      if (!visible(el)) continue
      const bg = getComputedStyle(el).backgroundImage
      if (!bg || bg === 'none' || !bg.includes('url(')) continue
      const urls = Array.from(bg.matchAll(/url\(["']?(.*?)["']?\)/g)).map(m => m[1]).filter(Boolean)
      for (const url of urls) backgrounds.push({ src: new URL(url, location.href).href, rect: rectOf(el), context: contextOf(el) })
    }
    const stylesheets = Array.from(document.querySelectorAll('link[rel="stylesheet"]')).map(l => l.href).filter(Boolean)
    const navTexts = Array.from(document.querySelectorAll('nav a, nav span, .nav a, .menu a')).map(el => (el.textContent || '').trim()).filter(Boolean).slice(0, 100)
    const styleSamples = {}
    const selectors = ['body', 'header', 'nav', 'footer', 'h1', 'h2', 'h3', 'main']
    for (const selector of selectors) {
      const el = document.querySelector(selector)
      if (!el) continue
      const s = getComputedStyle(el)
      styleSamples[selector] = {
        color: s.color,
        backgroundColor: s.backgroundColor,
        backgroundImage: s.backgroundImage,
        fontSize: s.fontSize,
        fontWeight: s.fontWeight,
        lineHeight: s.lineHeight,
        width: s.width,
        height: s.height,
      }
    }
    const colorCount = new Map()
    const addColor = c => {
      if (!c || c === 'rgba(0, 0, 0, 0)' || c === 'transparent') return
      colorCount.set(c, (colorCount.get(c) || 0) + 1)
    }
    for (const el of Array.from(document.querySelectorAll('body *'))) {
      if (!visible(el)) continue
      const r = el.getBoundingClientRect()
      if (r.width * r.height < 400) continue
      const s = getComputedStyle(el)
      addColor(s.color)
      addColor(s.backgroundColor)
      addColor(s.borderTopColor)
    }
    const colors = Array.from(colorCount.entries()).sort((a, b) => b[1] - a[1]).slice(0, 30).map(([color, count]) => ({ color, count }))
    return {
      url: location.href,
      title: document.title,
      bodySize: { width: document.documentElement.scrollWidth, height: document.documentElement.scrollHeight },
      images,
      backgrounds,
      stylesheets,
      navTexts,
      styleSamples,
      colors,
    }
  })

  fs.writeFileSync(path.join(OUT, 'page.html'), await page.content())

  const candidates = []
  for (const item of [...manifest.images, ...manifest.backgrounds]) {
    const r = item.rect || { width: 0, height: 0, y: 0 }
    if (!item.src || item.src.startsWith('data:')) continue
    if (r.width < 24 || r.height < 24) continue
    if (r.y > 3600) continue
    if (!candidates.some(x => x.src === item.src)) candidates.push(item)
  }

  const downloaded = []
  let index = 1
  for (const item of candidates.slice(0, 80)) {
    try {
      const response = await context.request.get(item.src, { timeout: 30000, failOnStatusCode: false })
      if (!response.ok()) continue
      const contentType = response.headers()['content-type'] || ''
      if (!contentType.startsWith('image/')) continue
      const body = await response.body()
      if (!body.length) continue
      const filename = safeName(item.src, index, contentType)
      fs.writeFileSync(path.join(ASSET_DIR, filename), body)
      downloaded.push({ filename, src: item.src, contentType, bytes: body.length, rect: item.rect, context: item.context || '', alt: item.alt || '' })
      index += 1
    } catch (error) {
      downloaded.push({ src: item.src, error: String(error), rect: item.rect, context: item.context || '' })
    }
  }

  manifest.downloaded = downloaded
  fs.writeFileSync(path.join(OUT, 'manifest.json'), JSON.stringify(manifest, null, 2))

  await captureViewport(page, 'home-desktop-1440', 1440, 1000)
  await captureViewport(page, 'home-mobile-390', 390, 844)

  const summary = [
    `URL: ${manifest.url}`,
    `Title: ${manifest.title}`,
    `Body: ${manifest.bodySize.width}x${manifest.bodySize.height}`,
    `Images visible: ${manifest.images.length}`,
    `Background images: ${manifest.backgrounds.length}`,
    `Downloaded candidates: ${downloaded.filter(x => x.filename).length}`,
    '',
    'Navigation:',
    ...manifest.navTexts.slice(0, 40).map(x => `- ${x}`),
    '',
    'Top colors:',
    ...manifest.colors.slice(0, 20).map(x => `- ${x.color}: ${x.count}`),
    '',
    'Downloaded assets:',
    ...downloaded.filter(x => x.filename).map(x => `- ${x.filename} | ${x.rect.width}x${x.rect.height}@y${x.rect.y} | ${x.src} | ${x.context}`),
  ].join('\n')
  fs.writeFileSync(path.join(OUT, 'summary.txt'), summary)
  console.log(summary)

  await browser.close()
})().catch(error => {
  console.error(error)
  process.exit(1)
})
