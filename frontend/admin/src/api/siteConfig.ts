export type SitePropertyType='TEXT'|'RESOURCE_PATH'|'JSON'|'URL'|'BOOLEAN'
export interface SiteConfigItem{key:string;name:string;groupCode:string;value:string;valueType:SitePropertyType;description:string;sortOrder:number;required:boolean;system:boolean;enabled:boolean}
export interface SiteConfigDraft{key:string;name:string;groupCode:string;value:string;valueType:SitePropertyType;description:string;sortOrder:number;required:boolean;system:boolean;enabled:boolean}
async function request<T>(url:string,init?:RequestInit):Promise<T>{const r=await fetch(url,{...init,headers:{'Content-Type':'application/json',...(init?.headers??{})}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}if(r.status===204)return undefined as T;return r.json() as Promise<T>}
export const listSiteConfig=()=>request<SiteConfigItem[]>('/api/admin/site-config')
export const listPublicSiteConfig=()=>request<SiteConfigItem[]>('/api/public/site-config')
export const createSiteConfig=(draft:SiteConfigDraft)=>request<SiteConfigItem>('/api/admin/site-config',{method:'POST',body:JSON.stringify(draft)})
export const updateSiteConfig=(key:string,value:string)=>request<SiteConfigItem>(`/api/admin/site-config/${encodeURIComponent(key)}`,{method:'PUT',body:JSON.stringify({value})})
export const updateSiteConfigDefinition=(key:string,draft:SiteConfigDraft)=>request<SiteConfigItem>(`/api/admin/site-config/${encodeURIComponent(key)}/definition`,{method:'PUT',body:JSON.stringify(draft)})
export const deleteSiteConfig=(key:string)=>request<void>(`/api/admin/site-config/${encodeURIComponent(key)}`,{method:'DELETE'})
