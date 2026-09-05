import type { ContentImagePolicy } from '../cmsEnums'
import type { ArticleStatus, ArticleType } from './articles'
export type { ContentImagePolicy } from '../cmsEnums'

export type CmsListItemSourceType='LINK'|'ARTICLE'
export interface CmsListDefinition{id:number;code:string;name:string;groupCode:string;imagePolicy:ContentImagePolicy;description:string;sortOrder:number;enabled:boolean;system:boolean;preset:boolean}
export interface CmsListItem{id:number;listId:number;sourceType:CmsListItemSourceType;articleId:number|null;articleType:ArticleType|null;articleStatus:ArticleStatus|null;title:string;subtitle:string|null;url:string|null;imagePath:string|null;imageResourceId:number|null;effectiveImageResourceId:number|null;openMode:string;sortOrder:number;enabled:boolean;extraJson:string|null}
export interface PublicCmsList{id:number;code:string;name:string;groupCode:string;imagePolicy:ContentImagePolicy;items:CmsListItem[]}
export interface CmsListDraft{code:string;name:string;groupCode:string;imagePolicy:ContentImagePolicy;description:string;sortOrder:number;enabled:boolean;system:boolean}
export interface CmsListItemDraft{sourceType:CmsListItemSourceType;articleId:number|null;title:string;subtitle:string|null;url:string|null;imagePath:string|null;imageResourceId:number|null;openMode:string;sortOrder:number;enabled:boolean;extraJson:string|null}
async function request<T>(url:string,init?:RequestInit):Promise<T>{const r=await fetch(url,{...init,headers:{'Content-Type':'application/json',...(init?.headers??{})}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}if(r.status===204)return undefined as T;return r.json() as Promise<T>}
export const listCmsLists=()=>request<CmsListDefinition[]>('/api/admin/lists')
export const createCmsList=(d:CmsListDraft)=>request<CmsListDefinition>('/api/admin/lists',{method:'POST',body:JSON.stringify(d)})
export const updateCmsList=(id:number,d:CmsListDraft)=>request<CmsListDefinition>(`/api/admin/lists/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteCmsList=(id:number)=>request<void>(`/api/admin/lists/${id}`,{method:'DELETE'})
export const listCmsListItems=(id:number)=>request<CmsListItem[]>(`/api/admin/lists/${id}/items`)
export const createCmsListItem=(id:number,d:CmsListItemDraft)=>request<CmsListItem>(`/api/admin/lists/${id}/items`,{method:'POST',body:JSON.stringify(d)})
export const updateCmsListItem=(listId:number,itemId:number,d:CmsListItemDraft)=>request<CmsListItem>(`/api/admin/lists/${listId}/items/${itemId}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteCmsListItem=(listId:number,itemId:number)=>request<void>(`/api/admin/lists/${listId}/items/${itemId}`,{method:'DELETE'})
export const listPublicCmsLists=()=>request<PublicCmsList[]>('/api/public/lists')
