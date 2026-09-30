import type { ContentImagePolicy } from '../cmsEnums'
import type { ArticleStatus, ArticleType } from './articles'
import { requestJson } from '../../../shared/adminHttp'
export type { ContentImagePolicy } from '../cmsEnums'

export type CmsListItemSourceType='LINK'|'ARTICLE'
export type LinkOpenMode=null|'_self'|'_blank'
export interface CmsListDefinition{id:number;code:string;name:string;groupCode:string;imagePolicy:ContentImagePolicy;description:string;sortOrder:number;enabled:boolean;system:boolean;preset:boolean}
export interface CmsListItem{id:number;listId:number;sourceType:CmsListItemSourceType;articleId:number|null;articleType:ArticleType|null;articleStatus:ArticleStatus|null;title:string;subtitle:string|null;url:string|null;imagePath:string|null;imageResourceId:number|null;effectiveImageResourceId:number|null;openMode:LinkOpenMode;sortOrder:number;enabled:boolean;extraJson:string|null}
export interface PublicCmsList{id:number;code:string;name:string;groupCode:string;imagePolicy:ContentImagePolicy;items:CmsListItem[]}
export interface CmsListDraft{code:string;name:string;groupCode:string;imagePolicy:ContentImagePolicy;description:string;sortOrder:number;enabled:boolean;system:boolean}
export interface CmsListItemDraft{sourceType:CmsListItemSourceType;articleId:number|null;title:string;subtitle:string|null;url:string|null;imagePath:string|null;imageResourceId:number|null;openMode:LinkOpenMode;sortOrder:number;enabled:boolean;extraJson:string|null}
export const listCmsLists=()=>requestJson<CmsListDefinition[]>('/api/admin/lists')
export const createCmsList=(d:CmsListDraft)=>requestJson<CmsListDefinition>('/api/admin/lists',{method:'POST',body:JSON.stringify(d)})
export const updateCmsList=(id:number,d:CmsListDraft)=>requestJson<CmsListDefinition>(`/api/admin/lists/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteCmsList=(id:number)=>requestJson<void>(`/api/admin/lists/${id}`,{method:'DELETE'})
export const listCmsListItems=(id:number)=>requestJson<CmsListItem[]>(`/api/admin/lists/${id}/items`)
export const createCmsListItem=(id:number,d:CmsListItemDraft)=>requestJson<CmsListItem>(`/api/admin/lists/${id}/items`,{method:'POST',body:JSON.stringify(d)})
export const updateCmsListItem=(listId:number,itemId:number,d:CmsListItemDraft)=>requestJson<CmsListItem>(`/api/admin/lists/${listId}/items/${itemId}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteCmsListItem=(listId:number,itemId:number)=>requestJson<void>(`/api/admin/lists/${listId}/items/${itemId}`,{method:'DELETE'})
export const listPublicCmsLists=()=>requestJson<PublicCmsList[]>('/api/public/lists')
