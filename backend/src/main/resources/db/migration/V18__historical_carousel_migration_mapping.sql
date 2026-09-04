-- EU-29: 仅增加中心党建历史轮播迁移所需的追溯/幂等结构，不通过 Flyway 注入历史轮播成员或图片。
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

CREATE INDEX idx_cms_list_item_legacy_image
    ON cms_list_item_legacy_mapping(source_system, image_sha256);
