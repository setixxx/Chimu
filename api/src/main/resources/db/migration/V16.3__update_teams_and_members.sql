DROP TRIGGER IF EXISTS trigger_update_teams_updated_at ON teams;
ALTER TABLE teams DROP COLUMN IF EXISTS updated_at;

ALTER TABLE team_members DROP COLUMN IF EXISTS left_at;
ALTER TABLE team_members DROP COLUMN IF EXISTS role;

ALTER TABLE team_members
    ADD COLUMN specialization_id BIGINT,
    ADD CONSTRAINT fk_team_members_specialization
        FOREIGN KEY (specialization_id)
        REFERENCES specializations(id)
        ON DELETE SET NULL;

DROP INDEX IF EXISTS uq_team_members_active;

ALTER TABLE team_members
    ADD CONSTRAINT uq_team_members_team_user UNIQUE (team_id, user_id);

DROP INDEX IF EXISTS idx_team_members_left_at;
DROP INDEX IF EXISTS idx_team_members_active;

CREATE INDEX IF NOT EXISTS idx_team_members_specialization_id ON team_members(specialization_id);

COMMENT ON COLUMN team_members.specialization_id IS 'Специализация участника в команде';