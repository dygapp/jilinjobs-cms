CREATE TABLE cms_navigation_location (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    system TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_navigation_location_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO cms_navigation_location(code,name,description,sort_order,enabled,system) VALUES
('MAIN','主导航','网站 Header 主导航及多级菜单',10,1,1),
('HOME_SHORTCUT','首页快捷入口','首页首屏右侧蓝色快捷入口',20,1,1),
('HOME_QUICK','首页快速导航','首页业务指南等快速导航入口',30,1,1),
('SERVICE','服务入口（兼容）','保留既有数据兼容，后续可按实际需要迁移',90,1,1),
('SITE','网站导航（兼容）','保留既有数据兼容；网站链接组新基线使用通用列表',100,1,1);

ALTER TABLE cms_navigation
    ADD CONSTRAINT fk_cms_navigation_location FOREIGN KEY (position) REFERENCES cms_navigation_location(code) ON DELETE RESTRICT;

ALTER TABLE cms_site_config
    ADD COLUMN property_name VARCHAR(100) NOT NULL DEFAULT '' AFTER config_key,
    ADD COLUMN group_code VARCHAR(50) NOT NULL DEFAULT 'GENERAL' AFTER property_name,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER description,
    ADD COLUMN required TINYINT(1) NOT NULL DEFAULT 0 AFTER sort_order,
    ADD COLUMN system TINYINT(1) NOT NULL DEFAULT 0 AFTER required,
    ADD COLUMN enabled TINYINT(1) NOT NULL DEFAULT 1 AFTER system;

UPDATE cms_site_config SET property_name=description WHERE property_name='';
UPDATE cms_site_config SET group_code='BASIC' WHERE config_key IN ('SITE_NAME','SITE_SHORT_NAME');
UPDATE cms_site_config SET group_code='BRAND' WHERE config_key IN ('LOGO_PATH','PLATFORM_LOGO_ICON_PATH','PLATFORM_LOGO_TEXT_PATH','HEADER_BANNER_PATH');
UPDATE cms_site_config SET group_code='CONTACT' WHERE config_key IN ('CONTACT_PHONE','CONTACT_ADDRESS','OFFICE_HOURS');
UPDATE cms_site_config SET group_code='FOOTER' WHERE config_key IN ('ICP_NUMBER','FOOTER_COPYRIGHT');
UPDATE cms_site_config SET system=1, sort_order=10 WHERE config_key='SITE_NAME';
UPDATE cms_site_config SET system=1, sort_order=20 WHERE config_key='SITE_SHORT_NAME';

CREATE TABLE cms_list (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    group_code VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    item_type VARCHAR(32) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    system TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_list_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cms_list_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    list_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(500) NULL,
    url VARCHAR(1000) NULL,
    image_path VARCHAR(1000) NULL,
    open_mode VARCHAR(32) NOT NULL DEFAULT 'DEFAULT',
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    extra_json LONGTEXT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_cms_list_item_list FOREIGN KEY (list_id) REFERENCES cms_list(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_list_item_sort ON cms_list_item(list_id,enabled,sort_order,id);

CREATE TABLE cms_ad_slot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    system TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_ad_slot_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cms_advertisement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slot_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    image_path VARCHAR(1000) NOT NULL,
    url VARCHAR(1000) NULL,
    open_mode VARCHAR(32) NOT NULL DEFAULT 'DEFAULT',
    start_at DATETIME(3) NULL,
    end_at DATETIME(3) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_cms_advertisement_slot FOREIGN KEY (slot_id) REFERENCES cms_ad_slot(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_advertisement_active ON cms_advertisement(slot_id,enabled,sort_order,id);

INSERT INTO cms_navigation(parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,sort_order,enabled) VALUES
(NULL,'就业信息填报','HOME_SHORTCUT',NULL,'LINK',NULL,NULL,'https://zhjy.jilinjobs.cn/dist/index.html#/user/login?redirect=%2F','DEFAULT',10,1),
(NULL,'学历认证','HOME_SHORTCUT',NULL,'LINK',NULL,NULL,'https://www.chsi.com.cn/xlrz/index.jsp','DEFAULT',20,1),
(NULL,'全国征兵网','HOME_SHORTCUT',NULL,'LINK',NULL,NULL,'https://www.gfbzb.gov.cn/','DEFAULT',30,1),
(NULL,'预决算公开','HOME_SHORTCUT',NULL,'PAGE',NULL,(SELECT id FROM cms_page WHERE group_id IS NULL AND alias='budget' LIMIT 1),NULL,'DEFAULT',40,1),
(NULL,'举报电话及邮箱','HOME_SHORTCUT',NULL,'PAGE',NULL,(SELECT id FROM cms_page WHERE group_id IS NULL AND alias='employment-report-contact' LIMIT 1),NULL,'DEFAULT',50,1);

INSERT INTO cms_navigation(parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,sort_order,enabled)
SELECT NULL,p.name,'HOME_QUICK',NULL,'PAGE',NULL,p.id,NULL,'DEFAULT',p.sort_order,1
FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id
WHERE g.alias='guide';

INSERT INTO cms_list(code,name,group_code,item_type,description,sort_order,enabled,system) VALUES
('HOME_CAROUSEL','首页轮播','HOME','IMAGE_LINK','首页首屏轮播内容',10,1,1),
('SITE_RELATED','相关网站服务','SITE_LINKS','LINK','网站导航：相关网站服务',10,1,1),
('SITE_REGIONAL_GRADUATES','各地毕业生','SITE_LINKS','LINK','网站导航：各地毕业生',20,1,1),
('SITE_JILIN_UNIVERSITIES','吉林省高校联盟','SITE_LINKS','LINK','网站导航：吉林省高校联盟',30,1,1);

INSERT INTO cms_list_item(list_id,title,url,image_path,open_mode,sort_order,enabled)
VALUES ((SELECT id FROM cms_list WHERE code='HOME_CAROUSEL'),'这里美得不愿离开','https://mp.weixin.qq.com/s/fUv21IynaUh_N8OjjLkDXQ','/static/home/carousel-01.jpg','DEFAULT',10,1);

INSERT INTO cms_list_item(list_id,title,url,open_mode,sort_order,enabled) VALUES
((SELECT id FROM cms_list WHERE code='SITE_RELATED'),'中国高等教育学生信息网','https://www.chsi.com.cn/','DEFAULT',10,1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'),'国家24365大学生就业服务平台','https://www.ncss.cn/','DEFAULT',20,1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'),'学历认证','https://www.chsi.com.cn/xlrz/index.jsp','DEFAULT',30,1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'),'全国征兵网','https://www.gfbzb.gov.cn/','DEFAULT',40,1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'),'吉林省教育厅','http://jyt.jl.gov.cn/','DEFAULT',50,1);

INSERT INTO cms_ad_slot(code,name,description,sort_order,enabled,system)
VALUES ('HOME_RECRUITMENT_PROMO','首页招聘活动广告位','首页招聘活动横幅区域',10,1,1);
INSERT INTO cms_advertisement(slot_id,title,image_path,url,open_mode,sort_order,enabled)
VALUES ((SELECT id FROM cms_ad_slot WHERE code='HOME_RECRUITMENT_PROMO'),'吉林省高校毕业生招聘活动','/static/home/recruitment-campaign.png','https://24365.jl.smartedu.cn/','DEFAULT',10,1);

DELETE FROM cms_site_config WHERE config_key IN (
    'HOME_BANNERS',
    'SERVICE_LINKS',
    'SITE_LINK_GROUPS',
    'HOME_PROMO_BANNER_PATH',
    'HOME_NCSS_LOGO_PATH'
);
