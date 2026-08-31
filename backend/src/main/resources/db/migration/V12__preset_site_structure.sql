ALTER TABLE cms_column
    ADD COLUMN preset TINYINT(1) NOT NULL DEFAULT 0 AFTER enabled;

ALTER TABLE cms_navigation_location
    ADD COLUMN preset TINYINT(1) NOT NULL DEFAULT 0 AFTER system_flag;

ALTER TABLE cms_navigation
    ADD COLUMN preset TINYINT(1) NOT NULL DEFAULT 0 AFTER enabled;

ALTER TABLE cms_page_group
    ADD COLUMN preset TINYINT(1) NOT NULL DEFAULT 0 AFTER enabled;

ALTER TABLE cms_page
    ADD COLUMN preset TINYINT(1) NOT NULL DEFAULT 0 AFTER enabled;

ALTER TABLE cms_list
    ADD COLUMN preset TINYINT(1) NOT NULL DEFAULT 0 AFTER system_flag;

ALTER TABLE cms_ad_slot
    ADD COLUMN preset TINYINT(1) NOT NULL DEFAULT 0 AFTER system_flag;

ALTER TABLE cms_site_config
    ADD COLUMN preset TINYINT(1) NOT NULL DEFAULT 0 AFTER enabled;

-- 栏目：只保护已经由站点规划明确建立的结构，不把既有运行期自定义栏目整体升级为预置数据。
UPDATE cms_column SET preset=1 WHERE alias IN (
    'notice','employment-news','policy','policy-month','policy-outside','policy-jilin','policy-national',
    'typical','typical-grassroots','typical-startup','typical-military','recruitment-announcement'
);

-- 单页分组与单页：保护站点规划中具有稳定公开 URL / Tab 语义的预置对象。
UPDATE cms_page_group SET preset=1 WHERE alias IN ('guide','jobs');
UPDATE cms_page SET preset=1 WHERE alias IN (
    'about','budget','teacher-library','live-course','employment-report-contact',
    'jypq','dagl','dygl','xlrz','contact','faq',
    'positions','recruitment','jobfair','presentation','jilin'
);

-- 导航位置和已确认的预置导航条目。运行期新增导航保持 preset=0。
UPDATE cms_navigation_location SET preset=1 WHERE code IN ('MAIN','HOME_SHORTCUT','HOME_QUICK');
UPDATE cms_navigation SET preset=1
WHERE position='MAIN' AND name IN (
    '网站首页','中心党建','招聘信息','业务指南','政策法规','就业指导','典型事迹','预决算公开','关于我们',
    '在招职位','招聘简章','双选会','现场宣讲','留省就业',
    '就业派遣','档案管理','流动党员','学历认证','联系我们','常见问题',
    '就业创业政策宣传月','省外政策','省内政策','国家政策',
    '直播课程','就业创业师资库','基层就业典型事迹','创业典型事迹','军营战士典型事迹'
);
UPDATE cms_navigation SET preset=1
WHERE position='HOME_SHORTCUT' AND name IN ('就业信息填报','学历认证','全国征兵网','预决算公开','举报电话及邮箱');
UPDATE cms_navigation SET preset=1
WHERE position='HOME_QUICK' AND name IN ('就业派遣','档案管理','流动党员','学历认证','联系我们','常见问题');

-- 页面固定依赖的结构容器。
UPDATE cms_list SET preset=1 WHERE code IN ('HOME_CAROUSEL','SITE_RELATED','SITE_REGIONAL_GRADUATES','SITE_JILIN_UNIVERSITIES');
UPDATE cms_ad_slot SET preset=1 WHERE code='HOME_RECRUITMENT_PROMO';

-- 稳定站点属性定义。值仍可维护，但定义本身不能被误删。
UPDATE cms_site_config SET preset=1 WHERE config_key IN (
    'SITE_NAME','SITE_SHORT_NAME','LOGO_PATH','PLATFORM_LOGO_ICON_PATH','PLATFORM_LOGO_TEXT_PATH','HEADER_BANNER_PATH',
    'CONTACT_PHONE','CONTACT_ADDRESS','OFFICE_HOURS','ICP_NUMBER','FOOTER_COPYRIGHT','HOME_CAROUSEL_INTERVAL_SECONDS'
);
