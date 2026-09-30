import { requestJson } from '../../../shared/adminHttp'

export type SitePropertyType='TEXT'|'RESOURCE_PATH'|'JSON'|'URL'|'BOOLEAN'|'INTEGER'
export interface SiteConfigItem{key:string;name:string;groupCode:string;value:string;valueType:SitePropertyType;description:string;sortOrder:number;required:boolean;system:boolean;enabled:boolean;preset:boolean}
export interface SiteConfigDraft{key:string;name:string;groupCode:string;value:string;valueType:SitePropertyType;description:string;sortOrder:number;required:boolean;system:boolean;enabled:boolean}
export interface SitePropertyGroupDefinition{code:string;name:string;order:number}
export const listSiteConfig=()=>requestJson<SiteConfigItem[]>('/api/admin/site-config')
export const listSitePropertyGroups=()=>requestJson<SitePropertyGroupDefinition[]>('/api/admin/site-config/groups')
export const listPublicSiteConfig=()=>requestJson<SiteConfigItem[]>('/api/public/site-config')
export const createSiteConfig=(draft:SiteConfigDraft)=>requestJson<SiteConfigItem>('/api/admin/site-config',{method:'POST',body:JSON.stringify(draft)})
export const updateSiteConfig=(key:string,value:string)=>requestJson<SiteConfigItem>(`/api/admin/site-config/${encodeURIComponent(key)}`,{method:'PUT',body:JSON.stringify({value})})
export const updateSiteConfigDefinition=(key:string,draft:SiteConfigDraft)=>requestJson<SiteConfigItem>(`/api/admin/site-config/${encodeURIComponent(key)}/definition`,{method:'PUT',body:JSON.stringify(draft)})
export const deleteSiteConfig=(key:string)=>requestJson<void>(`/api/admin/site-config/${encodeURIComponent(key)}`,{method:'DELETE'})
