CREATE TABLE cms_article (
    id BIGINT NOT NULL AUTO_INCREMENT,
    column_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    body_html LONGTEXT NOT NULL,
    source VARCHAR(200) NOT NULL DEFAULT '',
    publish_date DATE NULL,
    pinned TINYINT(1) NOT NULL DEFAULT 0,
    recommended TINYINT(1) NOT NULL DEFAULT 0,
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

CREATE INDEX idx_cms_article_column_status_sort
    ON cms_article(column_id, status, pinned, recommended, sort_order, id);

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

CREATE INDEX idx_cms_article_resource_article_role
    ON cms_article_resource(article_id, resource_role, sort_order, resource_id);
