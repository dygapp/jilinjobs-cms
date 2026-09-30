import { adminFetch, requestJson } from '../../../shared/adminHttp'

export type ArticleStatus='DRAFT'|'PUBLISHED'|'WITHDRAWN'
export type ArticleType='INTERNAL'|'EXTERNAL_LINK'
export interface CmsArticle{id:number;columnId:number;title:string;bodyHtml:string;source:string;articleType:ArticleType;externalUrl:string|null;publishDate:string|null;pinned:boolean;sortOrder:number;status:ArticleStatus;actualPublishedAt:string|null;viewCount:number;updatedAt:string;coverResourceId:number|null;bodyImageResourceIds:number[];attachmentResourceIds:number[]}
export interface AdminArticleSummary{id:number;columnId:number;title:string;source:string;articleType:ArticleType;publishDate:string|null;status:ArticleStatus;viewCount:number;updatedAt:string}
export interface AdminArticlePage{items:AdminArticleSummary[];page:number;size:number;total:number}
export interface AdminArticleQuery{keyword?:string;columnId?:number|null;status?:ArticleStatus|null;articleType?:ArticleType|null;page?:number;size?:number}
export interface ArticleDraft{columnId:number;title:string;bodyHtml:string;source:string;articleType:ArticleType;externalUrl:string|null;publishDate:string|null;pinned:boolean;sortOrder:number;coverResourceId:number|null;bodyImageResourceIds:number[];attachmentResourceIds:number[]}
export interface CmsResource{id:number;storageKey:string;originalFilename:string;contentType:string|null;sizeBytes:number}
export interface PublicArticleSummary{id:number;columnId:number;columnName:string;columnAlias:string;title:string;source:string;articleType:ArticleType;externalUrl:string|null;publishDate:string|null;pinned:boolean;sortOrder:number}
export interface PublicArticleDetail{id:number;columnId:number;columnName:string;columnAlias:string;title:string;bodyHtml:string;source:string;articleType:ArticleType;externalUrl:string|null;publishDate:string|null;bodyImageResourceIds:number[];attachments:PublicArticleAttachment[]}
export interface PublicArticleAttachment{id:number;originalFilename:string;contentType:string|null;sizeBytes:number}
export interface PublicArticlePage{items:PublicArticleSummary[];page:number;size:number;total:number}
export function listArticles(query:AdminArticleQuery={}){const p=new URLSearchParams({page:String(query.page??0),size:String(query.size??10)});const keyword=query.keyword?.trim();if(keyword)p.set('keyword',keyword);if(query.columnId!=null)p.set('columnId',String(query.columnId));if(query.status)p.set('status',query.status);if(query.articleType)p.set('articleType',query.articleType);return requestJson<AdminArticlePage>(`/api/admin/articles?${p}`)}
export const getArticle=(id:number)=>requestJson<CmsArticle>(`/api/admin/articles/${id}`)
export const createArticle=(d:ArticleDraft)=>requestJson<CmsArticle>('/api/admin/articles',{method:'POST',body:JSON.stringify(d)})
export const updateArticle=(id:number,d:ArticleDraft)=>requestJson<CmsArticle>(`/api/admin/articles/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const publishArticle=(id:number)=>requestJson<CmsArticle>(`/api/admin/articles/${id}/publish`,{method:'POST'})
export const withdrawArticle=(id:number)=>requestJson<CmsArticle>(`/api/admin/articles/${id}/withdraw`,{method:'POST'})
export function listPublicArticles(columnId:number|null,page=0,size=10){const p=new URLSearchParams({page:String(page),size:String(size)});if(columnId!=null)p.set('columnId',String(columnId));return requestJson<PublicArticlePage>(`/api/public/articles?${p}`)}
export const getPublicArticle=(id:number)=>requestJson<PublicArticleDetail>(`/api/public/articles/${id}`)
export async function uploadResource(file:File):Promise<CmsResource>{const f=new FormData();f.append('file',file);const r=await adminFetch('/api/admin/resources',{method:'POST',body:f});if(!r.ok){const e=await r.json().catch(()=>({message:`上传失败：${r.status}`})) as {message?:string};throw new Error(e.message??`上传失败：${r.status}`)}return r.json() as Promise<CmsResource>}
export const getResource=(id:number)=>requestJson<CmsResource>(`/api/admin/resources/${id}`)
export const resourceContentUrl=(id:number)=>`/api/admin/resources/${id}/content`
export const publicResourceContentUrl=(id:number)=>`/api/public/resources/${id}/content`
export const publicAttachmentUrl=(id:number)=>`/api/public/resources/${id}/attachment`
export function publicBodyHtml(a:PublicArticleDetail){return a.bodyImageResourceIds.reduce((html,id)=>html.split(resourceContentUrl(id)).join(publicResourceContentUrl(id)),a.bodyHtml)}
