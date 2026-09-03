import { expect, test } from '@playwright/test'
import fs from 'node:fs'
import path from 'node:path'

const ROOT = path.resolve(__dirname, '../..')
const SCAN_ROOTS = [path.join(ROOT, 'src'), path.join(ROOT, 'index.html'), path.join(ROOT, 'party.html')]
const TEXT_EXTENSIONS = new Set(['.vue', '.ts', '.css', '.html'])

function collect(target: string): string[] {
  const stat = fs.statSync(target)
  if (stat.isFile()) return [target]
  return fs.readdirSync(target, { withFileTypes: true }).flatMap(entry => {
    const child = path.join(target, entry.name)
    return entry.isDirectory() ? collect(child) : TEXT_EXTENSIONS.has(path.extname(entry.name)) ? [child] : []
  })
}

test('公开站设计模板不直接依赖外部静态资源', () => {
  const violations: string[] = []
  const patterns = [
    { name: '外部媒体 src/poster', regex: /(?:src|poster)\s*=\s*["']https?:\/\//gi },
    { name: 'CSS 外部 url()', regex: /url\(\s*["']?https?:\/\//gi },
    { name: '资源常量外部 URL', regex: /(?:banner|logo|icon|image|background|font)[\w$]*\s*=\s*["']https?:\/\//gi },
  ]

  for (const file of SCAN_ROOTS.flatMap(collect)) {
    const source = fs.readFileSync(file, 'utf8')
    for (const pattern of patterns) {
      for (const match of source.matchAll(pattern.regex)) {
        const line = source.slice(0, match.index).split('\n').length
        violations.push(`${path.relative(ROOT, file)}:${line} ${pattern.name}: ${match[0]}`)
      }
    }
  }

  expect(violations, `发现公开站模板直接依赖外部静态资源：\n${violations.join('\n')}`).toEqual([])
})
