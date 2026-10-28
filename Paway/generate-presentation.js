const path = require('path');
const pptxgen = require('pptxgenjs');

const pptx = new pptxgen();
const W = 13.333;
const H = 7.5;
const C = {
  red: 'B01116',
  redDark: '790D12',
  ink: '20262E',
  muted: '606A75',
  paper: 'F5F5F2',
  white: 'FFFFFF',
  line: 'D9DDE0',
  teal: '2D777B',
  gold: 'E5A644',
  paleRed: 'F9E9E8',
  paleTeal: 'E7F1F0',
};

const root = __dirname;
const logo = path.join(root, 'src/main/resources/static/images/logo-paway.jpg');
const hero = path.join(root, 'src/main/resources/static/images/paway-social.png');
const output = path.join(root, 'Presentacion_Paway.pptx');

pptx.layout = 'LAYOUT_WIDE';
pptx.author = 'Grupo 5';
pptx.subject = 'Funcionamiento de la aplicación Paway';
pptx.title = 'Paway | Guía de funcionamiento por ventanas';
pptx.company = 'Grupo 5';
pptx.lang = 'es-PE';
pptx.theme = {
  headFontFace: 'Aptos Display',
  bodyFontFace: 'Aptos',
  lang: 'es-PE',
};

function addBase(slide, page, section) {
  slide.background = { color: C.paper };
  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: W, h: 0.12,
    line: { color: C.red, transparency: 100 },
    fill: { color: C.red },
  });
  slide.addText(section.toUpperCase(), {
    x: 0.62, y: 0.32, w: 8.8, h: 0.22,
    fontFace: 'Aptos', fontSize: 9, bold: true, color: C.red,
    charSpacing: 0.8, margin: 0,
  });
  slide.addText('PAWAY  /  GRUPO 5', {
    x: 9.0, y: 7.13, w: 3.2, h: 0.18,
    fontFace: 'Aptos', fontSize: 8, bold: true, color: C.muted,
    align: 'right', margin: 0,
  });
  slide.addText(String(page).padStart(2, '0'), {
    x: 12.35, y: 7.1, w: 0.38, h: 0.22,
    fontFace: 'Aptos', fontSize: 9, bold: true, color: C.red,
    align: 'right', margin: 0,
  });
}

function addPanel(slide, x, y, w, h, fill = C.white) {
  slide.addShape(pptx.ShapeType.roundRect, {
    x, y, w, h, rectRadius: 0.08,
    line: { color: C.line, width: 0.7 },
    fill: { color: fill },
  });
}

function addBulletList(slide, items, x, y, w, options = {}) {
  const fontSize = options.fontSize || 15;
  const gap = options.gap || 0.8;
  items.forEach((item, index) => {
    const yy = y + index * gap;
    slide.addShape(pptx.ShapeType.ellipse, {
      x, y: yy + 0.09, w: 0.12, h: 0.12,
      line: { color: options.dotColor || C.red, transparency: 100 },
      fill: { color: options.dotColor || C.red },
    });
    slide.addText(item, {
      x: x + 0.25, y: yy, w: w - 0.25, h: gap - 0.08,
      fontFace: 'Aptos', fontSize, color: C.ink,
      breakLine: false, valign: 'mid', margin: 0,
      fit: 'shrink',
    });
  });
}

function addSteps(slide, steps, x, y, w) {
  const gap = Math.min(0.78, 3.0 / Math.max(steps.length, 1));
  steps.forEach((step, index) => {
    const yy = y + index * gap;
    slide.addShape(pptx.ShapeType.ellipse, {
      x, y: yy + 0.01, w: 0.34, h: 0.34,
      line: { color: index === steps.length - 1 ? C.teal : C.red, transparency: 100 },
      fill: { color: index === steps.length - 1 ? C.teal : C.red },
    });
    slide.addText(String(index + 1), {
      x, y: yy + 0.055, w: 0.34, h: 0.18,
      fontFace: 'Aptos', fontSize: 10, bold: true, color: C.white,
      align: 'center', margin: 0,
    });
    slide.addText(step, {
      x: x + 0.5, y: yy, w: w - 0.55, h: 0.37,
      fontFace: 'Aptos', fontSize: 13, color: C.ink,
      valign: 'mid', margin: 0, fit: 'shrink',
    });
    if (index < steps.length - 1) {
      slide.addShape(pptx.ShapeType.line, {
        x: x + 0.165, y: yy + 0.35, w: 0, h: gap - 0.4,
        line: { color: C.line, width: 1.2 },
      });
    }
  });
}

