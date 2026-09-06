import { readdir, readFile } from 'node:fs/promises'
import { extname, join, relative } from 'node:path'
import { fileURLToPath } from 'node:url'

const sourceRoot = fileURLToPath(new URL('../src/', import.meta.url))
const sourceExtensions = new Set(['.ts', '.tsx', '.js', '.jsx', '.vue', '.css', '.html', '.json'])
const forbiddenEndpointKnowledge = '/api/admin/'

async function collectSourceFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true })
  const files = []
  for (const entry of entries) {
    const path = join(directory, entry.name)
    if (entry.isDirectory()) {
      files.push(...await collectSourceFiles(path))
    } else if (entry.isFile() && sourceExtensions.has(extname(entry.name))) {
      files.push(path)
    }
  }
  return files
}

const violations = []
for (const file of await collectSourceFiles(sourceRoot)) {
  const content = await readFile(file, 'utf8')
  content.split('\n').forEach((line, index) => {
    if (line.includes(forbiddenEndpointKnowledge)) {
      violations.push(`${relative(sourceRoot, file)}:${index + 1}`)
    }
  })
}

if (violations.length > 0) {
  console.error('Public source must not contain Admin API endpoint knowledge:')
  for (const violation of violations) console.error(`- ${violation}`)
  process.exit(1)
}

console.log('Public source boundary PASS: no /api/admin/ endpoint knowledge under src/**')
