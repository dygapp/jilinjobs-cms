const STORAGE_KEY = 'jilinjobs.suneditor.roundtrip.v7'

let editor = null
const original = {}
let pendingStates = []
let applyCount = 0
const logs = []
const loadBound = new WeakSet()

const q = (selector) => document.querySelector(selector)
const setStatus = (text) => { q('#status').textContent = text }

function log(message) {
  logs.push(`${new Date().toISOString().slice(11, 23)} ${message}`)
  if (logs.length > 40) logs.shift()
  q('#events-log').textContent = logs.join('\n')
}

async function fetchFixture(name) {
  const response = await fetch(`/fixture/${name}`)
  if (!response.ok) throw new Error(await response.text())
  return response.text()
}

async function initFixtures() {
  for (const name of ['p1', 'p2', 'minimal']) {
    original[name] = await fetchFixture(name)
    const box = q(`#source-${name}`)
    box.value = original[name]
    box.addEventListener('input', () => updateMeta(name))
    updateMeta(name)
  }
}

function updateMeta(name) {
  q(`#${name}-meta`).textContent = `${q(`#source-${name}`).value.length} chars`
}

function dimension(value) {
  const normalized = String(value || '').trim()
  if (/^\d+(?:\.\d+)?$/.test(normalized)) return `${normalized}px`
  if (/^\d+(?:\.\d+)?(?:px|%)$/i.test(normalized)) return normalized
  return ''
}

function attrDimension(value) {
  const normalized = dimension(value)
  return normalized.endsWith('px') ? normalized.slice(0, -2) : normalized
}

function imageStates(raw) {
  const template = document.createElement('template')
  template.innerHTML = raw
  return [...template.content.querySelectorAll('img')].map((img) => ({
    src: img.getAttribute('src') || '',
    alt: img.getAttribute('alt') || '',
    width: dimension(img.style.width) || dimension(img.getAttribute('width')),
    height: dimension(img.style.height) || dimension(img.getAttribute('height')),
    float: (img.style.float || img.getAttribute('align') || '').toLowerCase(),
  }))
}

function prepareForEditor(raw) {
  if (!q('#bridge').checked) return raw

  const template = document.createElement('template')
  template.innerHTML = raw
  template.content.querySelectorAll('img').forEach((img) => {
    if (img.closest('.se-component')) return

    const wrapper = document.createElement('span')
    const float = (img.style.float || img.getAttribute('align') || '').toLowerCase()
    const align = ['left', 'right', 'center'].includes(float) ? float : 'none'
    wrapper.className = `se-component se-inline-component se-image-container __se__float-${align}`
    img.replaceWith(wrapper)
    wrapper.appendChild(img)
  })

  return template.innerHTML
}

function surface() {
  return editor?.$?.frameContext?.get?.('wysiwyg') || q('.se-wrapper-wysiwyg[contenteditable="true"]')
}

function bindNativeLoads() {
  const wysiwyg = surface()
  if (!wysiwyg) return

  for (const img of wysiwyg.querySelectorAll('img')) {
    if (loadBound.has(img)) continue
    loadBound.add(img)
    img.addEventListener('load', () => {
      log(`native img load: ${img.getAttribute('alt') || img.src}`)
      applyLegacyStatesViaApi('native-load')
    }, { once: true })
  }
}

function applyLegacyStatesViaApi(reason) {
  if (!q('#bridge').checked || !editor || pendingStates.length === 0) return

  const imagePlugin = editor.$?.plugins?.image
  const wysiwyg = surface()
  if (!imagePlugin?.figure || !imagePlugin?.sizeService || !wysiwyg) {
    log(`${reason}: image Figure/SizeService API unavailable`)
    return
  }

  const images = [...wysiwyg.querySelectorAll('img')]
  let applied = 0

  images.forEach((img, index) => {
    const state = pendingStates[index]
    if (!state) return

    try {
      const info = imagePlugin.figure.open(img, {
        nonResizing: true,
        nonSizeInfo: true,
        nonBorder: true,
        figureTarget: false,
        infoOnly: true,
      })

      if (!info?.container) {
        log(`${reason} image ${index + 1}: Figure.open returned no component`)
        return
      }

      imagePlugin.sizeService.applySize(state.width || 'auto', state.height || 'auto')

      const component = img.closest('.se-component.se-image-container')
      if (component) {
        component.classList.remove('__se__float-none', '__se__float-left', '__se__float-center', '__se__float-right')
        const align = ['left', 'right', 'center'].includes(state.float) ? state.float : 'none'
        component.classList.add(`__se__float-${align}`)
      }

      applied += 1
    } catch (error) {
      log(`${reason} image ${index + 1} ERROR: ${error?.message || error}`)
    }
  })

  applyCount += 1
  log(`${reason}: API applied ${applied}/${images.length} (#${applyCount})`)
  diagnostics()
}

