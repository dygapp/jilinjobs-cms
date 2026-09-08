import * as cheerio from 'cheerio'
import { readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'

const configPath = path.resolve(process.env.MAIN_SOURCE_CONFIG || 'main/source-surfaces.json')
const inventoryRoot = path.resolve(process.env.MAIN_INVENTORY_ROOT || 'main/v1/generated-inventory')
const outputRoot = path.resolve(process.env.MIGRATION_OUTPUT || 'main/v1/generated-candidate')

const config = JSON.parse(await readFile(configPath, 'utf8'))
const inventory = JSON.parse(await readFile(path.join(inventoryRoot, 'inventory.json'), 'utf8'))
const issuesPath = path.join(outputRoot, 'reports', 'issues.json')
const issues = JSON.parse(await readFile(issuesPath, 'utf8'))
const allowedContentHosts = new Set(config.allowedHosts || [new URL(config.sourceRoot).host])

const auditCodes = new Set([
  'AMBIGUOUS_INTERNAL_HOST_EXTERNAL_LINK',
  'BODY_LINK_URL_INVALID',
  'JAVASCRIPT_BODY_LINK_REQUIRES_REVIEW',
  'LEGACY_DYNAMIC_ATTACHMENT_LINK_REQUIRES_REVIEW',
  'RELATIVE_BODY_LINK_REQUIRES_REVIEW',
  'UNSUPPORTED_BODY_LINK_SCHEME_REQUIRES_REVIEW',
  'UNMIGRATED_EMBEDDED_MEDIA_REQUIRES_REVIEW',
  'CSS_RESOURCE_REFERENCE_REQUIRES_REVIEW',
  'SRCSET_RESOURCE_REFERENCE_REQUIRES_REVIEW',
])

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

function isLegacyDynamicAttachment(parsed) {
  return /\/common\/downloadfile\.aspx$/i.test(parsed.pathname) && parsed.searchParams.has('fileid')
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
  const article = JSON.parse(await readFile(path.join(outputRoot, reference.path), 'utf8'))
  if (article?.target?.articleType !== 'INTERNAL') continue

  const legacyKey = article.source.legacyKey
  const sourceUrl = article.source.url
  const bodyHtml = String(article?.content?.bodyHtml || '')
  const $ = cheerio.load(bodyHtml, null, false)

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

    if (isLegacyDynamicAttachment(parsed)) {
      addAuditIssue('LEGACY_DYNAMIC_ATTACHMENT_LINK_REQUIRES_REVIEW', {
        legacyKey,
        evidenceKey: legacyKey,
        sourceUrl,
        rawReference,
        resolvedUrl: parsed.toString(),
        message: 'Legacy Common/DownLoadFile.aspx attachment route is not yet represented as a collected canonical attachment',
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
const added = issues.filter(issue => auditCodes.has(issue.code))
const byCode = Object.fromEntries([...auditCodes].map(code => [code, added.filter(issue => issue.code === code).length]).filter(([, count]) => count > 0))
console.log(`EU50_MAIN_CANDIDATE_AUDIT ${JSON.stringify({ auditIssues: added.length, byCode })}`)
