import { test as base } from '@playwright/test'
import { createReviewTest } from './scopedReviewTest'

export { expect } from '@playwright/test'
export type { APIRequestContext, Page, Locator } from './scopedReviewTest'
export const test = createReviewTest(base).extend({ reviewBrowserIdentity: true })
