import { createServer } from 'node:http'
import { expect, test } from '@playwright/test'
import { scopedReviewRequest } from '../support/scopedReviewTest'

test('EU-72：自动化 fixture 只认证同源 Admin API 并拒绝凭证随重定向转发', async ({ playwright }) => {
  const server = createServer((request, response) => {
    if (request.url === '/api/admin/redirect') {
      response.writeHead(302, { Location: '/api/public/echo' }).end()
      return
    }
    response.setHeader('Content-Type', 'application/json')
    response.end(JSON.stringify({ credential: request.headers['x-cms-review-credential'] ?? null }))
  })
  await new Promise<void>(resolve => server.listen(0, '127.0.0.1', resolve))
  const address = server.address() as { port: number }
  const baseURL = `http://127.0.0.1:${address.port}`
  const request = await playwright.request.newContext({ baseURL })
  try {
    const scoped = scopedReviewRequest(request, baseURL)
    expect(await (await scoped.get('/api/admin/echo')).json()).toEqual({ credential: 'cms-local-review-super' })
    expect(await (await scoped.get('/api/public/echo')).json()).toEqual({ credential: null })
    // A different origin must stay anonymous even when its path looks like an Admin endpoint.
    const otherOrigin = createServer((incoming, response) => {
      response.setHeader('Content-Type', 'application/json')
      response.end(JSON.stringify({ credential: incoming.headers['x-cms-review-credential'] ?? null }))
    })
    await new Promise<void>(resolve => otherOrigin.listen(0, '127.0.0.1', resolve))
    try {
      const otherAddress = otherOrigin.address() as { port: number }
      expect(await (await scoped.get(`http://127.0.0.1:${otherAddress.port}/api/admin/echo`)).json()).toEqual({ credential: null })
    } finally {
      await new Promise<void>((resolve, reject) => otherOrigin.close(error => error ? reject(error) : resolve()))
    }
    expect((await scoped.get('/api/admin/redirect')).status()).toBe(302)
  } finally {
    await request.dispose()
    await new Promise<void>((resolve, reject) => server.close(error => error ? reject(error) : resolve()))
  }
})

test('EU-72：匿名 Browser 的 Public 与外部 iframe 请求不携带自动化身份', async ({ page }) => {
  const publicHeaders: Array<Record<string, string>> = []
  page.on('request', request => {
    if (new URL(request.url()).pathname.startsWith('/api/public/')) publicHeaders.push(request.headers())
  })
  let externalHeaders: Record<string, string> | undefined
  await page.route('https://review-transport.invalid/probe', async route => {
    externalHeaders = route.request().headers()
    await route.fulfill({ contentType: 'text/html', body: '<p>isolated iframe</p>' })
  })
  await page.goto('/')
  await expect.poll(() => publicHeaders.length).toBeGreaterThan(0)
  const externalResponse = page.waitForResponse('https://review-transport.invalid/probe')
  await page.evaluate(() => {
    const iframe = document.createElement('iframe')
    iframe.src = 'https://review-transport.invalid/probe'
    document.body.append(iframe)
  })
  await externalResponse
  expect(externalHeaders?.['x-cms-review-credential']).toBeUndefined()
  expect(publicHeaders.every(headers => !headers['x-cms-review-credential'])).toBeTruthy()
})
