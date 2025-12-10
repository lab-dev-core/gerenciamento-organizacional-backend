-- V6: Add tenant_id to all tables for SaaS multi-tenancy support

-- Add tenant_id to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);

-- Add tenant_id to roles table
ALTER TABLE roles ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
ALTER TABLE roles ADD CONSTRAINT fk_roles_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
CREATE INDEX IF NOT EXISTS idx_roles_tenant_id ON roles(tenant_id);

-- Add tenant_id to mission_locations table
ALTER TABLE mission_locations ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
ALTER TABLE mission_locations ADD CONSTRAINT fk_mission_locations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
CREATE INDEX IF NOT EXISTS idx_mission_locations_tenant_id ON mission_locations(tenant_id);

-- Add tenant_id to formative_documents table
ALTER TABLE formative_documents ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
ALTER TABLE formative_documents ADD CONSTRAINT fk_formative_documents_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
CREATE INDEX IF NOT EXISTS idx_formative_documents_tenant_id ON formative_documents(tenant_id);

-- Add tenant_id to document_categories table
ALTER TABLE document_categories ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
ALTER TABLE document_categories ADD CONSTRAINT fk_document_categories_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
CREATE INDEX IF NOT EXISTS idx_document_categories_tenant_id ON document_categories(tenant_id);

-- Add tenant_id to formative_stages table
ALTER TABLE formative_stages ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
ALTER TABLE formative_stages ADD CONSTRAINT fk_formative_stages_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
CREATE INDEX IF NOT EXISTS idx_formative_stages_tenant_id ON formative_stages(tenant_id);

-- Add tenant_id to document_reading_progress table
ALTER TABLE document_reading_progress ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
ALTER TABLE document_reading_progress ADD CONSTRAINT fk_document_reading_progress_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
CREATE INDEX IF NOT EXISTS idx_document_reading_progress_tenant_id ON document_reading_progress(tenant_id);

-- Add tenant_id to follow_up_meetings table
ALTER TABLE follow_up_meetings ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
ALTER TABLE follow_up_meetings ADD CONSTRAINT fk_follow_up_meetings_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
CREATE INDEX IF NOT EXISTS idx_follow_up_meetings_tenant_id ON follow_up_meetings(tenant_id);

-- Update constraints to make tenant_id NOT NULL after data migration
-- Note: In production, you would first populate tenant_id values before making them NOT NULL
-- For new installations, this is fine as data will be created with tenant_id from the start
