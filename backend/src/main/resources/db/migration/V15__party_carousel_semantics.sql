-- Human Review 2026-09-03: 中心党建是主站下的特殊栏目/专题入口，不使用“首页轮播”产品语义。
-- V14 已执行，不修改历史 migration；通过稳定列表 ID 原地更新 code/name/description，既有轮播成员关系保持不变。

UPDATE cms_list
SET code = 'PARTY_CAROUSEL',
    name = '中心党建轮播',
    description = '中心党建顶部图片轮播'
WHERE code = 'PARTY_HOME_CAROUSEL';
