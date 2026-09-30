# Desplegar Paway en Coolify

Paway incluye un Dockerfile de dos etapas: compila y prueba con JDK 21, y ejecuta el JAR con JRE 21. La imagen usa un usuario no root y verifica la salud en `/actuator/health`.

## Configuración en Coolify

Crea una aplicación desde este repositorio y selecciona **Dockerfile** como Build Pack.

| Campo | Valor |
|---|---|
| Base Directory | `/Paway` |
| Dockerfile Location | `/Dockerfile` |
| Ports Exposes / puerto interno | `8080` |
| Variable de ejecución | `PORT=8080` |
| Dominio | Tu dominio asignado a la aplicación, con HTTPS |
| Health check | GET `/actuator/health`, puerto `8080`, estado esperado `200` |

La ruta del Dockerfile es relativa a la Base Directory: Coolify resolverá `/Paway/Dockerfile`. El contexto de construcción debe ser `Paway`, no la raíz de todos los laboratorios. No necesitas un comando de inicio adicional: el Dockerfile ya lo define.

El Dockerfile incluye su propio `HEALTHCHECK`. Puedes usarlo o configurar el equivalente en Coolify. Da un margen inicial de 60 segundos para que Java arranque. La respuesta incluye `"status":"UP"` cuando la aplicación está disponible.

Coolify termina HTTPS en su proxy y dirige el tráfico al puerto interno 8080. La aplicación procesa las cabeceras de ese proxy con `server.forward-headers-strategy=framework`. Cambiar `PORT` también exige cambiar el puerto configurado en Coolify; lo más sencillo es mantener 8080.

## Probar Docker localmente

Con Docker Desktop iniciado, desde `Paway`:

```powershell
docker build -t paway:local .
docker run --rm --name paway -p 8080:8080 paway:local
```

En otra terminal:

```powershell
curl.exe --fail http://localhost:8080/actuator/health
```

Abre `http://localhost:8080` para revisar la interfaz. El Java instalado en tu equipo no afecta a esta imagen: el build y el runtime usan Java 21 dentro de Docker.

## PostgreSQL en Coolify: siguiente etapa

**El Dockerfile despliega la aplicación actual. Todavía no guarda en PostgreSQL.** Configurar una URL de base de datos no basta: `ShipmentService` sigue utilizando una colección en memoria.

Recomendamos PostgreSQL administrado como recurso separado en Coolify. Así puedes actualizar la aplicación sin reemplazar el contenedor de datos y usar las opciones de volumen persistente y respaldo de la BD. Una base incluida en un Docker Compose también funciona, pero acopla más su configuración al despliegue de la aplicación.

### 1. Preparar la base de datos

En el mismo servidor de Coolify, crea un recurso **PostgreSQL**, por ejemplo con base y usuario `paway`. Guarda la contraseña como variable secreta y confirma el volumen persistente. Mantén aplicación y BD en la misma red Docker; estar en el mismo proyecto visual no garantiza por sí solo esa conectividad.

Usa el host y puerto de la **Internal URL** que muestra Coolify. Dentro del contenedor de Paway, `localhost` apunta a Paway, no a PostgreSQL. No necesitas publicar el puerto 5432 para que dos recursos conectados a la misma red se comuniquen.

### 2. Implementar persistencia en Spring Boot

| Trabajo | Resultado |
|---|---|
| Añadir `spring-boot-starter-data-jpa` y el driver `org.postgresql:postgresql` | Conexión JDBC y repositorios JPA |
| Añadir `spring-boot-starter-flyway` y `org.flywaydb:flyway-database-postgresql` | Migraciones versionadas para PostgreSQL |
| Crear entidades para clientes, agencias, envíos y eventos | Relaciones persistentes y claves foráneas |
| Reemplazar el mapa de `ShipmentService` por repositorios | Los envíos sobreviven al reinicio y al redeploy |
| Guardar el envío y sus eventos en una transacción | Historial e información del envío consistentes |
| Usar una secuencia de BD y una restricción única para las guías | Evitar duplicados bajo concurrencia o varias instancias |
| Migraciones iniciales y datos de prueba separados | No reinsertar ejemplos en cada arranque |
| Prueba de persistencia contra PostgreSQL | Registrar, reiniciar y comprobar que los datos siguen ahí |

Los `record` actuales pueden seguir siendo objetos de vista; las entidades JPA se agregan por separado. Las reglas de tarifas de `QuoteService` se pueden conservar en Java inicialmente. No es necesario cambiar el frontend para implementar la persistencia.

### 3. Configurar variables de ejecución

Después de implementar las dependencias, entidades y repositorios, configura en Coolify:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://<host-interno>:5432/paway
SPRING_DATASOURCE_USERNAME=paway
SPRING_DATASOURCE_PASSWORD=<contraseña-secreta>
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_OPEN_IN_VIEW=false
```

Reemplaza el host y puerto por los de tu recurso real. Spring Boot espera una URL **JDBC**, no la URL `postgres://...` copiada literalmente de Coolify. Las credenciales van en sus variables, sin añadirlas a Git ni al Dockerfile.

Flyway aplicará las migraciones desde `src/main/resources/db/migration`; Hibernate validará el esquema sin recrearlo. Una vez integrada la BD, el indicador de salud de Actuator también comprobará su conexión.

## Alcance actual del despliegue

Los perfiles siguen siendo de demostración y los datos todavía se restablecen al reiniciar. Antes de utilizar cuentas reales, implementaremos autenticación y autorización con Spring Security; PostgreSQL por sí solo no convierte el selector de perfiles en un login.

## Referencias

- [Coolify: configuración general y rutas](https://coolify.io/docs/applications/configuration/general)
- [Coolify: health checks](https://coolify.io/docs/applications/configuration/health-checks)
- [Coolify: bases de datos](https://coolify.io/docs/databases/)
- [Docker: aplicaciones Java](https://docs.docker.com/guides/java/)
