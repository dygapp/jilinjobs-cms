export type ArticleStatus='DRAFT'|'PUBLISHED'|'WITHDRAWN'
export type ArticleType='INTERNAL'|'EXTERNAL_LINK'
export interface PublicArticleSummary{id:number;columnId:number;columnName:string;columnAlias:string;title:string;source:string;articleType:ArticleType;externalUrl:string|null;publishDate:string|null;pinned:boolean;sortOrder:number;coverResourceId:number|null}
export interface PublicArticleDetail{id:number;columnId:number;columnName:string;columnAlias:string;title:string;bodyHtml:string;source:string;articleType:ArticleType;externalUrl:string|null;publishDate:string|null;bodyImageResourceIds:number[];attachments:PublicArticleAttachment[]}
export interface PublicArticleAttachment{id:number;originalFilename:string;contentType:string|null;sizeBytes:number}
export interface PublicArticlePage{items:PublicArticleSummary[];page:number;size:number;total:number}
async function jsonRequest<T>(url:string,init?:RequestInit):Promise<T>{const r=await fetch(url,{...init,headers:{'Content-Type':'application/json',...(init?.headers??{})}});if(!r.ok){const e=await r.json().catch(()=>({message:`请求失败：${r.status}`})) as {message?:string};throw new Error(e.message??`请求失败：${r.status}`)}return r.json() as Promise<T>}
export function listPublicArticles(columnId:number|null,page=0,size=10,articleType:ArticleType|null=null){const p=new URLSearchParams({page:String(page),size:String(size)});if(columnId!=null)p.set('columnId',String(columnId));if(articleType!=null)p.set('articleType',articleType);return jsonRequest<PublicArticlePage>(`/api/public/articles?${p}`)}
export const getPublicArticle=(id:number)=>jsonRequest<PublicArticleDetail>(`/api/public/articles/${id}`)
export const publicAttachmentUrl=(id:number)=>`/api/public/resources/${id}/attachment`
