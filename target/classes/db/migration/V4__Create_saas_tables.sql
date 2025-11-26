CREATE TABLE IF NOT EXISTS tenants (
                                       id BIGSERIAL PRIMARY KEY,
                                       name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS plans (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    max_users INT NOT NULL DEFAULT 5,
    max_documents INT NOT NULL DEFAULT 100,
    max_storage_mb BIGINT NOT NULL DEFAULT 1024,
    active BOOLEAN NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS subscriptions (
                                             id BIGSERIAL PRIMARY KEY,
                                             tenant_id BIGINT NOT NULL,
                                             plan_id BIGINT NOT NULL,
                                             start_date TIMESTAMP NOT NULL,
                                             end_date TIMESTAMP,
                                             status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (plan_id) REFERENCES plans(id)
    );

-- Inserir planos apenas se não existirem (usando INSERT IGNORE pattern para PostgreSQL)
INSERT INTO plans (name, description, price, max_users, max_documents, max_storage_mb)
SELECT 'Básico', 'Plano básico para pequenas equipes', 29.90, 5, 100, 1024
    WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'Básico');

INSERT INTO plans (name, description, price, max_users, max_documents, max_storage_mb)
SELECT 'Profissional', 'Plano para empresas em crescimento', 79.90, 20, 1000, 10240
    WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'Profissional');

INSERT INTO plans (name, description, price, max_users, max_documents, max_storage_mb)
SELECT 'Enterprise', 'Plano ilimitado para grandes organizações', 199.90, 9999, 99999, 102400
    WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'Enterprise');

ALTER TABLE users ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'fk_users_tenant' AND table_name = 'users') THEN
ALTER TABLE users ADD CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_tenants_subdomain ON tenants(subdomain);
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant ON subscriptions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_users_tenant ON users(tenant_id);