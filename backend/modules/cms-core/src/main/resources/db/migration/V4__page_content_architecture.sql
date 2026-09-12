-- EU-55: site-neutral Page content architecture.
-- Keep one persisted renderer identity while separating content shape and ownership.

ALTER TABLE cms_page
    CHANGE COLUMN render_mode renderer_key VARCHAR(100) NOT NULL DEFAULT 'RICH_TEXT';

ALTER TABLE cms_page
    ADD COLUMN content_model VARCHAR(32) NOT NULL DEFAULT 'RICH_TEXT' AFTER body_html,
    ADD COLUMN content_owner VARCHAR(32) NOT NULL DEFAULT 'OPERATOR' AFTER renderer_key,
    ADD COLUMN structured_payload LONGTEXT NULL AFTER content_owner;

UPDATE cms_page
SET content_model = CASE renderer_key
        WHEN 'RICH_TEXT' THEN 'RICH_TEXT'
        ELSE 'NONE'
    END,
    content_owner = CASE renderer_key
        WHEN 'EMBED_PLACEHOLDER' THEN 'EXTERNAL'
        WHEN 'INTERNAL_STATIC' THEN 'ENGINEERING'
        ELSE 'OPERATOR'
    END,
    structured_payload = NULL;
