export interface CmsColumn {
  id: number
  parentId: number | null
  name: string
  sortOrder: number
  enabled: boolean
}

export interface ColumnDraft {
  parentId: number | null
  name: string
  sortOrder: number
  enabled: boolean
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

export function listColumns(): Promise<CmsColumn[]> {
  return request('/api/admin/columns')
}

export function createColumn(draft: ColumnDraft): Promise<CmsColumn> {
  return request('/api/admin/columns', {
    method: 'POST',
    body: JSON.stringify(draft),
  })
}

export function updateColumn(id: number, draft: ColumnDraft): Promise<CmsColumn> {
  return request(`/api/admin/columns/${id}`, {
    method: 'PUT',
    body: JSON.stringify(draft),
  })
}

export function deleteColumn(id: number): Promise<void> {
  return request(`/api/admin/columns/${id}`, {
    method: 'DELETE',
  })
}
