-- EU-49: site-neutral Page canonical migration mapping.
-- This table records explicit canonical Page content application and contains no Site-specific identity or seed data.

CREATE TABLE cms_page_legacy_mapping (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_system VARCHAR(100) NOT NULL,
    legacy_key VARCHAR(255) NOT NULL,
    source_url VARCHAR(2000) NOT NULL,
    source_fingerprint CHAR(64) NOT NULL,
    page_id BIGINT NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_page_legacy_source_key UNIQUE (source_system, legacy_key),
    CONSTRAINT fk_cms_page_legacy_page
        FOREIGN KEY (page_id) REFERENCES cms_page(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_cms_page_legacy_page ON cms_page_legacy_mapping(page_id);
