-- Tabela de documentos formativos
CREATE TABLE formative_documents (
                                     id BIGSERIAL PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL,
                                     content TEXT,
                                     creation_date TIMESTAMP NOT NULL,
                                     last_modified_date TIMESTAMP,
                                     author_id BIGINT,
                                     document_type VARCHAR(255) NOT NULL,
                                     access_level VARCHAR(255) NOT NULL,
                                     attachment_data BYTEA,
                                     attachment_name VARCHAR(255),
                                     attachment_type VARCHAR(255),
                                     keywords TEXT,

                                     FOREIGN KEY (author_id) REFERENCES users(id)
);

-- Tabela de categorias de documentos
CREATE TABLE document_categories (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(255) NOT NULL UNIQUE,
                                     description TEXT,
                                     parent_category_id BIGINT,

                                     FOREIGN KEY (parent_category_id) REFERENCES document_categories(id)
);

-- Tabela de estágios formativos
CREATE TABLE formative_stages (
                                  id BIGSERIAL PRIMARY KEY,
                                  name VARCHAR(255) NOT NULL,
                                  start_date DATE NOT NULL,
                                  end_date DATE,
                                  duration_months INTEGER,
                                  user_id BIGINT,

                                  FOREIGN KEY (user_id) REFERENCES users(id)
);