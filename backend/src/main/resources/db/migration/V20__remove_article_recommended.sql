CREATE INDEX idx_cms_article_column_status_sort_v20
    ON cms_article(column_id, status, pinned, sort_order, id);

DROP INDEX idx_cms_article_column_status_sort ON cms_article;

ALTER TABLE cms_article
    DROP COLUMN recommended;

ALTER TABLE cms_article
    RENAME INDEX idx_cms_article_column_status_sort_v20 TO idx_cms_article_column_status_sort;
