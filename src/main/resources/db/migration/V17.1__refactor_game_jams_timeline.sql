ALTER TABLE game_jams
DROP CONSTRAINT IF EXISTS chk_game_jams_dates,
    DROP CONSTRAINT IF EXISTS chk_game_jams_submission,
    DROP CONSTRAINT IF EXISTS chk_game_jams_judging;

ALTER TABLE game_jams
    RENAME COLUMN start_date TO registration_start;

ALTER TABLE game_jams
    RENAME COLUMN end_date TO registration_end;

ALTER TABLE game_jams
DROP COLUMN IF EXISTS submission_deadline;

ALTER TABLE game_jams
    RENAME COLUMN judging_end_date TO judging_end;

ALTER TABLE game_jams
    ALTER COLUMN judging_end SET NOT NULL;

ALTER TABLE game_jams
    ADD COLUMN jam_start TIMESTAMPTZ NOT NULL,
    ADD COLUMN jam_end TIMESTAMPTZ NOT NULL,
    ADD COLUMN judging_start TIMESTAMPTZ NOT NULL;

ALTER TABLE game_jams
    ADD CONSTRAINT chk_game_jams_phases CHECK (
        registration_start < registration_end AND
        registration_end <= jam_start AND
        jam_start < jam_end AND
        jam_end <= judging_start AND
        judging_start < judging_end AND
        registration_end >= registration_start + INTERVAL '30 minutes'
    );

DROP INDEX IF EXISTS idx_game_jams_dates;

CREATE INDEX IF NOT EXISTS idx_game_jams_registration_dates
    ON game_jams(registration_start, registration_end);

CREATE INDEX IF NOT EXISTS idx_game_jams_jam_dates
    ON game_jams(jam_start, jam_end);

CREATE INDEX IF NOT EXISTS idx_game_jams_judging_dates
    ON game_jams(judging_start, judging_end);

COMMENT ON COLUMN game_jams.registration_start IS 'Начало регистрации команд';
COMMENT ON COLUMN game_jams.registration_end IS 'Конец регистрации команд (минимум 30 минут от начала)';
COMMENT ON COLUMN game_jams.jam_start IS 'Начало разработки игр';
COMMENT ON COLUMN game_jams.jam_end IS 'Конец разработки игр (дедлайн подачи проектов)';
COMMENT ON COLUMN game_jams.judging_start IS 'Начало оценивания проектов';
COMMENT ON COLUMN game_jams.judging_end IS 'Конец оценивания проектов';