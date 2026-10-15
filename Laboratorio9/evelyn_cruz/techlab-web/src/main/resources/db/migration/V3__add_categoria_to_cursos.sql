ALTER TABLE cursos
    ADD COLUMN categoria VARCHAR(40) NOT NULL DEFAULT 'General';

UPDATE cursos SET categoria = 'Frontend' WHERE id IN (1, 2);
UPDATE cursos SET categoria = 'Backend' WHERE id = 3;
