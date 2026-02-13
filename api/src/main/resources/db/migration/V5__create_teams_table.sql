CREATE TABLE IF NOT EXISTS teams (
                                     id BIGSERIAL PRIMARY KEY,
                                     public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    leader_id BIGINT NOT NULL,
    invite_token VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_teams_leader FOREIGN KEY (leader_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_teams_public_id UNIQUE (public_id),
    CONSTRAINT uq_teams_invite_token UNIQUE (invite_token)
    );

CREATE TRIGGER trigger_update_teams_updated_at
    BEFORE UPDATE ON teams
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_teams_leader_id ON teams(leader_id);
CREATE INDEX IF NOT EXISTS idx_teams_invite_token ON teams(invite_token);
CREATE INDEX IF NOT EXISTS idx_teams_created_at ON teams(created_at DESC);

COMMENT ON TABLE teams IS 'Команды участников (независимы от джемов)';
COMMENT ON COLUMN teams.invite_token IS 'Уникальный токен для вступления в команду по ссылке';
COMMENT ON COLUMN teams.leader_id IS 'Капитан команды';