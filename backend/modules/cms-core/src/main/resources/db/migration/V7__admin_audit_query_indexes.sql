-- EU-71: stable cursor ordering and action filtering for the growing audit stream.

CREATE INDEX idx_cms_admin_audit_started
    ON cms_admin_audit_event(started_at, audit_id);

CREATE INDEX idx_cms_admin_audit_action
    ON cms_admin_audit_event(action, started_at, audit_id);
