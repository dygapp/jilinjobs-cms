import { requestJson } from '../../../shared/adminHttp'

export type AdvertisementOpenMode=null|'_self'|'_blank'|'NO_LINK'
export interface AdvertisementSlot{id:number;code:string;name:string;description:string;sortOrder:number;enabled:boolean;system:boolean;preset:boolean}
export interface Advertisement{id:number;slotId:number;title:string;imagePath:string;url:string|null;openMode:AdvertisementOpenMode;startAt:string|null;endAt:string|null;sortOrder:number;enabled:boolean}
export interface PublicAdvertisementSlot{id:number;code:string;name:string;advertisements:Advertisement[]}
export interface AdvertisementSlotDraft{code:string;name:string;description:string;sortOrder:number;enabled:boolean;system:boolean}
export interface AdvertisementDraft{title:string;imagePath:string;url:string|null;openMode:AdvertisementOpenMode;startAt:string|null;endAt:string|null;sortOrder:number;enabled:boolean}
export const listAdvertisementSlots=()=>requestJson<AdvertisementSlot[]>('/api/admin/advertisements/slots')
export const createAdvertisementSlot=(d:AdvertisementSlotDraft)=>requestJson<AdvertisementSlot>('/api/admin/advertisements/slots',{method:'POST',body:JSON.stringify(d)})
export const updateAdvertisementSlot=(id:number,d:AdvertisementSlotDraft)=>requestJson<AdvertisementSlot>(`/api/admin/advertisements/slots/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteAdvertisementSlot=(id:number)=>requestJson<void>(`/api/admin/advertisements/slots/${id}`,{method:'DELETE'})
export const listAdvertisements=(slotId:number)=>requestJson<Advertisement[]>(`/api/admin/advertisements/slots/${slotId}/items`)
export const createAdvertisement=(slotId:number,d:AdvertisementDraft)=>requestJson<Advertisement>(`/api/admin/advertisements/slots/${slotId}/items`,{method:'POST',body:JSON.stringify(d)})
export const updateAdvertisement=(slotId:number,id:number,d:AdvertisementDraft)=>requestJson<Advertisement>(`/api/admin/advertisements/slots/${slotId}/items/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const deleteAdvertisement=(slotId:number,id:number)=>requestJson<void>(`/api/admin/advertisements/slots/${slotId}/items/${id}`,{method:'DELETE'})
export const listPublicAdvertisements=()=>requestJson<PublicAdvertisementSlot[]>('/api/public/advertisements')
