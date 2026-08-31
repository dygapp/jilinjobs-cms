export type NavigationTargetType='HOME'|'COLUMN'|'PAGE'|'LINK'|'PLACEHOLDER'
export interface PublicNavigation{id:number;parentId:number|null;name:string;position:string;category:string|null;sortOrder:number;targetType:NavigationTargetType;href:string;external:boolean;newWindow:boolean;clickable:boolean;iconPath:string|null}
async function request<T>(url:string):Promise<T>{const r=await fetch(url);if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export const listPublicNavigations=()=>request<PublicNavigation[]>('/api/public/navigations')
