-- JilinJobs fresh-site initial operational defaults.
-- This script is applied once after stable Site Package structure provisioning.
-- After bootstrap, these rows are ordinary operator-managed data and are never reconciled by Site Package runtime composition.

INSERT INTO cms_list_item(list_id, source_type, title, url, image_path, open_mode, sort_order, enabled)
VALUES ((SELECT id FROM cms_list WHERE code='HOME_CAROUSEL'), 'LINK', '这里美得不愿离开', 'https://mp.weixin.qq.com/s/fUv21IynaUh_N8OjjLkDXQ', '/static/home/carousel-01.jpg', 'DEFAULT', 10, 1);

INSERT INTO cms_list_item(list_id, source_type, title, url, open_mode, sort_order, enabled) VALUES
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '中国高等教育学生信息网', 'https://www.chsi.com.cn/', 'DEFAULT', 10, 1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '国家24365大学生就业服务平台', 'https://www.ncss.cn/', 'DEFAULT', 20, 1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '学历认证', 'https://www.chsi.com.cn/xlrz/index.jsp', 'DEFAULT', 30, 1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '全国征兵网', 'https://www.gfbzb.gov.cn/', 'DEFAULT', 40, 1),
((SELECT id FROM cms_list WHERE code='SITE_RELATED'), 'LINK', '吉林省教育厅', 'http://jyt.jl.gov.cn/', 'DEFAULT', 50, 1);

INSERT INTO cms_advertisement(slot_id, title, image_path, url, open_mode, sort_order, enabled)
VALUES ((SELECT id FROM cms_ad_slot WHERE code='HOME_RECRUITMENT_PROMO'), '吉林省高校毕业生招聘活动', '/static/home/recruitment-campaign.png', 'https://24365.jl.smartedu.cn/', 'DEFAULT', 10, 1);
