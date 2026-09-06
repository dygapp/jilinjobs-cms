-- EU-39: add an explicit, provisioning-only stable identity for navigation items.
-- Existing operator-created navigation rows remain valid with code = NULL.
ALTER TABLE cms_navigation
    ADD COLUMN code VARCHAR(100) NULL AFTER id;

CREATE UNIQUE INDEX uk_cms_navigation_code ON cms_navigation(code);
