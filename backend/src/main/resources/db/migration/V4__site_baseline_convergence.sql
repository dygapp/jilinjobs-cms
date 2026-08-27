ALTER TABLE cms_column ADD COLUMN alias VARCHAR(100) NULL AFTER parent_id;
UPDATE cms_column SET alias = CONCAT('legacy-', id) WHERE alias IS NULL OR alias = '';
ALTER TABLE cms_column MODIFY COLUMN alias VARCHAR(100) NOT NULL;
CREATE UNIQUE INDEX uk_cms_column_alias ON cms_column(alias);

CREATE TABLE cms_page_group (
    id BIGINT NOT NULL AUTO_INCREMENT,
    alias VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_page_group_alias UNIQUE (alias)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cms_page (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NULL,
    alias VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    body_html LONGTEXT NOT NULL,
    render_mode VARCHAR(32) NOT NULL DEFAULT 'RICH_TEXT',
    embed_url VARCHAR(1000) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_cms_page_group FOREIGN KEY (group_id) REFERENCES cms_page_group(id) ON DELETE RESTRICT,
    CONSTRAINT uk_cms_page_group_alias UNIQUE (group_id, alias)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_page_group_sort ON cms_page(group_id, enabled, sort_order, id);

CREATE TABLE cms_site_config (
    config_key VARCHAR(100) NOT NULL,
    config_value LONGTEXT NOT NULL,
    value_type VARCHAR(32) NOT NULL DEFAULT 'TEXT',
    description VARCHAR(255) NOT NULL DEFAULT '',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE cms_navigation
    ADD COLUMN parent_id BIGINT NULL AFTER id,
    ADD COLUMN target_page_id BIGINT NULL AFTER target_column_id,
    ADD COLUMN open_mode VARCHAR(32) NOT NULL DEFAULT 'DEFAULT' AFTER target_url;
CREATE INDEX idx_cms_navigation_parent_sort ON cms_navigation(parent_id, sort_order, id);
ALTER TABLE cms_navigation
    ADD CONSTRAINT fk_cms_navigation_parent FOREIGN KEY (parent_id) REFERENCES cms_navigation(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_cms_navigation_page FOREIGN KEY (target_page_id) REFERENCES cms_page(id) ON DELETE RESTRICT;

INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT NULL, 'notice', '通知公告', 10, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'notice');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT NULL, 'employment-news', '就业动态', 20, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'employment-news');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT NULL, 'policy', '政策法规', 30, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'policy');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT (SELECT id FROM cms_column WHERE alias='policy' LIMIT 1), 'policy-month', '就业创业政策宣传月', 10, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'policy-month');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT (SELECT id FROM cms_column WHERE alias='policy' LIMIT 1), 'policy-outside', '省外政策', 20, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'policy-outside');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT (SELECT id FROM cms_column WHERE alias='policy' LIMIT 1), 'policy-jilin', '省内政策', 30, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'policy-jilin');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT (SELECT id FROM cms_column WHERE alias='policy' LIMIT 1), 'policy-national', '国家政策', 40, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'policy-national');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT NULL, 'typical', '典型事迹', 40, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'typical');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT (SELECT id FROM cms_column WHERE alias='typical' LIMIT 1), 'typical-grassroots', '基层就业典型事迹', 10, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'typical-grassroots');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT (SELECT id FROM cms_column WHERE alias='typical' LIMIT 1), 'typical-startup', '创业典型事迹', 20, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'typical-startup');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT (SELECT id FROM cms_column WHERE alias='typical' LIMIT 1), 'typical-military', '军营战士典型事迹', 30, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'typical-military');
INSERT INTO cms_column(parent_id, alias, name, sort_order, enabled)
SELECT NULL, 'recruitment-announcement', '招聘公告', 50, 1 WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias = 'recruitment-announcement');

INSERT INTO cms_page_group(alias, name, sort_order, enabled) VALUES
('guide', '业务指南', 10, 1),
('jobs', '招聘信息', 20, 1);

