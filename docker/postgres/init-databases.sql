-- Script tự động khởi tạo 5 database riêng biệt theo Database-per-service pattern
CREATE DATABASE auth_db;
CREATE DATABASE job_db;
CREATE DATABASE candidate_db;
CREATE DATABASE application_db;
CREATE DATABASE notification_db;

GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE job_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE candidate_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE application_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO postgres;