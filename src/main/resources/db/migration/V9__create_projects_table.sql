CREATE TYPE project_status AS ENUM (
    'DRAFT',
    'SUBMITTED',
    'UNDER_REVIEW',
    'PUBLISHED',
    'DISQUALIFIED'
);

CREATE TABLE IF NOT EXISTS projects (
                                        id BIGSERIAL PRIMARY KEY,
                                        public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    team_id BIGINT NOT NULL,
    jam_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    game_url TEXT,
    repository_url TEXT,
    status project_status NOT NULL DEFAULT 'DRAFT',
    submitted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_projects_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE RESTRICT,
    CONSTRAINT fk_projects_jam FOREIGN KEY (jam_id) REFERENCES game_jams(id) ON DELETE CASCADE,
    CONSTRAINT uq_projects_public_id UNIQUE (public_id),
    CONSTRAINT uq_projects_team_jam UNIQUE (team_id, jam_id)
    );

CREATE TRIGGER trigger_update_projects_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_projects_team_id ON projects(team_id);
CREATE INDEX IF NOT EXISTS idx_projects_jam_id ON projects(jam_id);
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);
CREATE INDEX IF NOT EXISTS idx_projects_submitted_at ON projects(submitted_at DESC);
CREATE INDEX IF NOT EXISTS idx_projects_created_at ON projects(created_at DESC);

COMMENT ON TABLE projects IS 'Проекты/игры команд для джемов';
COMMENT ON COLUMN projects.game_url IS 'Ссылка на игру (Itch.io, WebGL и т.д.)';
COMMENT ON COLUMN projects.repository_url IS 'Ссылка на репозиторий (GitHub, GitLab)';
COMMENT ON COLUMN projects.submitted_at IS 'Дата и время подачи проекта';
COMMENT ON CONSTRAINT uq_projects_team_jam ON projects IS 'Одна команда может иметь только один проект на джем';