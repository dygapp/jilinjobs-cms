-- EU-60: converge CMS link open-mode values on HTML target semantics.
-- Existing observable behavior is preserved before removing Runtime URL-type inference.

ALTER TABLE cms_navigation
    MODIFY COLUMN open_mode VARCHAR(32) NULL DEFAULT NULL;

ALTER TABLE cms_list_item
    MODIFY COLUMN open_mode VARCHAR(32) NULL DEFAULT NULL;

ALTER TABLE cms_advertisement
    MODIFY COLUMN open_mode VARCHAR(32) NULL DEFAULT NULL;

UPDATE cms_navigation
SET open_mode = CASE
    WHEN open_mode = 'NEW_WINDOW' THEN '_blank'
    WHEN open_mode = 'SAME_WINDOW' THEN '_self'
    WHEN open_mode = 'DEFAULT'
         AND target_type = 'LINK'
         AND (LOWER(target_url) LIKE 'http://%' OR LOWER(target_url) LIKE 'https://%')
        THEN '_blank'
    WHEN open_mode = 'DEFAULT' THEN NULL
    ELSE open_mode
END;

UPDATE cms_list_item item
LEFT JOIN cms_article article ON article.id = item.article_id
SET item.open_mode = CASE
    WHEN item.open_mode = 'NEW_WINDOW' THEN '_blank'
    WHEN item.open_mode = 'SAME_WINDOW' THEN '_self'
    WHEN item.open_mode = 'DEFAULT'
         AND item.source_type = 'LINK'
         AND (LOWER(item.url) LIKE 'http://%' OR LOWER(item.url) LIKE 'https://%')
        THEN '_blank'
    WHEN item.open_mode = 'DEFAULT'
         AND item.source_type = 'ARTICLE'
         AND article.article_type = 'EXTERNAL_LINK'
         AND (LOWER(article.external_url) LIKE 'http://%' OR LOWER(article.external_url) LIKE 'https://%')
        THEN '_blank'
    WHEN item.open_mode = 'DEFAULT' THEN NULL
    ELSE item.open_mode
END;

UPDATE cms_advertisement
SET open_mode = CASE
    WHEN open_mode = 'NEW_WINDOW' THEN '_blank'
    WHEN open_mode = 'SAME_WINDOW' THEN '_self'
    WHEN open_mode = 'DEFAULT'
         AND (LOWER(url) LIKE 'http://%' OR LOWER(url) LIKE 'https://%')
        THEN '_blank'
    WHEN open_mode = 'DEFAULT' THEN NULL
    ELSE open_mode
END;
