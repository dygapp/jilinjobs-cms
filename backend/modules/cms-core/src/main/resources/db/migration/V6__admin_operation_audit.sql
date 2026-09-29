-- EU-70: append-only business-operation audit attempts and immutable role snapshots.

CREATE TABLE cms_admin_audit_event (
    audit_id CHAR(36) NOT NULL,
    request_correlation_id CHAR(36) NOT NULL,
    identity_source VARCHAR(100) NOT NULL,
    user_id VARCHAR(200) NOT NULL,
    action VARCHAR(40) NOT NULL,
    object_type VARCHAR(60) NOT NULL,
    object_id VARCHAR(500) NULL,
    started_at TIMESTAMP(3) NOT NULL,
    completed_at TIMESTAMP(3) NULL,
    result VARCHAR(20) NOT NULL,
    PRIMARY KEY (audit_id),
    CONSTRAINT chk_cms_admin_audit_result
        CHECK (result IN ('STARTED', 'SUCCEEDED', 'FAILED', 'ROLLED_BACK')),
    CONSTRAINT chk_cms_admin_audit_completion
        CHECK (
            (result = 'STARTED' AND completed_at IS NULL)
            OR (result <> 'STARTED' AND completed_at IS NOT NULL)
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_cms_admin_audit_actor
    ON cms_admin_audit_event(identity_source, user_id, started_at, audit_id);
CREATE INDEX idx_cms_admin_audit_object
    ON cms_admin_audit_event(object_type, object_id, started_at, audit_id);
CREATE INDEX idx_cms_admin_audit_request
    ON cms_admin_audit_event(request_correlation_id);
CREATE INDEX idx_cms_admin_audit_result
    ON cms_admin_audit_event(result, started_at, audit_id);

CREATE TABLE cms_admin_audit_role_snapshot (
    audit_id CHAR(36) NOT NULL,
    role_code VARCHAR(40) NOT NULL,
    PRIMARY KEY (audit_id, role_code),
    CONSTRAINT fk_cms_admin_audit_role_event
        FOREIGN KEY (audit_id) REFERENCES cms_admin_audit_event(audit_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
