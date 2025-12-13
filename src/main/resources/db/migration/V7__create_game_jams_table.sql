CREATE TYPE game_jam_status AS ENUM (
    'DRAFT',
    'ANNOUNCED',
    'IN_PROGRESS',
    'JUDGING',
    'COMPLETED',
    'CANCELLED'
);

CREATE TABLE IF NOT EXISTS game_jams (
                                         id BIGSERIAL PRIMARY KEY,
                                         public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    organizer_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    theme VARCHAR(200),
    rules TEXT,
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ NOT NULL,
    submission_deadline TIMESTAMPTZ NOT NULL,
    judging_end_date TIMESTAMPTZ,
    max_team_size INT NOT NULL DEFAULT 10,
    min_team_size INT NOT NULL DEFAULT 1,
    status game_jam_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_game_jams_organizer FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_game_jams_public_id UNIQUE (public_id),
    CONSTRAINT chk_game_jams_dates CHECK (start_date < end_date),
    CONSTRAINT chk_game_jams_submission CHECK (submission_deadline >= end_date),
    CONSTRAINT chk_game_jams_judging CHECK (judging_end_date IS NULL OR judging_end_date >= submission_deadline),
    CONSTRAINT chk_game_jams_team_size CHECK (min_team_size > 0 AND min_team_size <= max_team_size)
    );

CREATE TRIGGER trigger_update_game_jams_updated_at
    BEFORE UPDATE ON game_jams
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_game_jams_organizer_id ON game_jams(organizer_id);
CREATE INDEX IF NOT EXISTS idx_game_jams_status ON game_jams(status);
CREATE INDEX IF NOT EXISTS idx_game_jams_dates ON game_jams(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_game_jams_created_at ON game_jams(created_at DESC);

COMMENT ON TABLE game_jams IS 'Game Jam события';
COMMENT ON COLUMN game_jams.theme IS 'Тема джема';
COMMENT ON COLUMN game_jams.rules IS 'Правила и ограничения джема';
COMMENT ON COLUMN game_jams.submission_deadline IS 'Крайний срок подачи проектов';
COMMENT ON COLUMN game_jams.judging_end_date IS 'Окончание периода оценивания';