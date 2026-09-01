export type SitePropertyType='TEXT'|'RESOURCE_PATH'|'JSON'|'URL'|'BOOLEAN'
export interface SiteConfigItem{key:string;name:string;groupCode:string;value:string;valueType:SitePropertyType;description:string;sortOrder:number;required:boolean;system:boolean;enabled:boolean}
async function request<T>(url:string):Promise<T>{const r=await fetch(url);if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export const listPublicSiteConfig=()=>request<SiteConfigItem[]>('/api/public/site-config')
