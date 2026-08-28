from pathlib import Path

path = Path('docs/requirements/information-publishing.md')
text = path.read_text()
text = text.replace('updated_at: 2026-08-27', 'updated_at: 2026-08-28', 1)
old = '外链文章在首页和栏目列表中直接打开原文链接，按外部网站规则使用新窗口，不先进入本站文章详情页；即使用户直接访问其兼容文章地址，前端也应跳转原文链接。“招聘公告”栏目当前按外链文章组织，来源内容由外部网站采集或同步进入本站。'
new = '外链文章在首页和栏目列表中直接打开原文链接，按外部网站规则使用新窗口，不先进入本站文章详情页；即使用户直接访问其兼容文章地址，前端也应跳转原文链接。\n\n“招聘公告”栏目同时支持站内文章和外链文章，不在栏目层面强制内容类型。由外部网站采集或同步的招聘公告使用外链文章，只维护标题、日期、来源和原文链接；本站自主发布的招聘公告可以使用站内文章并维护本站正文。首页“招聘公告”区域用于聚合外部来源内容，只展示该栏目中已发布的外链文章；站内招聘公告仍可通过栏目列表和本站文章详情正常访问。'
if old not in text:
    raise SystemExit('recruitment rule anchor not found')
text = text.replace(old, new, 1)
acceptance_anchor = '18. 自动化验证和人工 Review Environment 能够对上述主要行为形成 Current Evidence。'
acceptance_new = acceptance_anchor + '\n19. “招聘公告”栏目可同时发布站内文章和外链文章；首页“招聘公告”区域仅展示已发布外链文章并直接打开原文链接。'
if acceptance_anchor not in text:
    raise SystemExit('acceptance anchor not found')
if '19. “招聘公告”栏目可同时发布站内文章和外链文章' not in text:
    text = text.replace(acceptance_anchor, acceptance_new, 1)
path.write_text(text)
