-- SERVICE / SITE were transitional navigation locations introduced only for
-- compatibility during the generic CMS migration. Current authority uses
-- HOME_SHORTCUT / HOME_QUICK for homepage navigation and generic lists for
-- site-link groups, so these legacy locations must not remain visible in the
-- formal CMS baseline.

-- Detach possible legacy hierarchy first so the self-referencing parent FK
-- cannot block cleanup of old rows that may exist in an upgraded database.
UPDATE cms_navigation
SET parent_id = NULL
WHERE position IN ('SERVICE', 'SITE');

DELETE FROM cms_navigation
WHERE position IN ('SERVICE', 'SITE');

DELETE FROM cms_navigation_location
WHERE code IN ('SERVICE', 'SITE');
