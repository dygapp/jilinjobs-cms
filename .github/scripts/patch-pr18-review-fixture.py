from pathlib import Path

req = Path('docs/requirements/information-publishing.md')
text = req.read_text()
text = text.replace('version: "V4.0"', 'version: "V4.1"', 1)
text = text.replace(
    '本版本 V4.0 固化 2026-08-27 人工确认的站点收敛结论。',
    '本版本 V4.1 在 V4.0 视觉与结构基线基础上，补充 2026-08-28 人工确认的外链文章与页脚展示规则。',
    1,
)
anchor = '文章至少支持标题、主栏目、富文本正文、来源、发布日期、封面/缩略图、附件、置顶、推荐、排序、草稿/已发布/已撤回、实际发布时间和浏览量。\n'
addition = '''\n文章按内容承载方式区分为“站内文章”和“外链文章”。站内文章继续维护本站富文本正文、资源和附件，并使用本站文章详情页。外链文章用于聚合或抓取外部网站内容，本站只维护标题、发布日期、来源、原文链接及必要的发布/排序属性，不复制外部正文、图片和附件。\n\n外链文章在首页和栏目列表中直接打开原文链接，按外部网站规则使用新窗口，不先进入本站文章详情页；即使用户直接访问其兼容文章地址，前端也应跳转原文链接。“招聘公告”栏目当前按外链文章组织，来源内容由外部网站采集或同步进入本站。\n'''
if addition.strip() not in text:
    if anchor not in text:
        raise SystemExit('requirement article anchor not found')
    text = text.replace(anchor, anchor + addition, 1)
req.write_text(text)

workflow = Path('.github/workflows/review-environment.yml')
text = workflow.read_text()
function_anchor = '''          create_article() {
            column_id="$1"; title="$2"; published="$3"
            payload=$(jq -n --argjson columnId "$column_id" --arg title "$title" --arg published "$published" '{columnId:$columnId,title:$title,bodyHtml:"<p>人工评审环境示例内容，用于观察页面排版与视觉效果。</p>",source:"人工评审示例数据",publishDate:$published,pinned:false,recommended:false,sortOrder:0,coverResourceId:null,bodyImageResourceIds:[],attachmentResourceIds:[]}')
            article_id=$(curl --fail --silent -H 'Content-Type: application/json' -d "$payload" http://127.0.0.1:5173/api/admin/articles | jq -r '.id')
            curl --fail --silent -X POST "http://127.0.0.1:5173/api/admin/articles/${article_id}/publish" > /dev/null
          }
'''
external_function = '''
          create_external_article() {
            column_id="$1"; title="$2"; published="$3"; url="$4"
            payload=$(jq -n --argjson columnId "$column_id" --arg title "$title" --arg published "$published" --arg url "$url" '{columnId:$columnId,title:$title,bodyHtml:"",source:"外部招聘信息源",articleType:"EXTERNAL_LINK",externalUrl:$url,publishDate:$published,pinned:false,recommended:false,sortOrder:0,coverResourceId:null,bodyImageResourceIds:[],attachmentResourceIds:[]}')
            article_id=$(curl --fail --silent -H 'Content-Type: application/json' -d "$payload" http://127.0.0.1:5173/api/admin/articles | jq -r '.id')
            curl --fail --silent -X POST "http://127.0.0.1:5173/api/admin/articles/${article_id}/publish" > /dev/null
          }
'''
if external_function.strip() not in text:
    if function_anchor not in text:
        raise SystemExit('review fixture function anchor not found')
    text = text.replace(function_anchor, function_anchor + external_function, 1)

replacements = {
    "          create_article \"$recruitment_id\" '吉林省高校毕业生专场招聘活动公告' '2026-08-28'": "          create_external_article \"$recruitment_id\" '吉林省高校毕业生专场招聘活动公告' '2026-08-28' 'https://example.com/review/recruitment-1'",
    "          create_article \"$recruitment_id\" '长春市重点用人单位高校毕业生招聘公告' '2026-08-24'": "          create_external_article \"$recruitment_id\" '长春市重点用人单位高校毕业生招聘公告' '2026-08-24' 'https://example.com/review/recruitment-2'",
    "          create_article \"$recruitment_id\" '吉林省事业单位面向高校毕业生公开招聘信息' '2026-08-20'": "          create_external_article \"$recruitment_id\" '吉林省事业单位面向高校毕业生公开招聘信息' '2026-08-20' 'https://example.com/review/recruitment-3'",
    "          create_article \"$recruitment_id\" '高校毕业生线上双选会参会公告' '2026-08-17'": "          create_external_article \"$recruitment_id\" '高校毕业生线上双选会参会公告' '2026-08-17' 'https://example.com/review/recruitment-4'",
    "          create_article \"$recruitment_id\" '省内重点企业招聘高校毕业生岗位信息' '2026-08-13'": "          create_external_article \"$recruitment_id\" '省内重点企业招聘高校毕业生岗位信息' '2026-08-13' 'https://example.com/review/recruitment-5'",
}
for old, new in replacements.items():
    if old in text:
        text = text.replace(old, new, 1)
    elif new not in text:
        raise SystemExit(f'review fixture article anchor not found: {old}')
workflow.write_text(text)
