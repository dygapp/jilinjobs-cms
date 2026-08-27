export interface StaticEntry{path:string;name:string;directory:boolean;size:number;modifiedAt:string|null}
export interface TrashEntry{id:string;originalPath:string}
async function checked<T>(r:Response):Promise<T>{if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export const listStaticResources=(path='')=>checked<StaticEntry[]>(fetch(`/api/admin/static-resources?path=${encodeURIComponent(path)}`))
export async function uploadStaticResource(path:string,file:File,replace=false){const form=new FormData();form.append('file',file);return checked<StaticEntry>(await fetch(`/api/admin/static-resources?path=${encodeURIComponent(path)}&replace=${replace}`,{method:'POST',body:form}))}
export const deleteStaticResource=(path:string)=>checked<TrashEntry>(fetch(`/api/admin/static-resources?path=${encodeURIComponent(path)}`,{method:'DELETE'}))
export const listStaticTrash=()=>checked<TrashEntry[]>(fetch('/api/admin/static-resources/trash'))
export const restoreStaticResource=(id:string)=>checked<StaticEntry>(fetch(`/api/admin/static-resources/restore/${id}`,{method:'POST'}))