function normalizeForApp(html) {
  const template = document.createElement('template')
  template.innerHTML = html

  template.content.querySelectorAll('.se-component.se-image-container').forEach((component) => {
    const img = component.querySelector('img')
    if (!img) return

    const clone = img.cloneNode(true)
    const dataSize = (clone.getAttribute('data-se-size') || '').split(',')
    const width = dimension(clone.style.width) || dimension(dataSize[0]) || dimension(clone.getAttribute('width'))
    const height = dimension(clone.style.height) || dimension(dataSize[1]) || dimension(clone.getAttribute('height'))

    if (width) clone.setAttribute('width', attrDimension(width))
    else clone.removeAttribute('width')
    if (height) clone.setAttribute('height', attrDimension(height))
    else clone.removeAttribute('height')

    for (const name of [...clone.getAttributeNames()]) {
      if (name.startsWith('data-se-')) clone.removeAttribute(name)
    }

    clone.style.removeProperty('width')
    clone.style.removeProperty('height')
    clone.style.removeProperty('float')
    if (component.classList.contains('__se__float-left')) clone.style.float = 'left'
    if (component.classList.contains('__se__float-right')) clone.style.float = 'right'
    if (!clone.getAttribute('style')) clone.removeAttribute('style')

    component.replaceWith(clone)
  })

  return template.innerHTML
}

function getHtml() {
  if (!editor) return ''
  return typeof editor.getContents === 'function' ? editor.getContents() : editor.$.html.get()
}

function setRawHtml(raw) {
  if (!editor) return

  pendingStates = imageStates(raw)
  const prepared = prepareForEditor(raw)
  log(`setRawHtml: ${pendingStates.length} legacy image state(s)`)

  if (typeof editor.setContents === 'function') editor.setContents(prepared)
  else editor.$.html.set(prepared)

  bindNativeLoads()

  try {
    editor.$?.pluginManager?.checkFileInfo?.(true)
    log('checkFileInfo(true) completed')
  } catch (error) {
    log(`checkFileInfo ERROR: ${error?.message || error}`)
  }

  applyLegacyStatesViaApi('post-set')
  requestAnimationFrame(() => applyLegacyStatesViaApi('raf'))
}

function destroyEditor() {
  if (!editor) return
  try { editor.destroy() } catch {}
  editor = null
}

function rebuild() {
  destroyEditor()
  const oldEditorNode = q('#editor')
  const freshEditorNode = document.createElement('textarea')
  freshEditorNode.id = 'editor'
  oldEditorNode.replaceWith(freshEditorNode)
  q('#startup-error').textContent = ''

  const migration = q('#v2-migration').checked

  try {
    editor = SUNEDITOR.create('#editor', {
      plugins: SUNEDITOR.plugins,
      lang: window.SUNEDITOR_LANG?.zh_cn,
      height: '430px',
      v2Migration: migration,
      buttonList: [
        ['undo', 'redo'],
        ['bold', 'italic', 'underline', 'strike'],
        ['list', 'table'],
        ['link', 'image'],
      ],
      attributeWhitelist: {
        table: 'align|cellpadding|cellspacing',
        img: 'width|height|align',
      },
      tagStyles: {
        td: 'width|height',
        img: 'width|height|float',
      },
      events: {
        onImageLoad: () => {
          log('onImageLoad')
          requestAnimationFrame(() => applyLegacyStatesViaApi('onImageLoad'))
        },
      },
    })

    q('#mode').textContent = `bridge=${q('#bridge').checked}, v2Migration=${migration}`
    setRawHtml(q('#app-html').value || original.minimal || '')
    setStatus('SunEditor 初始化成功')
  } catch (error) {
    q('#startup-error').textContent = `SunEditor 初始化失败：${error?.message || error}`
    console.error(error)
  }
}

