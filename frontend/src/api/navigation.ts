export type NavigationPosition = 'MAIN' | 'SERVICE' | 'SITE'
export type NavigationTargetType = 'COLUMN' | 'LINK'

export interface CmsNavigation {
  id: number
  name: string
  position: NavigationPosition
  category: string | null
  targetType: NavigationTargetType
  targetColumnId: number | null
  targetUrl: string | null
  sortOrder: number
  enabled: boolean
}

export interface NavigationDraft {
  name: string
  position: NavigationPosition
  category: string | null
  targetType: NavigationTargetType
  targetColumnId: number | null
  targetUrl: string | null
  sortOrder: number
  enabled: boolean
}

export interface PublicNavigation {
  id: number
  name: string
  position: NavigationPosition
  category: string | null
  sortOrder: number
  targetType: NavigationTargetType
  href: string
  external: boolean
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
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

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export function listNavigations(): Promise<CmsNavigation[]> {
  return request('/api/admin/navigations')
}

export function listPublicNavigations(): Promise<PublicNavigation[]> {
  return request('/api/public/navigations')
}

export function createNavigation(draft: NavigationDraft): Promise<CmsNavigation> {
  return request('/api/admin/navigations', {
    method: 'POST',
    body: JSON.stringify(draft),
  })
}

export function updateNavigation(id: number, draft: NavigationDraft): Promise<CmsNavigation> {
  return request(`/api/admin/navigations/${id}`, {
    method: 'PUT',
    body: JSON.stringify(draft),
  })
}

export function deleteNavigation(id: number): Promise<void> {
  return request(`/api/admin/navigations/${id}`, {
    method: 'DELETE',
  })
}
