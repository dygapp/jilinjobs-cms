export type NavigationPosition='MAIN'|'SERVICE'|'SITE'
export type NavigationTargetType='HOME'|'COLUMN'|'PAGE'|'LINK'|'PLACEHOLDER'
export type NavigationOpenMode='DEFAULT'|'SAME_WINDOW'|'NEW_WINDOW'
export interface CmsNavigation{id:number;parentId:number|null;name:string;position:NavigationPosition;category:string|null;targetType:NavigationTargetType;targetColumnId:number|null;targetPageId:number|null;targetUrl:string|null;openMode:NavigationOpenMode;sortOrder:number;enabled:boolean}
export interface NavigationDraft{name:string;position:NavigationPosition;category:string|null;targetType:NavigationTargetType;targetColumnId:number|null;targetUrl:string|null;sortOrder:number;enabled:boolean;parentId?:number|null;targetPageId?:number|null;openMode?:NavigationOpenMode}
export interface PublicNavigation{id:number;parentId:number|null;name:string;position:NavigationPosition;category:string|null;sortOrder:number;targetType:NavigationTargetType;href:string;external:boolean;newWindow:boolean;clickable:boolean}
async function request<T>(url:string,init?:RequestInit):Promise<T>{const r=await fetch(url,{...init,headers:{'Content-Type':'application/json',...(init?.headers??{})}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}if(r.status===204)return undefined as T;return r.json() as Promise<T>}
export const listNavigations=()=>request<CmsNavigation[]>('/api/admin/navigations')
export const listPublicNavigations=()=>request<PublicNavigation[]>('/api/public/navigations')
export const createNavigation=(d:NavigationDraft)=>request<CmsNavigation>('/api/admin/navigations',{method:'POST',body:JSON.stringify(d)})
export const updateNavigation=(id:number,d:NavigationDraft)=>request<CmsNavigation>(`/api/admin/navigations/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteNavigation=(id:number)=>request<void>(`/api/admin/navigations/${id}`,{method:'DELETE'})
