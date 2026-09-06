export type ContentImagePolicy='NONE'|'OPTIONAL'|'REQUIRED'
export interface PublicColumn { id:number; parentId:number|null; name:string; alias:string; coverPolicy:ContentImagePolicy }
async function request<T>(url:string):Promise<T>{const r=await fetch(url,{headers:{'Content-Type':'application/json'}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export const getPublicColumn=(id:number)=>request<PublicColumn>(`/api/public/columns/${id}`)
export const getPublicColumnByAlias=(alias:string)=>request<PublicColumn>(`/api/public/columns/by-alias/${encodeURIComponent(alias)}`)
