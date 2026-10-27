CREATE TABLE categorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    CONSTRAINT pk_categorias PRIMARY KEY (id),
    CONSTRAINT uk_categorias_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO categorias (id, nombre) VALUES
(1, 'Front-end'),
(2, 'Back-end'),
(3, 'Bases de datos'),
(4, 'General');

ALTER TABLE categorias AUTO_INCREMENT = 5;

ALTER TABLE cursos
ADD COLUMN categoria_id BIGINT NULL AFTER horas;

UPDATE cursos SET categoria_id = 4;
UPDATE cursos SET categoria_id = 1 WHERE titulo IN ('HTML y CSS', 'Bootstrap');
UPDATE cursos SET categoria_id = 2 WHERE titulo = 'Spring Boot';

ALTER TABLE cursos
MODIFY categoria_id BIGINT NOT NULL;

CREATE INDEX idx_cursos_categoria_id ON cursos (categoria_id);

ALTER TABLE cursos
ADD CONSTRAINT fk_cursos_categoria
FOREIGN KEY (categoria_id)
REFERENCES categorias (id)
ON UPDATE RESTRICT
ON DELETE RESTRICT;