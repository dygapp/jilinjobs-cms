-- EU-31: curated development baseline for the current post-EU-30 CMS runtime schema.
-- Historical Party business content remains outside Flyway under data-migrations/party/v1.

CREATE TABLE cms_column (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NULL,
    alias VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    cover_policy VARCHAR(16) NOT NULL DEFAULT 'OPTIONAL',
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    preset TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_column_alias UNIQUE (alias),
    CONSTRAINT fk_cms_column_parent
        FOREIGN KEY (parent_id) REFERENCES cms_column(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_column_parent_sort ON cms_column(parent_id, sort_order, id);

CREATE TABLE cms_page_group (
    id BIGINT NOT NULL AUTO_INCREMENT,
    alias VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    preset TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_page_group_alias UNIQUE (alias)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cms_page (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NULL,
    alias VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    body_html LONGTEXT NOT NULL,
    render_mode VARCHAR(32) NOT NULL DEFAULT 'RICH_TEXT',
    embed_url VARCHAR(1000) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    preset TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_page_group_alias UNIQUE (group_id, alias),
    CONSTRAINT fk_cms_page_group
        FOREIGN KEY (group_id) REFERENCES cms_page_group(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_page_group_sort ON cms_page(group_id, enabled, sort_order, id);

CREATE TABLE cms_navigation_location (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    system_flag TINYINT(1) NOT NULL DEFAULT 0,
    preset TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_navigation_location_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cms_navigation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    position VARCHAR(32) NOT NULL,
    category VARCHAR(100) NULL,
    target_type VARCHAR(32) NOT NULL,
    target_column_id BIGINT NULL,
    target_page_id BIGINT NULL,
    target_url VARCHAR(1000) NULL,
    open_mode VARCHAR(32) NOT NULL DEFAULT 'DEFAULT',
    icon_path VARCHAR(1000) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    preset TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_cms_navigation_parent
        FOREIGN KEY (parent_id) REFERENCES cms_navigation(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_cms_navigation_page
        FOREIGN KEY (target_page_id) REFERENCES cms_page(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_cms_navigation_location
        FOREIGN KEY (position) REFERENCES cms_navigation_location(code)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_navigation_public ON cms_navigation(enabled, position, category, sort_order, id);
CREATE INDEX idx_cms_navigation_parent_sort ON cms_navigation(parent_id, sort_order, id);

CREATE TABLE cms_site_config (
    config_key VARCHAR(100) NOT NULL,
    property_name VARCHAR(100) NOT NULL DEFAULT '',
    group_code VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    config_value LONGTEXT NOT NULL,
    value_type VARCHAR(32) NOT NULL DEFAULT 'TEXT',
    description VARCHAR(255) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    required TINYINT(1) NOT NULL DEFAULT 0,
    system_flag TINYINT(1) NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    preset TINYINT(1) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cms_article (
    id BIGINT NOT NULL AUTO_INCREMENT,
    column_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    body_html LONGTEXT NOT NULL,
    source VARCHAR(200) NOT NULL DEFAULT '',
    article_type VARCHAR(20) NOT NULL DEFAULT 'INTERNAL',
    external_url VARCHAR(2000) NULL,
    publish_date DATE NULL,
    pinned TINYINT(1) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    actual_published_at TIMESTAMP(3) NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_cms_article_column
        FOREIGN KEY (column_id) REFERENCES cms_column(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_article_column_status_sort ON cms_article(column_id, status, pinned, sort_order, id);

CREATE TABLE cms_resource (
    id BIGINT NOT NULL AUTO_INCREMENT,
    storage_key VARCHAR(100) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_resource_storage_key UNIQUE (storage_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cms_article_resource (
    article_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    resource_role VARCHAR(20) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (article_id, resource_id, resource_role),
    CONSTRAINT fk_cms_article_resource_article
        FOREIGN KEY (article_id) REFERENCES cms_article(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cms_article_resource_resource
        FOREIGN KEY (resource_id) REFERENCES cms_resource(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_article_resource_article_role ON cms_article_resource(article_id, resource_role, sort_order, resource_id);

CREATE TABLE cms_list (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    group_code VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    image_policy VARCHAR(16) NOT NULL DEFAULT 'OPTIONAL',
    description VARCHAR(255) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    system_flag TINYINT(1) NOT NULL DEFAULT 0,
    preset TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_list_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cms_list_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    list_id BIGINT NOT NULL,
    source_type VARCHAR(16) NOT NULL DEFAULT 'LINK',
    article_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(500) NULL,
    url VARCHAR(1000) NULL,
    image_path VARCHAR(1000) NULL,
    image_resource_id BIGINT NULL,
    open_mode VARCHAR(32) NOT NULL DEFAULT 'DEFAULT',
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    extra_json LONGTEXT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_cms_list_item_list
        FOREIGN KEY (list_id) REFERENCES cms_list(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cms_list_item_article
        FOREIGN KEY (article_id) REFERENCES cms_article(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_cms_list_item_image_resource
        FOREIGN KEY (image_resource_id) REFERENCES cms_resource(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_list_item_sort ON cms_list_item(list_id, enabled, sort_order, id);
CREATE INDEX idx_cms_list_item_article ON cms_list_item(article_id, enabled, sort_order, id);
CREATE INDEX idx_cms_list_item_image_resource ON cms_list_item(image_resource_id);

CREATE TABLE cms_ad_slot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    system_flag TINYINT(1) NOT NULL DEFAULT 0,
    preset TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_ad_slot_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cms_advertisement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slot_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    image_path VARCHAR(1000) NOT NULL,
    url VARCHAR(1000) NULL,
    open_mode VARCHAR(32) NOT NULL DEFAULT 'DEFAULT',
    start_at DATETIME(3) NULL,
    end_at DATETIME(3) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_cms_advertisement_slot
        FOREIGN KEY (slot_id) REFERENCES cms_ad_slot(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_advertisement_active ON cms_advertisement(slot_id, enabled, sort_order, id);

-- These empty mapping tables are part of the runtime migration support model.
-- Canonical historical content populates them through the dedicated importers, never through Flyway seed data.
CREATE TABLE cms_article_legacy_mapping (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_system VARCHAR(100) NOT NULL,
    legacy_key VARCHAR(255) NOT NULL,
    content_id VARCHAR(100) NULL,
    type_code VARCHAR(100) NOT NULL,
    detail_path VARCHAR(500) NOT NULL,
    source_url VARCHAR(2000) NOT NULL,
    source_fingerprint CHAR(64) NOT NULL,
    article_id BIGINT NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_article_legacy_source_key UNIQUE (source_system, legacy_key),
    CONSTRAINT fk_cms_article_legacy_article
        FOREIGN KEY (article_id) REFERENCES cms_article(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_article_legacy_content ON cms_article_legacy_mapping(source_system, type_code, content_id);

CREATE TABLE cms_list_item_legacy_mapping (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_system VARCHAR(100) NOT NULL,
    legacy_key VARCHAR(255) NOT NULL,
    source_url VARCHAR(2000) NOT NULL,
    source_fingerprint CHAR(64) NOT NULL,
    image_source_url VARCHAR(2000) NOT NULL,
    image_sha256 CHAR(64) NOT NULL,
    list_item_id BIGINT NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uk_cms_list_item_legacy_source_key UNIQUE (source_system, legacy_key),
    CONSTRAINT fk_cms_list_item_legacy_item
        FOREIGN KEY (list_item_id) REFERENCES cms_list_item(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_cms_list_item_legacy_image ON cms_list_item_legacy_mapping(source_system, image_sha256);
