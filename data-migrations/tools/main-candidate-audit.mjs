import * as cheerio from 'cheerio'
import { createHash } from 'node:crypto'
import { readFile, rm, writeFile } from 'node:fs/promises'
import path from 'node:path'

const configPath = path.resolve(process.env.MAIN_SOURCE_CONFIG || 'main/source-surfaces.json')
const inventoryRoot = path.resolve(process.env.MAIN_INVENTORY_ROOT || 'main/v1/generated-inventory')
const outputRoot = path.resolve(process.env.MIGRATION_OUTPUT || 'main/v1/generated-candidate')

const config = JSON.parse(await readFile(configPath, 'utf8'))
const inventory = JSON.parse(await readFile(path.join(inventoryRoot, 'inventory.json'), 'utf8'))
const inventoryByLegacyKey = new Map(inventory.map(item => [item.legacyKey, item]))
const issuesPath = path.join(outputRoot, 'reports', 'issues.json')
const rawIssues = JSON.parse(await readFile(issuesPath, 'utf8'))
const allowedContentHosts = new Set(config.allowedHosts || [new URL(config.sourceRoot).host])
const sha256 = value => createHash('sha256').update(value).digest('hex')

const auditCodes = new Set([
  'AMBIGUOUS_INTERNAL_HOST_EXTERNAL_LINK',
  'BODY_LINK_URL_INVALID',
  'BODY_LINK_HOST_INVALID_REQUIRES_REVIEW',
  'BODY_CONTROL_CHARACTER_REQUIRES_REVIEW',
  'JAVASCRIPT_BODY_LINK_REQUIRES_REVIEW',
  'RELATIVE_BODY_LINK_REQUIRES_REVIEW',
  'UNSUPPORTED_BODY_LINK_SCHEME_REQUIRES_REVIEW',
  'UNMIGRATED_EMBEDDED_MEDIA_REQUIRES_REVIEW',
  'CSS_RESOURCE_REFERENCE_REQUIRES_REVIEW',
  'SRCSET_RESOURCE_REFERENCE_REQUIRES_REVIEW',
  'MALFORMED_BODY_HTML_REQUIRES_REVIEW',
])

function isLegacyDynamicAttachment(parsed) {
  return /\/common\/downloadfile\.aspx$/i.test(parsed.pathname) && parsed.searchParams.has('fileid')
}

function legacyDynamicReference(value, baseUrl = config.sourceRoot) {
  if (!value) return null
  try {
    const parsed = new URL(value, baseUrl)
    return isLegacyDynamicAttachment(parsed) ? parsed : null
  } catch {
    return null
  }
}

const legacyAttachmentEvidence = new Map()
function recordLegacyAttachment(detail) {
  const rawReference = String(detail.rawReference || '')
  const key = `${detail.legacyKey || detail.evidenceKey || ''}\u0000${rawReference}`
  const previous = legacyAttachmentEvidence.get(key)
  legacyAttachmentEvidence.set(key, {
    classification: 'LEGACY_ATTACHMENT_MIGRATION_RESIDUE',
    decision: 'PRESERVE_ORIGINAL_LINK_NON_BLOCKING',
    legacyKey: detail.legacyKey || detail.evidenceKey || null,
    title: detail.title || previous?.title || null,
    sourceUrl: detail.sourceUrl || previous?.sourceUrl || null,
    rawReference,
    resolvedUrl: detail.resolvedUrl || previous?.resolvedUrl || null,
    collectorOutcome: detail.collectorOutcome || previous?.collectorOutcome || 'PRESERVED_ORIGINAL',
    collectorMessage: detail.collectorMessage || previous?.collectorMessage || null,
    note: 'Historical attachment-migration residue from the legacy Common/DownLoadFile.aspx route; original body href is preserved and this evidence does not block Article migration.',
  })
}

const issues = []
for (const issue of rawIssues) {
  if (issue.code === 'ATTACHMENT_FETCH_FAILED') {
    const parsed = legacyDynamicReference(issue.rawReference)
    if (parsed) {
      const item = inventoryByLegacyKey.get(issue.legacyKey || issue.evidenceKey)
      recordLegacyAttachment({
        legacyKey: issue.legacyKey || issue.evidenceKey,
        title: item?.title || null,
        sourceUrl: item?.url || null,
        rawReference: issue.rawReference,
        resolvedUrl: parsed.toString(),
        collectorOutcome: 'COLLECTION_FAILED_NON_BLOCKING',
        collectorMessage: issue.message || null,
      })
      continue
    }
  }
  issues.push(issue)
}

