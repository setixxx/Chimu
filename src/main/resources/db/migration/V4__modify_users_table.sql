CREATE TYPE user_primary_role AS ENUM (
    'DEVELOPER',
    'ARTIST_2D',
    'ARTIST_3D',
    'GAME_DESIGNER',
    'LEVEL_DESIGNER',
    'SOUND_DESIGNER',
    'COMPOSER',
    'WRITER',
    'UI_UX_DESIGNER',
    'ANIMATOR',
    'PRODUCER',
    'QA_TESTER',
    'OTHER'
);

ALTER TABLE users
DROP COLUMN IF EXISTS display_name,
    ADD COLUMN first_name VARCHAR(100),
    ADD COLUMN last_name VARCHAR(100),
    ADD COLUMN nickname VARCHAR(50) NOT NULL,
    ADD COLUMN bio TEXT,
    ADD COLUMN primary_role user_primary_role,
    ADD COLUMN skills TEXT[],
    ADD COLUMN github_url VARCHAR(255),
    ADD COLUMN telegram_username VARCHAR(100);

ALTER TABLE users
    ADD CONSTRAINT uq_users_nickname UNIQUE (nickname);

ALTER TABLE users
    ADD CONSTRAINT chk_users_nickname_format
        CHECK (nickname ~ '^[a-zA-Z0-9_]+$');

CREATE INDEX IF NOT EXISTS idx_users_nickname ON users(nickname);

CREATE INDEX IF NOT EXISTS idx_users_full_name ON users(first_name, last_name);

CREATE INDEX IF NOT EXISTS idx_users_primary_role ON users(primary_role);

COMMENT ON COLUMN users.first_name IS 'Имя пользователя';
COMMENT ON COLUMN users.last_name IS 'Фамилия пользователя';
COMMENT ON COLUMN users.nickname IS 'Уникальный никнейм (латиница, цифры, подчеркивания)';
COMMENT ON COLUMN users.bio IS 'Короткое описание о себе';
COMMENT ON COLUMN users.primary_role IS 'Основная роль в разработке игр';
COMMENT ON COLUMN users.skills IS 'Массив инструментов и технологий';
COMMENT ON COLUMN users.github_url IS 'Ссылка на профиль GitHub';
COMMENT ON COLUMN users.telegram_username IS 'Username в Telegram (без @)';