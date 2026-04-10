CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO app_user (name, email, password_hash)
VALUES ('Admin', 'admin@ksentinel.local', '{noop}admin123');

ALTER TABLE company
DROP CONSTRAINT IF EXISTS company_name_key;

ALTER TABLE company
ADD COLUMN user_id BIGINT;

UPDATE company
SET user_id = (SELECT id FROM app_user WHERE email = 'admin@ksentinel.local');

ALTER TABLE company
ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE company
ADD CONSTRAINT fk_company_user
FOREIGN KEY (user_id) REFERENCES app_user(id);

CREATE INDEX idx_company_user_id ON company(user_id);
CREATE UNIQUE INDEX uk_company_user_name_lower ON company(user_id, lower(name));
