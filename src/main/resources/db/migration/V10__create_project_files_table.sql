CREATE TYPE project_file_type AS ENUM (
    'SCREENSHOT',
    'BUILD',
    'VIDEO',
    'DOCUMENT',
    'OTHER'
);

CREATE TABLE IF NOT EXISTS project_files (
                                             id BIGSERIAL PRIMARY KEY,
                                             project_id BIGINT NOT NULL,
                                             file_type project_file_type NOT NULL,
                                             file_url TEXT NOT NULL,
                                             file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_project_files_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_files_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_project_files_size CHECK (file_size > 0)
    );

CREATE INDEX IF NOT EXISTS idx_project_files_project_id ON project_files(project_id);
CREATE INDEX IF NOT EXISTS idx_project_files_file_type ON project_files(file_type);
CREATE INDEX IF NOT EXISTS idx_project_files_uploaded_by ON project_files(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_project_files_uploaded_at ON project_files(uploaded_at DESC);

COMMENT ON TABLE project_files IS 'Файлы проектов (скриншоты, билды, видео и т.д.)';
COMMENT ON COLUMN project_files.file_url IS 'URL файла в MinIO или другом хранилище';
COMMENT ON COLUMN project_files.file_size IS 'Размер файла в байтах';
COMMENT ON COLUMN project_files.uploaded_by IS 'Пользователь, загрузивший файл';