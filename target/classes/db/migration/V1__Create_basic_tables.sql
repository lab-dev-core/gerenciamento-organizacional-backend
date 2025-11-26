-- Tabela de roles
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE,
                       description TEXT,
                       can_manage_users BOOLEAN DEFAULT false,
                       can_manage_roles BOOLEAN DEFAULT false,
                       can_manage_stages BOOLEAN DEFAULT false,
                       can_manage_documents BOOLEAN DEFAULT false
);

-- Tabela de localizações
CREATE TABLE mission_locations (
                                   id BIGSERIAL PRIMARY KEY,
                                   name VARCHAR(255) NOT NULL,
                                   description TEXT,
                                   city VARCHAR(255) NOT NULL,
                                   state VARCHAR(255) NOT NULL,
                                   country VARCHAR(255),
                                   address TEXT,
                                   postal_code VARCHAR(50),
                                   coordinator_id BIGINT
);

-- Tabela de usuários
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       name VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       is_enabled BOOLEAN DEFAULT true,
                       is_account_non_expired BOOLEAN DEFAULT true,
                       is_account_non_locked BOOLEAN DEFAULT true,
                       is_credentials_non_expired BOOLEAN DEFAULT true,
                       mission_location_id BIGINT,
                       city VARCHAR(255) NOT NULL,
                       state VARCHAR(255) NOT NULL,
                       age INTEGER,
                       phone VARCHAR(50),
                       profile_picture BYTEA,
                       education TEXT,
                       mentor_id BIGINT,
                       role_id BIGINT NOT NULL,
                       life_stage VARCHAR(255) NOT NULL,
                       community_years INTEGER NOT NULL,
                       community_months INTEGER,

                       FOREIGN KEY (mission_location_id) REFERENCES mission_locations(id),
                       FOREIGN KEY (mentor_id) REFERENCES users(id),
                       FOREIGN KEY (role_id) REFERENCES roles(id)
);