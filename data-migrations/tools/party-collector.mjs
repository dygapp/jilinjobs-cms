import * as cheerio from 'cheerio'
import { createHash } from 'node:crypto'
import { mkdir, rm, writeFile } from 'node:fs/promises'
import path from 'node:path'

const origin = 'https://24365.jl.smartedu.cn'
const outputRoot = path.resolve(process.env.MIGRATION_OUTPUT || 'party/v1/generated')
const rawRoot = path.join(outputRoot, 'raw')
const assetRoot = path.join(outputRoot, 'assets')
const reportRoot = path.join(outputRoot, 'reports')
const sourceSystem = 'legacy-jilinjobs'
const userAgent = 'jilinjobs-cms-eu29-migration/1.0 (+https://github.com/dygapp/jilinjobs-cms)'
const scopes = [
  { typeCode: 'gcsy', columnAlias: 'party-voice' },
  { typeCode: 'gzdt', columnAlias: 'party-work' },
  { typeCode: 'dgdz', columnAlias: 'party-rules' },
  { typeCode: 'llxx', columnAlias: 'party-study' },
]
const attachmentExtensions = new Set(['pdf', 'doc', 'docx', 'xls', 'xlsx'])
const resourceCache = new Map(), resourceRecords = new Map(), issues = []
const sha256 = value => createHash('sha256').update(value).digest('hex')
const normalizeText = value => String(value ?? '').replace(/\s+/g, ' ').trim()
function normalizeUrl(value, base = origin) { const url = new URL(value, base); url.hash = ''; return url.toString() }
const safeName = value => value.replace(/[^a-zA-Z0-9._-]/g, '_').slice(0, 120)

