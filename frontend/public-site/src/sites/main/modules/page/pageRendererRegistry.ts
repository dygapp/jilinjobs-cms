export type PageRendererKind='rich'|'embed-placeholder'|'internal-static'|'jilinjobs-guide-cards'

const PAGE_RENDERERS:Readonly<Record<string,PageRendererKind>>=Object.freeze({
  RICH_TEXT:'rich',
  EMBED_PLACEHOLDER:'embed-placeholder',
  INTERNAL_STATIC:'internal-static',
  JILINJOBS_GUIDE_CARDS:'jilinjobs-guide-cards',
})

export function resolvePageRenderer(rendererKey:string):PageRendererKind|null{
  return PAGE_RENDERERS[rendererKey]??null
}

export function isRegisteredPageRenderer(rendererKey:string):boolean{
  return resolvePageRenderer(rendererKey)!==null
}
