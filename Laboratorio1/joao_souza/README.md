# Ruta Dev · Laboratorio Semana 1

**Curso:** Marcos de Desarrollo Web · **Autor:** Joao Souza

Página web estática con HTML semántico, CSS adaptable y una interacción en JavaScript,
con los archivos separados por responsabilidad.

## Cómo ejecutarlo

Abrir `index.html` en el navegador, o iniciar Live Server desde Visual Studio Code
(`http://127.0.0.1:5500`). No requiere back-end.

## Estructura

```
joao_souza/
├── index.html          Estructura y significado del contenido
├── css/estilos.css     Presentación y adaptación
├── js/app.js           Comportamiento
├── img/                Imágenes del proyecto
└── evidencias/         Capturas de verificación
```

## Reto de aplicación

| Requisito | Dónde se resolvió |
| --------- | ----------------- |
| Cuarta tarjeta "Herramientas del navegador" | `index.html`, sección `#tecnologias` |
| Sección "Mi meta de aprendizaje" | `index.html`, sección `#meta` |
| Botón "Cambiar tema" con clase en `body` | `index.html` (encabezado) + `js/app.js` |
| Estado accesible con `aria-pressed` | `js/app.js` |
| Comprobación a 320 px y con teclado | `evidencias/` |

### Decisión sobre el botón de tema

Lo coloqué en el encabezado, junto a la navegación, porque afecta a toda la página y no a
una sección concreta; el encabezado es `sticky`, así que queda disponible sin importar
dónde esté leyendo la persona. Es un `<button type="button">` con `id="boton-tema"`,
porque ejecuta una acción en la misma página y no navega a ningún lado — `type="button"`
evita que se comporte como envío si algún día queda dentro de un formulario. Su
`aria-pressed` inicial es `false` porque la página arranca en tema claro, y la etiqueta
se mantiene fija en "Cambiar tema": `aria-pressed` ya comunica el estado, así que el texto
nombra la acción, no el estado.

Esa es justamente la diferencia con el botón de consejo: ahí sí cambia el texto
("Mostrar" / "Ocultar") porque `aria-expanded` describe la visibilidad de **otro**
elemento, no el estado del propio botón.

### Ajuste sobre la pista CSS de la guía

La pista de la sección 10 propone redefinir `--blanco` dentro de `.tema-oscuro`. Aplicada
tal cual, el tema oscuro queda ilegible: en el CSS base `--blanco` cumple dos papeles
distintos — es el fondo de la página **y** el color del texto que va sobre las franjas
azules — y al oscurecerla se rompe el segundo. Medido con las herramientas del navegador,
el encabezado, el pie y los títulos de sección caen a un contraste de **1.16:1** y
**2.07:1**, muy por debajo del mínimo AA de 4.5:1.

La solución fue separar la **paleta** (los colores base) de los **roles**
(`--fondo`, `--superficie`, `--titulo`, `--franja-texto`, …). El tema oscuro solo reasigna
roles, y así todos los pares de texto/fondo quedan entre **9.66:1 y 17.11:1**.

## Verificación

| Comprobación | Resultado |
| ------------ | --------- |
| Título de pestaña descriptivo | OK |
| Enlaces internos llegan a su sección | OK — 3 de 3 |
| Adaptación 320 / 768 / 1440 px | OK — 1, 2 y 4 columnas, sin desplazamiento horizontal |
| Interacción con mouse y teclado | OK — Tab, Enter y Espacio |
| `aria-expanded` refleja el consejo visible | OK |
| `aria-pressed` refleja el tema activo | OK |
| Contraste AA en ambos temas | OK — mínimo 9.66:1 |
| Errores en consola | Ninguno |
| HTML, CSS y JavaScript separados | OK |

Capturas en `evidencias/`: `movil-320.png`, `escritorio-1440.png` y
`escritorio-1440-oscuro.png`.

## Respuestas de la guía

**¿Cómo se separan HTML, CSS y JavaScript?** Cada archivo responde una pregunta distinta.
`index.html` responde *qué contenido existe* y con qué jerarquía; `estilos.css` responde
*cómo se presenta* y cómo se adapta al ancho disponible; `app.js` responde *cómo reacciona*
ante una acción de la persona. El límite se nota en el botón de tema: el JavaScript no
asigna ni un solo color, únicamente activa una clase en `body` y el CSS decide qué
significa esa clase. Si mañana cambia la paleta, se toca un archivo y ninguno más.

**¿Qué problema resolverá Bootstrap y qué no debería reemplazar?** Resuelve el trabajo
repetitivo de presentación: una grilla ya probada, componentes con estados consistentes y
comportamiento entre navegadores que no hay que volver a escribir. No debería reemplazar
la semántica del HTML, la accesibilidad ni el criterio sobre la estructura del contenido.
Una tarjeta con las clases correctas de Bootstrap sigue siendo inaccesible si por dentro
es un `div` que finge ser un botón.

**¿Por qué `defer` es apropiado aquí?** Descarga el script en paralelo sin bloquear el
análisis del HTML y lo ejecuta cuando el DOM ya existe, que es justo lo que necesitan los
`querySelector` del inicio del archivo.

**¿Qué seguiría funcionando si el JavaScript no cargara?** Todo el contenido: encabezado,
navegación, tarjetas, listas y secciones. Solo se perderían el consejo desplegable y el
cambio de tema, porque son mejoras sobre una página que ya es legible sin ellos.

## Pendiente para la entrega

- Captura de la pestaña **Console** del navegador (hay que tomarla desde DevTools).
- Comprimir la carpeta como `SOUZA_Joao_S01_RutaDev.zip`.
