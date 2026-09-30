# Paway — Segundo avance

Aplicación de envíos que integra los cinco módulos del primer avance con **Spring Boot, Spring MVC, Thymeleaf y Bootstrap**. El cotizador, el panel administrativo, el historial del cliente y el rastreo utilizan servicios Java y los mismos datos en memoria.

## Ejecutar

Necesitas un JDK **21 o superior** y conexión a Internet para la primera descarga de Maven y dependencias. El proyecto conserva `java.version=21`: puede compilarse con JDK 26 sin cambiar la versión objetivo. Bootstrap e iconos se cargan desde CDN.

Desde la carpeta `Paway`, en PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Abre **http://localhost:8080**. En Linux/macOS usa `./mvnw spring-boot:run`.

Si tu Java está instalado con NetBeans y no está en el PATH, puedes definirlo solo para esa terminal:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Apache NetBeans\jdk'
.\mvnw.cmd spring-boot:run
```

## Demostrar la integración

1. En **Acceso demo**, selecciona **Administrador**.
2. Registra un envío para Brayan o María; copia la guía generada.
3. Consulta esa guía en **Rastrear**. Una guía inexistente muestra un mensaje, no un resultado simulado.
4. En administración, abre **Ver** y actualiza el estado a **En tránsito** con una observación. Vuelve a rastrear: verás el nuevo estado y el evento.
5. Sal del perfil y entra como el cliente elegido. El envío aparece en su historial, con el mismo importe y estado.
6. Cotiza un envío y pulsa **Solicitar envío**. Los datos de cotización pasan al formulario del cliente, incluso si primero debes seleccionar su perfil.
7. Prueba editar, filtrar y anular desde administración. La anulación conserva la guía y el historial; no permite modificar después el envío.

Hay cuatro envíos iniciales, con guías `PW-<año actual>-0001` a `0004`, y dos clientes de demostración. Los registros y cambios se restablecen al reiniciar la aplicación. **Acceso demo selecciona un perfil en sesión; no autentica con usuario y contraseña.**

## Qué aplicamos de los laboratorios

| Concepto | Aplicación en Paway |
|---|---|
| Spring MVC | Controladores con rutas GET/POST, formularios y redirección tras guardar |
| Servicios y modelos | Reglas de cotización y envíos separadas de la interfaz |
| Thymeleaf | Tablas, condiciones, formularios enlazados y fragmentos compartidos |
| Bootstrap 5.3.2 | Navegación adaptable, tarjetas, tablas, formularios, indicadores y reportes |
| REST y JavaScript | API JSON de distritos consumida al cambiar la provincia |
| Validación | Restricciones de formularios y comprobación de catálogos en el servidor |
| Pruebas | Cálculo de tarifas y recorridos completos mediante MockMvc |

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
├── model/        # Cliente, envío, estados y eventos
└── service/      # Cotización y datos compartidos en memoria
src/main/resources/
├── templates/    # Vistas y fragmentos Thymeleaf
└── static/       # CSS y JavaScript
```

Los módulos originales se encuentran en `../Avance del Proyecto/Avance1`. Esta aplicación retoma sus pantallas y funciones con un layout común y operaciones en el servidor.

## Verificar

```powershell
.\mvnw.cmd test
```

Las pruebas comprueban las tarifas, validaciones, vistas, API, acceso por perfil, traslado de cotización, asociación del cliente, registro, edición, rastreo, actualización de estados, anulación y paginación.
