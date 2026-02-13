CREATE TABLE IF NOT EXISTS specializations (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(100) NOT NULL UNIQUE,
description TEXT,
created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_specializations_name ON specializations(name);

COMMENT ON TABLE specializations IS 'Специализации участников (Developer, Artist, Designer и т.д.)';
COMMENT ON COLUMN specializations.name IS 'Название специализации';
COMMENT ON COLUMN specializations.description IS 'Описание специализации';

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