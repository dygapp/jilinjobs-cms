export type ArticleStatus = 'DRAFT' | 'PUBLISHED' | 'WITHDRAWN'

export interface CmsArticle {
  id: number
  columnId: number
  title: string
  bodyHtml: string
  source: string
  publishDate: string | null
  pinned: boolean
  recommended: boolean
  sortOrder: number
  status: ArticleStatus
  actualPublishedAt: string | null
  viewCount: number
  updatedAt: string
  coverResourceId: number | null
  bodyImageResourceIds: number[]
  attachmentResourceIds: number[]
}

export interface ArticleDraft {
  columnId: number
  title: string
  bodyHtml: string
  source: string
  publishDate: string | null
  pinned: boolean
  recommended: boolean
  sortOrder: number
  coverResourceId: number | null
  bodyImageResourceIds: number[]
  attachmentResourceIds: number[]
}

export interface CmsResource {
  id: number
  storageKey: string
  originalFilename: string
  contentType: string | null
  sizeBytes: number
}

export interface PublicArticleSummary {
  id: number
  columnId: number
  columnName: string
  title: string
  publishDate: string | null
  pinned: boolean
  recommended: boolean
  sortOrder: number
}

export interface PublicArticleDetail {
  id: number
  columnId: number
  columnName: string
  title: string
  bodyHtml: string
  source: string
  publishDate: string | null
  bodyImageResourceIds: number[]
}

export interface PublicArticlePage {
  items: PublicArticleSummary[]
  page: number
  size: number
  total: number
}

async function jsonRequest<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
  })
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: `请求失败：${response.status}` })) as { message?: string }
    throw new Error(error.message ?? `请求失败：${response.status}`)
  }
  return response.json() as Promise<T>
}

export function listArticles(): Promise<CmsArticle[]> {
  return jsonRequest('/api/admin/articles')
}

export function getArticle(id: number): Promise<CmsArticle> {
  return jsonRequest(`/api/admin/articles/${id}`)
}

export function createArticle(draft: ArticleDraft): Promise<CmsArticle> {
  return jsonRequest('/api/admin/articles', {
    method: 'POST',
    body: JSON.stringify(draft),
  })
}

export function updateArticle(id: number, draft: ArticleDraft): Promise<CmsArticle> {
  return jsonRequest(`/api/admin/articles/${id}`, {
    method: 'PUT',
    body: JSON.stringify(draft),
  })
}

export function publishArticle(id: number): Promise<CmsArticle> {
  return jsonRequest(`/api/admin/articles/${id}/publish`, { method: 'POST' })
}

export function withdrawArticle(id: number): Promise<CmsArticle> {
  return jsonRequest(`/api/admin/articles/${id}/withdraw`, { method: 'POST' })
}

export function listPublicArticles(columnId: number | null, page = 0, size = 10): Promise<PublicArticlePage> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (columnId != null) params.set('columnId', String(columnId))
  return jsonRequest(`/api/public/articles?${params.toString()}`)
}

export function getPublicArticle(id: number): Promise<PublicArticleDetail> {
  return jsonRequest(`/api/public/articles/${id}`)
}

export async function uploadResource(file: File): Promise<CmsResource> {
  const form = new FormData()
  form.append('file', file)
  const response = await fetch('/api/admin/resources', {
    method: 'POST',
    body: form,
  })
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: `上传失败：${response.status}` })) as { message?: string }
    throw new Error(error.message ?? `上传失败：${response.status}`)
  }
  return response.json() as Promise<CmsResource>
}

export function getResource(id: number): Promise<CmsResource> {
  return jsonRequest(`/api/admin/resources/${id}`)
}

export function resourceContentUrl(id: number): string {
  return `/api/admin/resources/${id}/content`
}

export function publicResourceContentUrl(id: number): string {
  return `/api/public/resources/${id}/content`
}

export function publicBodyHtml(article: PublicArticleDetail): string {
  return article.bodyImageResourceIds.reduce(
    (html, id) => html.replaceAll(resourceContentUrl(id), publicResourceContentUrl(id)),
    article.bodyHtml,
  )
}
