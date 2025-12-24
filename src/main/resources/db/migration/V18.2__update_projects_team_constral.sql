ALTER TABLE projects
DROP CONSTRAINT IF EXISTS fk_projects_team;

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_team
        FOREIGN KEY (team_id)
            REFERENCES teams(id)
            ON DELETE SET NULL;

ALTER TABLE projects
    ALTER COLUMN team_id DROP NOT NULL;

COMMENT ON COLUMN projects.team_id IS 'ID команды (NULL если команда удалена)';