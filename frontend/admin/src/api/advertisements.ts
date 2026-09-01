export interface AdvertisementSlot{id:number;code:string;name:string;description:string;sortOrder:number;enabled:boolean;system:boolean;preset:boolean}
export interface Advertisement{id:number;slotId:number;title:string;imagePath:string;url:string|null;openMode:string;startAt:string|null;endAt:string|null;sortOrder:number;enabled:boolean}
export interface PublicAdvertisementSlot{id:number;code:string;name:string;advertisements:Advertisement[]}
export interface AdvertisementSlotDraft{code:string;name:string;description:string;sortOrder:number;enabled:boolean;system:boolean}
export interface AdvertisementDraft{title:string;imagePath:string;url:string|null;openMode:string;startAt:string|null;endAt:string|null;sortOrder:number;enabled:boolean}
async function request<T>(url:string,init?:RequestInit):Promise<T>{const r=await fetch(url,{...init,headers:{'Content-Type':'application/json',...(init?.headers??{})}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}if(r.status===204)return undefined as T;return r.json() as Promise<T>}
export const listAdvertisementSlots=()=>request<AdvertisementSlot[]>('/api/admin/advertisements/slots')
export const createAdvertisementSlot=(d:AdvertisementSlotDraft)=>request<AdvertisementSlot>('/api/admin/advertisements/slots',{method:'POST',body:JSON.stringify(d)})
export const updateAdvertisementSlot=(id:number,d:AdvertisementSlotDraft)=>request<AdvertisementSlot>(`/api/admin/advertisements/slots/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteAdvertisementSlot=(id:number)=>request<void>(`/api/admin/advertisements/slots/${id}`,{method:'DELETE'})
export const listAdvertisements=(slotId:number)=>request<Advertisement[]>(`/api/admin/advertisements/slots/${slotId}/items`)
export const createAdvertisement=(slotId:number,d:AdvertisementDraft)=>request<Advertisement>(`/api/admin/advertisements/slots/${slotId}/items`,{method:'POST',body:JSON.stringify(d)})
export const updateAdvertisement=(slotId:number,id:number,d:AdvertisementDraft)=>request<Advertisement>(`/api/admin/advertisements/slots/${slotId}/items/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteAdvertisement=(slotId:number,id:number)=>request<void>(`/api/admin/advertisements/slots/${slotId}/items/${id}`,{method:'DELETE'})
export const listPublicAdvertisements=()=>request<PublicAdvertisementSlot[]>('/api/public/advertisements')
