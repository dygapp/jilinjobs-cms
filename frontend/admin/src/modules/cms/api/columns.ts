import type { ContentImagePolicy } from '../cmsEnums'
import { requestJson } from '../../../shared/adminHttp'
export type { ContentImagePolicy } from '../cmsEnums'

export interface CmsColumn { id:number; parentId:number|null; name:string; sortOrder:number; enabled:boolean; alias:string; coverPolicy:ContentImagePolicy; preset:boolean }
export interface PublicColumn { id:number; parentId:number|null; name:string; alias:string }
export interface ColumnDraft { parentId:number|null; name:string; sortOrder:number; enabled:boolean; alias:string; coverPolicy:ContentImagePolicy }
export const listColumns=()=>requestJson<CmsColumn[]>('/api/admin/columns')
export const getPublicColumn=(id:number)=>requestJson<PublicColumn>(`/api/public/columns/${id}`)
export const getPublicColumnByAlias=(alias:string)=>requestJson<PublicColumn>(`/api/public/columns/by-alias/${encodeURIComponent(alias)}`)
export const createColumn=(draft:ColumnDraft)=>requestJson<CmsColumn>('/api/admin/columns',{method:'POST',body:JSON.stringify(draft)})
export const updateColumn=(id:number,draft:ColumnDraft)=>requestJson<CmsColumn>(`/api/admin/columns/${id}`,{method:'PUT',body:JSON.stringify(draft)})
export const deleteColumn=(id:number)=>requestJson<void>(`/api/admin/columns/${id}`,{method:'DELETE'})
