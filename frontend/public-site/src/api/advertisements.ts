export interface Advertisement{id:number;slotId:number;title:string;imagePath:string;url:string|null;openMode:string;startAt:string|null;endAt:string|null;sortOrder:number;enabled:boolean}
export interface PublicAdvertisementSlot{id:number;code:string;name:string;advertisements:Advertisement[]}
async function request<T>(url:string):Promise<T>{const r=await fetch(url);if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export const listPublicAdvertisements=()=>request<PublicAdvertisementSlot[]>('/api/public/advertisements')
