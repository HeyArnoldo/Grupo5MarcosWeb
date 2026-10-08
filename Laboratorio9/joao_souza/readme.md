# Laboratorio 9 — Persistencia de TechLab

**Integrante:** Adriano Joao Souza Reyna (HeyArnoldo).

## Base y alcance

Se tomó una copia de `Laboratorio7/joao_souza/techlab-web` del commit
`a25bffcd7be002d76013c32b1780c876ac515c50`. No hubo Laboratorio 8:
esa semana correspondió a la presentación del proyecto.

Se conservan el paquete `pe.edu.utp.techlab_web`, el nombre `CourseDTO`,
las rutas y el diseño previo. Se incorporó `/cursos/resumen`, ausente en la base,
y se corrigió en esta copia la prueba que esperaba la antigua redirección a
`/catalogo.html`; el controlador ya redirigía a `/cursos`.

El catálogo es de solo lectura. No hay formularios de alta, edición ni eliminación.
El reto añade `categoria` al DTO y a HTML/JSON, sin cambiar las rutas.

## Requisitos

- JDK 25 y `JAVA_HOME` apuntando a su instalación.
- Maven Wrapper incluido; no se necesita Maven global.
- Docker Desktop iniciado con soporte para contenedores Linux y Compose.
- Puertos 8080 y 3307 disponibles.
- Las versiones indicadas por la guía: Spring Boot 4.1.1 y MySQL 8.4.12.
  Su descarga y compatibilidad deben confirmarse al ejecutar el proyecto.

Todos los comandos siguientes se ejecutan en PowerShell desde
`Laboratorio9/joao_souza/techlab-web`.

```powershell
java --version
.\mvnw.cmd --version
docker compose version
```

## Configurar e iniciar MySQL

```powershell
Copy-Item .env.example .env
```

Editar `.env` localmente y reemplazar las dos contraseñas por claves distintas.
No subir ese archivo ni publicar capturas de sus valores. El usuario de aplicación
es `techlab_app`, no `root`. `.env` y `application-local.properties` están ignorados.

```powershell
git check-ignore .env
docker compose config --quiet
docker compose up -d mysql
docker compose ps
```

Esperar a que MySQL esté `healthy`. El puerto se publica solo en localhost.
Los datos se conservan en el volumen `techlab_mysql_data`.
No reutilizar ese volumen si pertenece a otro laboratorio con datos que deban conservarse.

Docker Compose lee `.env`, pero Java no lo lee automáticamente. Definir las variables
en la misma terminal que ejecutará Maven o el JAR. Para no escribir la contraseña
literal en el historial, se puede solicitar de forma oculta:

```powershell
$env:DB_USER = 'techlab_app'
$clave = Read-Host 'DB_PASSWORD (la misma de .env)' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $clave).Password
Remove-Variable clave
```

## Pruebas y ejecución

Pruebas unitarias y web, sin MySQL ni variables de base de datos:

```powershell
.\mvnw.cmd '-Dtest=CourseServiceTests,CourseViewControllerTests,WebRoutesTests' test
```

Suite completa con MySQL `healthy` y las variables definidas:

```powershell
.\mvnw.cmd dependency:tree
.\mvnw.cmd clean test
.\mvnw.cmd clean package
java -jar target/techlab-web-0.0.1-SNAPSHOT.jar
```

`TechlabWebApplicationTests` y `CoursePersistenceIntegrationTests` requieren MySQL real.
Las pruebas de integración asumen el catálogo de laboratorio sin escrituras externas.
La aplicación ejecuta Flyway antes de validar el mapeo JPA.

## Rutas

| Ruta | Resultado esperado |
| --- | --- |
| `/` y `/portal` | Redirección a `/cursos` |
| `/cursos` | Tres cursos y sus categorías |
| `/cursos?q=boot` | Solo Spring Boot |
| `/cursos?categoria=frontend` | HTML y CSS y Bootstrap |
| `/cursos?q=boot&categoria=backend` | Spring Boot |
| `/cursos/2` | Bootstrap, 16 horas, Frontend |
| `/cursos/999` | HTTP 404 |
| `/cursos/resumen` | 3 cursos, 48 horas |
| `/api/v1/cursos` | Tres objetos JSON con categoría |
| `/api/v1/cursos?categoria=Backend` | Solo Spring Boot |
| `/api/v1/cursos/2` | JSON del detalle |
| `/actuator/health` | `status: UP` |

La búsqueda por título admite hasta 60 caracteres y la categoría hasta 40.
La categoría se compara por igualdad sin distinguir mayúsculas; el título por contenido.
Los filtros pueden combinarse. El resumen siempre corresponde al catálogo completo.

## Arquitectura y migraciones

```text
Controlador MVC/REST → CourseService → CourseRepository → JPA/Hibernate/JDBC → MySQL
                            ↓
                       CourseMapper → CourseDTO → HTML/JSON
```

