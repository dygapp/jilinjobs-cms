import { requestJson } from '../../../shared/adminHttp'

export type NavigationTargetType='HOME'|'COLUMN'|'PAGE'|'LINK'|'PLACEHOLDER'
export type NavigationOpenMode=null|'_self'|'_blank'
export interface NavigationLocation{id:number;code:string;name:string;description:string;sortOrder:number;enabled:boolean;system:boolean;preset:boolean}
export interface NavigationLocationDraft{code:string;name:string;description:string;sortOrder:number;enabled:boolean;system:boolean}
export interface CmsNavigation{id:number;parentId:number|null;name:string;position:string;category:string|null;targetType:NavigationTargetType;targetColumnId:number|null;targetPageId:number|null;targetUrl:string|null;openMode:NavigationOpenMode;sortOrder:number;enabled:boolean;iconPath:string|null;preset:boolean}
export interface NavigationDraft{name:string;position:string;category:string|null;targetType:NavigationTargetType;targetColumnId:number|null;targetUrl:string|null;sortOrder:number;enabled:boolean;parentId?:number|null;targetPageId?:number|null;openMode?:NavigationOpenMode;iconPath?:string|null}
export interface PublicNavigation{id:number;parentId:number|null;name:string;position:string;category:string|null;sortOrder:number;targetType:NavigationTargetType;href:string;external:boolean;openMode:NavigationOpenMode;clickable:boolean;iconPath:string|null}
export const listNavigationLocations=()=>requestJson<NavigationLocation[]>('/api/admin/navigation-locations')
export const createNavigationLocation=(d:NavigationLocationDraft)=>requestJson<NavigationLocation>('/api/admin/navigation-locations',{method:'POST',body:JSON.stringify(d)})
export const updateNavigationLocation=(code:string,d:NavigationLocationDraft)=>requestJson<NavigationLocation>(`/api/admin/navigation-locations/${encodeURIComponent(code)}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteNavigationLocation=(code:string)=>requestJson<void>(`/api/admin/navigation-locations/${encodeURIComponent(code)}`,{method:'DELETE'})
export const listNavigations=()=>requestJson<CmsNavigation[]>('/api/admin/navigations')
export const listPublicNavigations=()=>requestJson<PublicNavigation[]>('/api/public/navigations')
export const createNavigation=(d:NavigationDraft)=>requestJson<CmsNavigation>('/api/admin/navigations',{method:'POST',body:JSON.stringify(d)})
export const updateNavigation=(id:number,d:NavigationDraft)=>requestJson<CmsNavigation>(`/api/admin/navigations/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteNavigation=(id:number)=>requestJson<void>(`/api/admin/navigations/${id}`,{method:'DELETE'})
