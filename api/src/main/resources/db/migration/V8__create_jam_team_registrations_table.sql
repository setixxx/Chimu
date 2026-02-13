CREATE TYPE registration_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'WITHDRAWN'
);

CREATE TABLE IF NOT EXISTS jam_team_registrations (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      jam_id BIGINT NOT NULL,
                                                      team_id BIGINT NOT NULL,
                                                      status registration_status NOT NULL DEFAULT 'PENDING',
                                                      registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    registered_by BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_registrations_jam FOREIGN KEY (jam_id) REFERENCES game_jams(id) ON DELETE CASCADE,
    CONSTRAINT fk_registrations_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_registrations_user FOREIGN KEY (registered_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_registrations_jam_team UNIQUE (jam_id, team_id)
    );

CREATE TRIGGER trigger_update_registrations_updated_at
    BEFORE UPDATE ON jam_team_registrations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_registrations_jam_id ON jam_team_registrations(jam_id);
CREATE INDEX IF NOT EXISTS idx_registrations_team_id ON jam_team_registrations(team_id);
CREATE INDEX IF NOT EXISTS idx_registrations_status ON jam_team_registrations(status);
CREATE INDEX IF NOT EXISTS idx_registrations_registered_by ON jam_team_registrations(registered_by);

COMMENT ON TABLE jam_team_registrations IS 'Регистрации команд на джемы (связь M:N)';
COMMENT ON COLUMN jam_team_registrations.registered_by IS 'Пользователь, зарегистрировавший команду (обычно лидер)';
COMMENT ON COLUMN jam_team_registrations.status IS 'Статус регистрации (ожидание/одобрена/отклонена/отозвана)';