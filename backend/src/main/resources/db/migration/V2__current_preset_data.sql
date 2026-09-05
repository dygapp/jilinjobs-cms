-- EU-31: curated current preset/site baseline.
-- This file intentionally seeds no historical Party articles or Party carousel members.

-- Main-site and Party columns.
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset) VALUES
(NULL, 'notice', '通知公告', 'OPTIONAL', 10, 1, 1),
(NULL, 'employment-news', '就业动态', 'OPTIONAL', 20, 1, 1),
(NULL, 'policy', '政策法规', 'OPTIONAL', 30, 1, 1),
(NULL, 'typical', '典型事迹', 'OPTIONAL', 40, 1, 1),
(NULL, 'recruitment-announcement', '招聘公告', 'OPTIONAL', 50, 1, 1),
(NULL, 'party', '中心党建', 'OPTIONAL', 60, 1, 1);

INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'policy-month', '就业创业政策宣传月', 'OPTIONAL', 10, 1, 1 FROM cms_column WHERE alias='policy';
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'policy-outside', '省外政策', 'OPTIONAL', 20, 1, 1 FROM cms_column WHERE alias='policy';
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'policy-jilin', '省内政策', 'OPTIONAL', 30, 1, 1 FROM cms_column WHERE alias='policy';
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'policy-national', '国家政策', 'OPTIONAL', 40, 1, 1 FROM cms_column WHERE alias='policy';

INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'typical-grassroots', '基层就业典型事迹', 'OPTIONAL', 10, 1, 1 FROM cms_column WHERE alias='typical';
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'typical-startup', '创业典型事迹', 'OPTIONAL', 20, 1, 1 FROM cms_column WHERE alias='typical';
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'typical-military', '军营战士典型事迹', 'OPTIONAL', 30, 1, 1 FROM cms_column WHERE alias='typical';

INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'party-voice', '高层声音', 'OPTIONAL', 10, 1, 1 FROM cms_column WHERE alias='party';
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'party-work', '工作动态', 'OPTIONAL', 20, 1, 1 FROM cms_column WHERE alias='party';
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'party-rules', '党规党章', 'OPTIONAL', 30, 1, 1 FROM cms_column WHERE alias='party';
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'party-study', '理论学习', 'OPTIONAL', 40, 1, 1 FROM cms_column WHERE alias='party';
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT id, 'party-theme-education', '主题教育', 'OPTIONAL', 50, 1, 1 FROM cms_column WHERE alias='party';

-- Stable page groups and fixed pages.
INSERT INTO cms_page_group(alias, name, sort_order, enabled, preset) VALUES
('guide', '业务指南', 10, 1, 1),
('jobs', '招聘信息', 20, 1, 1);

INSERT INTO cms_page(group_id, alias, name, body_html, render_mode, embed_url, sort_order, enabled, preset) VALUES
(NULL, 'about', '关于我们', '<p>关于我们内容可通过固定页面管理维护。</p>', 'RICH_TEXT', NULL, 10, 1, 1),
(NULL, 'budget', '预决算公开', '<p>预决算公开内容可通过固定页面管理维护，并可引用相关文件。</p>', 'RICH_TEXT', NULL, 20, 1, 1),
(NULL, 'teacher-library', '就业创业师资库', '<p>就业创业师资库内容可通过固定页面管理维护，并可引用网站静态资源中的人物图片。</p>', 'RICH_TEXT', NULL, 30, 1, 1),
(NULL, 'live-course', '直播课程', '<p>直播课程由外部平台提供，本轮保留页面入口与展示占位。</p>', 'EMBED_PLACEHOLDER', NULL, 40, 1, 1),
(NULL, 'employment-report-contact', '举报电话及邮箱', '<p>为贯彻落实教育部、吉林省教育厅有关高校毕业生就业工作相关要求，严格落实“四不准”纪律要求，不准以任何方式强迫、诱导毕业生签订就业协议和劳动合同，不准将毕业证书、学位证书发放与毕业生签约挂钩，不准以户档托管为由劝说毕业生签订虚假就业协议，不准将毕业生顶岗实习、见习证明材料作为就业证明材料。现将吉林省高校毕业生就业工作举报电话和邮箱公布如下：</p><p>举报电话：</p><p>0431-84657570<br>0431-84657571</p><p>举报邮箱：</p><p>xxb@jilinjobs.cn</p>', 'RICH_TEXT', NULL, 50, 1, 1);

