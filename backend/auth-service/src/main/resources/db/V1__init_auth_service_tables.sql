create table companies (
	id UUID primary key,
	name varchar(255) not null,
	tax_code varchar(100) not null unique, 
	verified boolean not null default false, 
	created_at timestamptz default current_timestamp,
	updated_at timestamptz default current_timestamp
);

create table users (
	id UUID primary key, 
	email varchar(255) not null unique, 
	password_hash varchar(255) not null,
	role varchar(50) not null,
	status varchar(50) not null, 
	company_id UUID references companies(id),
	created_at timestamptz default current_timestamp,
	update_at timestamptz default current_timestamp
);

create table outbox (
	id UUID primary key, 
	aggregate_type varchar(255) not null,
	aggreget_id varchar(255) not null,
	type varchar(255) not null,
	payload text not null,
	created_at timestamptz default current_timestamp,
	processed boolean default false
);

create index idx_users_email on users(email);
create index idx_users_company_id on users(company_id);
create index idx_outbox_processed on outbox(processed);