const existing = new Set(
  issues
    .filter(issue => auditCodes.has(issue.code))
    .map(issue => `${issue.code}\u0000${issue.legacyKey || issue.evidenceKey || ''}\u0000${issue.rawReference || issue.sourceUrl || ''}`),
)

function addAuditIssue(code, detail) {
  const key = `${code}\u0000${detail.legacyKey || detail.evidenceKey || ''}\u0000${detail.rawReference || detail.sourceUrl || ''}`
  if (existing.has(key)) return
  existing.add(key)
  issues.push({ level: 'error', code, ...detail })
}

function isRelativeReference(value) {
  return !/^[a-zA-Z][a-zA-Z\d+.-]*:/.test(value) && !value.startsWith('//')
}

function hasInvalidHostname(parsed) {
  const hostname = String(parsed.hostname || '')
  return !hostname || /[,\s_\\]/.test(hostname) || hostname.includes('..')
}

function malformedHtmlEvidence($) {
  const findings = []
  const validTagName = /^[a-z][a-z0-9:-]*$/i
  const validAttributeName = /^[a-z_:][a-z0-9_:.-]*$/i

  for (const element of $('*').toArray()) {
    const tagName = String(element.tagName || element.name || '').toLowerCase()
    const invalidAttributes = Object.keys(element.attribs || {}).filter(name => !validAttributeName.test(name))
    if (validTagName.test(tagName) && invalidAttributes.length === 0) continue
    findings.push({
      tagName: tagName || null,
      invalidAttributes,
    })
  }
  return findings
}

function controlCharacterEvidence(value) {
  const counts = new Map()
  for (const character of value) {
    const codePoint = character.codePointAt(0)
    const invalid = character === '\uFFFD'
      || codePoint === 0x7F
      || (codePoint < 0x20 && !['\n', '\r', '\t'].includes(character))
      || (codePoint >= 0x80 && codePoint <= 0x9F)
    if (!invalid) continue
    const key = `U+${codePoint.toString(16).toUpperCase().padStart(4, '0')}`
    counts.set(key, (counts.get(key) || 0) + 1)
  }
  return Object.fromEntries(counts)
}

for (const item of inventory) {
  if (item.articleType !== 'EXTERNAL_LINK') continue
  let parsed
  try { parsed = new URL(item.url) } catch { continue }
  if (!allowedContentHosts.has(parsed.host)) continue
  addAuditIssue('AMBIGUOUS_INTERNAL_HOST_EXTERNAL_LINK', {
    legacyKey: item.legacyKey,
    evidenceKey: item.legacyKey,
    title: item.title,
    sourceUrl: item.url,
    host: parsed.host,
    pathname: parsed.pathname,
    message: 'EXTERNAL_LINK uses an approved Legacy Source content host but does not match the canonical internal Article route',
  })
}

const indexText = await readFile(path.join(outputRoot, 'index.ndjson'), 'utf8')
const articleRefs = indexText.split(/\r?\n/).filter(Boolean).map(line => JSON.parse(line))

