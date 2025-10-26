CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    email CITEXT NOT NULL,
    password_hash TEXT NOT NULL,
    role VARCHAR NOT NULL DEFAULT 'PARTICIPANT' CHECK (role IN ('PARTICIPANT','ORGANIZER','JUDGE','ADMIN','GUEST')),
    display_name VARCHAR(120) NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_public_id UNIQUE (public_id),
    CONSTRAINT uq_users_email UNIQUE (email)
    );

-- Авто-обновление updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Полезные индексы
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
