-- EU-30: 通用列表支持 LINK / ARTICLE 两种内容来源，并统一轮播展示配置。

ALTER TABLE cms_list_item
    ADD COLUMN source_type VARCHAR(16) NOT NULL DEFAULT 'LINK' AFTER list_id,
    ADD COLUMN article_id BIGINT NULL AFTER source_type,
    ADD COLUMN image_resource_id BIGINT NULL AFTER image_path;

ALTER TABLE cms_list_item
    ADD CONSTRAINT fk_cms_list_item_article
        FOREIGN KEY (article_id) REFERENCES cms_article(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_cms_list_item_image_resource
        FOREIGN KEY (image_resource_id) REFERENCES cms_resource(id) ON DELETE RESTRICT;

CREATE INDEX idx_cms_list_item_article ON cms_list_item(article_id, enabled, sort_order, id);
CREATE INDEX idx_cms_list_item_image_resource ON cms_list_item(image_resource_id);

-- V11 的 Main-only 属性收敛为 Main / Party 共用的轮播展示参数。
UPDATE cms_site_config
SET config_key='CAROUSEL_INTERVAL_SECONDS',
    property_name='轮播切换间隔',
    description='轮播存在多张有效内容时的自动切换间隔，单位：秒',
    group_code='PRESENTATION',
    config_value=CASE WHEN CAST(config_value AS SIGNED) > 0 THEN config_value ELSE '4' END,
    value_type='INTEGER',
    sort_order=10,
    required=1,
    system_flag=1,
    enabled=1
WHERE config_key='HOME_CAROUSEL_INTERVAL_SECONDS';

INSERT INTO cms_site_config(
    config_key, property_name, group_code, config_value, value_type, description,
    sort_order, required, system_flag, enabled
)
SELECT
    'CAROUSEL_MAX_ITEMS',
    '轮播最大展示数量',
    'PRESENTATION',
    '5',
    'INTEGER',
    '单个轮播区域前台最多展示的有效内容数量；后台允许维护更多记录',
    20,
    1,
    1,
    1
WHERE NOT EXISTS (SELECT 1 FROM cms_site_config WHERE config_key='CAROUSEL_MAX_ITEMS');

-- 原站历史栏目名“主题教育2023”在新系统收敛为“主题教育”；legacy typeCode 只保留在迁移证据中。
INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT (SELECT id FROM cms_column WHERE alias='party-building' LIMIT 1),
       'party-theme-education', '主题教育', 'OPTIONAL', 50, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias='party-theme-education');
