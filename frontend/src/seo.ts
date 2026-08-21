const SITE_NAME = '吉林就业信息发布原型'
const DEFAULT_DESCRIPTION = '吉林省智慧就业云平台中心主站，提供已发布就业信息、栏目导航与公共服务入口。'

interface PageMeta {
  title?: string
  description?: string
}

export function setPageMeta({ title, description }: PageMeta = {}) {
  document.title = title ? `${title} - ${SITE_NAME}` : SITE_NAME

  let descriptionMeta = document.querySelector<HTMLMetaElement>('meta[name="description"]')
  if (!descriptionMeta) {
    descriptionMeta = document.createElement('meta')
    descriptionMeta.name = 'description'
    document.head.appendChild(descriptionMeta)
  }
  descriptionMeta.content = normalizeDescription(description) || DEFAULT_DESCRIPTION
}

export function summarizeHtml(html: string, fallback: string): string {
  const parsed = new DOMParser().parseFromString(html, 'text/html')
  return normalizeDescription(parsed.body.textContent ?? '') || normalizeDescription(fallback)
}

function normalizeDescription(value?: string): string {
  const normalized = value?.replace(/\s+/g, ' ').trim() ?? ''
  if (normalized.length <= 160) return normalized
  return `${normalized.slice(0, 159).trimEnd()}…`
}
