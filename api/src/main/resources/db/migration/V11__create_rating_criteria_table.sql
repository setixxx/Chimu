CREATE TABLE IF NOT EXISTS rating_criteria (
                                               id BIGSERIAL PRIMARY KEY,
                                               jam_id BIGINT NOT NULL,
                                               name VARCHAR(100) NOT NULL,
    description TEXT,
    max_score INT NOT NULL DEFAULT 10,
    weight DECIMAL(3,2) NOT NULL DEFAULT 1.0,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_rating_criteria_jam FOREIGN KEY (jam_id) REFERENCES game_jams(id) ON DELETE CASCADE,
    CONSTRAINT chk_rating_criteria_max_score CHECK (max_score > 0 AND max_score <= 100),
    CONSTRAINT chk_rating_criteria_weight CHECK (weight > 0 AND weight <= 10)
    );

CREATE UNIQUE INDEX uq_rating_criteria_jam_name
    ON rating_criteria(jam_id, name);

CREATE INDEX IF NOT EXISTS idx_rating_criteria_jam_id ON rating_criteria(jam_id);
CREATE INDEX IF NOT EXISTS idx_rating_criteria_order ON rating_criteria(jam_id, order_index);

COMMENT ON TABLE rating_criteria IS 'Критерии оценки для джемов (Gameplay, Graphics, Sound и т.д.)';
COMMENT ON COLUMN rating_criteria.max_score IS 'Максимальная оценка по критерию (обычно 10)';
COMMENT ON COLUMN rating_criteria.weight IS 'Вес критерия в итоговой оценке (1.0 = стандартный вес)';
COMMENT ON COLUMN rating_criteria.order_index IS 'Порядок отображения критериев';