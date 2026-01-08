-- Create schemas
CREATE SCHEMA IF NOT EXISTS auth_svc;
CREATE SCHEMA IF NOT EXISTS beat_svc;
CREATE SCHEMA IF NOT EXISTS artist_svc;
CREATE SCHEMA IF NOT EXISTS analytics_svc;
CREATE SCHEMA IF NOT EXISTS email_svc;
CREATE SCHEMA IF NOT EXISTS ai_svc;

-- Create users (for local dev, on Render these might be different or use same user with schema isolation)
-- For the "unified web service" on Render, we might use one DB user but separate schemas
-- To enforce isolation in local dev:

CREATE USER auth_service WITH PASSWORD 'auth_password';
GRANT ALL PRIVILEGES ON SCHEMA auth_svc TO auth_service;

CREATE USER beat_service WITH PASSWORD 'beat_password';
GRANT ALL PRIVILEGES ON SCHEMA beat_svc TO beat_service;

CREATE USER artist_service WITH PASSWORD 'artist_password';
GRANT ALL PRIVILEGES ON SCHEMA artist_svc TO artist_service;

CREATE USER analytics_service WITH PASSWORD 'analytics_password';
GRANT ALL PRIVILEGES ON SCHEMA analytics_svc TO analytics_service;

CREATE USER email_service WITH PASSWORD 'email_password';
GRANT ALL PRIVILEGES ON SCHEMA email_svc TO email_service;

CREATE USER ai_service WITH PASSWORD 'ai_password';
GRANT ALL PRIVILEGES ON SCHEMA ai_svc TO ai_service;
