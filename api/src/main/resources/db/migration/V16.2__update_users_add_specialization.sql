DROP INDEX IF EXISTS idx_users_primary_role;
ALTER TABLE users DROP COLUMN IF EXISTS primary_role;
DROP TYPE IF EXISTS user_primary_role;

ALTER TABLE users
    ADD COLUMN specialization_id BIGINT,
    ADD CONSTRAINT fk_users_specialization
        FOREIGN KEY (specialization_id)
        REFERENCES specializations(id)
        ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_users_specialization_id ON users(specialization_id);

COMMENT ON COLUMN users.specialization_id IS 'Основная специализация пользователя';