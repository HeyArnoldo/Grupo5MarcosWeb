# Paway — Tercer avance

Aplicación de envíos que integra los cinco módulos del primer avance con **Spring Boot, Spring MVC, Thymeleaf y Bootstrap**. El cotizador, el panel administrativo, el historial del cliente, el rastreo y el libro de reclamaciones comparten los mismos datos, guardados en **PostgreSQL** con Spring Data JPA (Hibernate) y migraciones Flyway.

El [Capítulo III del Avance 3](docs/avance3/README.md) presenta el diagrama relacional (3.4), el prototipo y su recorrido (3.5), y los componentes y la arquitectura (3.6), con diagramas renderizados y fuentes Mermaid editables.

## Ejecutar

Necesitas un JDK **21 o superior**, **Docker** (para PostgreSQL) y conexión a Internet para la primera descarga de Maven y dependencias. El proyecto conserva `java.version=21`: puede compilarse con JDK 26 sin cambiar la versión objetivo. Bootstrap e iconos se cargan desde CDN.

Desde la carpeta `Paway`, en PowerShell:

```powershell
docker compose up -d          # PostgreSQL 16 en localhost:5440 (base, usuario y clave: paway)
.\mvnw.cmd spring-boot:run
```

Al arrancar, Flyway crea las tablas (`V1`) y carga los datos de demostración (`V2`); Hibernate solo **valida** el esquema (`ddl-auto=validate`). `docker compose down` detiene la base y conserva los datos en el volumen `paway-postgres-data`; `docker compose down -v` los borra.

| Variable | Valor por defecto | Uso |
|---|---|---|
| `PAWAY_DB_URL` | `jdbc:postgresql://localhost:5440/paway` | URL JDBC de PostgreSQL |
| `PAWAY_DB_USER` | `paway` | Usuario |
| `PAWAY_DB_PASSWORD` | `paway` | Contraseña (en producción, como secreto) |
| `PORT` | `5000` | Puerto HTTP de la aplicación |

Abre **http://localhost:5000** (o el puerto de `PORT`). En Linux/macOS usa `sh ./mvnw spring-boot:run`.

