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
