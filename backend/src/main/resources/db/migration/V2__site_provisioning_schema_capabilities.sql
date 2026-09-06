-- EU-41: Generic CMS schema capabilities used by Site Package provisioning.
-- This migration contains no site-specific instance data.

-- Stable identity for provisioned navigation items. Ordinary operator-created rows may keep code = NULL.
ALTER TABLE cms_navigation
    ADD COLUMN code VARCHAR(100) NULL AFTER id;

CREATE UNIQUE INDEX uk_cms_navigation_code ON cms_navigation(code);

-- Generic one-time Site Package bootstrap state.
-- Site-specific bootstrap content lives outside the Backend Flyway lineage.
CREATE TABLE cms_site_bootstrap_state (
    package_id VARCHAR(64) NOT NULL,
    bootstrap_id VARCHAR(64) NOT NULL,
    content_sha256 CHAR(64) NOT NULL,
    applied_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (package_id, bootstrap_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
