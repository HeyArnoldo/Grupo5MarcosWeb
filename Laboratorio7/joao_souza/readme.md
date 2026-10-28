# Laboratorio 7 — Thymeleaf y fragmentos

**Integrante:** Joao Souza — Grupo 5

## Alcance

El proyecto `techlab-web` de este laboratorio parte del Laboratorio 6 y conserva sus
rutas y el diseño. Las plantillas Thymeleaf ya estaban integradas en esa base, por lo
que ambos laboratorios comparten el mismo contenido:

- `templates/fragments/layout.html`: encabezado, pie de página y navegación reutilizables.
- `templates/cursos/lista.html` y `templates/cursos/detalle.html`: vistas que consumen los fragmentos con `th:replace`.
- `templates/error/4xx.html`: página de error con el mismo layout.

## Ejecutar

Desde `Laboratorio7/joao_souza/techlab-web`:

```bash
./mvnw spring-boot:run
```

Abrir `http://localhost:8080/cursos`.
