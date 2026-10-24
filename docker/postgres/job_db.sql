create extension if not exists "pgcrypto";

create table if not exists jobs (
	id UUID primary key default gen_random_uuid(),
	company_id uuid not null,
	recruiter_id uuid not null,
	title varchar(255) not null,
	description text not null,
	requirements text,
	benefits text,
	salary_min numeric(15, 2),
	salary_max numeric(15, 2),
	currency varchar(10) default 'VND',
	location varchar(255),
	employment_type varchar(50) not null,
	experience_level varchar(50) not null,
	status varchar(30) not null default 'DRAFT',
	created_at timestamptz not null default CURRENT_TIMESTAMP,
	updated_at timestamptz not null default CURRENT_TIMESTAMP
); 

create table if not exists skills (
	id serial primary key,
	name varchar(100) not null unique,
	category varchar(50),
	created_at timestamptz not null default CURRENT_TIMESTAMP
);

create table job_skills(
	job_id uuid not null references jobs(id) on delete cascade,
	skill_id int references skills(id) on delete cascade,
	required_level varchar(50) default 'REQUIRED',
	primary key (job_id, skill_id)
); 