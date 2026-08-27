export type ArticleStatus='DRAFT'|'PUBLISHED'|'WITHDRAWN'
export interface CmsArticle{id:number;columnId:number;title:string;bodyHtml:string;source:string;publishDate:string|null;pinned:boolean;recommended:boolean;sortOrder:number;status:ArticleStatus;actualPublishedAt:string|null;viewCount:number;updatedAt:string;coverResourceId:number|null;bodyImageResourceIds:number[];attachmentResourceIds:number[]}
export interface ArticleDraft{columnId:number;title:string;bodyHtml:string;source:string;publishDate:string|null;pinned:boolean;recommended:boolean;sortOrder:number;coverResourceId:number|null;bodyImageResourceIds:number[];attachmentResourceIds:number[]}
export interface CmsResource{id:number;storageKey:string;originalFilename:string;contentType:string|null;sizeBytes:number}
export interface PublicArticleSummary{id:number;columnId:number;columnName:string;columnAlias:string;title:string;publishDate:string|null;pinned:boolean;recommended:boolean;sortOrder:number}
export interface PublicArticleDetail{id:number;columnId:number;columnName:string;columnAlias:string;title:string;bodyHtml:string;source:string;publishDate:string|null;bodyImageResourceIds:number[];attachments:PublicArticleAttachment[]}
export interface PublicArticleAttachment{id:number;originalFilename:string;contentType:string|null;sizeBytes:number}
export interface PublicArticlePage{items:PublicArticleSummary[];page:number;size:number;total:number}
async function jsonRequest<T>(url:string,init?:RequestInit):Promise<T>{const r=await fetch(url,{...init,headers:{'Content-Type':'application/json',...(init?.headers??{})}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export const listArticles=()=>jsonRequest<CmsArticle[]>('/api/admin/articles')
export const getArticle=(id:number)=>jsonRequest<CmsArticle>(`/api/admin/articles/${id}`)
export const createArticle=(d:ArticleDraft)=>jsonRequest<CmsArticle>('/api/admin/articles',{method:'POST',body:JSON.stringify(d)})
export const updateArticle=(id:number,d:ArticleDraft)=>jsonRequest<CmsArticle>(`/api/admin/articles/${id}`,{method:'PUT',body:JSON.stringify(d)})
export const publishArticle=(id:number)=>jsonRequest<CmsArticle>(`/api/admin/articles/${id}/publish`,{method:'POST'})
export const withdrawArticle=(id:number)=>jsonRequest<CmsArticle>(`/api/admin/articles/${id}/withdraw`,{method:'POST'})
export function listPublicArticles(columnId:number|null,page=0,size=10){const p=new URLSearchParams({page:String(page),size:String(size)});if(columnId!=null)p.set('columnId',String(columnId));return jsonRequest<PublicArticlePage>(`/api/public/articles?${p}`)}
export const getPublicArticle=(id:number)=>jsonRequest<PublicArticleDetail>(`/api/public/articles/${id}`)
export async function uploadResource(file:File):Promise<CmsResource>{const f=new FormData();f.append('file',file);const r=await fetch('/api/admin/resources',{method:'POST',body:f});if(!r.ok){const e=await r.json().catch(()=>({message:`上传失败：${r.status}`})) as {message?:string};throw new Error(e.message??`上传失败：${r.status}`)}return r.json() as Promise<CmsResource>}
export const getResource=(id:number)=>jsonRequest<CmsResource>(`/api/admin/resources/${id}`)
export const resourceContentUrl=(id:number)=>`/api/admin/resources/${id}/content`
export const publicResourceContentUrl=(id:number)=>`/api/public/resources/${id}/content`
export const publicAttachmentUrl=(id:number)=>`/api/public/resources/${id}/attachment`
export function publicBodyHtml(a:PublicArticleDetail){return a.bodyImageResourceIds.reduce((html,id)=>html.split(resourceContentUrl(id)).join(publicResourceContentUrl(id)),a.bodyHtml)}
