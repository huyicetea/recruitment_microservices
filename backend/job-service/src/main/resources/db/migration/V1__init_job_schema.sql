CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    created_by UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    level VARCHAR(50) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    work_mode VARCHAR(50) NOT NULL,
    location VARCHAR(255) NOT NULL,
    industry VARCHAR(255) NOT NULL,
    salary_min BIGINT,
    salary_max BIGINT,
    min_years_exp INT DEFAULT 0,
    deadline TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_skills (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill VARCHAR(100) NOT NULL,
    must_have BOOLEAN DEFAULT FALSE,
    required BOOLEAN DEFAULT TRUE
);

CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_jobs_status_deadline ON jobs(status, deadline);
CREATE INDEX idx_outbox_processed ON outbox(processed);
CREATE INDEX idx_jobs_company_id ON jobs(company_id);
CREATE INDEX idx_job_skills_job_id ON job_skills(job_id);