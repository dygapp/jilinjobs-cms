ALTER TABLE cms_navigation
    ADD COLUMN icon_path VARCHAR(1000) NULL AFTER open_mode;

UPDATE cms_navigation
SET icon_path = CASE name
    WHEN '就业信息填报' THEN '/static/icons/top-nav-01.png'
    WHEN '学历认证' THEN '/static/icons/top-nav-02.png'
    WHEN '全国征兵网' THEN '/static/icons/top-nav-03.png'
    WHEN '预决算公开' THEN '/static/icons/top-nav-04.png'
    WHEN '举报电话及邮箱' THEN '/static/icons/top-nav-05.png'
    ELSE icon_path
END
WHERE position = 'HOME_SHORTCUT'
  AND name IN ('就业信息填报','学历认证','全国征兵网','预决算公开','举报电话及邮箱');

UPDATE cms_navigation
SET icon_path = CASE name
    WHEN '就业派遣' THEN '/static/icons/guide-01.png'
    WHEN '档案管理' THEN '/static/icons/guide-02.png'
    WHEN '流动党员' THEN '/static/icons/guide-03.png'
    WHEN '学历认证' THEN '/static/icons/guide-04.png'
    WHEN '联系我们' THEN '/static/icons/guide-05.png'
    WHEN '常见问题' THEN '/static/icons/guide-06.png'
    ELSE icon_path
END
WHERE position = 'HOME_QUICK'
  AND name IN ('就业派遣','档案管理','流动党员','学历认证','联系我们','常见问题');

ALTER TABLE cms_list
    DROP COLUMN item_type;

UPDATE cms_ad_slot
SET name = '首页招聘活动展示位',
    description = '首页招聘活动宣传展示区域'
WHERE code = 'HOME_RECRUITMENT_PROMO';
