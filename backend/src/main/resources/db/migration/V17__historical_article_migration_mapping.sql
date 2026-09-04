-- EU-29: Flyway 只增加历史内容迁移所需的稳定追溯/幂等结构，不注入历史运营内容。
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

CREATE INDEX idx_cms_article_legacy_content
    ON cms_article_legacy_mapping(source_system, type_code, content_id);