- JPA define el mapeo objeto-relacional; Hibernate lo implementa.
- Spring Data JPA genera el repositorio; JDBC proporciona acceso a la base.
- MySQL almacena las filas y aplica sus restricciones.
- Flyway es el único responsable de crear y evolucionar el esquema.
- `ddl-auto=validate` valida; no se utiliza `update`, `schema.sql` ni `data.sql`.
- `@Transactional(readOnly = true)` delimita las lecturas en el servicio.
- `open-in-view=false` evita acceso a persistencia desde las vistas.
- El mapper transforma entidades a DTO dentro de la transacción.
- Aunque `JpaRepository` ofrece métodos de escritura, no se invocan.

| Migración | Responsabilidad |
| --- | --- |
| V1 | Tabla cursos, clave primaria, título único y horas entre 1 y 500 |
| V2 | Tres cursos iniciales, 48 horas, siguiente ID 4 |
| V3 | Categoría obligatoria de hasta 40 caracteres; Frontend para IDs 1/2 y Backend para ID 3 |

Una migración aplicada no se edita. Un cambio posterior requiere una nueva versión.

## Evidencia incremental del reto

En una base nueva, el arranque normal aplica V1, V2 y V3 en secuencia. Eso no demuestra
por sí solo una actualización de una instalación anterior. Para comprobar V3 sobre
una base que ya tenga V1/V2, sin borrar datos:

1. Si la base ya tiene V1/V2, ejecutar directamente la versión actual.
2. Si es una base nueva y se quiere reproducir la fase anterior, iniciar temporalmente:

   ```powershell
   .\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--spring.flyway.target=2 --spring.jpa.hibernate.ddl-auto=none'
   ```

   Esta fase solo prepara V1/V2. No abrir las rutas de cursos: el código actual ya
   espera la columna categoría. No ejecutar la suite completa en esta fase.
3. Detener con Ctrl+C y registrar las tres filas y el historial V1/V2.
4. Ejecutar normalmente sin esos argumentos; vuelve a estar activo `ddl-auto=validate`.
5. Confirmar que V3 se añadió al historial, que se conservaron IDs/títulos/horas y que
   las categorías son correctas. No modificar V1/V2 ni eliminar el volumen.

## Comprobaciones SQL y reinicio

```powershell
docker compose exec mysql mysql -utechlab_app -p techlab
```

Introducir la contraseña cuando se solicite, nunca como parte del comando.

```sql
SELECT id, titulo, horas, categoria FROM cursos ORDER BY id;
SELECT COUNT(*) AS cantidad, SUM(horas) AS total_horas FROM cursos;
SELECT installed_rank, version, description, success
FROM flyway_schema_history ORDER BY installed_rank;
EXIT;
```

Detener la aplicación, ejecutar `docker compose restart mysql`, esperar `healthy`
y volver a iniciar el JAR. Verificar que las filas permanecen y las migraciones
no se vuelven a aplicar. Para detener MySQL sin borrar sus datos: `docker compose stop mysql`.

## Matriz y estado real de verificación

Durante la preparación del código, la terminal no encontró `java` y Maven informó
que `JAVA_HOME` no estaba configurado correctamente. No se pudo validar la línea base,
compilar, ejecutar pruebas ni generar el JAR. No se crearon credenciales ni se inició MySQL.
No se afirma `BUILD SUCCESS` hasta ejecutar las comprobaciones siguientes.

| Comprobación | Estado |
| --- | --- |
| Configuración sintáctica de Compose con valores de muestra | Aprobada |
| Java 25 y Maven Wrapper | Pendiente: configurar JDK/JAVA_HOME |
| Descarga de dependencias e imagen MySQL | Pendiente |
| MySQL healthy | Pendiente |
| Pruebas unitarias y web | Pendiente |
| Pruebas de integración y empaquetado | Pendiente |
| Tres filas, 48 horas y categorías | Pendiente |
| Historial V1/V2/V3 con success=1 | Pendiente |
| Rutas HTML, API, errores y health | Pendiente |
| Datos conservados tras reiniciar aplicación/MySQL | Pendiente |
| V3 aplicada sobre una instalación con V1/V2 | Pendiente |

## Preguntas de cierre

1. **¿Qué resuelve Flyway frente a `ddl-auto=update`?** Un historial ordenado y auditable,
   con versiones y checksums que permiten detectar cambios en migraciones aplicadas.
2. **¿Por qué no devolver la entidad como respuesta REST?** Mezclaría persistencia con
   el contrato público y podría filtrar campos internos o activar asociaciones perezosas.
3. **¿Dónde termina la transacción sin open-in-view?** Al terminar la invocación
   transaccional del servicio; el controlador recibe DTO ya construidos.
4. **¿Qué falla si se renombra `titulo` solo en la entidad?** El arranque de una prueba
   `@SpringBootTest` falla en la validación del esquema, antes de sus aserciones.
5. **¿Qué puede permanecer estable al incorporar CRUD?** La separación por capas y
   las rutas/contratos de lectura existentes; las operaciones de escritura se agregarán
   con sus propias reglas, formularios y pruebas.
