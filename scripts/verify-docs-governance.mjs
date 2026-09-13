import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()

const historicalCurrentPaths = new Set([
  'docs/project/documentation-authority-convergence.md',
  'docs/project/agentic-dev-continuous-execution-mode.md',
  'docs/project/main-site-formal-content-plan.md',
  'docs/project/site-package-planning.md',
  'docs/project/pre-e1e3-convergence-plan.md',
  'docs/project/agentic-dev-v3-08-track-b-evidence.md',
  'docs/project/agentic-dev-v3-closure-baseline-upgrade-evidence.md',
  'docs/requirements/backend-application-core-boundary.md',
  'docs/specifications/backend-application-core-boundary.md',
  'docs/technical/backend-application-core-boundary.md',
  'docs/requirements/generic-content-migration-application.md',
  'docs/specifications/generic-content-migration-application.md',
  'docs/technical/generic-content-migration-application.md',
  'docs/requirements/party-migration-despecialization-compatibility.md',
  'docs/specifications/party-migration-despecialization-compatibility.md',
  'docs/technical/party-migration-despecialization-compatibility.md',
  'docs/requirements/main-single-page-formal-content.md',
  'docs/specifications/main-single-page-formal-content.md',
  'docs/technical/main-single-page-formal-content.md',
  'docs/requirements/main-stable-listitem-site-package.md',
  'docs/specifications/main-stable-listitem-site-package.md',
  'docs/technical/main-stable-listitem-site-package.md',
  'docs/requirements/public-frontend-replaceability.md',
  'docs/specifications/public-frontend-replaceability.md',
  'docs/technical/public-frontend-replaceability.md',
  'docs/requirements/database-migration-baseline-convergence.md',
  'docs/specifications/database-migration-baseline-convergence.md',
  'docs/technical/database-migration-baseline-convergence.md',
])

const allowedMissingProvenance = new Set([
  'docs/project/project.md',
  'docs/requirements/overview/system-module-boundaries.md',
])

const currentRoots = [
  'AGENTS.md',
  'README.md',
  'docs/README.md',
  'docs/project',
  'docs/requirements',
  'docs/specifications',
  'docs/technical',
  'docs/architecture/decisions',
  'docs/work/README.md',
  'docs/work/current',
]

function collectMarkdown(target) {
  const absolute = path.join(root, target)
  if (!fs.existsSync(absolute)) return []
  const stat = fs.statSync(absolute)
  if (stat.isFile()) return target.endsWith('.md') ? [target] : []
  const result = []
  for (const entry of fs.readdirSync(absolute, { withFileTypes: true })) {
    const rel = path.posix.join(target, entry.name)
    if (entry.isDirectory()) {
      if (entry.name === 'archive') continue
      result.push(...collectMarkdown(rel))
    } else if (entry.name.endsWith('.md')) {
      result.push(rel)
    }
  }
  return result
}

const files = [...new Set(currentRoots.flatMap(collectMarkdown))]
  .filter((file) => !historicalCurrentPaths.has(file))
  .sort()

const failures = []
const warnings = []
const cjkRe = /[\u3400-\u9fff]/g
const latinRe = /[A-Za-z]/g

function stripNonNarrative(content) {
  let text = content
    .replace(/^---\n[\s\S]*?\n---\n?/, '')
    .replace(/```[\s\S]*?```/g, '')
    .replace(/`[^`]*`/g, '')
    .replace(/https?:\/\/\S+/g, '')
    .replace(/\[[^\]]*\]\([^)]*\)/g, '')
  return text
}

function narrativeParagraphs(content) {
  const stripped = stripNonNarrative(content)
  return stripped
    .split(/\n\s*\n/)
    .map((p) => p
      .split('\n')
      .filter((line) => !/^\s*[|>-]/.test(line) && !/^\s*#{1,6}\s+/.test(line))
      .join(' ')
      .trim())
    .filter(Boolean)
}

for (const file of files) {
  const absolute = path.join(root, file)
  const content = fs.readFileSync(absolute, 'utf8')
  const narrative = stripNonNarrative(content)
  const cjkCount = (narrative.match(cjkRe) || []).length
  const latinCount = (narrative.match(latinRe) || []).length

  if (cjkCount === 0 && latinCount > 80) {
    failures.push(`${file}: Current 文档没有中文主叙述，属于纯英文文档。`)
  } else if (latinCount > 800 && latinCount > cjkCount * 4) {
    failures.push(`${file}: Current 文档英文字符显著压倒中文主叙述（中文 ${cjkCount} / 英文 ${latinCount}）。`)
  }

  const h1 = content.match(/^#\s+(.+)$/m)?.[1]?.trim()
  if (h1 && !/[\u3400-\u9fff]/.test(h1) && !['AGENTS.md', 'README.md'].includes(h1)) {
    failures.push(`${file}: 一级标题必须以中文为主，可在括号中保留英文精确名称；当前为“${h1}”。`)
  }

  for (const paragraph of narrativeParagraphs(content)) {
    const paragraphLatin = (paragraph.match(latinRe) || []).length
    const paragraphCjk = (paragraph.match(cjkRe) || []).length
    if (paragraphLatin >= 160 && paragraphCjk === 0) {
      const preview = paragraph.replace(/\s+/g, ' ').slice(0, 100)
      failures.push(`${file}: 存在纯英文长段落：“${preview}${paragraph.length > 100 ? '…' : ''}”`)
      break
    }
  }

  for (const match of content.matchAll(/docs\/[A-Za-z0-9_.\/-]+\.md/g)) {
    const ref = match[0]
    if (allowedMissingProvenance.has(ref)) continue
    if (!fs.existsSync(path.join(root, ref))) {
      failures.push(`${file}: 引用了不存在的本地文档 ${ref}`)
    }
  }

  if (/Current Ready Execution Unit\s*:/i.test(content) && file !== 'docs/work/current/README.md') {
    warnings.push(`${file}: 仍包含 Current Ready Execution Unit 字样；确认它不是第二份 Current State truth。`)
  }
}

console.log(`文档治理扫描：${files.length} 个 Current Markdown 文档`)
if (warnings.length) {
  console.log('\n警告：')
  for (const warning of warnings) console.log(`- ${warning}`)
}
if (failures.length) {
  console.error('\n失败：')
  for (const failure of failures) console.error(`- ${failure}`)
  process.exit(1)
}
console.log('\nPASS：Current 文档满足中文主语言与本地引用完整性基线。')
