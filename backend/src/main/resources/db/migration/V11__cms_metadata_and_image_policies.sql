ALTER TABLE cms_column
    ADD COLUMN cover_policy VARCHAR(16) NOT NULL DEFAULT 'OPTIONAL' AFTER name;

ALTER TABLE cms_list
    ADD COLUMN image_policy VARCHAR(16) NOT NULL DEFAULT 'OPTIONAL' AFTER group_code;

UPDATE cms_list SET image_policy='REQUIRED' WHERE code='HOME_CAROUSEL';
UPDATE cms_list SET image_policy='NONE' WHERE code IN ('SITE_RELATED','SITE_REGIONAL_GRADUATES','SITE_JILIN_UNIVERSITIES');

INSERT INTO cms_site_config(
    config_key, property_name, group_code, config_value, value_type, description,
    sort_order, required, system_flag, enabled
) VALUES (
    'HOME_CAROUSEL_INTERVAL_SECONDS',
    '首页轮播切换间隔',
    'PRESENTATION',
    '4',
    'INTEGER',
    '首页轮播存在多张有效图片时的自动切换间隔，单位：秒',
    10,
    1,
    1,
    1
);
