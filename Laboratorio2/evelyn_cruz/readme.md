==================================================
UTP - MARCOS DE DESARROLLO WEB
Laboratorio - Semana 2
==================================================

1. DECISIONES DE BREAKPOINTS
--------------------------------------------------
- Mobile (375 px): Diseño de una sola columna (col-12) para maximizar legibilidad en pantallas reducidas.
- Tablet (800 px): Disposición de 2 columnas (col-md-6) para aprovechar el espacio horizontal sin saturar.
- Desktop (1440 px): Layout de 4 columnas (col-xl-3) para visualización completa de los talleres en una sola fila.

2. MEJORA DE ACCESIBILIDAD IMPLEMENTADA
--------------------------------------------------
Se incorporó el atributo role="status" y aria-live="polite" en el contenedor #estado-registro. Esto permite que los lectores de pantalla anuncien la confirmación del taller seleccionado a los usuarios con discapacidad visual sin interrumpir su navegación.

3. PROBLEMA RESUELTO
--------------------------------------------------
Problema: Al hacer clic en los botones de selección dentro de la tabla en dispositivos móviles, el mensaje de confirmación se mostraba fuera del área visible del usuario.
Solución: Se implementó un desplazamiento suave en JavaScript mediante la función statusElement.scrollIntoView() para garantizar que el usuario note la actualización de la pantalla inmediatamente.