function addWindowSlide(page, item) {
  const slide = pptx.addSlide();
  addBase(slide, page, 'Ventanas del sistema');
  slide.addText(item.title, {
    x: 0.62, y: 0.72, w: 8.9, h: 0.48,
    fontFace: 'Aptos Display', fontSize: 27, bold: true, color: C.ink,
    margin: 0, fit: 'shrink',
  });
  slide.addText(item.route, {
    x: 0.64, y: 1.28, w: 11.9, h: 0.32,
    fontFace: 'Aptos', fontSize: 11, color: C.teal,
    margin: 0, fit: 'shrink',
  });

  addPanel(slide, 0.62, 1.82, 5.95, 4.93);
  addPanel(slide, 6.78, 1.82, 5.93, 4.93);
  slide.addText('QUÉ PERMITE', {
    x: 0.95, y: 2.12, w: 4.8, h: 0.24,
    fontFace: 'Aptos', fontSize: 10, bold: true, color: C.red,
    charSpacing: 0.5, margin: 0,
  });
  addBulletList(slide, item.features, 0.98, 2.55, 5.15, {
    fontSize: item.features.length > 4 ? 13 : 14,
    gap: item.features.length > 4 ? 0.65 : 0.78,
  });
  slide.addText('RECORRIDO', {
    x: 7.1, y: 2.12, w: 4.9, h: 0.24,
    fontFace: 'Aptos', fontSize: 10, bold: true, color: C.teal,
    charSpacing: 0.5, margin: 0,
  });
  addSteps(slide, item.steps, 7.15, 2.55, 5.05);

  slide.addShape(pptx.ShapeType.line, {
    x: 7.1, y: 5.57, w: 5.25, h: 0,
    line: { color: C.line, width: 0.9 },
  });
  slide.addText('DATO CLAVE', {
    x: 7.1, y: 5.78, w: 1.45, h: 0.22,
    fontFace: 'Aptos', fontSize: 9, bold: true, color: C.red,
    margin: 0,
  });
  slide.addText(item.note, {
    x: 7.1, y: 6.08, w: 5.15, h: 0.48,
    fontFace: 'Aptos', fontSize: 11, color: C.muted,
    margin: 0, valign: 'top', fit: 'shrink',
  });
}

