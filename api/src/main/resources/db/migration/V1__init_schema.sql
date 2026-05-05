CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS citext;

-- Specializations
CREATE TABLE IF NOT EXISTS specializations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_specializations_name ON specializations(name);

INSERT INTO specializations (name, description) VALUES
('Developer', 'Программист, разработчик игровой логики'),
('Artist 2D', 'Художник 2D графики'),
('Artist 3D', '3D художник и моделлер'),
('Game Designer', 'Геймдизайнер'),
('Level Designer', 'Дизайнер уровней'),
('Sound Designer', 'Звуковой дизайнер'),
('Composer', 'Композитор'),
('Writer', 'Сценарист'),
('UI/UX Designer', 'Дизайнер интерфейсов'),
('Animator', 'Аниматор'),
('Producer', 'Продюсер'),
('QA Tester', 'Тестировщик'),
('Other', 'Другая специализация');

CREATE TYPE user_roles AS ENUM (
    'PARTICIPANT','ORGANIZER','JUDGE','ADMIN','GUEST'
);

-- Users
CREATE TABLE IF NOT EXISTS users (
     id BIGSERIAL PRIMARY KEY,
     public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
     email CITEXT NOT NULL,
     password_hash TEXT NOT NULL,
     role user_roles NOT NULL DEFAULT 'PARTICIPANT',
     first_name VARCHAR(100),
     last_name VARCHAR(100),
     nickname VARCHAR(50) NOT NULL,
     bio TEXT,
     specialization_id BIGINT,
     github_url VARCHAR(255),
     telegram_username VARCHAR(100),
     avatar_url TEXT,
     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
     updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
     deleted_at TIMESTAMPTZ DEFAULT NULL,
     CONSTRAINT chk_users_nickname_format CHECK (nickname ~ '^[a-zA-Z0-9_]+$'),
     CONSTRAINT fk_users_specialization FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE SET NULL
);

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

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_active ON users (email) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_nickname_active ON users (nickname) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_public_id_active ON users (public_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_nickname ON users(nickname);
CREATE INDEX IF NOT EXISTS idx_users_full_name ON users(first_name, last_name);
CREATE INDEX IF NOT EXISTS idx_users_specialization_id ON users(specialization_id);

-- Role request
CREATE TYPE role_request_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED'
);

