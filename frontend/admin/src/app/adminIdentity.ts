import { reactive, readonly } from 'vue'
import {
  REVIEW_CREDENTIAL_HEADER,
  adminFetch,
  clearReviewCredential,
  getReviewCredential,
  registerAdminAccessFailureHandler,
  setReviewCredential,
} from '../shared/adminHttp'

export type CmsRole = 'admin' | 'super'
export interface AdminIdentity { identitySource: string; userId: string; roles: CmsRole[] }
type IdentityPhase = 'checking' | 'authenticated' | 'unauthenticated' | 'forbidden' | 'error'

const mutableState = reactive({
  phase: 'checking' as IdentityPhase,
  identity: null as AdminIdentity | null,
  reviewEnabled: false,
  message: '正在确认管理身份…',
  deniedMessage: '',
  busy: false,
})

export const adminIdentityState = readonly(mutableState)
let identityCheckGeneration = 0

registerAdminAccessFailureHandler((status, message) => {
  if (status === 401) {
    identityCheckGeneration += 1
    const wasAuthenticated = mutableState.identity !== null
    mutableState.identity = null
    mutableState.phase = 'unauthenticated'
    mutableState.message = wasAuthenticated ? '管理身份已失效，请重新认证。' : message
  } else {
    mutableState.deniedMessage = message
    if (!mutableState.identity) {
      mutableState.phase = 'forbidden'
      mutableState.message = message
    }
  }
})

export async function initializeAdminIdentity() {
  mutableState.phase = 'checking'
  mutableState.message = '正在确认管理身份…'
  mutableState.deniedMessage = ''
  mutableState.reviewEnabled = await detectReviewRuntime()
  await refreshAdminIdentity()
}

export async function refreshAdminIdentity() {
  const generation = ++identityCheckGeneration
  mutableState.phase = 'checking'
  try {
    const response = await adminFetch('/api/admin/identity', { cache: 'no-store' })
    if (generation !== identityCheckGeneration) return
    if (!response.ok) {
      mutableState.identity = null
      if (response.status === 403) {
        mutableState.phase = 'forbidden'
        mutableState.message = mutableState.deniedMessage || '当前身份无权进入管理端。'
      } else if (response.status !== 401) {
        mutableState.phase = 'error'
        mutableState.message = `管理身份检查失败（${response.status}）`
      }
      return
    }
    const identity = await response.json() as AdminIdentity
    if (generation !== identityCheckGeneration) return
    mutableState.identity = identity
    mutableState.phase = 'authenticated'
    mutableState.message = ''
    mutableState.deniedMessage = ''
  } catch {
    if (generation !== identityCheckGeneration) return
    mutableState.identity = null
    mutableState.phase = 'error'
    mutableState.message = '暂时无法确认管理身份，请检查服务状态后重试。'
  }
}

export async function loginWithReviewProfile(profile: CmsRole) {
  if (mutableState.busy) return
  mutableState.busy = true
  mutableState.message = ''
  try {
    const response = await fetch('/api/review/identity/sessions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ profile }),
    })
    if (!response.ok) throw new Error(`测试身份登录失败（${response.status}）`)
    const payload = await response.json() as { credential: string }
    setReviewCredential(payload.credential)
    await refreshAdminIdentity()
    if (mutableState.phase !== 'authenticated') throw new Error('测试身份未通过服务端确认')
  } catch (error) {
    clearReviewCredential()
    mutableState.identity = null
    mutableState.phase = 'unauthenticated'
    mutableState.message = error instanceof Error ? error.message : '测试身份登录失败'
  } finally {
    mutableState.busy = false
  }
}

export async function logoutReviewIdentity() {
  const invalidated = await invalidateReviewSession('DELETE')
  mutableState.identity = null
  mutableState.phase = 'unauthenticated'
  mutableState.message = invalidated
    ? '已退出测试身份。'
    : '已清除当前页面中的测试身份；服务端会话未能确认退出，将在到期后失效。'
}

export async function expireReviewIdentity() {
  const invalidated = await invalidateReviewSession('POST')
  if (invalidated) {
    await refreshAdminIdentity()
  } else {
    mutableState.identity = null
    mutableState.phase = 'unauthenticated'
    mutableState.message = '已清除当前页面中的测试身份；服务端未能确认模拟失效，将在到期后失效。'
  }
}

export function dismissAccessDenied() {
  mutableState.deniedMessage = ''
}

async function invalidateReviewSession(method: 'DELETE' | 'POST'): Promise<boolean> {
  identityCheckGeneration += 1
  const credential = getReviewCredential()
  mutableState.busy = true
  try {
    if (!credential) return true
    const suffix = method === 'POST' ? '/expire' : ''
    const response = await fetch(`/api/review/identity/sessions/current${suffix}`, {
      method,
      headers: { [REVIEW_CREDENTIAL_HEADER]: credential },
    })
    return response.ok
  } catch {
    return false
  } finally {
    clearReviewCredential()
    mutableState.busy = false
  }
}

async function detectReviewRuntime() {
  try {
    const response = await fetch('/review-environment.json', { cache: 'no-store' })
    if (!response.ok || !response.headers.get('content-type')?.includes('application/json')) return false
    const payload = await response.json() as { reviewIdentity?: { enabled?: boolean } }
    return payload.reviewIdentity?.enabled === true
  } catch {
    return false
  }
}
