INSERT INTO cms_page(group_id, alias, name, body_html, render_mode, sort_order, enabled)
SELECT NULL,
       'employment-report-contact',
       '举报电话及邮箱',
       '<p>吉林省高校毕业生就业工作举报电话：</p><p>0431-84657570<br>0431-84657571</p><p>举报邮箱：scb@jilinjobs.cn</p>',
       'RICH_TEXT',
       50,
       1
WHERE NOT EXISTS (
    SELECT 1 FROM cms_page WHERE group_id IS NULL AND alias = 'employment-report-contact'
);

UPDATE cms_site_config
SET config_value = '[{"name":"就业信息填报","url":"https://zhjy.jilinjobs.cn/dist/index.html#/user/login?redirect=%2F"},{"name":"学历认证","url":"https://www.chsi.com.cn/xlrz/index.jsp"},{"name":"全国征兵网","url":"https://www.gfbzb.gov.cn/"},{"name":"预决算公开","url":"/page/budget"},{"name":"举报电话及邮箱","url":"/page/employment-report-contact"}]'
WHERE config_key = 'SERVICE_LINKS';
