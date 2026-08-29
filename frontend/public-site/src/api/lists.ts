export interface CmsListItem{id:number;listId:number;title:string;subtitle:string|null;url:string|null;imagePath:string|null;openMode:string;sortOrder:number;enabled:boolean;extraJson:string|null}
export interface PublicCmsList{id:number;code:string;name:string;groupCode:string;itemType:string;items:CmsListItem[]}
async function request<T>(url:string):Promise<T>{const r=await fetch(url);if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export const listPublicCmsLists=()=>request<PublicCmsList[]>('/api/public/lists')
