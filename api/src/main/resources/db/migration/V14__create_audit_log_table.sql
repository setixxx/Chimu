CREATE TABLE IF NOT EXISTS audit_log (
                                         id BIGSERIAL PRIMARY KEY,
                                         entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id BIGINT,
    changes JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
    );

CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_log_changes ON audit_log USING GIN(changes);

COMMENT ON TABLE audit_log IS 'Журнал всех изменений в системе для аудита';
COMMENT ON COLUMN audit_log.entity_type IS 'Тип сущности (GAME_JAM, TEAM, PROJECT, USER и т.д.)';
COMMENT ON COLUMN audit_log.entity_id IS 'ID сущности';
COMMENT ON COLUMN audit_log.action IS 'Действие (CREATED, UPDATED, DELETED, STATUS_CHANGED и т.д.)';
COMMENT ON COLUMN audit_log.changes IS 'JSON с деталями изменений (до/после)';
COMMENT ON COLUMN audit_log.ip_address IS 'IP адрес пользователя';
COMMENT ON COLUMN audit_log.user_agent IS 'User-Agent браузера';

CREATE OR REPLACE FUNCTION log_game_jam_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
        VALUES ('GAME_JAM', NEW.id, 'CREATED', NEW.organizer_id,
                jsonb_build_object('data', row_to_json(NEW)));
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
        VALUES ('GAME_JAM', NEW.id, 'UPDATED', NEW.organizer_id,
                jsonb_build_object('before', row_to_json(OLD), 'after', row_to_json(NEW)));
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit_log (entity_type, entity_id, action, changes)
        VALUES ('GAME_JAM', OLD.id, 'DELETED',
                jsonb_build_object('data', row_to_json(OLD)));
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_audit_game_jams
    AFTER INSERT OR UPDATE OR DELETE ON game_jams
    FOR EACH ROW
    EXECUTE FUNCTION log_game_jam_changes();