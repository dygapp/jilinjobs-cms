import type { test as baseTest, APIRequestContext } from '@playwright/test'
export type { APIRequestContext, Page, Locator, Browser, Response, TestInfo } from '@playwright/test'

const credential = 'cms-local-review-super'
const credentialHeader = 'X-Cms-Review-Credential'
const requestMethods = new Set(['get', 'post', 'put', 'patch', 'delete', 'head', 'fetch'])

// Fixture setup may write Admin data, but Public and external requests remain anonymous.
export function scopedReviewRequest(request: APIRequestContext, baseURL: string): APIRequestContext {
  const origin = new URL(baseURL).origin
  return new Proxy(request, {
    get(target, property) {
      const value = Reflect.get(target, property)
      if (typeof value !== 'function') return value
      if (!requestMethods.has(String(property))) return value.bind(target)
      return (url: string, options: Record<string, unknown> = {}) => {
        const resolved = new URL(url, baseURL)
        const admin = resolved.origin === origin && resolved.pathname.startsWith('/api/admin/')
        return value.call(target, url, admin ? {
          ...options,
          headers: { ...(options.headers as Record<string, string> | undefined), [credentialHeader]: credential },
          maxRedirects: 0,
        } : options)
      }
    },
  })
}

export function createReviewTest(base: typeof baseTest) {
  return base.extend<{ reviewBrowserIdentity: boolean }>({
    reviewBrowserIdentity: [false, { option: true }],
    request: async ({ request, baseURL }, use) => {
      await use(scopedReviewRequest(request, baseURL!))
    },
    context: async ({ context, baseURL, reviewBrowserIdentity }, use) => {
      if (reviewBrowserIdentity) {
        const origin = new URL(baseURL!).origin
        await context.addInitScript(({ origin, credential }) => {
          if (window.location.origin === origin && window.location.pathname.startsWith('/admin')) {
            sessionStorage.setItem('cms.review.identity.credential', credential)
          }
        }, { origin, credential })
        // Native image previews use the same narrowly scoped Review-only Cookie as manual login.
        await context.addCookies([{
          name: 'cms_review_resource', value: credential, domain: new URL(origin).hostname,
          path: '/api/admin/resources/', httpOnly: true, secure: false, sameSite: 'Strict',
        }])
      }
      await use(context)
    },
  })
}