const screens = [
  {
    title: 'Inicio | punto de entrada', route: 'Ventana: /',
    features: [
      'Búsqueda directa de envíos por número de guía.',
      'Accesos rápidos para enviar, rastrear y cotizar.',
      'Explica el proceso en tres pasos y la cobertura.',
      'Enlaces a ayuda, privacidad, términos y reclamos.',
    ],
    steps: ['Ingresar una guía o elegir una acción.', 'Ir al cotizador, rastreo o perfil.', 'Continuar el flujo elegido.'],
    note: 'La navegación y el pie de página se comparten con las demás ventanas.',
  },
  {
    title: 'Cotizador de envíos', route: 'Ventana: /cotizar  |  API: /api/v1/destinos/{provincia}/distritos',
    features: [
      'Provincia, distrito, categoría, peso y cantidad.',
      'Desglose de base, sobrepeso, recargo y total.',
      'Valida cobertura, distrito y límites del paquete.',
      'La lista de distritos cambia según la provincia.',
    ],
    steps: ['Elegir destino y características.', 'Calcular tarifa en el servidor.', 'Solicitar envío y conservar la cotización.'],
    note: 'Fórmula: (base + sobrepeso desde 2 kg + categoría) × paquetes. El servidor recalcula el total.',
  },
  {
    title: 'Selección de perfil', route: 'Ventanas: /login  |  POST /login  |  POST /logout',
    features: [
      'Permite continuar como Brayan, María o administrador.',
      'El cliente consulta su historial y solicita envíos.',
      'El administrador gestiona envíos y estados.',
      'Cerrar sesión invalida el perfil de la sesión.',
    ],
    steps: ['Abrir Entrar o solicitar un envío.', 'Seleccionar un perfil de demostración.', 'Continuar al panel correspondiente.'],
    note: 'No hay autenticación con usuario y contraseña: es una selección de perfil para demostración.',
  },
  {
    title: 'Formulario de envío', route: 'Ventanas: /cliente/envios/nuevo  |  /admin/envios/nuevo  |  /admin/envios/{id}/editar',
    features: [
      'Datos del destinatario, DNI y agencia de origen.',
      'Destino, categoría, peso, cantidad y observación.',
      'En administración se selecciona el cliente.',
      'Al cliente se le asigna su perfil automáticamente.',
      'Edición conserva guía, estado e historial.',
    ],
    steps: ['Completar datos personales y del paquete.', 'Validar en servidor y recalcular importe.', 'Guardar y volver al panel del perfil.'],
    note: 'El envío inicia como REGISTRADO y recibe una guía PW-año-correlativo.',
  },
  {
    title: 'Panel del cliente', route: 'Ventana: /cliente',
    features: [
      'Muestra los datos del cliente seleccionado.',
      'Lista sus envíos y estadísticas por estado.',
      'Permite seguir cada envío desde su guía.',
      'Incluye acceso para cotizar un nuevo envío.',
    ],
    steps: ['Entrar con perfil de cliente.', 'Revisar el historial propio.', 'Abrir rastreo o iniciar otra cotización.'],
    note: 'La lista se filtra por cliente. Los perfiles son datos de demostración.',
  },
  {
    title: 'Panel administrativo', route: 'Ventana: /admin',
    features: [
      'Busca por guía o nombre del destinatario.',
      'Filtra por estado y agencia de origen.',
      'Paginación de cinco resultados por página.',
      'Indicadores por estado y actividad por agencia.',
      'Registra, abre, edita o anula envíos.',
    ],
    steps: ['Revisar indicadores y listado.', 'Filtrar o abrir el detalle de una guía.', 'Gestionar el envío desde su ficha.'],
    note: 'La anulación conserva guía e historial y bloquea modificaciones posteriores.',
  },
  {
    title: 'Detalle del envío', route: 'Ventana: /admin/envios/{id}',
    features: [
      'Datos de cliente, destinatario, origen y destino.',
      'Muestra paquetes, peso, importe y guía.',
      'Consulta el historial completo de estados.',
      'Enlaza al rastreo público y a la edición.',
      'Permite añadir estado y observación.',
    ],
    steps: ['Abrir Ver desde administración.', 'Revisar datos e historial.', 'Actualizar estado y registrar observación.'],
    note: 'El cambio queda disponible en el rastreo público; los envíos anulados no se editan.',
  },
  {
    title: 'Rastreo público', route: 'Ventana: /rastrear?guide=PW-AAAA-0001',
    features: [
      'Busca por guía y muestra estado actual.',
      'Presenta origen, destino, paquetes y fecha.',
      'Despliega la línea de tiempo de eventos.',
      'Puede mostrar un punto de referencia en mapa.',
      'Informa cuando la guía no existe o es inválida.',
    ],
    steps: ['Ingresar la guía.', 'Consultar estado e historial.', 'Abrir el mapa referencial si está disponible.'],
    note: 'El mapa es referencial, no rastreo GPS en tiempo real; usa cartografía de OpenStreetMap.',
  },
  {
    title: 'Libro de Reclamaciones', route: 'Ventana: /libro-de-reclamaciones',
    features: [
      'Identifica consumidor y medio de contacto.',
      'Incluye datos de representante para menores.',
      'Registra servicio/producto, monto y guía opcional.',
      'Distingue reclamo de queja y captura el pedido.',
      'Permite elegir respuesta por correo o domicilio.',
    ],
    steps: ['Completar identificación y solicitud.', 'Validar token y campos obligatorios.', 'Registrar y abrir la constancia privada.'],
    note: 'La respuesta se informa en hasta 15 días hábiles. La hoja no requiere cuenta ni pago.',
  },
  {
    title: 'Constancia de reclamación', route: 'Ventana: /libro-de-reclamaciones/constancia/{token}',
    features: [
      'Muestra número correlativo y fecha de registro.',
      'Conserva una copia de los datos enviados.',
      'Indica si la atención sigue pendiente o respondida.',
      'Permite imprimir o guardar la página como PDF.',
    ],
    steps: ['Registrar la hoja.', 'Guardar el enlace privado y la constancia.', 'Consultar la respuesta desde ese enlace.'],
    note: 'El enlace contiene datos personales: el sistema advierte que no debe compartirse.',
  },
  {
    title: 'Atención de reclamaciones', route: 'Ventana: /admin/reclamaciones',
    features: [
      'Lista hojas, consumidor, solicitud y medio elegido.',
      'Permite revisar la constancia completa.',
      'Registra respuesta y referencia de entrega.',
      'Actualiza la constancia del consumidor.',
    ],
    steps: ['Revisar la hoja y el canal de respuesta.', 'Gestionar la respuesta fuera del sistema.', 'Guardar texto y referencia de entrega.'],
    note: 'El formulario documenta una respuesta enviada; no envía correos ni cartas por sí mismo.',
  },
  {
    title: 'Preguntas frecuentes', route: 'Ventana: /ayuda',
    features: [
      'Explica destinos, tarifas y solicitud de envíos.',
      'Describe el uso de la guía de rastreo.',
      'Aclara que es una aplicación de demostración.',
      'Ofrece un acceso directo al cotizador.',
    ],
    steps: ['Abrir Centro de ayuda desde el pie.', 'Consultar la pregunta relacionada.', 'Ir al flujo de cotización si se necesita.'],
    note: 'La ayuda advierte que los envíos son de prueba y se restablecen al reiniciar.',
  },
  {
    title: 'Términos y condiciones', route: 'Ventana: /terminos-y-condiciones  |  /terminos redirige aquí',
    features: [
      'Identifica proveedor y cobertura disponible.',
      'Explica tarifas, contratación y preparación.',
      'Resume entrega, seguimiento e incidencias.',
      'Detalla derechos, datos y canal de reclamos.',
    ],
    steps: ['Abrir desde el pie de página.', 'Navegar por el índice de secciones.', 'Ir al libro de reclamaciones cuando corresponda.'],
    note: 'La cotización no constituye por sí sola contratación ni comprobante de pago.',
  },
  {
    title: 'Política de privacidad', route: 'Ventana: /privacidad',
    features: [
      'Identifica al responsable y sus datos de contacto.',
      'Explica datos tratados, finalidad y comunicación.',
      'Describe conservación, seguridad y derechos.',
      'Informa cookies de sesión y servicios externos.',
    ],
    steps: ['Abrir Privacidad desde el pie.', 'Consultar la sección del tratamiento.', 'Contactar al responsable para ejercer derechos.'],
    note: 'La página informa sobre OpenStreetMap, jsDelivr y la conservación de hojas de reclamo.',
  },
  {
    title: 'Ventana de error', route: 'Vista: templates/error/4xx.html',
    features: [
      'Unifica respuestas para solicitud no disponible.',
      'Explica que la página o el envío puede no existir.',
      'Muestra el código de respuesta HTTP.',
      'Ofrece volver al inicio.',
    ],
    steps: ['Ocurre una ruta inexistente o dato inválido.', 'La aplicación muestra el código y el mensaje.', 'El usuario vuelve al inicio y retoma el flujo.'],
    note: 'Incluye errores de recurso inexistente, validación o conflicto de operación.',
  },
];

