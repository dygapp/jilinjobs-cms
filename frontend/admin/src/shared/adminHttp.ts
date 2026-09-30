export const REVIEW_CREDENTIAL_HEADER = 'X-Cms-Review-Credential'
const REVIEW_CREDENTIAL_KEY = 'cms.review.identity.credential'

export class AdminRequestError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message)
  }
}

type AccessFailureHandler = (status: 401 | 403, message: string) => void
let accessFailureHandler: AccessFailureHandler | null = null

export function registerAdminAccessFailureHandler(handler: AccessFailureHandler) {
  accessFailureHandler = handler
}

export function getReviewCredential() {
  return sessionStorage.getItem(REVIEW_CREDENTIAL_KEY)
}

export function setReviewCredential(credential: string) {
  sessionStorage.setItem(REVIEW_CREDENTIAL_KEY, credential)
}

export function clearReviewCredential() {
  sessionStorage.removeItem(REVIEW_CREDENTIAL_KEY)
}

export async function adminFetch(input: RequestInfo | URL, init: RequestInit = {}): Promise<Response> {
  const url = typeof input === 'string' ? input : input instanceof URL ? input.toString() : input.url
  const resolvedUrl = new URL(url, window.location.origin)
  const isAdminRequest = resolvedUrl.origin === window.location.origin
    && resolvedUrl.pathname.startsWith('/api/admin/')
  const headers = new Headers(input instanceof Request ? input.headers : undefined)
  new Headers(init.headers).forEach((value, key) => headers.set(key, value))
  const credential = isAdminRequest ? getReviewCredential() : null
  if (credential) headers.set(REVIEW_CREDENTIAL_HEADER, credential)

  const response = await fetch(input, { ...init, headers, ...(isAdminRequest ? { redirect: 'error' } as const : {}) })
  if (isAdminRequest && (response.status === 401 || response.status === 403)) {
    const fallback = response.status === 401 ? '管理身份未认证' : '管理身份无访问权限'
    const payload = await response.clone().json().catch(() => null) as { message?: string } | null
    // An earlier session's response must not clear a newly selected identity.
    if (credential !== getReviewCredential()) return response
    if (response.status === 401) clearReviewCredential()
    accessFailureHandler?.(response.status, payload?.message ?? fallback)
  }
  return response
}

export async function requestJson<T>(url: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body != null && !(init.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  const response = await adminFetch(url, { ...init, headers })
  if (!response.ok) {
    const payload = await response.json().catch(() => null) as { message?: string } | null
    throw new AdminRequestError(response.status, payload?.message ?? `请求失败：${response.status}`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
