-- Mapeamento documento-categoria
CREATE TABLE document_category_mapping (
                                           category_id BIGINT NOT NULL,
                                           document_id BIGINT NOT NULL,
                                           PRIMARY KEY (category_id, document_id),
                                           FOREIGN KEY (category_id) REFERENCES document_categories(id),
                                           FOREIGN KEY (document_id) REFERENCES formative_documents(id)
);

-- Estágios permitidos para documentos
CREATE TABLE document_allowed_stages (
                                         document_id BIGINT NOT NULL,
                                         life_stage VARCHAR(255) NOT NULL,
                                         PRIMARY KEY (document_id, life_stage),
                                         FOREIGN KEY (document_id) REFERENCES formative_documents(id)
);

-- Localizações permitidas para documentos
CREATE TABLE document_allowed_locations (
                                            document_id BIGINT NOT NULL,
                                            location_id BIGINT NOT NULL,
                                            PRIMARY KEY (document_id, location_id),
                                            FOREIGN KEY (document_id) REFERENCES formative_documents(id),
                                            FOREIGN KEY (location_id) REFERENCES mission_locations(id)
);

-- Usuários com acesso específico a documentos
CREATE TABLE document_allowed_users (
                                        document_id BIGINT NOT NULL,
                                        user_id BIGINT NOT NULL,
                                        PRIMARY KEY (document_id, user_id),
                                        FOREIGN KEY (document_id) REFERENCES formative_documents(id),
                                        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Roles com acesso específico a documentos
CREATE TABLE document_allowed_roles (
                                        document_id BIGINT NOT NULL,
                                        role_id BIGINT NOT NULL,
                                        PRIMARY KEY (document_id, role_id),
                                        FOREIGN KEY (document_id) REFERENCES formative_documents(id),
                                        FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE document_reading_progress (
                                           id BIGSERIAL PRIMARY KEY,
                                           user_id BIGINT NOT NULL,
                                           document_id BIGINT NOT NULL,
                                           progress_percentage INTEGER DEFAULT 0,
                                           completed BOOLEAN DEFAULT false,
                                           first_view_date TIMESTAMP,
                                           last_view_date TIMESTAMP,
                                           completed_date TIMESTAMP,
                                           user_notes TEXT,

                                           FOREIGN KEY (user_id) REFERENCES users(id),
                                           FOREIGN KEY (document_id) REFERENCES formative_documents(id),
                                           UNIQUE(user_id, document_id)
);