-- Current technical naming uses "party"; preserve executed V13/V14 migration history unchanged.
UPDATE cms_column
SET alias = 'party'
WHERE alias = 'party-building'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT alias FROM cms_column WHERE alias = 'party') AS existing_party
  );
