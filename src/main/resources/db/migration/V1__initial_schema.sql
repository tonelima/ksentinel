CREATE TABLE api_config (
    id                          BIGSERIAL PRIMARY KEY,
    name                        VARCHAR(255)    NOT NULL,
    url                         VARCHAR(2048)   NOT NULL,
    http_method                 VARCHAR(10)     NOT NULL DEFAULT 'GET',
    interval_seconds            INTEGER         NOT NULL DEFAULT 60,
    timeout_seconds             INTEGER         NOT NULL DEFAULT 10,
    enabled                     BOOLEAN         NOT NULL DEFAULT TRUE,
    auth_type                   VARCHAR(50)     NOT NULL DEFAULT 'NONE',
    request_headers             TEXT,
    request_body                TEXT,
    alert_email                 VARCHAR(255),
    alert_webhook_url           VARCHAR(2048),
    description                 VARCHAR(1024),
    consecutive_failures        INTEGER         NOT NULL DEFAULT 0,
    last_checked_at             TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

CREATE TABLE auth_credential (
    id              BIGSERIAL PRIMARY KEY,
    api_config_id   BIGINT          NOT NULL REFERENCES api_config(id) ON DELETE CASCADE,
    auth_type       VARCHAR(50)     NOT NULL,
    username        VARCHAR(255),
    password        TEXT,
    token           TEXT,
    api_key         TEXT,
    api_key_header  VARCHAR(255),
    api_key_as_query_param BOOLEAN  DEFAULT FALSE,
    client_id       VARCHAR(255),
    client_secret   TEXT,
    token_url       VARCHAR(2048),
    scope           VARCHAR(512),
    cached_token    TEXT,
    token_expires_at TIMESTAMPTZ,
    UNIQUE(api_config_id)
);

CREATE TABLE validation_rule (
    id              BIGSERIAL PRIMARY KEY,
    api_config_id   BIGINT          NOT NULL REFERENCES api_config(id) ON DELETE CASCADE,
    rule_type       VARCHAR(50)     NOT NULL,
    json_path       VARCHAR(512),
    operator        VARCHAR(20),
    expected_value  VARCHAR(512),
    max_latency_ms  INTEGER,
    expected_status INTEGER,
    description     VARCHAR(255)
);

CREATE TABLE monitoring_result (
    id              BIGSERIAL PRIMARY KEY,
    api_config_id   BIGINT          NOT NULL REFERENCES api_config(id) ON DELETE CASCADE,
    status          VARCHAR(20)     NOT NULL,
    http_status     INTEGER,
    latency_ms      BIGINT,
    error_message   TEXT,
    response_body   TEXT,
    checked_at      TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    validation_details TEXT
);

CREATE INDEX idx_monitoring_result_api_config_id ON monitoring_result(api_config_id);
CREATE INDEX idx_monitoring_result_checked_at    ON monitoring_result(checked_at DESC);
CREATE INDEX idx_api_config_enabled              ON api_config(enabled);
