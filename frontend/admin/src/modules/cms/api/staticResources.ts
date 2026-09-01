export interface StaticEntry {
  path: string
  name: string
  directory: boolean
  size: number
  modifiedAt: string | null
  protectedResource: boolean
}

export interface TrashEntry {
  id: string
  originalPath: string
}

export const STATIC_RESOURCE_MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024
export const STATIC_RESOURCE_MAX_FILE_SIZE_LABEL = '20MB'

async function checked<T>(responsePromise: Promise<Response>): Promise<T> {
  const response = await responsePromise
  if (!response.ok) {
    const error = await response.json().catch(() => null) as { message?: string } | null
    if (response.status === 413) {
      throw new Error(error?.message ?? `上传文件过大，单个文件不能超过 ${STATIC_RESOURCE_MAX_FILE_SIZE_LABEL}`)
    }
    throw new Error(error?.message ?? `请求失败：${response.status}`)
  }
  return response.json() as Promise<T>
}

export const listStaticResources = (path = '') => checked<StaticEntry[]>(
  fetch(`/api/admin/static-resources?path=${encodeURIComponent(path)}`),
)

export async function uploadStaticResource(path: string, file: File, replace = false) {
  const form = new FormData()
  form.append('file', file)
  return checked<StaticEntry>(fetch(
    `/api/admin/static-resources?path=${encodeURIComponent(path)}&replace=${replace}`,
    { method: 'POST', body: form },
  ))
}

export const deleteStaticResource = (path: string) => checked<TrashEntry>(
  fetch(`/api/admin/static-resources?path=${encodeURIComponent(path)}`, { method: 'DELETE' }),
)

export const listStaticTrash = () => checked<TrashEntry[]>(fetch('/api/admin/static-resources/trash'))

export const restoreStaticResource = (id: string) => checked<StaticEntry>(
  fetch(`/api/admin/static-resources/restore/${id}`, { method: 'POST' }),
)
