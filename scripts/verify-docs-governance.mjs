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
  'docs/architecture',
  'docs/requirements',
  'docs/specifications',
  'docs/technical',
  'docs/work/README.md',
  'docs/work/current',
  'docs/design',
  'docs/governance',
  'frontend/AGENTS.md',
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
const currentDocFiles = collectMarkdown('docs', { skipArchive: true })
const languageFiles = currentFiles
const failures = []
const warnings = []
const cjkRe = /[\u3400-\u9fff]/g
const latinRe = /[A-Za-z]/g
const designMdCanonicalHeadings = new Set([
  'Overview',
  'Colors',
  'Typography',
  'Layout',
  'Elevation & Depth',
  'Shapes',
  'Components',
  "Do's and Don'ts",
])

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

// 文件头规则由 docs/governance/constraints.md 持有；archive 不进入 currentFiles。
// README、Guide、Governance 说明可以没有文件头，DESIGN.md 使用设计工具自己的 schema。
const authorityHeaderRules = {
  architecture: { types: ['architecture', 'architecture-state'], status: 'active' },
  project: { types: ['project'], status: 'active' },
  requirements: { types: ['business-requirement', 'domain-requirement'], status: 'confirmed' },
  specifications: { types: ['specification'], status: 'accepted' },
  technical: { types: ['technical-contract', 'technical-strategy'], status: 'active' },
}
const seenIds = new Map()
let checkedHeaders = 0
let checkedAdrs = 0

