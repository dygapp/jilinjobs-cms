import { expect, test } from '@playwright/test'

test('EU-71：super 可有界筛选、翻页并查看不含请求内容的操作审计详情', async ({ page, request }) => {
  const suffix = Date.now()
  const created: number[] = []
  const sensitiveName = `audit-sensitive-body-${suffix}`
  try {
    for (let index = 0; index < 21; index += 1) {
      const response = await request.post('/api/admin/columns', {
        data: {
          parentId: null,
          name: index === 0 ? sensitiveName : `审计分页-${suffix}-${index}`,
          alias: `audit-query-${suffix}-${index}`,
          coverPolicy: 'OPTIONAL',
          sortOrder: 980 + index,
          enabled: true,
        },
      })
      expect(response.ok()).toBeTruthy()
      created.push((await response.json() as { id: number }).id)
    }

    const apiPage = await request.get('/api/admin/audit-events?objectType=COLUMN&action=CREATE&limit=20')
    expect(apiPage.ok()).toBeTruthy()
    const projection = await apiPage.json() as { items: Array<Record<string, unknown>>; nextCursor: string | null }
    expect(projection.items).toHaveLength(20)
    expect(projection.nextCursor).toBeTruthy()
    expect(JSON.stringify(projection)).not.toContain(sensitiveName)
    expect(Object.keys(projection.items[0]).sort()).toEqual([
      'action', 'auditId', 'completedAt', 'identitySource', 'objectId', 'objectType',
      'requestCorrelationId', 'result', 'roles', 'startedAt', 'userId',
    ])

    await page.goto('/admin/audit')
    await expect(page).toHaveURL(/\/admin\/cms\/audit$/)
    await expect(page.getByRole('heading', { name: '操作审计' })).toBeVisible()

    await page.getByTestId('audit-filter-action').click()
    await page.getByRole('option', { name: '创建' }).click()
    await page.getByTestId('audit-filter-object-type').click()
    await page.getByRole('option', { name: '栏目' }).click()
    await page.getByTestId('audit-filter-object-id').fill(String(created[0]))
    await page.getByTestId('audit-apply-filters').click()

    const row = page.getByTestId('audit-table').getByRole('row').filter({ hasText: String(created[0]) })
    await expect(row).toContainText('成功')
    await expect(row).toContainText('super')
    await expect(page.locator('main')).not.toContainText(sensitiveName)
    await row.getByRole('button', { name: '详情' }).click()
    await expect(page.getByTestId('audit-detail-drawer')).toContainText('请求关联标识')
    await expect(page.getByTestId('audit-detail-drawer')).toContainText('成功')
    await page.keyboard.press('Escape')
    await expect(page.getByTestId('audit-detail-drawer')).not.toBeVisible()

    await page.getByRole('button', { name: '重置' }).click()
    await expect(page.getByTestId('audit-pagination').getByRole('button', { name: '下一页' })).toBeEnabled()
    await page.getByTestId('audit-pagination').getByRole('button', { name: '下一页' }).click()
    await expect(page.getByTestId('audit-pagination')).toContainText('第 2 页')
    await page.getByTestId('audit-pagination').getByRole('button', { name: '上一页' }).click()
    await expect(page.getByTestId('audit-pagination')).toContainText('第 1 页')

    await expect(page.getByRole('button', { name: '导出' })).toHaveCount(0)
    await expect(page.getByRole('button', { name: '删除' })).toHaveCount(0)
  } finally {
    for (const id of created.reverse()) await request.delete(`/api/admin/columns/${id}`)
  }
})
