# Desplegar Paway en Coolify

Paway incluye un Dockerfile de dos etapas: compila con JDK 21 y ejecuta el JAR con JRE 21. Las pruebas usan Testcontainers (necesitan Docker), por eso no se ejecutan dentro de la imagen: corre `./mvnw test` antes de desplegar. La imagen usa un usuario no root y verifica la salud en `/actuator/health`.

## Configuración en Coolify

Crea una aplicación desde este repositorio y selecciona **Dockerfile** como Build Pack.

| Campo | Valor |
|---|---|
| Base Directory | `/Paway` |
| Dockerfile Location | `/Dockerfile` |
| Ports Exposes / puerto interno | `8080` |
| Variable de ejecución | `PORT=8080` |
| Dominio público | `https://paway.groowtech.com` |
| Health check | GET `/actuator/health`, puerto `8080`, estado esperado `200` |

La ruta del Dockerfile es relativa a la Base Directory: Coolify resolverá `/Paway/Dockerfile`. El contexto de construcción debe ser `Paway`, no la raíz de todos los laboratorios. No necesitas un comando de inicio adicional: el Dockerfile ya lo define.

El Dockerfile incluye su propio `HEALTHCHECK`. Puedes usarlo o configurar el equivalente en Coolify. Da un margen inicial de 60 segundos para que Java arranque. La respuesta incluye `"status":"UP"` cuando la aplicación está disponible.

Coolify termina HTTPS en su proxy y dirige el tráfico al puerto interno 8080. La aplicación procesa las cabeceras de ese proxy con `server.forward-headers-strategy=framework`. Cambiar `PORT` también exige cambiar el puerto configurado en Coolify; lo más sencillo es mantener 8080.

### Dominio de Paway

La dirección pública será **https://paway.groowtech.com**. En la zona DNS de `groowtech.com`, el registro `A` con nombre `paway` debe apuntar a la IPv4 pública del servidor donde Coolify ejecuta la aplicación.

En el campo **Domains** de Coolify puedes indicar explícitamente `https://paway.groowtech.com:8080` para dirigir el proxy al puerto interno de Paway. Ese sufijo configura el destino del proxy: los visitantes siguen accediendo a **https://paway.groowtech.com**, sin escribir `:8080`.

Guarda la configuración y despliega la aplicación. Con el DNS correcto y el proxy accesible, Coolify podrá emitir el certificado HTTPS. No necesitas colocar el dominio en `application.properties` ni cambiar el puerto de Java a 443. Para comprobar el despliegue, consulta **https://paway.groowtech.com/actuator/health**.

## Probar Docker localmente

Con Docker Desktop iniciado, desde `Paway`:

```powershell
docker build -t paway:local .
docker compose up -d
docker run --rm --name paway -p 8080:8080 --add-host=host.docker.internal:host-gateway -e PAWAY_DB_URL=jdbc:postgresql://host.docker.internal:5440/paway paway:local
```

En otra terminal:

```powershell
curl.exe --fail http://localhost:8080/actuator/health
```

Abre `http://localhost:8080` para revisar la interfaz. El Java instalado en tu equipo no afecta a esta imagen: el build y el runtime usan Java 21 dentro de Docker.

## PostgreSQL en Coolify

La aplicación guarda clientes, agencias, envíos, eventos de rastreo y hojas de reclamación en PostgreSQL. Flyway aplica `src/main/resources/db/migration` al arrancar (`V1` esquema, `V2` datos de demostración) y Hibernate solo valida el esquema (`ddl-auto=validate`); nunca lo crea ni lo modifica.

Recomendamos PostgreSQL administrado como recurso separado en Coolify. Así puedes actualizar la aplicación sin reemplazar el contenedor de datos y usar las opciones de volumen persistente y respaldo de la BD.

### 1. Preparar la base de datos

En el mismo servidor de Coolify, crea un recurso **PostgreSQL 16**, con base y usuario `paway`. Guarda la contraseña como variable secreta y confirma el volumen persistente. Mantén aplicación y BD en la misma red Docker; estar en el mismo proyecto visual no garantiza por sí solo esa conectividad.

Usa el host y puerto de la **Internal URL** que muestra Coolify. Dentro del contenedor de Paway, `localhost` apunta a Paway, no a PostgreSQL. No necesitas publicar el puerto 5432 para que dos recursos conectados a la misma red se comuniquen.

### 2. Configurar variables de ejecución

```text
PAWAY_DB_URL=jdbc:postgresql://<host-interno>:5432/paway
PAWAY_DB_USER=paway
PAWAY_DB_PASSWORD=<contraseña-secreta>
```

Spring Boot espera una URL **JDBC**, no la URL `postgres://...` copiada literalmente de Coolify. Las credenciales van en sus variables, sin añadirlas a Git ni al Dockerfile. `ddl-auto=validate` y `open-in-view=false` ya están en `application.properties`.

Con la BD conectada, `/actuator/health` también comprueba la conexión: si PostgreSQL no responde, la salud deja de ser `UP` y Coolify lo verá en el health check.

## Alcance actual del despliegue

Los datos sobreviven a reinicios y redeploys. Los perfiles siguen siendo de demostración: antes de utilizar cuentas reales, implementaremos autenticación y autorización con Spring Security; PostgreSQL por sí solo no convierte el selector de perfiles en un login.

## Referencias

- [Coolify: configuración general y rutas](https://coolify.io/docs/applications/configuration/general)
- [Coolify: health checks](https://coolify.io/docs/applications/configuration/health-checks)
- [Coolify: bases de datos](https://coolify.io/docs/databases/)
- [Docker: aplicaciones Java](https://docs.docker.com/guides/java/)
