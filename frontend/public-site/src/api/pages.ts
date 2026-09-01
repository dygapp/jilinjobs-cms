export type PageRenderMode='RICH_TEXT'|'EMBED_PLACEHOLDER'|'INTERNAL_STATIC'
export interface CmsPageGroup{id:number;alias:string;name:string;sortOrder:number;enabled:boolean}
export interface CmsPage{id:number;groupId:number|null;alias:string;name:string;bodyHtml:string;renderMode:PageRenderMode;embedUrl:string|null;sortOrder:number;enabled:boolean}
export interface PageGroupDraft{alias:string;name:string;sortOrder:number;enabled:boolean}
export interface PageDraft{groupId:number|null;alias:string;name:string;bodyHtml:string;renderMode:PageRenderMode;embedUrl:string|null;sortOrder:number;enabled:boolean}
export interface PublicPageMember{alias:string;name:string;href:string;sortOrder:number}
export interface PublicPageGroup{alias:string;name:string;members:PublicPageMember[]}
export interface PublicPage{id:number;alias:string;name:string;bodyHtml:string;renderMode:PageRenderMode;embedUrl:string|null;canonicalUrl:string;group:PublicPageGroup|null;breadcrumbs:Array<{title:string;href:string|null}>}
async function request<T>(url:string,init?:RequestInit):Promise<T>{const r=await fetch(url,{...init,headers:{'Content-Type':'application/json',...(init?.headers??{})}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}if(r.status===204)return undefined as T;return r.json() as Promise<T>}
export const listPageGroups=()=>request<CmsPageGroup[]>('/api/admin/page-groups')
export const listPages=()=>request<CmsPage[]>('/api/admin/pages')
export const createPageGroup=(d:PageGroupDraft)=>request<CmsPageGroup>('/api/admin/page-groups',{method:'POST',body:JSON.stringify(d)})
export const updatePageGroup=(id:number,d:PageGroupDraft)=>request<CmsPageGroup>(`/api/admin/page-groups/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const createPage=(d:PageDraft)=>request<CmsPage>('/api/admin/pages',{method:'POST',body:JSON.stringify(d)})
export const updatePage=(id:number,d:PageDraft)=>request<CmsPage>(`/api/admin/pages/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const deletePage=(id:number)=>request<void>(`/api/admin/pages/${id}`,{method:'DELETE'})
export const getPublicPage=(alias:string)=>request<PublicPage>(`/api/public/pages/${encodeURIComponent(alias)}`)
export const getPublicGroupedPage=(group:string,alias:string)=>request<PublicPage>(`/api/public/page-groups/${encodeURIComponent(group)}/${encodeURIComponent(alias)}`)
