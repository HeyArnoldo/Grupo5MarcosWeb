# TechLab Web - Semana 13 (Spring Security)

## Requisitos
- Java 21 (o el que indique el curso)
- MySQL en localhost:3306 con la base `techlab`
- Maven Wrapper incluido (`mvnw`)

## Variables de entorno (PowerShell)
$env:TECHLAB_DB_PASSWORD='<clave de MySQL>'
$env:APP_DEMO_USERNAME='docente'
$env:APP_DEMO_PASSWORD='<clave temporal del laboratorio>'

## Ejecutar
.\mvnw.cmd spring-boot:run     # app en http://localhost:8081
.\mvnw.cmd clean test          # pruebas

## Rutas
- Públicas: /cursos, /cursos/{id}, /cursos/resumen, /login
- Privadas: /cursos/nuevo, /cursos/{id}/editar, POST de cursos, /perfil
- API: /api/v1/** (Basic, sin sesión)

## Nota
La cuenta es temporal y vive en memoria. Los tests usan H2 en memoria.