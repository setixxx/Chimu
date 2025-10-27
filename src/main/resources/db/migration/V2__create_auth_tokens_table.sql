CREATE TABLE IF NOT EXISTS auth_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    token_type VARCHAR(20) NOT NULL DEFAULT 'REFRESH' CHECK (token_type IN ('REFRESH', 'ACCESS')),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    user_agent TEXT,
    ip_address VARCHAR(45),

    CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_auth_tokens_token_hash UNIQUE (token_hash)
    );

CREATE INDEX IF NOT EXISTS idx_auth_tokens_user_id ON auth_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_token_hash ON auth_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_expires_at ON auth_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_revoked_at ON auth_tokens(revoked_at) WHERE revoked_at IS NULL;

CREATE OR REPLACE FUNCTION delete_expired_tokens()
RETURNS void AS $$
BEGIN
DELETE FROM auth_tokens
WHERE expires_at < NOW()
   OR revoked_at IS NOT NULL AND revoked_at < NOW() - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

COMMENT ON TABLE auth_tokens IS 'Хранение refresh и access токенов для аутентификации';
COMMENT ON COLUMN auth_tokens.token_hash IS 'SHA-256 хеш токена для безопасного хранения';
COMMENT ON COLUMN auth_tokens.token_type IS 'Тип токена: REFRESH или ACCESS';
COMMENT ON COLUMN auth_tokens.revoked_at IS 'Время отзыва токена (для logout)';
COMMENT ON COLUMN auth_tokens.last_used_at IS 'Последнее использование токена';
COMMENT ON COLUMN auth_tokens.user_agent IS 'User-Agent браузера/клиента';
COMMENT ON COLUMN auth_tokens.ip_address IS 'IP адрес клиента';