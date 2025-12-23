CREATE TYPE game_jam_status_new AS ENUM (
    'REGISTRATION_OPEN',
    'REGISTRATION_CLOSED',
    'IN_PROGRESS',
    'JUDGING',
    'COMPLETED',
    'CANCELLED'
);

ALTER TABLE game_jams
    ALTER COLUMN status DROP DEFAULT,
ALTER COLUMN status TYPE game_jam_status_new
        USING 'REGISTRATION_OPEN'::game_jam_status_new;

DROP TYPE game_jam_status;

ALTER TYPE game_jam_status_new RENAME TO game_jam_status;

ALTER TABLE game_jams
    ALTER COLUMN status SET DEFAULT 'REGISTRATION_OPEN'::game_jam_status;

COMMENT ON TYPE game_jam_status IS 'Статусы game jam: REGISTRATION_OPEN (идет регистрация), REGISTRATION_CLOSED (регистрация закончена), IN_PROGRESS (идет разработка), JUDGING (идет оценивание), COMPLETED (завершен), CANCELLED (отменен)';