function capture() {
  const internal = getHtml()
  const appHtml = normalizeForApp(internal)
  q('#internal').value = internal
  q('#app-html').value = appHtml
  diagnostics()
  setStatus('已读取当前 HTML')
  return appHtml
}

function diagnostics() {
  const wysiwyg = surface()
  if (!wysiwyg) {
    q('#diagnostics').textContent = '未找到可编辑 surface'
    return
  }

  const rows = [...wysiwyg.querySelectorAll('img')].map((img, index) => {
    const rect = img.getBoundingClientRect()
    const computed = getComputedStyle(img)
    const component = img.closest('.se-component.se-image-container')
    const componentRect = component?.getBoundingClientRect()

    return {
      index: index + 1,
      alt: img.getAttribute('alt'),
      src: img.getAttribute('src'),
      width: img.getAttribute('width'),
      height: img.getAttribute('height'),
      style: img.getAttribute('style'),
      dataSeSize: img.getAttribute('data-se-size'),
      componentTag: component?.tagName || null,
      componentClass: component?.className || null,
      componentStyle: component?.getAttribute('style') || null,
      naturalWidth: img.naturalWidth,
      naturalHeight: img.naturalHeight,
      computedWidth: computed.width,
      computedHeight: computed.height,
      renderedWidth: Math.round(rect.width * 100) / 100,
      renderedHeight: Math.round(rect.height * 100) / 100,
      componentRenderedWidth: componentRect ? Math.round(componentRect.width * 100) / 100 : null,
      componentRenderedHeight: componentRect ? Math.round(componentRect.height * 100) / 100 : null,
    }
  })

  q('#diagnostics').textContent = rows.length ? JSON.stringify(rows, null, 2) : '当前没有图片'
}

function loadFixture(name) {
  setRawHtml(q(`#source-${name}`).value)
  requestAnimationFrame(() => {
    capture()
    setStatus(`已加载 ${name}`)
  })
}

function resetFixture(name) {
  q(`#source-${name}`).value = original[name]
  updateMeta(name)
}

function bindActions() {
  q('#rebuild').addEventListener('click', rebuild)
  q('#capture').addEventListener('click', capture)
  q('#save').addEventListener('click', () => {
    const appHtml = capture()
    localStorage.setItem(STORAGE_KEY, appHtml)
    q('#snapshot').value = appHtml
    setStatus('已保存模拟 CMS HTML 快照')
  })
  q('#reload-current').addEventListener('click', () => setRawHtml(q('#app-html').value))
  q('#reload-snapshot').addEventListener('click', () => {
    const raw = localStorage.getItem(STORAGE_KEY) || ''
    if (!raw) {
      setStatus('没有浏览器快照')
      return
    }
    q('#app-html').value = raw
    setRawHtml(raw)
  })
  q('#clear').addEventListener('click', () => {
    localStorage.removeItem(STORAGE_KEY)
    q('#snapshot').value = ''
    setStatus('已清除快照')
  })
  q('#copy').addEventListener('click', async () => {
    await navigator.clipboard.writeText(q('#app-html').value)
    setStatus('已复制模拟 CMS HTML')
  })

  document.querySelectorAll('[data-load]').forEach((button) => {
    button.addEventListener('click', () => loadFixture(button.dataset.load))
  })
  document.querySelectorAll('[data-reset]').forEach((button) => {
    button.addEventListener('click', () => resetFixture(button.dataset.reset))
  })
}

async function main() {
  bindActions()
  await initFixtures()
  q('#snapshot').value = localStorage.getItem(STORAGE_KEY) || ''
  q('#app-html').value = original.minimal
  rebuild()
  window.__roundtripV7 = {
    capture,
    getHtml,
    setRawHtml,
    applyLegacyStatesViaApi,
    normalizeForApp,
  }
}

main().catch((error) => {
  q('#startup-error').textContent = String(error?.stack || error)
  console.error(error)
})
