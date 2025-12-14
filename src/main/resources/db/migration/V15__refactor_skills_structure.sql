CREATE TABLE IF NOT EXISTS skills (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS user_skills (
                                           id BIGSERIAL PRIMARY KEY,
                                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE(user_id, skill_id)
    );

ALTER TABLE users DROP COLUMN IF EXISTS skills;

CREATE INDEX IF NOT EXISTS idx_user_skills_user ON user_skills(user_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_skill ON user_skills(skill_id);