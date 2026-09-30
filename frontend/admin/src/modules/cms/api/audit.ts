import { adminFetch } from '../../../shared/adminHttp'

export type AdminAuditResult = 'STARTED' | 'SUCCEEDED' | 'FAILED' | 'ROLLED_BACK'
export type AdminAuditAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'PUBLISH' | 'WITHDRAW' | 'UPLOAD' | 'REPLACE' | 'TRASH' | 'RESTORE'
export type AdminAuditObjectType = 'COLUMN' | 'ARTICLE' | 'NAVIGATION_LOCATION' | 'NAVIGATION_ITEM' | 'PAGE_GROUP' | 'PAGE' | 'CMS_LIST' | 'CMS_LIST_ITEM' | 'ADVERTISEMENT_SLOT' | 'ADVERTISEMENT_ITEM' | 'SITE_PROPERTY' | 'MANAGED_RESOURCE' | 'STATIC_RESOURCE'

export interface AdminAuditEvent {
  auditId: string
  requestCorrelationId: string
  identitySource: string
  userId: string
  roles: string[]
  action: AdminAuditAction
  objectType: AdminAuditObjectType
  objectId: string | null
  startedAt: string
  completedAt: string | null
  result: AdminAuditResult
}

export interface AdminAuditEventPage {
  items: AdminAuditEvent[]
  nextCursor: string | null
}

export interface AdminAuditQuery {
  identitySource?: string
  userId?: string
  action?: AdminAuditAction | ''
  objectType?: AdminAuditObjectType | ''
  objectId?: string
  result?: AdminAuditResult | ''
  requestCorrelationId?: string
  startedFrom?: string
  startedBefore?: string
  cursor?: string | null
  limit?: number
}

export class AdminAuditRequestError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message)
  }
}

async function request<T>(url: string): Promise<T> {
  const response = await adminFetch(url)
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: `请求失败：${response.status}` })) as { message?: string }
    throw new AdminAuditRequestError(response.status, error.message ?? `请求失败：${response.status}`)
  }
  return response.json() as Promise<T>
}

export function listAuditEvents(query: AdminAuditQuery = {}) {
  const params = new URLSearchParams({ limit: String(query.limit ?? 20) })
  const entries: Array<[string, string | null | undefined]> = [
    ['identitySource', query.identitySource],
    ['userId', query.userId],
    ['action', query.action],
    ['objectType', query.objectType],
    ['objectId', query.objectId],
    ['result', query.result],
    ['requestCorrelationId', query.requestCorrelationId],
    ['startedFrom', query.startedFrom],
    ['startedBefore', query.startedBefore],
    ['cursor', query.cursor],
  ]
  for (const [key, value] of entries) {
    const normalized = value?.trim()
    if (normalized) params.set(key, normalized)
  }
  return request<AdminAuditEventPage>(`/api/admin/audit-events?${params}`)
}

export const getAuditEvent = (auditId: string) =>
  request<AdminAuditEvent>(`/api/admin/audit-events/${encodeURIComponent(auditId)}`)
