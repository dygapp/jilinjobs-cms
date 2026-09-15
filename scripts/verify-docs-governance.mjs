import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()

const allowedMissingProvenance = new Set([
  'docs/project/project.md',
  'docs/requirements/overview/system-module-boundaries.md',
])

// 这里只列 ordinary Fresh Context 可能读取的 Current Authority / locator 根。
// archive/** 统一由 collectMarkdown(skipArchive=true) 排除，不再维护第二份历史路径例外清单。
const currentRoots = [
  'AGENTS.md',
  'README.md',
  'docs/README.md',
  'docs/project',
  'docs/methods',
  'docs/architecture',
  'docs/rules',
  'docs/requirements',
  'docs/specifications',
  'docs/technical',
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

const currentFiles = [...new Set(currentRoots.flatMap((p) => collectMarkdown(p, { skipArchive: true })))].sort()
const languageFiles = currentFiles
const failures = []
const warnings = []
const cjkRe = /[\u3400-\u9fff]/g
const latinRe = /[A-Za-z]/g

function stripNonNarrative(content) {
  return content
    .replace(/^---\r?\n[\s\S]*?\r?\n---\r?\n?/, '')
    .replace(/```[\s\S]*?```/g, '')
    .replace(/`[^`\n]*`/g, '')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/https?:\/\/\S+/g, '')
    .replace(/<[^>]+>/g, '')
    .replace(/(?:^|\s)(?:[A-Za-z0-9_.-]+\/)+[A-Za-z0-9_./-]+(?=\s|$|[，。；、：)])/g, ' ')
    .replace(/\b[A-Za-z_][A-Za-z0-9_.:-]{2,}\b/g, (token) => {
      if (/[_.:-]/.test(token) || /[A-Z].*[A-Z]/.test(token) || /[a-z][A-Z]/.test(token)) return ' '
      return token
    })
}

function narrativeSegments(content) {
  const result = []
  for (const block of stripNonNarrative(content).split(/\n\s*\n/)) {
    let proseLines = []
    const flushProse = () => {
      const prose = proseLines.join(' ').trim()
      if (prose) result.push(prose)
      proseLines = []
    }

    for (const rawLine of block.split('\n')) {
      const line = rawLine.trim()
      if (!line || /^#{1,6}\s+/.test(line) || /^[|>]/.test(line)) continue
      if (/^[-*+]\s+/.test(line) || /^\d+[.)]\s+/.test(line)) {
        flushProse()
        const item = line.replace(/^([-*+]\s+|\d+[.)]\s+)/, '').trim()
        if (item) result.push(item)
      } else {
        proseLines.push(line)
      }
    }
    flushProse()
  }
  return result
}

function addFailure(file, message) {
  failures.push(`${file}: ${message}`)
  const escaped = message.replace(/%/g, '%25').replace(/\r/g, '%0D').replace(/\n/g, '%0A')
  console.error(`::error file=${file}::${escaped}`)
}

// Current 文档必须中文主述；精确技术标识、路径、命令、枚举与专名不参与机械语言比例判断。
for (const file of languageFiles) {
  const content = fs.readFileSync(path.join(root, file), 'utf8')
  const narrative = stripNonNarrative(content)
  const cjkCount = (narrative.match(cjkRe) || []).length
  const latinCount = (narrative.match(latinRe) || []).length

  if (cjkCount === 0 && latinCount > 80) {
    addFailure(file, '文档没有中文主叙述，属于纯英文文档。')
  }

  const h1 = content.match(/^#\s+(.+)$/m)?.[1]?.trim()
  const standardizedAgentTitle = file === 'AGENTS.md' && h1 === 'AGENTS.md'
  if (h1 && !standardizedAgentTitle && !/[\u3400-\u9fff]/.test(h1)) {
    addFailure(file, `一级标题必须以中文为主，可在括号中保留英文精确名称；当前为“${h1}”。`)
  }

  for (const segment of narrativeSegments(content)) {
    const segmentLatin = (segment.match(latinRe) || []).length
    const segmentCjk = (segment.match(cjkRe) || []).length
    if (segmentLatin >= 160 && segmentCjk < 12) {
      const preview = segment.replace(/\s+/g, ' ').slice(0, 100)
      addFailure(file, `存在英文主导长叙述：“${preview}${segment.length > 100 ? '…' : ''}”`)
      break
    }
  }
}

// Current Authority / locator 的本地 Markdown 引用必须真实存在；archive 历史正文不参与此检查。
for (const file of currentFiles) {
  const content = fs.readFileSync(path.join(root, file), 'utf8')

  for (const match of content.matchAll(/docs\/[A-Za-z0-9_.\/-]+\.md/g)) {
    const ref = match[0]
    if (allowedMissingProvenance.has(ref)) continue
    if (!fs.existsSync(path.join(root, ref))) addFailure(file, `引用了不存在的本地文档 ${ref}`)
  }

  if (/Current Ready Execution Unit\s*:/i.test(content) && file !== 'docs/work/current/README.md') {
    warnings.push(`${file}: 仍包含 Current Ready Execution Unit 字样；确认它不是第二份 Current State truth。`)
  }
}

const currentLocator = fs.readFileSync(path.join(root, 'docs/work/current/README.md'), 'utf8')
if (!/Current Ready Execution Unit[：:]\s*\*\*NONE\*\*/.test(currentLocator)) {
  addFailure('docs/work/current/README.md', '当前治理任务不得改变 Current Ready Execution Unit = NONE。')
}

console.log(`Current 文档语言扫描：${languageFiles.length} 个 Markdown 文档`)
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

console.log('\nPASS：Current Markdown 满足中文主语言与本地引用完整性基线。')
