CREATE TABLE IF NOT EXISTS ratings (
                                       id BIGSERIAL PRIMARY KEY,
                                       project_id BIGINT NOT NULL,
                                       judge_id BIGINT NOT NULL,
                                       criteria_id BIGINT NOT NULL,
                                       score DECIMAL(4,2) NOT NULL,
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ratings_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_judge FOREIGN KEY (judge_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ratings_criteria FOREIGN KEY (criteria_id) REFERENCES rating_criteria(id) ON DELETE CASCADE,
    CONSTRAINT uq_ratings_project_judge_criteria UNIQUE (project_id, judge_id, criteria_id),
    CONSTRAINT chk_ratings_score CHECK (score >= 0)
    );

CREATE TRIGGER trigger_update_ratings_updated_at
    BEFORE UPDATE ON ratings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_ratings_project_id ON ratings(project_id);
CREATE INDEX IF NOT EXISTS idx_ratings_judge_id ON ratings(judge_id);
CREATE INDEX IF NOT EXISTS idx_ratings_criteria_id ON ratings(criteria_id);
CREATE INDEX IF NOT EXISTS idx_ratings_created_at ON ratings(created_at DESC);

COMMENT ON TABLE ratings IS 'Оценки судей по критериям для проектов';
COMMENT ON COLUMN ratings.score IS 'Оценка по критерию (дробное число для точности)';
COMMENT ON COLUMN ratings.comment IS 'Комментарий судьи к оценке';
COMMENT ON CONSTRAINT uq_ratings_project_judge_criteria ON ratings IS 'Судья может оценить проект по критерию только один раз';