INSERT INTO cms_page(group_id, alias, name, body_html, render_mode, embed_url, sort_order, enabled, preset)
SELECT g.id, p.alias, p.name, p.body_html, p.render_mode, NULL, p.sort_order, 1, 1
FROM cms_page_group g
JOIN (
    SELECT 'guide' group_alias, 'jypq' alias, '就业派遣' name, '<p>就业派遣页面保留专用展示扩展能力，第一版使用业务指南通用页面框架。</p>' body_html, 'RICH_TEXT' render_mode, 10 sort_order
    UNION ALL SELECT 'guide', 'dagl', '档案管理', '<p>档案管理业务指南内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 20
    UNION ALL SELECT 'guide', 'dygl', '流动党员', '<p>流动党员业务指南内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 30
    UNION ALL SELECT 'guide', 'xlrz', '学历认证', '<p>学历认证业务指南内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 40
    UNION ALL SELECT 'guide', 'contact', '联系我们', '<p>联系我们内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 50
    UNION ALL SELECT 'guide', 'faq', '常见问题', '<p>常见问题内容可通过固定页面管理维护。</p>', 'RICH_TEXT', 60
    UNION ALL SELECT 'jobs', 'positions', '在招职位', '<p>在招职位由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 10
    UNION ALL SELECT 'jobs', 'recruitment', '招聘简章', '<p>招聘简章由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 20
    UNION ALL SELECT 'jobs', 'jobfair', '双选会', '<p>双选会由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 30
    UNION ALL SELECT 'jobs', 'presentation', '现场宣讲', '<p>现场宣讲由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 40
    UNION ALL SELECT 'jobs', 'jilin', '留省就业', '<p>留省就业由慧就业等外部平台提供，本轮保留嵌入区域占位。</p>', 'EMBED_PLACEHOLDER', 50
) p ON p.group_alias=g.alias;

-- Only current formal navigation locations are seeded. SERVICE/SITE were transitional and are intentionally absent.
INSERT INTO cms_navigation_location(code, name, description, sort_order, enabled, system_flag, preset) VALUES
('MAIN', '主导航', '网站 Header 主导航及多级菜单', 10, 1, 1, 1),
('HOME_SHORTCUT', '首页快捷入口', '首页首屏右侧蓝色快捷入口', 20, 1, 1, 1),
('HOME_QUICK', '首页快速导航', '首页业务指南等快速导航入口', 30, 1, 1, 1);

-- Current stable site-property definitions and values.
INSERT INTO cms_site_config(
    config_key, property_name, group_code, config_value, value_type, description,
    sort_order, required, system_flag, enabled, preset
) VALUES
('SITE_NAME', '网站名称', 'BASIC', '吉林省高等学校毕业生就业信息网', 'TEXT', '网站名称', 10, 0, 1, 1, 1),
('SITE_SHORT_NAME', '网站简称', 'BASIC', '吉林就业', 'TEXT', '网站简称', 20, 0, 1, 1, 1),
('LOGO_PATH', '网站 Logo 静态资源路径', 'BRAND', '/static/brand/smartedu-logo-text.png', 'RESOURCE_PATH', '网站 Logo 静态资源路径', 0, 0, 0, 1, 1),
('PLATFORM_LOGO_ICON_PATH', '顶部吉林智慧教育平台图标', 'BRAND', '/static/brand/smartedu-logo-icon.png', 'RESOURCE_PATH', '顶部吉林智慧教育平台图标', 0, 0, 0, 1, 1),
('PLATFORM_LOGO_TEXT_PATH', '顶部吉林智慧教育平台文字标识', 'BRAND', '/static/brand/smartedu-logo-text.png', 'RESOURCE_PATH', '顶部吉林智慧教育平台文字标识', 0, 0, 0, 1, 1),
('HEADER_BANNER_PATH', '主站头部视觉 Banner', 'BRAND', '/static/home/header-banner.png', 'RESOURCE_PATH', '主站头部视觉 Banner', 0, 0, 0, 1, 1),
('CONTACT_PHONE', '业务咨询电话', 'CONTACT', '0431-84657570 0431-84657571', 'TEXT', '业务咨询电话', 0, 0, 0, 1, 1),
('CONTACT_ADDRESS', '办公地址', 'CONTACT', '长春市经济技术开发区金川街151号', 'TEXT', '办公地址', 0, 0, 0, 1, 1),
('OFFICE_HOURS', '办公时间', 'CONTACT', '周一至周五，法定假日不对外办公', 'TEXT', '办公时间', 0, 0, 0, 1, 1),
('ICP_NUMBER', 'ICP备案号', 'FOOTER', '吉ICP备09006292号-3', 'TEXT', 'ICP备案号', 0, 0, 0, 1, 1),
('FOOTER_COPYRIGHT', '页脚版权信息', 'FOOTER', 'Copyright 版权所有 吉林省高等学校毕业生就业指导中心 All Rights Reserved', 'TEXT', '页脚版权信息', 0, 0, 0, 1, 1),
('CAROUSEL_INTERVAL_SECONDS', '轮播切换间隔', 'PRESENTATION', '4', 'INTEGER', '轮播存在多张有效内容时的自动切换间隔，单位：秒', 10, 1, 1, 1, 1),
('CAROUSEL_MAX_ITEMS', '轮播最大展示数量', 'PRESENTATION', '5', 'INTEGER', '单个轮播区域前台最多展示的有效内容数量；后台允许维护更多记录', 20, 1, 1, 1, 1);