function addCover() {
  const slide = pptx.addSlide();
  slide.background = { color: C.paper };
  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: W, h: 0.14,
    line: { color: C.red, transparency: 100 }, fill: { color: C.red },
  });
  slide.addImage({ path: logo, x: 0.78, y: 0.68, w: 0.8, h: 0.95 });
  slide.addText('PAWAY  /  GRUPO 5', {
    x: 1.72, y: 0.91, w: 3.6, h: 0.25,
    fontFace: 'Aptos', fontSize: 11, bold: true, color: C.red,
    charSpacing: 1.1, margin: 0,
  });
  slide.addText('Cómo funciona\nPaway', {
    x: 0.78, y: 2.05, w: 5.3, h: 1.55,
    fontFace: 'Aptos Display', fontSize: 34, bold: true,
    color: C.ink, breakLine: false, margin: 0, fit: 'shrink',
  });
  slide.addText('Recorrido completo por cada ventana del sistema', {
    x: 0.82, y: 3.86, w: 4.9, h: 0.65,
    fontFace: 'Aptos', fontSize: 18, color: C.muted,
    margin: 0, fit: 'shrink',
  });
  slide.addShape(pptx.ShapeType.rect, {
    x: 0.82, y: 4.8, w: 1.0, h: 0.08,
    line: { color: C.gold, transparency: 100 }, fill: { color: C.gold },
  });
  slide.addText('Cotizar · registrar · rastrear · atender', {
    x: 0.82, y: 5.1, w: 5.1, h: 0.35,
    fontFace: 'Aptos', fontSize: 13, bold: true, color: C.teal,
    margin: 0,
  });
  slide.addImage({ path: hero, x: 6.35, y: 1.8, w: 6.3, h: 3.31 });
  slide.addText('Guía funcional del proyecto web · Octubre 2026', {
    x: 0.82, y: 6.72, w: 7.3, h: 0.28,
    fontFace: 'Aptos', fontSize: 10, color: C.muted, margin: 0,
  });
  slide.addText('01', {
    x: 12.3, y: 7.1, w: 0.4, h: 0.22,
    fontFace: 'Aptos', fontSize: 9, bold: true, color: C.red,
    align: 'right', margin: 0,
  });
}

