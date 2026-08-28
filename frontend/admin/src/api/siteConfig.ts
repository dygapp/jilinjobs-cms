export interface SiteConfigItem{key:string;value:string;valueType:string;description:string}
async function request<T>(url:string,init?:RequestInit):Promise<T>{const r=await fetch(url,{...init,headers:{'Content-Type':'application/json',...(init?.headers??{})}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export const listSiteConfig=()=>request<SiteConfigItem[]>('/api/admin/site-config')
export const listPublicSiteConfig=()=>request<SiteConfigItem[]>('/api/public/site-config')
export const updateSiteConfig=(key:string,value:string)=>request<SiteConfigItem>(`/api/admin/site-config/${encodeURIComponent(key)}`,{method:'PUT',body:JSON.stringify({value})})