async function fetchResponse(url, attempts = 3) {
  let lastError
  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    const controller = new AbortController(), timer = setTimeout(() => controller.abort(), 30_000)
    try {
      const response = await fetch(url, { headers: { 'user-agent': userAgent, accept: '*/*' }, redirect: 'follow', signal: controller.signal })
      if (!response.ok) throw new Error(`HTTP ${response.status} ${response.statusText}`)
      return response
    } catch (error) { lastError = error; if (attempt < attempts) await new Promise(resolve => setTimeout(resolve, attempt * 750)) }
    finally { clearTimeout(timer) }
  }
  throw lastError
}
async function fetchText(url) { const response = await fetchResponse(url); return { url: response.url, text: await response.text() } }
function detectMedia(bytes) {
  const b = Buffer.from(bytes), ascii = (start, end) => b.subarray(start, end).toString('ascii')
  if (b.length >= 8 && b.subarray(0, 8).equals(Buffer.from([0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a]))) return { contentType:'image/png', extension:'png' }
  if (b.length >= 2 && b[0] === 0xff && b[1] === 0xd8) return { contentType:'image/jpeg', extension:'jpg' }
  if (ascii(0,6) === 'GIF87a' || ascii(0,6) === 'GIF89a') return { contentType:'image/gif', extension:'gif' }
  if (ascii(0,4) === 'RIFF' && ascii(8,12) === 'WEBP') return { contentType:'image/webp', extension:'webp' }
  if (ascii(0,5) === '%PDF-') return { contentType:'application/pdf', extension:'pdf' }
  if (b.length >= 8 && b.subarray(0,8).equals(Buffer.from([0xd0,0xcf,0x11,0xe0,0xa1,0xb1,0x1a,0xe1]))) return { contentType:'application/x-ole-storage', extension:'bin' }
  if (ascii(0,2) === 'PK') return { contentType:'application/zip', extension:'zip' }
  return { contentType:null, extension:'bin' }
}
function resourceDirectory(role) {
  if (role === 'BODY_IMAGE') return 'body'
  if (role === 'ATTACHMENT') return 'attachments'
  if (role === 'CAROUSEL_IMAGE') return 'carousel'
  throw new Error(`未知资源角色：${role}`)
}
async function collectResource(rawReference, baseUrl, role) {
  const sourceUrl = normalizeUrl(rawReference, baseUrl); if (!/^https?:/.test(sourceUrl)) return null
  const cacheKey = `${role}:${sourceUrl}`; if (resourceCache.has(cacheKey)) return resourceCache.get(cacheKey)
  try {
    const response = await fetchResponse(sourceUrl), bytes = Buffer.from(await response.arrayBuffer())
    if (bytes.length === 0) {
      issues.push({ level:'warning', code:'EMPTY_RESOURCE_SKIPPED', role, sourceUrl })
      resourceCache.set(cacheKey, null)
      return null
    }
    const detected = detectMedia(bytes)
    if (role === 'CAROUSEL_IMAGE' && !detected.contentType?.startsWith('image/')) {
      issues.push({ level:'error', code:'CAROUSEL_IMAGE_MEDIA_INVALID', sourceUrl, detected:detected.contentType })
      resourceCache.set(cacheKey, null)
      return null
    }
    const declared = (response.headers.get('content-type') || '').split(';')[0].trim() || null, hash = sha256(bytes)
    const extension = detected.extension === 'bin' ? path.extname(new URL(sourceUrl).pathname).slice(1).toLowerCase() || 'bin' : detected.extension
    const relativePath = `assets/${resourceDirectory(role)}/${hash}.${safeName(extension)}`
    await mkdir(path.dirname(path.join(outputRoot, relativePath)), { recursive:true }); await writeFile(path.join(outputRoot, relativePath), bytes)
    const token = role === 'BODY_IMAGE' ? `migration-resource://${hash}` : role === 'ATTACHMENT' ? `migration-attachment://${hash}` : null
    const record = { role, sourceUrl, originalReference:rawReference, ...(token ? { token } : {}), snapshotPath:relativePath, sha256:hash, contentType:detected.contentType || declared, declaredContentType:declared, sizeBytes:bytes.length }
    resourceCache.set(cacheKey, record); if (!resourceRecords.has(hash)) resourceRecords.set(hash, record)
    if (detected.contentType && declared && declared !== detected.contentType && !(declared === 'image/jpg' && detected.contentType === 'image/jpeg')) issues.push({ level:'warning', code:'RESOURCE_MEDIA_MISMATCH', sourceUrl, declared, detected:detected.contentType })
    return record
  } catch (error) { issues.push({ level:'error', code:'RESOURCE_FETCH_FAILED', role, sourceUrl, message:String(error) }); return null }
}
function parsePagination(html, typeCode) {
  const $ = cheerio.load(html), text = normalizeText($('.pagination-wrap').first().text()), match = text.match(/共\s*(\d+)\s*页[，,]\s*(\d+)\s*条记录/)
  if (!match) throw new Error(`无法识别 ${typeCode} 分页总量：${text}`)
  return { pageCount:Number(match[1]), total:Number(match[2]), text }
}
function parseListPage(html, typeCode, pageNo) {
  const $ = cheerio.load(html)
  return $('ul.default-list > li.list-item > a.list-item-a').map((_, element) => {
    const anchor=$(element), href=anchor.attr('href'), title=normalizeText(anchor.find('.title').text()), dateText=normalizeText(anchor.find('.date').text())
    if (!href || !title) return null
    let url; try { url=normalizeUrl(href) } catch { return null }
    return { typeCode, pageNo, title, publishDate:/^\d{4}-\d{2}-\d{2}$/.test(dateText)?dateText:null, rawDate:dateText || null, href, url }
  }).get().filter(Boolean)
}
function classifyListItem(item, columnAlias) {
  const url=new URL(item.url), legacyDetail=url.origin===origin && /\/(pdetail|detail)\.html$/.test(url.pathname), contentId=legacyDetail?url.searchParams.get('content_id'):null
  if (legacyDetail && contentId) return { ...item, columnAlias, articleType:'INTERNAL', contentId, detailPath:url.pathname, legacyKey:`${item.typeCode}:content:${contentId}` }
  return { ...item, columnAlias, articleType:'EXTERNAL_LINK', contentId:null, detailPath:url.pathname, legacyKey:`${item.typeCode}:external:${sha256(url.toString())}` }
}
async function collectInternal(item) {
  const response=await fetchText(item.url), rawName=`${item.typeCode}-${item.contentId}.html`; await writeFile(path.join(rawRoot,'details',rawName),response.text,'utf8'); const $=cheerio.load(response.text)
  const title=normalizeText($('.detail-content-title').first().text()), tips=$('.detail-content-title-tips > div').map((_,element)=>normalizeText($(element).text())).get()
  const source=normalizeText((tips.find(value=>value.startsWith('信息来源：')) || '').replace(/^信息来源：/,'')), dateValue=normalizeText((tips.find(value=>value.startsWith('发布时间：')) || '').replace(/^发布时间：/,'')), detailDate=/^\d{4}-\d{2}-\d{2}$/.test(dateValue)?dateValue:null, rich=$('.rich-text-wrap').first()
  if (!title || !rich.length) { issues.push({level:'error',code:'INTERNAL_DETAIL_STRUCTURE_MISSING',legacyKey:item.legacyKey,url:item.url}); return null }
  if (normalizeText(item.title)!==title) issues.push({level:'error',code:'TITLE_CONFLICT',legacyKey:item.legacyKey,listTitle:item.title,detailTitle:title})
  if (item.publishDate && detailDate && item.publishDate!==detailDate) issues.push({level:'error',code:'DATE_CONFLICT',legacyKey:item.legacyKey,listDate:item.publishDate,detailDate})
  const resources=[]
  for (const image of rich.find('img[src]').toArray()) {
    const node=$(image), rawReference=node.attr('src'); if (!rawReference || rawReference.startsWith('data:')) continue
    const resource=await collectResource(rawReference,response.url,'BODY_IMAGE')
    if (resource) { resources.push(resource); node.attr('src',resource.token) } else { node.remove() }
  }
  for (const anchor of rich.find('a[href]').toArray()) {
    const node=$(anchor), rawReference=node.attr('href'); if (!rawReference) continue
    let parsed; try { parsed=new URL(rawReference,response.url) } catch { continue }
    if (!attachmentExtensions.has(path.extname(parsed.pathname).slice(1).toLowerCase())) continue
    const resource=await collectResource(rawReference,response.url,'ATTACHMENT'); if (resource) { resources.push(resource); node.attr('href',resource.token) }
  }
  const bodyHtml=rich.html() || '', content={title,source,publishDate:detailDate??item.publishDate,bodyHtml,externalUrl:null}, fingerprint=sha256(JSON.stringify({identity:item.legacyKey,target:item.columnAlias,content,resources:resources.map(resource=>[resource.role,resource.sha256]).sort()}))
  return {source:{system:sourceSystem,legacyKey:item.legacyKey,contentId:item.contentId,typeCode:item.typeCode,detailPath:item.detailPath,url:response.url},target:{columnAlias:item.columnAlias,articleType:'INTERNAL'},content,resources:resources.map(({declaredContentType,...resource})=>resource),sourceFingerprint:fingerprint,evidence:{listPage:item.pageNo,listTitle:item.title,listPublishDate:item.publishDate,sourceOrder:item.sourceOrder,detailPublishDate:detailDate,rawDetailPath:`raw/details/${rawName}`}}
}
function collectExternal(item) {
  const content={title:item.title,source:'',publishDate:item.publishDate,bodyHtml:'',externalUrl:item.url}, fingerprint=sha256(JSON.stringify({identity:item.legacyKey,target:item.columnAlias,content}))
  return {source:{system:sourceSystem,legacyKey:item.legacyKey,contentId:null,typeCode:item.typeCode,detailPath:item.detailPath,url:item.url},target:{columnAlias:item.columnAlias,articleType:'EXTERNAL_LINK'},content,resources:[],sourceFingerprint:fingerprint,evidence:{listPage:item.pageNo,listTitle:item.title,listPublishDate:item.publishDate,sourceOrder:item.sourceOrder}}
}
async function collectHome() {
  const {text,url}=await fetchText(`${origin}/dyzj`); await writeFile(path.join(rawRoot,'party-home.html'),text,'utf8'); const $=cheerio.load(text)
  const candidates=$('img').map((_,image)=>{const node=$(image),parent=node.closest('a[href]');return{src:node.attr('src')?normalizeUrl(node.attr('src'),url):null,alt:normalizeText(node.attr('alt'))||null,parentHref:parent.attr('href')?normalizeUrl(parent.attr('href'),url):null,className:node.attr('class')||'',parentClassName:parent.attr('class')||'',parentHtml:parent.length?$.html(parent).slice(0,1500):null}}).get()
  await writeFile(path.join(reportRoot,'home-image-candidates.json'),JSON.stringify(candidates,null,2),'utf8')

  const slideSelector='.banner-wrap.first-box .swiper-container .swiper-wrapper > .swiper-slide > a.swiper-slide-a'
  const slides=$(slideSelector).toArray()
  if (slides.length!==4) issues.push({level:'error',code:'PARTY_CAROUSEL_COUNT_MISMATCH',expected:4,actual:slides.length,selector:slideSelector})
  const items=[]
  for (const [index, element] of slides.entries()) {
    const anchor=$(element), title=normalizeText(anchor.find('.swiper-msg h6').first().text()), href=anchor.attr('href'), imageReference=anchor.find('img.swiper-banner[src]').first().attr('src'), target=anchor.attr('target') || ''
    const legacyKey=`party-carousel:position:${index+1}`
    if (!title || !href || !imageReference) {
      issues.push({level:'error',code:'PARTY_CAROUSEL_STRUCTURE_MISSING',legacyKey,title:!!title,href:!!href,image:!!imageReference})
      continue
    }
    let targetUrl
    try { targetUrl=normalizeUrl(href,url) } catch (error) { issues.push({level:'error',code:'PARTY_CAROUSEL_URL_INVALID',legacyKey,href,message:String(error)}); continue }
    const image=await collectResource(imageReference,url,'CAROUSEL_IMAGE')
    if (!image) { issues.push({level:'error',code:'PARTY_CAROUSEL_IMAGE_UNRESOLVED',legacyKey,imageReference}); continue }
    const openMode=target.toLowerCase()==='_blank'?'NEW_WINDOW':'SAME_WINDOW', sourceOrder=index+1
    const sourceFingerprint=sha256(JSON.stringify({legacyKey,sourceOrder,title,url:targetUrl,openMode,imageSha256:image.sha256}))
    const { role, declaredContentType, token, ...snapshotImage }=image
    items.push({legacyKey,sourceOrder,title,url:targetUrl,openMode,sourceFingerprint,image:snapshotImage,evidence:{sourceHref:href,sourceImageReference:imageReference,sourceTarget:target}})
  }
  const carousel={listCode:'PARTY_CAROUSEL',sourceSystem,sourcePage:url,items}
  await writeFile(path.join(outputRoot,'carousel.json'),JSON.stringify(carousel,null,2),'utf8')
  return {imageCandidates:candidates.length,carouselItems:items.length}
}
async function main() {
  await rm(outputRoot,{recursive:true,force:true}); await mkdir(path.join(rawRoot,'lists'),{recursive:true}); await mkdir(path.join(rawRoot,'details'),{recursive:true}); await mkdir(assetRoot,{recursive:true}); await mkdir(reportRoot,{recursive:true})
  // 首页体量小且包含 Party carousel 证据，优先保存；即使后续详情采集被外部中断，也不丢失首页结构证据。
  const home=await collectHome(), listItems=[], scopeReport=[]
  for (const scope of scopes) {
    const firstUrl=`${origin}/plist.html?typeCode=${scope.typeCode}`, first=await fetchText(firstUrl), pagination=parsePagination(first.text,scope.typeCode), seenPageFingerprints=new Set(), scoped=[]
    for (let pageNo=1;pageNo<=pagination.pageCount;pageNo+=1) { const pageUrl=`${origin}/plist.html?typeCode=${scope.typeCode}&pageNo=${pageNo}&pageSize=10`, page=pageNo===1?first:await fetchText(pageUrl); await writeFile(path.join(rawRoot,'lists',`${scope.typeCode}-page-${String(pageNo).padStart(2,'0')}.html`),page.text,'utf8'); const pageFingerprint=sha256(page.text); if(seenPageFingerprints.has(pageFingerprint))issues.push({level:'error',code:'REPEATED_PAGE_HTML',typeCode:scope.typeCode,pageNo}); seenPageFingerprints.add(pageFingerprint); scoped.push(...parseListPage(page.text,scope.typeCode,pageNo).map(item=>classifyListItem(item,scope.columnAlias))) }
    scoped.forEach((item,index)=>{item.sourceOrder=index+1}); const unique=new Map(scoped.map(item=>[item.legacyKey,item])); if(scoped.length!==pagination.total||unique.size!==pagination.total)issues.push({level:'error',code:'SCOPE_COUNT_MISMATCH',typeCode:scope.typeCode,reportedTotal:pagination.total,parsedCount:scoped.length,uniqueCount:unique.size})
    listItems.push(...unique.values()); scopeReport.push({typeCode:scope.typeCode,columnAlias:scope.columnAlias,reportedTotal:pagination.total,pageCount:pagination.pageCount,parsedCount:scoped.length,uniqueCount:unique.size,internal:[...unique.values()].filter(item=>item.articleType==='INTERNAL').length,external:[...unique.values()].filter(item=>item.articleType==='EXTERNAL_LINK').length})
  }
  const records=[]; for(const[index,item]of listItems.entries()){process.stdout.write(`collect ${index+1}/${listItems.length} ${item.legacyKey}\n`);const record=item.articleType==='INTERNAL'?await collectInternal(item):collectExternal(item);if(record)records.push(record)}
  await writeFile(path.join(outputRoot,'articles.ndjson'),records.map(record=>JSON.stringify(record)).join('\n')+'\n','utf8'); await writeFile(path.join(outputRoot,'resources.ndjson'),[...resourceRecords.values()].map(record=>JSON.stringify(record)).join('\n')+(resourceRecords.size?'\n':''),'utf8')
  const errors=issues.filter(issue=>issue.level==='error'), reconciliation={generatedAt:new Date().toISOString(),sourceSystem,sourceOrigin:origin,scopes:scopeReport,listedRecords:listItems.length,normalizedRecords:records.length,internal:records.filter(record=>record.target.articleType==='INTERNAL').length,external:records.filter(record=>record.target.articleType==='EXTERNAL_LINK').length,resources:resourceRecords.size,unresolved:errors.length,warnings:issues.length-errors.length,home}
  await writeFile(path.join(reportRoot,'reconciliation.json'),JSON.stringify(reconciliation,null,2),'utf8'); await writeFile(path.join(reportRoot,'issues.json'),JSON.stringify(issues,null,2),'utf8'); console.log(`EU29_COLLECTION_SUMMARY ${JSON.stringify(reconciliation)}`)
  if(scopeReport.some(scope=>scope.reportedTotal!==scope.uniqueCount))throw new Error('栏目分页采集数量与原站报告总量不一致')
  if(home.carouselItems!==4)throw new Error(`党建轮播采集数量不正确：${home.carouselItems}`)
}
await main()