CREATE TABLE IF NOT EXISTS role_upgrade_requests (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    user_id BIGINT NOT NULL,
    requested_role user_roles NOT NULL CHECK (requested_role IN ('ORGANIZER', 'JUDGE')),
    status role_request_status NOT NULL DEFAULT 'PENDING',
    user_message TEXT,
    admin_message TEXT,
    reviewed_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,
    reviewed_at TIMESTAMPTZ,

    CONSTRAINT fk_role_requests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_role_requests_admin FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE UNIQUE INDEX uq_pending_role_requests ON role_upgrade_requests(user_id, requested_role) WHERE status = 'PENDING' AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_role_requests_status ON role_upgrade_requests(status);
CREATE INDEX IF NOT EXISTS idx_role_requests_user_id ON role_upgrade_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_role_requests_created_at ON role_upgrade_requests(created_at DESC);

CREATE TRIGGER trigger_update_role_requests_updated_at
    BEFORE UPDATE ON role_upgrade_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE FUNCTION log_role_request_changes()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
        VALUES ('ROLE_REQUEST', NEW.id, 'CREATED', NEW.user_id,
                jsonb_build_object('requested_role', NEW.requested_role, 'status', NEW.status));
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.status != NEW.status THEN
            INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
            VALUES ('ROLE_REQUEST', NEW.id, 'STATUS_CHANGED', NEW.reviewed_by,
                    jsonb_build_object(
                        'status_before', OLD.status,
                        'status_after', NEW.status,
                        'admin_message', NEW.admin_message
                    ));
        END IF;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_audit_role_requests
    AFTER INSERT OR UPDATE ON role_upgrade_requests
                        FOR EACH ROW
                        EXECUTE FUNCTION log_role_request_changes();

-- Auth tokens
CREATE TABLE IF NOT EXISTS auth_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    user_agent TEXT,
    ip_address VARCHAR(45),

    CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
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


-- Teams
CREATE TABLE IF NOT EXISTS teams (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    leader_id BIGINT NOT NULL,
    invite_token VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_teams_leader FOREIGN KEY (leader_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_teams_invite_token UNIQUE (invite_token)
);

CREATE INDEX IF NOT EXISTS idx_teams_leader_id ON teams(leader_id);
CREATE INDEX IF NOT EXISTS idx_teams_invite_token ON teams(invite_token);
CREATE INDEX IF NOT EXISTS idx_teams_created_at ON teams(created_at DESC);
CREATE UNIQUE INDEX uq_teams_public_id ON teams (public_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_teams_invite_token_active ON teams (invite_token) WHERE deleted_at IS NULL;

-- Team members
CREATE TABLE IF NOT EXISTS team_members (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    specialization_id BIGINT,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,

    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE RESTRICT,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_team_members_specialization FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_team_members_team_id ON team_members(team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user_id ON team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_team_members_specialization_id ON team_members(specialization_id);
CREATE UNIQUE INDEX uq_team_members_team_user_active ON team_members (team_id, user_id) WHERE deleted_at IS NULL;

-- Game Jam Status
CREATE TYPE game_jam_status AS ENUM (
    'ANNOUNCED',
    'REGISTRATION_OPEN',
    'REGISTRATION_CLOSED',
    'IN_PROGRESS',
    'JUDGING',
    'COMPLETED',
    'CANCELLED'
);

-- Game jams
CREATE TABLE IF NOT EXISTS game_jams (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    organizer_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    banner_url TEXT,
    theme VARCHAR(200),
    rules TEXT,
    registration_start TIMESTAMPTZ NOT NULL,
    registration_end TIMESTAMPTZ NOT NULL,
    jam_start TIMESTAMPTZ NOT NULL,
    jam_end TIMESTAMPTZ NOT NULL,
    judging_start TIMESTAMPTZ NOT NULL,
    judging_end TIMESTAMPTZ NOT NULL,
    max_team_size INT NOT NULL DEFAULT 10,
    min_team_size INT NOT NULL DEFAULT 1,
    status game_jam_status NOT NULL DEFAULT 'ANNOUNCED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_game_jams_organizer FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_game_jams_team_size CHECK (min_team_size > 0 AND min_team_size <= max_team_size),
    CONSTRAINT chk_game_jams_phases CHECK (
    registration_start < registration_end AND
    registration_end <= jam_start AND
    jam_start < jam_end AND
    jam_end <= judging_start AND
    judging_start < judging_end AND
    registration_end >= registration_start + INTERVAL '30 minutes'
    )
);

CREATE TRIGGER trigger_update_game_jams_updated_at
    BEFORE UPDATE ON game_jams
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_game_jams_organizer_id ON game_jams(organizer_id);
CREATE INDEX IF NOT EXISTS idx_game_jams_status ON game_jams(status);
CREATE INDEX IF NOT EXISTS idx_game_jams_created_at ON game_jams(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_game_jams_registration_dates ON game_jams(registration_start, registration_end);
CREATE INDEX IF NOT EXISTS idx_game_jams_jam_dates ON game_jams(jam_start, jam_end);
CREATE INDEX IF NOT EXISTS idx_game_jams_judging_dates ON game_jams(judging_start, judging_end);
CREATE UNIQUE INDEX uq_game_jams_public_id_active ON game_jams (public_id) WHERE deleted_at IS NULL;

-- Jam transfer
CREATE TYPE transfer_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'REJECTED',
    'CANCELLED'
);

CREATE TABLE IF NOT EXISTS jam_transfer_requests (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    jam_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    status transfer_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '3 days'),
    deleted_at TIMESTAMPTZ DEFAULT NULL,

    CONSTRAINT fk_transfer_jam FOREIGN KEY (jam_id) REFERENCES game_jams(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfer_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfer_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_not_self_transfer CHECK (sender_id <> recipient_id)
);

CREATE UNIQUE INDEX uq_pending_jam_transfer ON jam_transfer_requests(jam_id) WHERE status = 'PENDING' AND deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_transfer_recipient ON jam_transfer_requests(recipient_id, status);

CREATE TRIGGER trigger_update_jam_transfer_updated_at
    BEFORE UPDATE ON jam_transfer_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE FUNCTION log_jam_transfer_changes()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
        VALUES ('JAM_TRANSFER', NEW.id, 'TRANSFER_OFFERED', NEW.sender_id,
                jsonb_build_object('jam_id', NEW.jam_id, 'to_user_id', NEW.recipient_id));
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.status != NEW.status THEN
            INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
            VALUES ('JAM_TRANSFER', NEW.id, 'TRANSFER_' || NEW.status,
                   (CASE WHEN NEW.status = 'ACCEPTED' THEN NEW.recipient_id ELSE NEW.sender_id END),
                   jsonb_build_object('jam_id', NEW.jam_id, 'status_before', OLD.status, 'status_after', NEW.status));
END IF;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_audit_jam_transfers
    AFTER INSERT OR UPDATE ON jam_transfer_requests
    FOR EACH ROW
    EXECUTE FUNCTION log_jam_transfer_changes();

-- Registration statuses
CREATE TYPE registration_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'WITHDRAWN',
    'CANCELLED'
);

-- Jam team registrations
CREATE TABLE IF NOT EXISTS jam_team_registrations (
    id BIGSERIAL PRIMARY KEY,
    jam_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    status registration_status NOT NULL DEFAULT 'PENDING',
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    registered_by BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,

    CONSTRAINT fk_registrations_jam FOREIGN KEY (jam_id) REFERENCES game_jams(id) ON DELETE RESTRICT,
    CONSTRAINT fk_registrations_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE RESTRICT,
    CONSTRAINT fk_registrations_user FOREIGN KEY (registered_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TRIGGER trigger_update_registrations_updated_at
    BEFORE UPDATE ON jam_team_registrations
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_registrations_jam_id ON jam_team_registrations(jam_id);
CREATE INDEX IF NOT EXISTS idx_registrations_team_id ON jam_team_registrations(team_id);
CREATE INDEX IF NOT EXISTS idx_registrations_status ON jam_team_registrations(status);
CREATE INDEX IF NOT EXISTS idx_registrations_registered_by ON jam_team_registrations(registered_by);
CREATE UNIQUE INDEX uq_registrations_jam_team_active ON jam_team_registrations (jam_id, team_id) WHERE deleted_at IS NULL;

-- Project statuses
CREATE TYPE project_status AS ENUM (
    'DRAFT',
    'SUBMITTED',
    'UNDER_REVIEW',
    'PUBLISHED',
    'DISQUALIFIED'
);

-- Projects
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    team_id BIGINT NOT NULL,
    jam_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    game_url TEXT,
    status project_status NOT NULL DEFAULT 'DRAFT',
    submitted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_projects_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE RESTRICT,
    CONSTRAINT fk_projects_jam FOREIGN KEY (jam_id) REFERENCES game_jams(id) ON DELETE RESTRICT
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
CREATE UNIQUE INDEX uq_projects_team_jam_active ON projects (team_id, jam_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_projects_public_id_active ON projects (public_id) WHERE deleted_at IS NULL;

-- Project file type
CREATE TYPE project_file_type AS ENUM (
    'SCREENSHOT',
    'BUILD',
    'VIDEO',
    'DOCUMENT',
    'OTHER'
);

-- Project files
CREATE TABLE IF NOT EXISTS project_files (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    project_id BIGINT NOT NULL,
    file_type project_file_type NOT NULL,
    file_url TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,

    CONSTRAINT fk_project_files_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE RESTRICT,
    CONSTRAINT fk_project_files_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_project_files_size CHECK (file_size > 0 AND file_size <= 1073741824)
);

CREATE INDEX IF NOT EXISTS idx_project_files_project_id ON project_files(project_id);
CREATE INDEX IF NOT EXISTS idx_project_files_file_type ON project_files(file_type);
CREATE INDEX IF NOT EXISTS idx_project_files_uploaded_by ON project_files(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_project_files_uploaded_at ON project_files(uploaded_at DESC);


-- Rating criteria
CREATE TABLE IF NOT EXISTS rating_criteria (
    id BIGSERIAL PRIMARY KEY,
    jam_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    max_score INT NOT NULL DEFAULT 10,
    weight DECIMAL(3,2) NOT NULL DEFAULT 1.0,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,

    CONSTRAINT fk_rating_criteria_jam FOREIGN KEY (jam_id) REFERENCES game_jams(id) ON DELETE RESTRICT,
    CONSTRAINT chk_rating_criteria_max_score CHECK (max_score > 0 AND max_score <= 100),
    CONSTRAINT chk_rating_criteria_weight CHECK (weight > 0 AND weight <= 10)
);

CREATE UNIQUE INDEX uq_rating_criteria_jam_name ON rating_criteria(jam_id, name) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_rating_criteria_jam_id ON rating_criteria(jam_id);
CREATE INDEX IF NOT EXISTS idx_rating_criteria_order ON rating_criteria(jam_id, order_index);


-- Jam judges
CREATE TABLE IF NOT EXISTS jam_judges (
    id BIGSERIAL PRIMARY KEY,
    jam_id BIGINT NOT NULL,
    judge_id BIGINT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,
    assigned_by BIGINT NOT NULL,

    CONSTRAINT fk_jam_judges_jam FOREIGN KEY (jam_id) REFERENCES game_jams(id) ON DELETE RESTRICT,
    CONSTRAINT fk_jam_judges_judge FOREIGN KEY (judge_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_jam_judges_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_jam_judges_jam_id ON jam_judges(jam_id);
CREATE INDEX IF NOT EXISTS idx_jam_judges_judge_id ON jam_judges(judge_id);
CREATE INDEX IF NOT EXISTS idx_jam_judges_assigned_by ON jam_judges(assigned_by);
CREATE UNIQUE INDEX uq_jam_judges_jam_judge_active ON jam_judges(jam_id, judge_id) WHERE deleted_at IS NULL;

-- Ratings
CREATE TABLE IF NOT EXISTS ratings (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    judge_id BIGINT NOT NULL,
    criteria_id BIGINT NOT NULL,
    score DECIMAL(4,2) NOT NULL,
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ratings_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ratings_judge FOREIGN KEY (judge_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ratings_criteria FOREIGN KEY (criteria_id) REFERENCES rating_criteria(id) ON DELETE RESTRICT,
    CONSTRAINT chk_ratings_score CHECK (score >= 0)
);

CREATE INDEX IF NOT EXISTS idx_ratings_project_id ON ratings(project_id);
CREATE INDEX IF NOT EXISTS idx_ratings_judge_id ON ratings(judge_id);
CREATE INDEX IF NOT EXISTS idx_ratings_criteria_id ON ratings(criteria_id);
CREATE INDEX IF NOT EXISTS idx_ratings_created_at ON ratings(created_at DESC);
CREATE UNIQUE INDEX uq_ratings_project_judge_criteria_active ON ratings(project_id, judge_id, criteria_id) WHERE deleted_at IS NULL;

-- Audit log
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id BIGINT,
    changes JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_log_changes ON audit_log USING GIN(changes);

CREATE OR REPLACE FUNCTION log_game_jam_changes()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
        VALUES ('GAME_JAM', NEW.id, 'CREATED', NEW.organizer_id,
                jsonb_build_object('data', row_to_json(NEW)));
    ELSIF TG_OP = 'UPDATE' THEN
        IF NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL THEN
            INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
            VALUES ('GAME_JAM', NEW.id, 'DELETED', NEW.organizer_id,
                    jsonb_build_object('data', row_to_json(OLD)));
        ELSE
            INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
            VALUES ('GAME_JAM', NEW.id, 'UPDATED', NEW.organizer_id,
                    jsonb_build_object('before', row_to_json(OLD), 'after', row_to_json(NEW)));
        END IF;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_audit_game_jams
    AFTER INSERT OR UPDATE OR DELETE ON game_jams
    FOR EACH ROW
EXECUTE FUNCTION log_game_jam_changes();

CREATE OR REPLACE FUNCTION log_rating_changes()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
        VALUES ('RATING', NEW.id, 'CREATED', NEW.judge_id,
                jsonb_build_object('score', NEW.score, 'criteria_id', NEW.criteria_id));
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.score != NEW.score THEN
            INSERT INTO audit_log (entity_type, entity_id, action, user_id, changes)
            VALUES ('RATING', NEW.id, 'UPDATED', NEW.judge_id,
                    jsonb_build_object('score_before', OLD.score, 'score_after', NEW.score));
        END IF;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_audit_ratings
    AFTER INSERT OR UPDATE ON ratings
    FOR EACH ROW
EXECUTE FUNCTION log_rating_changes();

-- Skills
CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- User skills
CREATE TABLE IF NOT EXISTS user_skills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    UNIQUE(user_id, skill_id)
);

INSERT INTO skills (id, name) VALUES (1, 'Программирование (Java/Kotlin)');
INSERT INTO skills (id, name) VALUES (2, 'Геймдизайн');
INSERT INTO skills (id, name) VALUES (3, '2D-графика (Pixel Art)');
INSERT INTO skills (id, name) VALUES (4, '3D-моделирование');
INSERT INTO skills (id, name) VALUES (5, 'Анимация');
INSERT INTO skills (id, name) VALUES (6, 'Звуковой дизайн');
INSERT INTO skills (id, name) VALUES (7, 'Музыка / Композиция');
INSERT INTO skills (id, name) VALUES (8, 'UI/UX дизайн');
INSERT INTO skills (id, name) VALUES (9, 'Тестирование (QA)');
INSERT INTO skills (id, name) VALUES (10, 'Нарратив / Сценарий');

CREATE INDEX IF NOT EXISTS idx_user_skills_user ON user_skills(user_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_skill ON user_skills(skill_id);

-- Triggers for soft delete
-- User delete
CREATE OR REPLACE FUNCTION prevent_active_user_delete()
    RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM teams WHERE leader_id = NEW.id AND deleted_at IS NULL) THEN
        RAISE EXCEPTION 'Cannot delete account: transfer leadership first.';
    END IF;

    IF EXISTS (SELECT 1 FROM game_jams WHERE organizer_id = NEW.id AND deleted_at IS NULL AND status NOT IN ('COMPLETED','CANCELLED')) THEN
        RAISE EXCEPTION 'Cannot delete account: transfer game jam ownership first.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM jam_judges jj
        JOIN game_jams gj ON jj.jam_id = gj.id
        WHERE jj.judge_id = NEW.id AND gj.status NOT IN ('COMPLETED', 'CANCELLED') AND gj.deleted_at IS NULL
    ) THEN
        RAISE EXCEPTION 'Cannot delete account: active judge in an ongoing jam.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_prevent_active_user_delete
        BEFORE UPDATE OF deleted_at ON users
        FOR EACH ROW
        WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
        EXECUTE FUNCTION prevent_active_user_delete();

CREATE OR REPLACE FUNCTION handle_user_soft_delete_side_effects()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE auth_tokens SET revoked_at = NOW() WHERE user_id = NEW.id AND revoked_at IS NULL;

    UPDATE jam_judges
    SET deleted_at = NOW()
    WHERE judge_id = NEW.id
      AND jam_id IN (SELECT id FROM game_jams WHERE status IN ('ANNOUNCED','REGISTRATION_OPEN', 'REGISTRATION_CLOSED'))
      AND deleted_at IS NULL;

    DELETE FROM user_skills WHERE user_id = NEW.id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_handle_user_soft_delete_side_effects
    AFTER UPDATE OF deleted_at ON users
    FOR EACH ROW
    WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
    EXECUTE FUNCTION handle_user_soft_delete_side_effects();

-- Projects delete
CREATE OR REPLACE FUNCTION handle_project_soft_delete()
    RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM game_jams WHERE id = NEW.jam_id AND status IN ('JUDGING')) THEN
        RAISE EXCEPTION 'Cannot delete project during judging phase.';
    END IF;

    UPDATE project_files SET deleted_at = NOW() WHERE project_id = NEW.id AND deleted_at IS NULL;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_handle_project_soft_delete
        BEFORE UPDATE OF deleted_at ON projects
        FOR EACH ROW
        WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
        EXECUTE FUNCTION handle_project_soft_delete();


-- Team delete
CREATE OR REPLACE FUNCTION handle_team_soft_delete()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE jam_team_registrations SET status = 'CANCELLED' WHERE team_id = NEW.id AND status IN ('PENDING', 'APPROVED');
    UPDATE team_members SET deleted_at = NOW() WHERE team_id = NEW.id AND deleted_at IS NULL;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_handle_team_soft_delete
        AFTER UPDATE OF deleted_at ON teams
        FOR EACH ROW
        WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
        EXECUTE FUNCTION handle_team_soft_delete();

-- Rating criteria delete
CREATE OR REPLACE FUNCTION check_rating_criteria_delete()
    RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM game_jams WHERE id = NEW.jam_id AND status NOT IN ('ANNOUNCED','REGISTRATION_OPEN', 'REGISTRATION_CLOSED')) THEN
        RAISE EXCEPTION 'Cannot delete rating criteria after registration phase.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_rating_criteria_delete
        BEFORE UPDATE OF deleted_at ON rating_criteria
        FOR EACH ROW
        WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
        EXECUTE FUNCTION check_rating_criteria_delete();

-- Rating delete
CREATE OR REPLACE FUNCTION check_rating_soft_delete()
    RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM projects p
        JOIN game_jams gj ON p.jam_id = gj.id
        WHERE p.id = OLD.project_id AND gj.status != 'JUDGING'
    ) THEN
        RAISE EXCEPTION 'Ratings can only be deleted during the judging phase.';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_rating_soft_delete
    BEFORE UPDATE OF deleted_at ON ratings
    FOR EACH ROW
    WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
EXECUTE FUNCTION check_rating_soft_delete();

-- Jam delete
CREATE OR REPLACE FUNCTION handle_game_jam_soft_delete()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status NOT IN ('ANNOUNCED', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED') THEN
        RAISE EXCEPTION 'Cannot delete game jam in progress or judging phases.';
    END IF;

    UPDATE rating_criteria SET deleted_at = NOW() WHERE jam_id = NEW.id AND deleted_at IS NULL;
    UPDATE jam_judges SET deleted_at = NOW() WHERE jam_id = NEW.id AND deleted_at IS NULL;
    UPDATE jam_team_registrations SET status = 'CANCELLED' WHERE jam_id = NEW.id AND status != 'CANCELLED';

    NEW.status = 'CANCELLED';

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_handle_game_jam_soft_delete
    BEFORE UPDATE OF deleted_at ON game_jams
    FOR EACH ROW
    WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
EXECUTE FUNCTION handle_game_jam_soft_delete();

-- Judge delete
CREATE OR REPLACE FUNCTION check_jam_judge_delete()
    RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM game_jams
        WHERE id = NEW.jam_id
          AND status IN ('COMPLETED')
    ) THEN
        RAISE EXCEPTION 'Cannot remove judge during completed phases.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_jam_judge_delete
    BEFORE UPDATE OF deleted_at ON jam_judges
    FOR EACH ROW
    WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
EXECUTE FUNCTION check_jam_judge_delete();


CREATE OR REPLACE FUNCTION handle_jam_judge_soft_delete()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE ratings r
    SET deleted_at = NOW()
    FROM projects p
    WHERE r.project_id = p.id
      AND r.judge_id = NEW.judge_id
      AND p.jam_id = NEW.jam_id
      AND r.deleted_at IS NULL;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_handle_jam_judge_soft_delete
    AFTER UPDATE OF deleted_at ON jam_judges
    FOR EACH ROW
    WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
EXECUTE FUNCTION handle_jam_judge_soft_delete();

-- Role upgrade and jam transfer delete
CREATE OR REPLACE FUNCTION handle_user_delete_for_requests()
    RETURNS TRIGGER AS $$
BEGIN
UPDATE role_upgrade_requests
SET status = 'CANCELLED', updated_at = NOW()
WHERE user_id = NEW.id AND status = 'PENDING' AND deleted_at IS NULL;

UPDATE jam_transfer_requests
SET status = 'CANCELLED', updated_at = NOW()
WHERE recipient_id = NEW.id AND status = 'PENDING' AND deleted_at IS NULL;

UPDATE jam_transfer_requests
SET deleted_at = NOW(), updated_at = NOW()
WHERE sender_id = NEW.id AND deleted_at IS NULL;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_user_delete_for_requests
    AFTER UPDATE OF deleted_at ON users
    FOR EACH ROW
    WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
    EXECUTE FUNCTION handle_user_delete_for_requests();

CREATE OR REPLACE FUNCTION handle_jam_delete_for_transfers()
    RETURNS TRIGGER AS $$
BEGIN

UPDATE jam_transfer_requests
SET deleted_at = NOW(), updated_at = NOW()
WHERE jam_id = NEW.id AND deleted_at IS NULL;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_jam_delete_for_transfers
    AFTER UPDATE OF deleted_at ON game_jams
    FOR EACH ROW
    WHEN (NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL)
    EXECUTE FUNCTION handle_jam_delete_for_transfers();