CREATE TABLE IF NOT EXISTS jam_judges (
                                          id BIGSERIAL PRIMARY KEY,
                                          jam_id BIGINT NOT NULL,
                                          judge_id BIGINT NOT NULL,
                                          assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    assigned_by BIGINT NOT NULL,

    CONSTRAINT fk_jam_judges_jam FOREIGN KEY (jam_id) REFERENCES game_jams(id) ON DELETE CASCADE,
    CONSTRAINT fk_jam_judges_judge FOREIGN KEY (judge_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_jam_judges_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_jam_judges_jam_judge UNIQUE (jam_id, judge_id)
    );

CREATE INDEX IF NOT EXISTS idx_jam_judges_jam_id ON jam_judges(jam_id);
CREATE INDEX IF NOT EXISTS idx_jam_judges_judge_id ON jam_judges(judge_id);
CREATE INDEX IF NOT EXISTS idx_jam_judges_assigned_by ON jam_judges(assigned_by);

COMMENT ON TABLE jam_judges IS 'Судьи, назначенные для оценки проектов джема';
COMMENT ON COLUMN jam_judges.assigned_by IS 'Пользователь (обычно организатор), назначивший судью';
COMMENT ON CONSTRAINT uq_jam_judges_jam_judge ON jam_judges IS 'Один пользователь может быть судьей джема только один раз';