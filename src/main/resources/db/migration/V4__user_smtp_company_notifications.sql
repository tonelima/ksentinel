ALTER TABLE app_user
ADD COLUMN smtp_host VARCHAR(255),
ADD COLUMN smtp_port INTEGER,
ADD COLUMN smtp_username VARCHAR(255),
ADD COLUMN smtp_password TEXT,
ADD COLUMN smtp_from_email VARCHAR(255),
ADD COLUMN smtp_auth BOOLEAN DEFAULT TRUE,
ADD COLUMN smtp_starttls BOOLEAN DEFAULT TRUE;

CREATE TABLE company_notification_email (
    company_id BIGINT NOT NULL REFERENCES company(id) ON DELETE CASCADE,
    email      VARCHAR(255) NOT NULL,
    PRIMARY KEY (company_id, email)
);

ALTER TABLE api_config
ADD COLUMN notification_delay_minutes INTEGER NOT NULL DEFAULT 0;