-- Main navigation uses the current Party route directly rather than the retired placeholder state.
INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, target_page_id, target_url, open_mode, icon_path, sort_order, enabled, preset) VALUES
(NULL, '网站首页', 'MAIN', NULL, 'HOME', NULL, NULL, NULL, 'DEFAULT', NULL, 10, 1, 1),
(NULL, '中心党建', 'MAIN', NULL, 'LINK', NULL, NULL, '/party/', 'SAME_WINDOW', NULL, 20, 1, 1),
(NULL, '招聘信息', 'MAIN', NULL, 'PLACEHOLDER', NULL, NULL, NULL, 'DEFAULT', NULL, 30, 1, 1),
(NULL, '业务指南', 'MAIN', NULL, 'PLACEHOLDER', NULL, NULL, NULL, 'DEFAULT', NULL, 40, 1, 1),
(NULL, '政策法规', 'MAIN', NULL, 'COLUMN', (SELECT id FROM cms_column WHERE alias='policy'), NULL, NULL, 'DEFAULT', NULL, 50, 1, 1),
(NULL, '就业指导', 'MAIN', NULL, 'PLACEHOLDER', NULL, NULL, NULL, 'DEFAULT', NULL, 60, 1, 1),
(NULL, '典型事迹', 'MAIN', NULL, 'COLUMN', (SELECT id FROM cms_column WHERE alias='typical'), NULL, NULL, 'DEFAULT', NULL, 70, 1, 1),
(NULL, '预决算公开', 'MAIN', NULL, 'PAGE', NULL, (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='budget'), NULL, 'DEFAULT', NULL, 80, 1, 1),
(NULL, '关于我们', 'MAIN', NULL, 'PAGE', NULL, (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='about'), NULL, 'DEFAULT', NULL, 90, 1, 1);

SET @jobs_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='招聘信息' LIMIT 1);
SET @guide_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='业务指南' LIMIT 1);
SET @policy_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='政策法规' LIMIT 1);
SET @guidance_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='就业指导' LIMIT 1);
SET @typical_navigation_id = (SELECT id FROM cms_navigation WHERE parent_id IS NULL AND name='典型事迹' LIMIT 1);

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, target_page_id, target_url, open_mode, icon_path, sort_order, enabled, preset)
SELECT @jobs_navigation_id, p.name, 'MAIN', NULL, 'PAGE', NULL, p.id, NULL, 'DEFAULT', NULL, p.sort_order, 1, 1
FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id WHERE g.alias='jobs';

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, target_page_id, target_url, open_mode, icon_path, sort_order, enabled, preset)
SELECT @guide_navigation_id, p.name, 'MAIN', NULL, 'PAGE', NULL, p.id, NULL, 'DEFAULT', NULL, p.sort_order, 1, 1
FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id WHERE g.alias='guide';

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, target_page_id, target_url, open_mode, icon_path, sort_order, enabled, preset)
SELECT @policy_navigation_id, c.name, 'MAIN', NULL, 'COLUMN', c.id, NULL, NULL, 'DEFAULT', NULL, c.sort_order, 1, 1
FROM cms_column c WHERE c.parent_id=(SELECT id FROM cms_column WHERE alias='policy');

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, target_page_id, target_url, open_mode, icon_path, sort_order, enabled, preset) VALUES
(@guidance_navigation_id, '直播课程', 'MAIN', NULL, 'PAGE', NULL, (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='live-course'), NULL, 'DEFAULT', NULL, 10, 1, 1),
(@guidance_navigation_id, '就业创业师资库', 'MAIN', NULL, 'PAGE', NULL, (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='teacher-library'), NULL, 'DEFAULT', NULL, 20, 1, 1);

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, target_page_id, target_url, open_mode, icon_path, sort_order, enabled, preset)
SELECT @typical_navigation_id, c.name, 'MAIN', NULL, 'COLUMN', c.id, NULL, NULL, 'DEFAULT', NULL, c.sort_order, 1, 1
FROM cms_column c WHERE c.parent_id=(SELECT id FROM cms_column WHERE alias='typical');

