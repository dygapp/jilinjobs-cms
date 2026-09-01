import type { ContentImagePolicy } from '../cmsEnums'
export type { ContentImagePolicy } from '../cmsEnums'

export interface CmsColumn { id:number; parentId:number|null; name:string; sortOrder:number; enabled:boolean; alias:string; coverPolicy:ContentImagePolicy; preset:boolean }
export interface PublicColumn { id:number; parentId:number|null; name:string; alias:string }
export interface ColumnDraft { parentId:number|null; name:string; sortOrder:number; enabled:boolean; alias:string; coverPolicy:ContentImagePolicy }
async function request<T>(url:string,init?:RequestInit):Promise<T>{const r=await fetch(url,{...init,headers:{'Content-Type':'application/json',...(init?.headers??{})}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}if(r.status===204)return undefined as T;return r.json() as Promise<T>}
export const listColumns=()=>request<CmsColumn[]>('/api/admin/columns')
export const getPublicColumn=(id:number)=>request<PublicColumn>(`/api/public/columns/${id}`)
export const getPublicColumnByAlias=(alias:string)=>request<PublicColumn>(`/api/public/columns/by-alias/${encodeURIComponent(alias)}`)
export const createColumn=(draft:ColumnDraft)=>request<CmsColumn>('/api/admin/columns',{method:'POST',body:JSON.stringify(draft)})
export const updateColumn=(id:number,draft:ColumnDraft)=>request<CmsColumn>(`/api/admin/columns/${id}`,{method:'PUT',body:JSON.stringify(draft)})
export const deleteColumn=(id:number)=>request<void>(`/api/admin/columns/${id}`,{method:'DELETE'})