for (const reference of articleRefs) {
  const articlePath = path.join(outputRoot, reference.path)
  const article = JSON.parse(await readFile(articlePath, 'utf8'))
  if (article?.target?.articleType !== 'INTERNAL') continue

  const legacyKey = article.source.legacyKey
  const sourceUrl = article.source.url
  const item = inventoryByLegacyKey.get(legacyKey)
  let bodyHtml = String(article?.content?.bodyHtml || '')

  const dynamicResources = []
  const keptResources = []
  for (const resource of article.resources || []) {
    const parsed = resource.role === 'ATTACHMENT'
      ? legacyDynamicReference(resource.originalReference, sourceUrl)
      : null
    if (!parsed) {
      keptResources.push(resource)
      continue
    }
    dynamicResources.push({ resource, parsed })
  }

  if (dynamicResources.length > 0) {
    for (const { resource, parsed } of dynamicResources) {
      if (resource.token && resource.originalReference) bodyHtml = bodyHtml.replaceAll(resource.token, resource.originalReference)
      recordLegacyAttachment({
        legacyKey,
        title: item?.title || article.content?.title || null,
        sourceUrl,
        rawReference: resource.originalReference,
        resolvedUrl: parsed.toString(),
        collectorOutcome: 'COLLECTED_THEN_DISCARDED_BY_POLICY',
      })
    }
    article.content.bodyHtml = bodyHtml
    article.resources = keptResources
    article.sourceFingerprint = sha256(JSON.stringify({
      identity: legacyKey,
      target: article.target.columnAlias,
      content: article.content,
      resources: keptResources.map(resource => [resource.role, resource.sha256]).sort(),
    }))
    await writeFile(articlePath, JSON.stringify(article, null, 2) + '\n', 'utf8')

    const remainingSnapshots = new Set(keptResources.map(resource => resource.snapshotPath).filter(Boolean))
    for (const { resource } of dynamicResources) {
      if (!resource.snapshotPath || remainingSnapshots.has(resource.snapshotPath)) continue
      await rm(path.join(path.dirname(articlePath), resource.snapshotPath), { force: true })
    }
  }

  const $ = cheerio.load(bodyHtml, null, false)

  const controlCharacters = controlCharacterEvidence(bodyHtml)
  if (Object.keys(controlCharacters).length > 0) {
    addAuditIssue('BODY_CONTROL_CHARACTER_REQUIRES_REVIEW', {
      legacyKey,
      evidenceKey: legacyKey,
      sourceUrl,
      controlCharacters,
      message: 'Legacy Source body contains replacement/control characters and requires an explicit content decision',
    })
  }

  const malformed = malformedHtmlEvidence($)
  if (malformed.length > 0) {
    addAuditIssue('MALFORMED_BODY_HTML_REQUIRES_REVIEW', {
      legacyKey,
      evidenceKey: legacyKey,
      sourceUrl,
      malformedNodeCount: malformed.length,
      examples: malformed.slice(0, 20),
      message: 'parsed Legacy Source body contains malformed tag or attribute names and requires an explicit content decision',
    })
  }

  for (const anchor of $('a[href]').toArray()) {
    const rawReference = String($(anchor).attr('href') || '').trim()
    if (!rawReference || rawReference.startsWith('#') || /^mailto:/i.test(rawReference) || /^tel:/i.test(rawReference) || /^migration-attachment:/i.test(rawReference)) continue

    if (/^javascript:/i.test(rawReference)) {
      addAuditIssue('JAVASCRIPT_BODY_LINK_REQUIRES_REVIEW', {
        legacyKey,
        evidenceKey: legacyKey,
        sourceUrl,
        rawReference,
        message: 'javascript: body link is preserved by source HTML and requires an explicit migration decision',
      })
      continue
    }

    let parsed
    try { parsed = new URL(rawReference, sourceUrl) }
    catch (error) {
      addAuditIssue('BODY_LINK_URL_INVALID', {
        legacyKey,
        evidenceKey: legacyKey,
        sourceUrl,
        rawReference,
        message: String(error),
      })
      continue
    }

    if (hasInvalidHostname(parsed)) {
      addAuditIssue('BODY_LINK_HOST_INVALID_REQUIRES_REVIEW', {
        legacyKey,
        evidenceKey: legacyKey,
        sourceUrl,
        rawReference,
        hostname: parsed.hostname || null,
        message: 'body link contains a malformed hostname and requires an explicit source-data decision',
      })
      continue
    }

    if (isLegacyDynamicAttachment(parsed)) {
      recordLegacyAttachment({
        legacyKey,
        title: item?.title || article.content?.title || null,
        sourceUrl,
        rawReference,
        resolvedUrl: parsed.toString(),
        collectorOutcome: 'PRESERVED_ORIGINAL',
      })
      continue
    }

    if (isRelativeReference(rawReference)) {
      addAuditIssue('RELATIVE_BODY_LINK_REQUIRES_REVIEW', {
        legacyKey,
        evidenceKey: legacyKey,
        sourceUrl,
        rawReference,
        resolvedUrl: parsed.toString(),
        message: 'relative body link would otherwise remain source-relative in the accepted snapshot',
      })
      continue
    }

    if (!['http:', 'https:', 'mailto:', 'tel:', 'migration-attachment:'].includes(parsed.protocol)) {
      addAuditIssue('UNSUPPORTED_BODY_LINK_SCHEME_REQUIRES_REVIEW', {
        legacyKey,
        evidenceKey: legacyKey,
        sourceUrl,
        rawReference,
        scheme: parsed.protocol,
        message: 'body link scheme is not covered by the current migration contract',
      })
    }
  }

  for (const element of $('video,audio,source,track').toArray()) {
    const current = $(element)
    const rawReference = String(current.attr('src') || current.attr('poster') || '').trim()
    addAuditIssue('UNMIGRATED_EMBEDDED_MEDIA_REQUIRES_REVIEW', {
      legacyKey,
      evidenceKey: legacyKey,
      sourceUrl,
      rawReference: rawReference || `<${String(element.tagName || element.name || '').toLowerCase()}>`,
      tagName: String(element.tagName || element.name || '').toLowerCase() || null,
      message: 'embedded media is preserved by source HTML but is not collected as a canonical migration resource',
    })
  }

  for (const element of $('[style]').toArray()) {
    const style = String($(element).attr('style') || '')
    if (!/url\s*\(/i.test(style)) continue
    addAuditIssue('CSS_RESOURCE_REFERENCE_REQUIRES_REVIEW', {
      legacyKey,
      evidenceKey: legacyKey,
      sourceUrl,
      rawReference: style,
      message: 'inline CSS contains a resource URL that is not localized by the current collector',
    })
  }

  for (const element of $('[srcset]').toArray()) {
    const srcset = String($(element).attr('srcset') || '').trim()
    if (!srcset) continue
    addAuditIssue('SRCSET_RESOURCE_REFERENCE_REQUIRES_REVIEW', {
      legacyKey,
      evidenceKey: legacyKey,
      sourceUrl,
      rawReference: srcset,
      message: 'srcset resource references are not localized by the current collector',
    })
  }
}

await writeFile(issuesPath, JSON.stringify(issues, null, 2) + '\n', 'utf8')
const legacyAttachmentRecords = [...legacyAttachmentEvidence.values()].sort((a, b) => `${a.legacyKey || ''}:${a.rawReference}`.localeCompare(`${b.legacyKey || ''}:${b.rawReference}`))
await writeFile(
  path.join(outputRoot, 'reports', 'legacy-attachment-residue.json'),
  JSON.stringify({
    generatedAt: new Date().toISOString(),
    classification: 'LEGACY_ATTACHMENT_MIGRATION_RESIDUE',
    policy: {
      bodyMigration: 'CONTINUE',
      imageMigration: 'CONTINUE',
      attachmentLink: 'PRESERVE_ORIGINAL_HREF',
      blocking: false,
      followUp: 'RETAIN_FOR_LATER_REMEDIATION_OR_CLIENT_CONFIRMATION',
    },
    count: legacyAttachmentRecords.length,
    records: legacyAttachmentRecords,
  }, null, 2) + '\n',
  'utf8',
)
const legacyMarkdown = [
  '# Legacy attachment migration residue',
  '',
  '> `Common/DownLoadFile.aspx?fileid=...` links are preserved as historical migration residue and do not block Article migration.',
  '',
  `- Classification: LEGACY_ATTACHMENT_MIGRATION_RESIDUE`,
  `- Records: ${legacyAttachmentRecords.length}`,
  `- Policy: preserve original href; migrate body and images; do not block accepted snapshot solely for this issue.`,
  '',
  '| Article | Title | Original href | Collector outcome |',
  '|---|---|---|---|',
  ...legacyAttachmentRecords.map(row => `| ${String(row.legacyKey || '').replaceAll('|', '\\|')} | ${String(row.title || '').replaceAll('|', '\\|')} | ${String(row.rawReference || '').replaceAll('|', '\\|')} | ${row.collectorOutcome} |`),
  '',
].join('\n')
await writeFile(path.join(outputRoot, 'reports', 'legacy-attachment-residue.md'), legacyMarkdown, 'utf8')

const added = issues.filter(issue => auditCodes.has(issue.code))
const byCode = Object.fromEntries([...auditCodes].map(code => [code, added.filter(issue => issue.code === code).length]).filter(([, count]) => count > 0))
console.log(`EU50_MAIN_CANDIDATE_AUDIT ${JSON.stringify({ auditIssues: added.length, byCode, legacyAttachmentResidues: legacyAttachmentRecords.length })}`)
