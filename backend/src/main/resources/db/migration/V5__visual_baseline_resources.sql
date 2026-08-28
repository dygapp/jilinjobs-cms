UPDATE cms_site_config
SET config_value = '/static/brand/smartedu-logo-text.png'
WHERE config_key = 'LOGO_PATH';

INSERT INTO cms_site_config(config_key, config_value, value_type, description) VALUES
('PLATFORM_LOGO_ICON_PATH', '/static/brand/smartedu-logo-icon.png', 'RESOURCE_PATH', '顶部吉林智慧教育平台图标'),
('PLATFORM_LOGO_TEXT_PATH', '/static/brand/smartedu-logo-text.png', 'RESOURCE_PATH', '顶部吉林智慧教育平台文字标识'),
('HEADER_BANNER_PATH', '/static/home/header-banner.png', 'RESOURCE_PATH', '主站头部视觉 Banner'),
('HOME_PROMO_BANNER_PATH', '/static/home/recruitment-campaign.png', 'RESOURCE_PATH', '首页招聘活动横幅'),
('HOME_NCSS_LOGO_PATH', '/static/home/ncss-logo.png', 'RESOURCE_PATH', '国家大学生就业服务平台标识')
ON DUPLICATE KEY UPDATE
config_value = VALUES(config_value), value_type = VALUES(value_type), description = VALUES(description);

UPDATE cms_site_config
SET config_value = '[{"image":"/static/home/carousel-01.jpg","title":"这里美得不愿离开","url":"https://mp.weixin.qq.com/s/fUv21IynaUh_N8OjjLkDXQ"}]'
WHERE config_key = 'HOME_BANNERS';

UPDATE cms_site_config
SET config_value = '[{"name":"就业信息填报","url":"https://zhjy.jilinjobs.cn/dist/index.html#/user/login?redirect=%2F"},{"name":"学历认证","url":"https://www.chsi.com.cn/xlrz/index.jsp"},{"name":"全国征兵网","url":"https://www.gfbzb.gov.cn/"},{"name":"预决算公开","url":"/page/budget"},{"name":"举报电话及邮箱","url":"https://24365.jl.smartedu.cn/detail.html?content_id=162461430857728"}]'
WHERE config_key = 'SERVICE_LINKS';

UPDATE cms_site_config
SET config_value = '[{"name":"相关网站服务","links":[{"name":"中国高等教育学生信息网","url":"https://www.chsi.com.cn/"},{"name":"国家24365大学生就业服务平台","url":"https://www.ncss.cn/"},{"name":"学历认证","url":"https://www.chsi.com.cn/xlrz/index.jsp"},{"name":"全国征兵网","url":"https://www.gfbzb.gov.cn/"},{"name":"吉林省教育厅","url":"http://jyt.jl.gov.cn/"}]},{"name":"各地毕业生","links":[]},{"name":"吉林省高校联盟","links":[]}]'
WHERE config_key = 'SITE_LINK_GROUPS';
