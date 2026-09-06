export type PageRenderMode='RICH_TEXT'|'EMBED_PLACEHOLDER'|'INTERNAL_STATIC'
export interface PublicPageMember{alias:string;name:string;href:string;sortOrder:number}
export interface PublicPageGroup{alias:string;name:string;members:PublicPageMember[]}
export interface PublicPage{id:number;alias:string;name:string;bodyHtml:string;renderMode:PageRenderMode;embedUrl:string|null;canonicalUrl:string;group:PublicPageGroup|null;breadcrumbs:Array<{title:string;href:string|null}>}
async function request<T>(url:string):Promise<T>{const r=await fetch(url,{headers:{'Content-Type':'application/json'}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export const getPublicPage=(alias:string)=>request<PublicPage>(`/api/public/pages/${encodeURIComponent(alias)}`)
export const getPublicGroupedPage=(group:string,alias:string)=>request<PublicPage>(`/api/public/page-groups/${encodeURIComponent(group)}/${encodeURIComponent(alias)}`)
