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
  'docs/work',
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

// 文件头规则由 docs/governance/constraints.md 持有；archive 不进入 currentDocFiles。
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
  if (decisionFile) checkedAdrs++
  if (decisionFile && !adrNumber) addFailure(file, '决策文件名应为 ADR-NNNN-说明.md。')

  const area = file.match(/^docs\/(architecture|project|requirements|specifications|technical)\/([^/]+)\.md$/)
  const rule = area && area[2] !== 'README' ? authorityHeaderRules[area[1]] : null
  const required = decisionFile
    ? { types: ['architecture-decision'], status: 'accepted' }
    : file === 'docs/requirements/index.md'
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
  if (decisionFile) {
    if (fields.get('id') !== `ADR-${adrNumber}`) addFailure(file, 'ADR id 必须与文件名编号一致。')
    if (!/^\d{4}-\d{2}-\d{2}$/.test(fields.get('date') || '')) addFailure(file, 'ADR date 必须为 YYYY-MM-DD。')
    const firstBodyLine = lines.slice(closing + 1).find(line => line.trim()) || ''
    if (!new RegExp(`^# ADR-${adrNumber}[：:]`).test(firstBodyLine)) addFailure(file, 'ADR 标题编号必须与文件名一致。')
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

}

// docs/work 根目录是 Execution Unit working set；状态来自 Unit 文件头，archive 只承担历史冷存储。
const workUnitStatuses = new Set(['planned', 'ready', 'active', 'blocked', 'completed'])
const workUnitFiles = collectMarkdown('docs/work', { skipArchive: true }).filter(file => file !== 'docs/work/README.md')
for (const file of workUnitFiles) {
  if (!/^docs\/work\/[^/]+\.md$/.test(file)) {
    addFailure(file, 'Execution Unit working set 必须直接位于 docs/work/ 根目录。')
    continue
  }

  const content = fs.readFileSync(path.join(root, file), 'utf8')
  const lines = content.split(/\r?\n/)
  if (lines[0] !== '---') {
    addFailure(file, 'Execution Unit 缺少 YAML 文件头。')
    continue
  }
  const closing = lines.indexOf('---', 1)
  if (closing < 0) {
    addFailure(file, 'Execution Unit YAML 文件头缺少结束分隔符。')
    continue
  }

  const fields = new Map()
  for (const line of lines.slice(1, closing)) {
    if (!line.trim() || /^\s/.test(line) || line.startsWith('#')) continue
    const match = line.match(/^([A-Za-z][A-Za-z0-9_-]*):(?:\s*(.*))?$/)
    if (!match) {
      addFailure(file, `Execution Unit 文件头无法识别字段：${line}`)
      continue
    }
    if (fields.has(match[1])) addFailure(file, `Execution Unit 文件头字段重复：${match[1]}`)
    fields.set(match[1], (match[2] || '').trim())
  }

  for (const key of ['id', 'type', 'status']) {
    if (!fields.get(key)) addFailure(file, `Execution Unit 文件头缺少非空 ${key}。`)
  }
  if (fields.get('type') && fields.get('type') !== 'execution-unit') {
    addFailure(file, `Execution Unit type 应为 execution-unit，当前为 ${fields.get('type')}。`)
  }
  const status = fields.get('status')
  if (status && !workUnitStatuses.has(status)) {
    addFailure(file, `Execution Unit status 应为 ${[...workUnitStatuses].join(' / ')}，当前为 ${status}。`)
  }
  const id = fields.get('id')
  if (id && !id.startsWith('execution-unit:')) {
    addFailure(file, 'Execution Unit id 必须以 execution-unit: 开头。')
  }
  if (id) {
    if (seenIds.has(id)) addFailure(file, `id ${id} 与 ${seenIds.get(id)} 重复。`)
    else seenIds.set(id, file)
  }
}

console.log(`Current 文档语言扫描：${languageFiles.length} 个 Markdown 文档`)
console.log(`Current Authority / locator 扫描：${currentFiles.length} 个 Markdown 文档`)
console.log(`docs/** 当前文件头适用范围：${currentDocFiles.length} 个；YAML 文件头：${checkedHeaders} 个（含 ${checkedAdrs} 个 ADR）`)

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