INSERT INTO cms_page(group_id, alias, name, body_html, render_mode, sort_order, enabled) VALUES
(NULL, 'about', '关于我们', '<p>关于我们内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 10, 1),
(NULL, 'budget', '预决算公开', '<p>预决算公开内容可通过固定页面管理维护，并可引用相关文件。</p>', 'RICH_TEXT', 20, 1),
(NULL, 'teacher-library', '就业创业师资库', '<p>就业创业师资库内容可通过固定页面管理维护，并可引用网站静态资源中的人物图片。</p>', 'RICH_TEXT', 30, 1),
(NULL, 'live-course', '直播课程', '<p>直播课程由外部平台提供，本轮保留页面入口与展示占位。</p>', 'EMBED_PLACEHOLDER', 40, 1),
((SELECT id FROM cms_page_group WHERE alias='guide'), 'jypq', '就业派遣', '<p>就业派遣页面保留专用展示扩展能力，第一版使用业务指南通用页面框架。</p>', 'RICH_TEXT', 10, 1),
((SELECT id FROM cms_page_group WHERE alias='guide'), 'dagl', '档案管理', '<p>档案管理业务指南内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 20, 1),
((SELECT id FROM cms_page_group WHERE alias='guide'), 'dygl', '流动党员', '<p>流动党员业务指南内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 30, 1),
((SELECT id FROM cms_page_group WHERE alias='guide'), 'xlrz', '学历认证', '<p>学历认证业务指南内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 40, 1),
((SELECT id FROM cms_page_group WHERE alias='guide'), 'contact', '联系我们', '<p>联系我们内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 50, 1),
((SELECT id FROM cms_page_group WHERE alias='guide'), 'faq', '常见问题', '<p>常见问题内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 60, 1),
((SELECT id FROM cms_page_group WHERE alias='jobs'), 'positions', '在招职位', '<p>在招职位由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 10, 1),
((SELECT id FROM cms_page_group WHERE alias='jobs'), 'recruitment', '招聘简章', '<p>招聘简章由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 20, 1),
((SELECT id FROM cms_page_group WHERE alias='jobs'), 'jobfair', '双选会', '<p>双选会由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 30, 1),
((SELECT id FROM cms_page_group WHERE alias='jobs'), 'presentation', '现场宣讲', '<p>现场宣讲由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 40, 1),
((SELECT id FROM cms_page_group WHERE alias='jobs'), 'jilin', '留省就业', '<p>留省就业由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 50, 1);

INSERT INTO cms_site_config(config_key, config_value, value_type, description) VALUES
('SITE_NAME', '吉林省高等学校毕业生就业信息网', 'TEXT', '网站名称'),
('SITE_SHORT_NAME', '吉林就业', 'TEXT', '网站简称'),
('LOGO_PATH', '', 'RESOURCE_PATH', '网站 Logo 静态资源路径'),
('CONTACT_PHONE', '0431-84657570 0431-84657571', 'TEXT', '业务咨询电话'),
('CONTACT_ADDRESS', '长春市经济技术开发区金川街151号', 'TEXT', '办公地址'),
('OFFICE_HOURS', '周一至周五，法定假日不对外办公', 'TEXT', '办公时间'),
('ICP_NUMBER', '吉ICP备09006292号-3', 'TEXT', 'ICP备案号'),
('FOOTER_COPYRIGHT', 'Copyright 2019 版权所有 吉林省高等学校毕业生就业指导中心 All Rights Reserved', 'TEXT', '页脚版权信息'),
('HOME_BANNERS', '[]', 'JSON', '首页 Banner 配置'),
('SERVICE_LINKS', '[]', 'JSON', '首页固定业务入口'),
('SITE_LINK_GROUPS', '[]', 'JSON', '网站导航与友情链接组');

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, target_page_id, target_url, open_mode, sort_order, enabled)
VALUES (NULL, '网站首页', 'MAIN', NULL, 'HOME', NULL, NULL, NULL, 'DEFAULT', 10, 1),
       (NULL, '中心党建', 'MAIN', NULL, 'PLACEHOLDER', NULL, NULL, NULL, 'DEFAULT', 20, 1),
       (NULL, '招聘信息', 'MAIN', NULL, 'PLACEHOLDER', NULL, NULL, NULL, 'DEFAULT', 30, 1),
       (NULL, '业务指南', 'MAIN', NULL, 'PLACEHOLDER', NULL, NULL, NULL, 'DEFAULT', 40, 1),
       (NULL, '政策法规', 'MAIN', NULL, 'COLUMN', (SELECT id FROM cms_column WHERE alias='policy'), NULL, NULL, 'DEFAULT', 50, 1),
       (NULL, '就业指导', 'MAIN', NULL, 'PLACEHOLDER', NULL, NULL, NULL, 'DEFAULT', 60, 1),
       (NULL, '典型事迹', 'MAIN', NULL, 'COLUMN', (SELECT id FROM cms_column WHERE alias='typical'), NULL, NULL, 'DEFAULT', 70, 1),
       (NULL, '预决算公开', 'MAIN', NULL, 'PAGE', NULL, (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='budget'), NULL, 'DEFAULT', 80, 1),
       (NULL, '关于我们', 'MAIN', NULL, 'PAGE', NULL, (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='about'), NULL, 'DEFAULT', 90, 1);

SET @jobs_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='招聘信息' ORDER BY id DESC LIMIT 1);
SET @guide_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='业务指南' ORDER BY id DESC LIMIT 1);
SET @policy_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='政策法规' ORDER BY id DESC LIMIT 1);
SET @guidance_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='就业指导' ORDER BY id DESC LIMIT 1);
SET @typical_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='典型事迹' ORDER BY id DESC LIMIT 1);

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_page_id, open_mode, sort_order, enabled)
SELECT @jobs_navigation_id, p.name, 'MAIN', NULL, 'PAGE', p.id, 'DEFAULT', p.sort_order, 1
FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id WHERE g.alias='jobs';

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_page_id, open_mode, sort_order, enabled)
SELECT @guide_navigation_id, p.name, 'MAIN', NULL, 'PAGE', p.id, 'DEFAULT', p.sort_order, 1
FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id WHERE g.alias='guide';

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, open_mode, sort_order, enabled)
SELECT @policy_navigation_id, name, 'MAIN', NULL, 'COLUMN', id, 'DEFAULT', sort_order, 1
FROM cms_column WHERE parent_id=(SELECT id FROM cms_column WHERE alias='policy');

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_page_id, open_mode, sort_order, enabled) VALUES
(@guidance_navigation_id, '直播课程', 'MAIN', NULL, 'PAGE', (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='live-course'), 'DEFAULT', 10, 1),
(@guidance_navigation_id, '就业创业师资库', 'MAIN', NULL, 'PAGE', (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='teacher-library'), 'DEFAULT', 20, 1);

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, open_mode, sort_order, enabled)
SELECT @typical_navigation_id, name, 'MAIN', NULL, 'COLUMN', id, 'DEFAULT', sort_order, 1
FROM cms_column WHERE parent_id=(SELECT id FROM cms_column WHERE alias='typical');
