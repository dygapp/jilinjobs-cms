ALTER TABLE cms_article
    ADD COLUMN article_type VARCHAR(20) NOT NULL DEFAULT 'INTERNAL' AFTER source,
    ADD COLUMN external_url VARCHAR(2000) NULL AFTER article_type;

UPDATE cms_page
SET name = '举报电话及邮箱'
WHERE group_id IS NULL AND alias = 'employment-report-contact';

UPDATE cms_site_config
SET config_value = 'Copyright 版权所有 吉林省高等学校毕业生就业指导中心 All Rights Reserved'
WHERE config_key = 'FOOTER_COPYRIGHT';
