ALTER TABLE game_jams
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE teams
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE projects
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN game_jams.version IS 'Версия для оптимистичной блокировки';
COMMENT ON COLUMN teams.version IS 'Версия для оптимистичной блокировки';
COMMENT ON COLUMN projects.version IS 'Версия для оптимистичной блокировки';