for (const file of currentDocFiles) {
  const content = fs.readFileSync(path.join(root, file), 'utf8')
  const lines = content.split(/\r?\n/)
  const decisionFile = file.startsWith('docs/architecture/decisions/')
  const adrNumber = file.match(/^docs\/architecture\/decisions\/ADR-(\d{4})-[^/]+\.md$/)?.[1]
  if (decisionFile) {
    checkedAdrs++
    const opening = lines.slice(1, 8).join('\n')
    if (!adrNumber) addFailure(file, '决策文件名应为 ADR-NNNN-说明.md。')
    if (lines[0] === '---') addFailure(file, 'ADR 使用标题、状态与日期条目，不使用 YAML 文件头。')
    if (adrNumber && !new RegExp(`^# ADR-${adrNumber}[：:]`).test(lines[0])) addFailure(file, 'ADR 标题编号必须与文件名一致。')
    if (!/^- (?:状态|Status)[：:]\s*\S+/m.test(opening)) addFailure(file, 'ADR 开头缺少状态条目。')
    if (!/^- (?:日期|Date)[：:]\s*\d{4}-\d{2}-\d{2}\b/m.test(opening)) addFailure(file, 'ADR 开头缺少 YYYY-MM-DD 日期条目。')
    continue
  }

  const area = file.match(/^docs\/(architecture|project|requirements|specifications|technical)\/([^/]+)\.md$/)
  const rule = area && area[2] !== 'README' ? authorityHeaderRules[area[1]] : null
  const required = file === 'docs/requirements/index.md'
    ? { types: ['requirement-index'], status: 'active' }
    : rule
  const design = file === 'docs/design/public-site/DESIGN.md'
  if (lines[0] !== '---') {
    if (required || design) addFailure(file, '当前 Authority 或设计文档缺少 YAML 文件头。')
    continue
  }

  const closing = lines.indexOf('---', 1)
  if (closing < 0) {
    addFailure(file, 'YAML 文件头缺少结束分隔符。')
    continue
  }
  checkedHeaders++
  const fields = new Map()
  for (const line of lines.slice(1, closing)) {
    if (!line.trim() || /^\s/.test(line) || line.startsWith('#')) continue
    const match = line.match(/^([A-Za-z][A-Za-z0-9_-]*):(?:\s*(.*))?$/)
    if (!match) {
      addFailure(file, `无法识别的文件头顶层字段：${line}`)
      continue
    }
    if (fields.has(match[1])) addFailure(file, `文件头字段重复：${match[1]}`)
    fields.set(match[1], (match[2] || '').trim())
  }

  if (required) {
    for (const key of ['id', 'type', 'status']) {
      if (!fields.get(key)) addFailure(file, `当前 Authority 文件头缺少非空 ${key}。`)
    }
    if (fields.get('type') && !required.types.includes(fields.get('type'))) {
      addFailure(file, `type 应为 ${required.types.join(' 或 ')}，当前为 ${fields.get('type')}。`)
    }
    if (fields.get('status') && fields.get('status') !== required.status) {
      addFailure(file, `status 应为 ${required.status}，当前为 ${fields.get('status')}。`)
    }
    const id = fields.get('id')
    if (id) {
      if (seenIds.has(id)) addFailure(file, `id ${id} 与 ${seenIds.get(id)} 重复。`)
      else seenIds.set(id, file)
    }
  }
  if (design) {
    for (const key of ['version', 'name', 'description', 'colors', 'typography']) {
      if (!fields.has(key) || (!['colors', 'typography'].includes(key) && !fields.get(key))) {
        addFailure(file, `设计文件头缺少 ${key}。`)
      }
    }
  }
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

  const skillName = file.endsWith('/SKILL.md') ? path.posix.basename(path.posix.dirname(file)) : null
  for (const match of content.matchAll(/^(#{1,6})\s+(.+)$/gm)) {
    const heading = match[2].trim()
    const standardizedAgentTitle = file === 'AGENTS.md' && heading === 'AGENTS.md'
    const exactSkillTitle = skillName && heading === skillName
    const exactCodeTitle = /^\`[^\`]+\`$/.test(heading)
    const stableTokenTitle = /^[A-Z][A-Z0-9_-]*$/.test(heading)
    const canonicalDesignHeading = file === 'docs/design/public-site/DESIGN.md' && designMdCanonicalHeadings.has(heading)
    if (
      !standardizedAgentTitle &&
      !exactSkillTitle &&
      !exactCodeTitle &&
      !stableTokenTitle &&
      !canonicalDesignHeading &&
      !/[\u3400-\u9fff]/.test(heading)
    ) {
      addFailure(file, `面向人的结构标题必须以中文为主；精确机器标识、Skill 名或稳定状态值可保留原样。当前为“${heading}”。`)
    }
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
const readyUnit = currentLocator.match(/Current Ready Execution Unit[：:]\s*\*\*([^*]+)\*\*/)?.[1]?.trim()
const currentWorkFiles = collectMarkdown('docs/work/current').filter(file => file !== 'docs/work/current/README.md')
if (!readyUnit) {
  addFailure('docs/work/current/README.md', '缺少可解析的 Current Ready Execution Unit。')
} else if (readyUnit === 'NONE') {
  if (currentWorkFiles.length) addFailure('docs/work/current/README.md', 'Current Ready Execution Unit 为 NONE 时不得保留 active work artifact。')
} else {
  const activeArtifact = currentLocator.match(/当前工作 artifact[：:]\s*`([^`]+\.md)`/)?.[1]
  if (!activeArtifact) {
    addFailure('docs/work/current/README.md', '存在 active Execution Unit 时必须定位当前工作 artifact。')
  } else if (!fs.existsSync(path.join(root, 'docs/work/current', activeArtifact))) {
    addFailure('docs/work/current/README.md', `当前工作 artifact 不存在：${activeArtifact}`)
  }
}

console.log(`Current 文档语言扫描：${languageFiles.length} 个 Markdown 文档`)
console.log(`Current Authority / locator 扫描：${currentFiles.length} 个 Markdown 文档`)
console.log(`docs/** 当前文件头适用范围：${currentDocFiles.length} 个；YAML 文件头：${checkedHeaders} 个；ADR：${checkedAdrs} 个`)

if (warnings.length) {
  console.log('\n警告：')
  for (const warning of warnings) console.log(`- ${warning}`)
}

if (failures.length) {
  console.error('\n失败：')
  for (const failure of failures) console.error(`- ${failure}`)
  process.exit(1)
}

console.log('\nPASS：Current Markdown 满足文件头、ADR、中文主语言与本地引用完整性基线。')
