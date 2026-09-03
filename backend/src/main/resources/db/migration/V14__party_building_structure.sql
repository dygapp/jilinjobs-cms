-- EU-27: 只固化中心党建稳定站点结构，不注入历史运营文章、轮播成员或正文资源。

INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT NULL, 'party-building', '中心党建', 'OPTIONAL', 60, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias='party-building');

INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT (SELECT id FROM cms_column WHERE alias='party-building' LIMIT 1), 'party-voice', '高层声音', 'OPTIONAL', 10, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias='party-voice');

INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT (SELECT id FROM cms_column WHERE alias='party-building' LIMIT 1), 'party-work', '工作动态', 'OPTIONAL', 20, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias='party-work');

INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT (SELECT id FROM cms_column WHERE alias='party-building' LIMIT 1), 'party-rules', '党规党章', 'OPTIONAL', 30, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias='party-rules');

INSERT INTO cms_column(parent_id, alias, name, cover_policy, sort_order, enabled, preset)
SELECT (SELECT id FROM cms_column WHERE alias='party-building' LIMIT 1), 'party-study', '理论学习', 'OPTIONAL', 40, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM cms_column WHERE alias='party-study');

INSERT INTO cms_list(code, name, group_code, image_policy, description, sort_order, enabled, system_flag, preset)
SELECT 'PARTY_HOME_CAROUSEL', '中心党建首页轮播', 'PARTY', 'REQUIRED', '中心党建首页顶部图片轮播', 10, 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM cms_list WHERE code='PARTY_HOME_CAROUSEL');