function addOverview() {
  const slide = pptx.addSlide();
  addBase(slide, 2, 'Antes de recorrer las ventanas');
  slide.addText('Un solo flujo, dos perfiles de trabajo', {
    x: 0.62, y: 0.72, w: 9.9, h: 0.48,
    fontFace: 'Aptos Display', fontSize: 27, bold: true, color: C.ink,
    margin: 0,
  });
  slide.addText('Paway organiza las operaciones de envío y la atención al consumidor en una aplicación Spring Boot.', {
    x: 0.65, y: 1.3, w: 11.9, h: 0.33,
    fontFace: 'Aptos', fontSize: 13, color: C.muted, margin: 0,
  });

  const flow = [
    { title: 'Cliente', text: 'Cotiza y solicita', color: C.red },
    { title: 'Envío', text: 'Se valida y registra', color: C.teal },
    { title: 'Administración', text: 'Actualiza estados', color: C.red },
    { title: 'Rastreo', text: 'Consulta la guía', color: C.teal },
  ];
  flow.forEach((item, i) => {
    const x = 0.72 + i * 3.15;
    addPanel(slide, x, 2.0, 2.75, 1.12);
    slide.addShape(pptx.ShapeType.rect, {
      x, y: 2.0, w: 0.08, h: 1.12,
      line: { color: item.color, transparency: 100 }, fill: { color: item.color },
    });
    slide.addText(item.title, {
      x: x + 0.24, y: 2.2, w: 2.25, h: 0.28,
      fontFace: 'Aptos Display', fontSize: 15, bold: true, color: C.ink, margin: 0,
    });
    slide.addText(item.text, {
      x: x + 0.24, y: 2.59, w: 2.3, h: 0.25,
      fontFace: 'Aptos', fontSize: 11, color: C.muted, margin: 0,
    });
  });

  addPanel(slide, 0.72, 3.55, 5.8, 2.9);
  addPanel(slide, 6.78, 3.55, 5.8, 2.9);
  slide.addText('Qué conecta las ventanas', {
    x: 1.02, y: 3.85, w: 5.0, h: 0.3,
    fontFace: 'Aptos Display', fontSize: 17, bold: true, color: C.ink, margin: 0,
  });
  addBulletList(slide, [
    'Spring MVC atiende las rutas y formularios.',
    'Thymeleaf renderiza las vistas compartidas.',
    'Servicios Java calculan tarifas y gestionan envíos.',
    'API JSON carga distritos según la provincia.',
  ], 1.05, 4.32, 5.0, { fontSize: 12, gap: 0.46, dotColor: C.teal });
  slide.addText('Alcance de la demostración', {
    x: 7.08, y: 3.85, w: 5.0, h: 0.3,
    fontFace: 'Aptos Display', fontSize: 17, bold: true, color: C.ink, margin: 0,
  });
  addBulletList(slide, [
    'Perfiles sin contraseña; no es autenticación real.',
    'Envíos de ejemplo guardados en memoria.',
    'Reclamos guardados como archivos XML locales.',
    'No hay conexión a base de datos ni envío automático.',
  ], 7.11, 4.32, 5.0, { fontSize: 12, gap: 0.46, dotColor: C.red });
}

addCover();
addOverview();
screens.forEach((screen, index) => addWindowSlide(index + 3, screen));

pptx.writeFile({ fileName: output });