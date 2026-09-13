import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()

// 这些文件已被 docs/README.md 明确降级，不再参与 Current Authority / locator 检查。
// 注意：它们仍参与全仓中文主语言检查。
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

// 仅真正的原始/机器证据可以豁免中文主叙述；普通 archive / work history 不豁免。
const rawEvidenceLanguageExempt = new Set([])

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

function collectMarkdown(target, { skipArchive = false } = {}) {
  const absolute = path.join(root, target)
  if (!fs.existsSync(absolute)) return []
  const stat = fs.statSync(absolute)
  if (stat.isFile()) return target.endsWith('.md') ? [target] : []
  const result = []
  for (const entry of fs.readdirSync(absolute, { withFileTypes: true })) {
    if (skipArchive && entry.isDirectory() && entry.name === 'archive') continue
    const rel = path.posix.join(target, entry.name)
    if (entry.isDirectory()) result.push(...collectMarkdown(rel, { skipArchive }))
    else if (entry.name.endsWith('.md')) result.push(rel)
  }
  return result
}

const currentFiles = [...new Set(currentRoots.flatMap((p) => collectMarkdown(p, { skipArchive: true })))]
  .filter((file) => !historicalCurrentPaths.has(file))
  .sort()

const languageFiles = [...new Set([
  'AGENTS.md',
  'README.md',
  ...collectMarkdown('docs'),
])].filter((file) => !rawEvidenceLanguageExempt.has(file)).sort()

const failures = []
const warnings = []
const cjkRe = /[\u3400-\u9fff]/g
const latinRe = /[A-Za-z]/g

function stripNonNarrative(content) {
  return content
    .replace(/^---\n[\s\S]*?\n---\n?/, '')
    .replace(/```[\s\S]*?```/g, '')
    .replace(/`[^`]*`/g, '')
    .replace(/https?:\/\/\S+/g, '')
    .replace(/\[[^\]]*\]\([^)]*\)/g, '')
}

function narrativeParagraphs(content) {
  return stripNonNarrative(content)
    .split(/\n\s*\n/)
    .map((p) => p
      .split('\n')
      .filter((line) => !/^\s*[|>-]/.test(line) && !/^\s*#{1,6}\s+/.test(line))
      .join(' ')
      .trim())
    .filter(Boolean)
}

function addFailure(file, message) {
  failures.push(`${file}: ${message}`)
  // GitHub Actions annotation，便于远程治理时直接读取具体失败项。
  const escaped = message.replace(/%/g, '%25').replace(/\r/g, '%0D').replace(/\n/g, '%0A')
  console.error(`::error file=${file}::${escaped}`)
}

// 仓库级语言规则：所有项目维护 Markdown（包含 archive）都必须中文主述。
for (const file of languageFiles) {
  const content = fs.readFileSync(path.join(root, file), 'utf8')
  const narrative = stripNonNarrative(content)
  const cjkCount = (narrative.match(cjkRe) || []).length
  const latinCount = (narrative.match(latinRe) || []).length

  if (cjkCount === 0 && latinCount > 80) {
    addFailure(file, '文档没有中文主叙述，属于纯英文文档。')
  } else if (latinCount > 1000 && latinCount > cjkCount * 4) {
    addFailure(file, `英文字符显著压倒中文主叙述（中文 ${cjkCount} / 英文 ${latinCount}）。`)
  }

  const h1 = content.match(/^#\s+(.+)$/m)?.[1]?.trim()
  if (h1 && !/[\u3400-\u9fff]/.test(h1) && !['AGENTS.md', 'README.md'].includes(h1)) {
    addFailure(file, `一级标题必须以中文为主，可在括号中保留英文精确名称；当前为“${h1}”。`)
  }

  for (const paragraph of narrativeParagraphs(content)) {
    const paragraphLatin = (paragraph.match(latinRe) || []).length
    const paragraphCjk = (paragraph.match(cjkRe) || []).length
    if (paragraphLatin >= 180 && paragraphCjk === 0) {
      const preview = paragraph.replace(/\s+/g, ' ').slice(0, 100)
      addFailure(file, `存在纯英文长段落：“${preview}${paragraph.length > 100 ? '…' : ''}”`)
      break
    }
  }
}

// Current Authority / locator 规则只检查 Current 读取集合，历史证据允许保留历史 locator。
for (const file of currentFiles) {
  const content = fs.readFileSync(path.join(root, file), 'utf8')

  for (const match of content.matchAll(/docs\/[A-Za-z0-9_.\/-]+\.md/g)) {
    const ref = match[0]
    if (allowedMissingProvenance.has(ref)) continue
    if (!fs.existsSync(path.join(root, ref))) {
      addFailure(file, `引用了不存在的本地文档 ${ref}`)
    }
  }

  if (/Current Ready Execution Unit\s*:/i.test(content) && file !== 'docs/work/current/README.md') {
    warnings.push(`${file}: 仍包含 Current Ready Execution Unit 字样；确认它不是第二份 Current State truth。`)
  }
}

console.log(`文档语言扫描：${languageFiles.length} 个 Markdown 文档`)
console.log(`Current Authority / locator 扫描：${currentFiles.length} 个 Markdown 文档`)

if (warnings.length) {
  console.log('\n警告：')
  for (const warning of warnings) console.log(`- ${warning}`)
}

if (failures.length) {
  console.error('\n失败：')
  for (const failure of failures) console.error(`- ${failure}`)
  process.exit(1)
}

console.log('\nPASS：项目 Markdown 满足中文主语言基线，Current 文档满足本地引用完整性基线。')
