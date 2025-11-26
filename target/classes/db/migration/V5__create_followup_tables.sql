-- Tabela principal de acompanhamentos
CREATE TABLE IF NOT EXISTS follow_up_meetings (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  mentor_id BIGINT NOT NULL,
                                                  mentee_id BIGINT NOT NULL,
                                                  title VARCHAR(255) NOT NULL,
    scheduled_date TIMESTAMP NOT NULL,
    actual_date TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    meeting_type VARCHAR(50) NOT NULL,
    content TEXT,
    objectives TEXT,
    discussion_points TEXT,
    commitments TEXT,
    next_steps TEXT,
    mentor_notes TEXT,
    visibility VARCHAR(50) NOT NULL DEFAULT 'PRIVATE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follow_up_mentor FOREIGN KEY (mentor_id)
    REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_up_mentee FOREIGN KEY (mentee_id)
    REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_mentor_mentee_different CHECK (mentor_id <> mentee_id)
    );

-- Índices
CREATE INDEX IF NOT EXISTS idx_follow_up_mentor ON follow_up_meetings(mentor_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_mentee ON follow_up_meetings(mentee_id);
CREATE INDEX IF NOT EXISTS idx_follow_up_scheduled_date ON follow_up_meetings(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_follow_up_status ON follow_up_meetings(status);
CREATE INDEX IF NOT EXISTS idx_follow_up_meeting_type ON follow_up_meetings(meeting_type);

-- Tabela de compartilhamento com usuários específicos
CREATE TABLE IF NOT EXISTS follow_up_shared_with (
                                                     follow_up_id BIGINT NOT NULL,
                                                     user_id BIGINT NOT NULL,
                                                     PRIMARY KEY (follow_up_id, user_id),
    CONSTRAINT fk_shared_follow_up FOREIGN KEY (follow_up_id)
    REFERENCES follow_up_meetings(id) ON DELETE CASCADE,
    CONSTRAINT fk_shared_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
    );
CREATE INDEX IF NOT EXISTS idx_shared_with_user ON follow_up_shared_with(user_id);

-- Tabela de compartilhamento com funções
CREATE TABLE IF NOT EXISTS follow_up_shared_roles (
                                                      follow_up_id BIGINT NOT NULL,
                                                      role_id BIGINT NOT NULL,
                                                      PRIMARY KEY (follow_up_id, role_id),
    CONSTRAINT fk_shared_follow_up_role FOREIGN KEY (follow_up_id)
    REFERENCES follow_up_meetings(id) ON DELETE CASCADE,
    CONSTRAINT fk_shared_role FOREIGN KEY (role_id)
    REFERENCES roles(id) ON DELETE CASCADE
    );
CREATE INDEX IF NOT EXISTS idx_shared_with_role ON follow_up_shared_roles(role_id);

-- Comentários (SEM IF NOT EXISTS)
COMMENT ON TABLE follow_up_meetings IS 'Armazena registros de acompanhamentos formativos entre formadores e formandos';
COMMENT ON COLUMN follow_up_meetings.mentor_id IS 'ID do formador responsável pelo acompanhamento';
COMMENT ON COLUMN follow_up_meetings.mentee_id IS 'ID do usuário sendo acompanhado';
COMMENT ON COLUMN follow_up_meetings.scheduled_date IS 'Data e hora agendada para o acompanhamento';
COMMENT ON COLUMN follow_up_meetings.actual_date IS 'Data e hora em que o acompanhamento realmente ocorreu';
COMMENT ON COLUMN follow_up_meetings.status IS 'Status: SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED';
COMMENT ON COLUMN follow_up_meetings.meeting_type IS 'Tipo de acompanhamento';
COMMENT ON COLUMN follow_up_meetings.mentor_notes IS 'Observações confidenciais do formador';
COMMENT ON COLUMN follow_up_meetings.visibility IS 'Nível de visibilidade: PRIVATE, SHARED_SPECIFIC, SHARED_ROLE, COORDINATION';

-- Views
CREATE OR REPLACE VIEW v_upcoming_follow_ups AS
SELECT
    fm.id,
    fm.mentor_id,
    u_mentor.name AS mentor_name,
    fm.mentee_id,
    u_mentee.name AS mentee_name,
    fm.title,
    fm.scheduled_date,
    fm.meeting_type,
    EXTRACT(DAY FROM (fm.scheduled_date - CURRENT_TIMESTAMP)) AS days_until_meeting
FROM follow_up_meetings fm
         JOIN users u_mentor ON fm.mentor_id = u_mentor.id
         JOIN users u_mentee ON fm.mentee_id = u_mentee.id
WHERE fm.status = 'SCHEDULED'
  AND fm.scheduled_date >= CURRENT_TIMESTAMP
ORDER BY fm.scheduled_date;

CREATE OR REPLACE VIEW v_mentor_statistics AS
SELECT
    u.id AS mentor_id,
    u.name AS mentor_name,
    COUNT(fm.*) AS total_meetings,
    COUNT(*) FILTER (WHERE fm.status = 'SCHEDULED') AS scheduled_count,
    COUNT(*) FILTER (WHERE fm.status = 'COMPLETED') AS completed_count,
    COUNT(*) FILTER (WHERE fm.status = 'CANCELLED') AS cancelled_count,
    COUNT(DISTINCT fm.mentee_id) AS unique_mentees
FROM users u
         LEFT JOIN follow_up_meetings fm ON u.id = fm.mentor_id
GROUP BY u.id, u.name;

CREATE OR REPLACE VIEW v_mentee_history AS
SELECT
    u.id AS mentee_id,
    u.name AS mentee_name,
    u.life_stage,
    COUNT(fm.*) AS total_meetings,
    COUNT(*) FILTER (WHERE fm.status = 'COMPLETED') AS completed_meetings,
    MAX(fm.actual_date) AS last_meeting_date,
    MIN(fm.scheduled_date) FILTER (WHERE fm.status = 'SCHEDULED' AND fm.scheduled_date >= CURRENT_TIMESTAMP) AS next_meeting_date
FROM users u
         LEFT JOIN follow_up_meetings fm ON u.id = fm.mentee_id
GROUP BY u.id, u.name, u.life_stage;

-- Função/Trigger updated_at
-- (Drop antes para evitar erro se já existir)
DROP TRIGGER IF EXISTS trigger_update_follow_up_timestamp ON follow_up_meetings;
DROP FUNCTION IF EXISTS update_follow_up_updated_at();

CREATE FUNCTION update_follow_up_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_follow_up_timestamp
    BEFORE UPDATE ON follow_up_meetings
    FOR EACH ROW
    EXECUTE FUNCTION update_follow_up_updated_at();