Si tu Java está instalado con NetBeans y no está en el PATH, puedes definirlo solo para esa terminal:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Apache NetBeans\jdk'
.\mvnw.cmd spring-boot:run
```

## Demostrar la integración

1. En **Entrar**, selecciona **Administrador**.
2. Registra un envío para Brayan o María; copia la guía generada.
3. Consulta esa guía en **Rastrear**. Una guía inexistente muestra un mensaje, no un resultado simulado.
4. En administración, abre **Ver** y actualiza el estado a **En tránsito** con una observación. Vuelve a rastrear: verás el nuevo estado y el evento.
5. Sal del perfil y entra como el cliente elegido. El envío aparece en su historial, con el mismo importe y estado.
6. Cotiza un envío y pulsa **Solicitar envío**. Los datos de cotización pasan al formulario del cliente, incluso si primero debes seleccionar su perfil.
7. Prueba editar, filtrar y anular desde administración. La anulación conserva la guía y el historial; no permite modificar después el envío.

Hay cuatro envíos iniciales, con guías `PW-<año>-0001` a `0004`, y dos clientes de demostración. El año es el de la fecha en que se aplicó la migración `V2`. Los registros y cambios **se conservan al reiniciar** la aplicación. **El acceso selecciona un perfil en sesión; no autentica con usuario y contraseña.**

## Docker, Coolify y PostgreSQL

Desde `Paway`, con Docker Desktop iniciado:

```powershell
docker build -t paway:local .
docker run --rm --name paway -p 8080:8080 --add-host=host.docker.internal:host-gateway -e PAWAY_DB_URL=jdbc:postgresql://host.docker.internal:5440/paway paway:local
```

Para Coolify: Build Pack **Dockerfile**, Base Directory **`/Paway`**, Dockerfile Location **`/Dockerfile`** y puerto **`8080`**. La ruta de salud es **`/actuator/health`**.

La [guía de despliegue](docs/deployment.md) detalla la configuración y la conexión con un recurso PostgreSQL de Coolify.

## Identidad visual

La interfaz utiliza el logo original de `Avance del Proyecto/Avance1/assets/logo-paway.jpg`, disponible en `src/main/resources/static/images/logo-paway.jpg`. Cabecera, pie y acceso comparten la marca, con una portada clara y adaptable a móviles.

La portada incluye un banner ilustrado de ciudad y vehículos, accesos rojos para enviar/rastrear/cotizar y fotografías de stock relacionadas con logística. El cotizador utiliza una ilustración de reparto. El [detalle de recursos visuales y SEO](docs/visual-assets.md) documenta sus fuentes y la imagen social de 1200 × 630 para **https://paway.groowtech.com**.

## Qué aplicamos de los laboratorios

| Concepto | Aplicación en Paway |
|---|---|
| Spring MVC | Controladores con rutas GET/POST, formularios y redirección tras guardar |
| Servicios y modelos | Reglas de cotización y envíos separadas de la interfaz |
| Thymeleaf | Tablas, condiciones, formularios enlazados y fragmentos compartidos |
| Bootstrap 5.3.2 | Navegación adaptable, tarjetas, tablas, formularios, indicadores y reportes |
| REST y JavaScript | API JSON de distritos consumida al cambiar la provincia |
| ORM / Hibernate y Spring Data JPA | Entidades `CustomerEntity`, `AgencyEntity`, `ShipmentEntity`, `ShipmentEventEntity` y `ComplaintEntity`; repositorios y consultas con `Specification` |
| Relaciones entre tablas | Cliente 1—N envío, agencia 1—N envío, envío 1—N evento de rastreo, con claves foráneas |
| Validación | Restricciones en formularios y entidades (Bean Validation), repetidas como `CHECK` en la base de datos |
| Transacciones y concurrencia | Servicios `@Transactional` y bloqueo optimista con `@Version` en el envío |
| Pruebas | Tarifas, repositorios, servicios y recorridos MockMvc contra PostgreSQL real (Testcontainers) |

## Rutas principales

| Ruta | Función |
|---|---|
| `/` | Inicio y búsqueda de guía |
| `/cotizar` | Cotización y solicitud de envío |
| `/rastrear?guide=...` | Estado e historial del envío |
| `/login` | Selección de perfil de demostración |
| `/cliente` | Perfil e historial del cliente en sesión |
| `/admin` | Gestión, filtros, paginación, agencias e indicadores |
| `/api/v1/destinos/{provincia}/distritos` | Distritos en formato JSON |

## Tarifas y cobertura

Se conserva la fórmula del cotizador del primer avance:

```text
total = (tarifa base + max(peso - 2, 0) × 3 + recargo de categoría) × cantidad
```

- Tarifa base: Lima S/ 10, Huarochirí S/ 18, Huaral S/ 20 y Cañete S/ 22.
- Recargos: textil/otros S/ 0, hogar e higiene S/ 2, tecnología S/ 5 y joyería S/ 13.
- El peso es por paquete. Se aceptan hasta dos decimales, de 0.01 a 1000 kg, y de 1 a 100 paquetes por envío.
- Las agencias de demostración se alinean con esta cobertura. No se asignan tarifas nacionales que no estaban definidas en el primer avance.
- Los importes usan `BigDecimal` y se recalculan al guardar; no se acepta un total enviado por el navegador.

## Estructura

```text
src/main/java/pe/edu/utp/Paway/
├── config/       # Acceso por perfil de demostración
├── controller/   # Rutas MVC, datos comunes y API de distritos
├── dto/          # Formularios y restricciones de entrada
├── entity/       # Entidades JPA (tablas de PostgreSQL)
├── model/        # Objetos de vista: cliente, envío, estados y eventos
├── repository/   # Repositorios Spring Data JPA
└── service/      # Casos de uso transaccionales y cotización
src/main/resources/
├── db/migration/ # Migraciones Flyway: V1 esquema, V2 datos de demostración
├── templates/    # Vistas y fragmentos Thymeleaf
└── static/       # CSS y JavaScript
```

Los módulos originales se encuentran en `../Avance del Proyecto/Avance1`. Esta aplicación retoma sus pantallas y funciones con un layout común y operaciones en el servidor.

## Verificar

```powershell
.\mvnw.cmd test
```

En Linux/macOS: `sh ./mvnw -B -ntp test`. Para verificar y generar el JAR ejecutable: `sh ./mvnw -B -ntp verify`.

Las pruebas levantan su propio PostgreSQL con **Testcontainers**: solo necesitan Docker en ejecución, no la base de `compose.yaml`. Comprueban las migraciones sobre una base vacía, la unicidad de la guía, el orden del historial, las restricciones de validación y `CHECK`, el bloqueo optimista, el libro de reclamaciones, las tarifas, validaciones, vistas, API, acceso por perfil, traslado de cotización, asociación del cliente, registro, edición, rastreo, actualización de estados, anulación y paginación.

También verifican que Ayuda describa la persistencia actual y que una restricción de PostgreSQL rechace un registro con HTTP 409, sin alterar las filas existentes ni exponer el nombre de la restricción en la página de error.