-- Homepage shortcut and guide navigation.
INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, target_page_id, target_url, open_mode, icon_path, sort_order, enabled, preset) VALUES
(NULL, '就业信息填报', 'HOME_SHORTCUT', NULL, 'LINK', NULL, NULL, 'https://zhjy.jilinjobs.cn/dist/index.html#/user/login?redirect=%2F', 'DEFAULT', '/static/icons/top-nav-01.png', 10, 1, 1),
(NULL, '学历认证', 'HOME_SHORTCUT', NULL, 'LINK', NULL, NULL, 'https://www.chsi.com.cn/xlrz/index.jsp', 'DEFAULT', '/static/icons/top-nav-02.png', 20, 1, 1),
(NULL, '全国征兵网', 'HOME_SHORTCUT', NULL, 'LINK', NULL, NULL, 'https://www.gfbzb.gov.cn/', 'DEFAULT', '/static/icons/top-nav-03.png', 30, 1, 1),
(NULL, '预决算公开', 'HOME_SHORTCUT', NULL, 'PAGE', NULL, (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='budget'), NULL, 'DEFAULT', '/static/icons/top-nav-04.png', 40, 1, 1),
(NULL, '举报电话及邮箱', 'HOME_SHORTCUT', NULL, 'PAGE', NULL, (SELECT id FROM cms_page WHERE group_id IS NULL AND alias='employment-report-contact'), NULL, 'DEFAULT', '/static/icons/top-nav-05.png', 50, 1, 1);

INSERT INTO cms_navigation(parent_id, name, position, category, target_type, target_column_id, target_page_id, target_url, open_mode, icon_path, sort_order, enabled, preset)
SELECT NULL, p.name, 'HOME_QUICK', NULL, 'PAGE', NULL, p.id, NULL, 'DEFAULT',
       CASE p.alias
           WHEN 'jypq' THEN '/static/icons/guide-01.png'
           WHEN 'dagl' THEN '/static/icons/guide-02.png'
           WHEN 'dygl' THEN '/static/icons/guide-03.png'
           WHEN 'xlrz' THEN '/static/icons/guide-04.png'
           WHEN 'contact' THEN '/static/icons/guide-05.png'
           WHEN 'faq' THEN '/static/icons/guide-06.png'
       END,
       p.sort_order, 1, 1
FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id WHERE g.alias='guide';

-- Current generic list definitions. Party historical members are deliberately not seeded.
INSERT INTO cms_list(code, name, group_code, image_policy, description, sort_order, enabled, system_flag, preset) VALUES
('HOME_CAROUSEL', '首页轮播', 'HOME', 'REQUIRED', '首页首屏轮播内容', 10, 1, 1, 1),
('SITE_RELATED', '相关网站服务', 'SITE_LINKS', 'NONE', '网站导航：相关网站服务', 10, 1, 1, 1),
('SITE_REGIONAL_GRADUATES', '各地毕业生', 'SITE_LINKS', 'NONE', '网站导航：各地毕业生', 20, 1, 1, 1),
('SITE_JILIN_UNIVERSITIES', '吉林省高校联盟', 'SITE_LINKS', 'NONE', '网站导航：吉林省高校联盟', 30, 1, 1, 1),
('PARTY_CAROUSEL', '中心党建轮播', 'PARTY', 'REQUIRED', '中心党建顶部图片轮播', 10, 1, 1, 1);

INSERT INTO cms_list_item(list_id, source_type, title, url, image_path, open_mode, sort_order, enabled)
VALUES ((SELECT id FROM cms_list WHERE code='HOME_CAROUSEL'), 'LINK', '这里美得不愿离开', 'https://mp.weixin.qq.com/s/fUv21IynaUh_N8OjjLkDXQ', '/static/home/carousel-01.jpg', 'DEFAULT', 10, 1);

INSERT INTO cms_list_item(list_id, source_type, title, url, open_mode, sort_order, enabled) VALUES
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '中国高等教育学生信息网', 'https://www.chsi.com.cn/', 'DEFAULT', 10, 1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '国家24365大学生就业服务平台', 'https://www.ncss.cn/', 'DEFAULT', 20, 1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '学历认证', 'https://www.chsi.com.cn/xlrz/index.jsp', 'DEFAULT', 30, 1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '全国征兵网', 'https://www.gfbzb.gov.cn/', 'DEFAULT', 40, 1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '吉林省教育厅', 'http://jyt.jl.gov.cn/', 'DEFAULT', 50, 1);

INSERT INTO cms_ad_slot(code, name, description, sort_order, enabled, system_flag, preset)
VALUES ('HOME_RECRUITMENT_PROMO', '首页招聘活动展示位', '首页招聘活动宣传展示区域', 10, 1, 1, 1);
INSERT INTO cms_advertisement(slot_id, title, image_path, url, open_mode, sort_order, enabled)
VALUES ((SELECT id FROM cms_ad_slot WHERE code='HOME_RECRUITMENT_PROMO'), '吉林省高校毕业生招聘活动', '/static/home/recruitment-campaign.png', 'https://24365.jl.smartedu.cn/', 'DEFAULT', 10, 1);
