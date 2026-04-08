CREATE TABLE company (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1024),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO company (name, description)
VALUES ('Sem empresa', 'Empresa padrão criada para APIs legadas');

ALTER TABLE api_config
ADD COLUMN company_id BIGINT;

UPDATE api_config
SET company_id = (SELECT id FROM company WHERE name = 'Sem empresa');

ALTER TABLE api_config
ALTER COLUMN company_id SET NOT NULL;

ALTER TABLE api_config
ADD CONSTRAINT fk_api_config_company
FOREIGN KEY (company_id) REFERENCES company(id);

CREATE INDEX idx_api_config_company_id ON api_config(company_id);
