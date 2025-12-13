CREATE TABLE IF NOT EXISTS team_members (
                                            id BIGSERIAL PRIMARY KEY,
                                            team_id BIGINT NOT NULL,
                                            user_id BIGINT NOT NULL,
                                            role VARCHAR(50),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    left_at TIMESTAMPTZ,

    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE UNIQUE INDEX uq_team_members_active
    ON team_members(team_id, user_id)
    WHERE left_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_team_members_team_id ON team_members(team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user_id ON team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_team_members_left_at ON team_members(left_at);

CREATE INDEX IF NOT EXISTS idx_team_members_active
    ON team_members(team_id, user_id)
    WHERE left_at IS NULL;

COMMENT ON TABLE team_members IS 'Участники команд с историей вступления/выхода';
COMMENT ON COLUMN team_members.role IS 'Роль участника в команде (Developer, Artist, Designer и т.д.)';
COMMENT ON COLUMN team_members.left_at IS 'Дата выхода из команды (NULL если активен)';