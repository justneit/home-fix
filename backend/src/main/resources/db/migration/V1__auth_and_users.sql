-- Phase 1: accounts, worker profile root, refresh tokens.
-- Conventions: BIGINT surrogate keys, DATETIME(6) in UTC, enum-like VARCHAR columns guarded by CHECK.

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20)  NULL,
    role          VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME(6)  NULL,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'WORKER', 'ADMIN')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DISABLED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_users_role_status ON users (role, status);

-- One row per WORKER account. Skills, areas and availability are added in Phase 2.
CREATE TABLE worker_profiles (
    user_id             BIGINT      NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    available           BOOLEAN     NOT NULL DEFAULT FALSE,
    version             BIGINT      NOT NULL DEFAULT 0,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NOT NULL,
    CONSTRAINT pk_worker_profiles PRIMARY KEY (user_id),
    CONSTRAINT fk_worker_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_worker_profiles_verification
        CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Opaque refresh tokens are stored only as SHA-256 hashes. Tokens issued from the same login
-- share a family_id so that reuse of a rotated token can revoke the whole chain.
CREATE TABLE refresh_tokens (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL,
    token_hash     CHAR(64)    NOT NULL,
    family_id      CHAR(36)    NOT NULL,
    expires_at     DATETIME(6) NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    revoked_at     DATETIME(6) NULL,
    replaced_by_id BIGINT      NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_tokens_replaced_by FOREIGN KEY (replaced_by_id) REFERENCES refresh_tokens (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_refresh_tokens_family ON refresh_tokens (family_id);
CREATE INDEX ix_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX ix_refresh_tokens_expires ON refresh_tokens (expires_at);
