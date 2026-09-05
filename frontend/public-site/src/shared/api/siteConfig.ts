export type SitePropertyType='TEXT'|'RESOURCE_PATH'|'JSON'|'URL'|'BOOLEAN'|'INTEGER'
export interface SiteConfigItem{key:string;name:string;groupCode:string;value:string;valueType:SitePropertyType;description:string;sortOrder:number;required:boolean;system:boolean;enabled:boolean}
async function request<T>(url:string):Promise<T>{const r=await fetch(url);if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}

const STRICT_POSITIVE_INTEGER_KEYS = new Set(['CAROUSEL_INTERVAL_SECONDS', 'CAROUSEL_MAX_ITEMS'])

export function normalizePublicSiteConfigItem(item: SiteConfigItem): SiteConfigItem {
  if (!STRICT_POSITIVE_INTEGER_KEYS.has(item.key)) return item
  if (!/^[0-9]+$/.test(item.value)) return { ...item, value: '' }
  const parsed = Number(item.value)
  return Number.isSafeInteger(parsed) && parsed > 0 ? item : { ...item, value: '' }
}

export const listPublicSiteConfig=async()=>
  (await request<SiteConfigItem[]>('/api/public/site-config')).map(normalizePublicSiteConfigItem)
