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

function miniRect(slide, x, y, w, h, fill, line = fill, radius = false) {
  slide.addShape(radius ? pptx.ShapeType.roundRect : pptx.ShapeType.rect, {
    x, y, w, h,
    line: { color: line, width: 0.45 },
    fill: { color: fill },
  });
}

function miniText(slide, text, x, y, w, h, options = {}) {
  slide.addText(text, {
    x, y, w, h, margin: 0, fit: 'shrink', valign: 'mid',
    fontFace: 'Aptos', fontSize: options.size || 7,
    color: options.color || C.ink, bold: options.bold || false,
    align: options.align || 'left',
  });
}

function miniField(slide, label, value, x, y, w) {
  miniText(slide, label, x, y, w, 0.12, { size: 6, color: C.muted, bold: true });
  miniRect(slide, x, y + 0.15, w, 0.28, C.white, C.line, true);
  miniText(slide, value, x + 0.08, y + 0.2, w - 0.16, 0.15, { size: 6, color: C.muted });
}

function miniButton(slide, label, x, y, w, fill = C.red) {
  miniRect(slide, x, y, w, 0.3, fill, fill, true);
  miniText(slide, label, x + 0.06, y + 0.04, w - 0.12, 0.2, { size: 6, color: C.white, bold: true, align: 'center' });
}

function miniTable(slide, x, y, widths, headers, rows) {
  const total = widths.reduce((sum, width) => sum + width, 0);
  miniRect(slide, x, y, total, 0.28, 'F0F1F2', 'F0F1F2');
  let xx = x;
  headers.forEach((header, index) => {
    miniText(slide, header, xx + 0.06, y + 0.04, widths[index] - 0.1, 0.18, { size: 5.5, color: C.muted, bold: true });
    xx += widths[index];
  });
  rows.forEach((row, rowIndex) => {
    const yy = y + 0.3 + rowIndex * 0.34;
    miniRect(slide, x, yy, total, 0.31, rowIndex % 2 ? 'FAFAF9' : C.white, 'F1F1F0');
    let colX = x;
    row.forEach((cell, index) => {
      miniText(slide, cell, colX + 0.06, yy + 0.06, widths[index] - 0.1, 0.16, { size: 5.5, color: index === row.length - 1 ? C.teal : C.ink, bold: index === row.length - 1 });
      colX += widths[index];
    });
  });
}

function addMockWindow(slide, kind, x, y, w, h) {
  addPanel(slide, x, y, w, h);
  miniRect(slide, x + 0.08, y + 0.08, w - 0.16, 0.3, 'ECEEED', 'ECEEED', true);
  ['E56D68', 'E8B653', '6EA987'].forEach((color, index) => {
    slide.addShape(pptx.ShapeType.ellipse, {
      x: x + 0.18 + index * 0.16, y: y + 0.17, w: 0.08, h: 0.08,
      line: { color, transparency: 100 }, fill: { color },
    });
  });
  miniRect(slide, x + 0.83, y + 0.13, w - 1.05, 0.2, C.white, C.white, true);
  miniText(slide, 'localhost:5000', x + 0.95, y + 0.16, w - 1.3, 0.12, { size: 5.5, color: C.muted });
  miniRect(slide, x + 0.08, y + 0.45, w - 0.16, 0.42, C.white, 'F0F0EE');
  miniText(slide, 'Paway.', x + 0.24, y + 0.53, 0.75, 0.2, { size: 10, bold: true, color: C.red });
  miniText(slide, 'Inicio   Cotizar   Rastrear   Mis envíos', x + 1.25, y + 0.56, w - 2.45, 0.14, { size: 5.7, color: C.muted });
  miniButton(slide, 'Entrar', x + w - 0.9, y + 0.52, 0.68, C.red);

  const cx = x + 0.28;
  const cy = y + 1.03;
  const cw = w - 0.56;
  const ch = h - 1.25;
  const colGap = 0.16;
  const half = (cw - colGap) / 2;
  const mutedFill = 'F5F6F5';

  if (kind === 'home') {
    miniRect(slide, cx, cy, cw, 1.15, 'F8EDEC', 'F8EDEC', true);
    miniText(slide, 'Cada envío nos acerca.', cx + 0.18, cy + 0.17, cw * 0.55, 0.34, { size: 12, bold: true });
    miniText(slide, 'Cotiza, envía y sigue tu paquete.', cx + 0.2, cy + 0.56, cw * 0.5, 0.2, { size: 7, color: C.muted });
    miniRect(slide, cx + 0.2, cy + 0.83, cw * 0.48, 0.22, C.white, C.line, true);
    miniText(slide, 'Ingresa tu guía…', cx + 0.3, cy + 0.87, cw * 0.35, 0.12, { size: 6, color: C.muted });
    miniButton(slide, 'Buscar', cx + cw * 0.52, cy + 0.82, 0.62);
    ['Hacer envío', 'Rastrear', 'Cotizar'].forEach((label, index) => {
      const tileX = cx + index * (cw / 3);
      miniRect(slide, tileX, cy + 1.34, cw / 3 - 0.08, 0.74, C.white, C.line, true);
      miniText(slide, ['↗', '◎', 'S/'][index], tileX + 0.12, cy + 1.44, 0.28, 0.22, { size: 11, bold: true, color: C.red });
      miniText(slide, label, tileX + 0.12, cy + 1.75, cw / 3 - 0.2, 0.16, { size: 7, bold: true });
    });
    miniText(slide, 'Lima     Huarochirí     Huaral     Cañete', cx + 0.05, cy + 2.28, cw - 0.1, 0.2, { size: 7, color: C.teal, align: 'center' });
  } else if (kind === 'quote') {
    miniText(slide, 'Cotizador de envíos', cx, cy, cw, 0.25, { size: 10, bold: true });
    miniField(slide, 'Provincia', 'Lima', cx, cy + 0.38, half);
    miniField(slide, 'Distrito', 'Miraflores', cx + half + colGap, cy + 0.38, half);
    miniField(slide, 'Categoría', 'Tecnología', cx, cy + 1.02, half);
    miniField(slide, 'Peso / cantidad', '3 kg  ·  2 paquetes', cx + half + colGap, cy + 1.02, half);
    miniButton(slide, 'Calcular tarifa', cx, cy + 1.72, 1.05);
    miniRect(slide, cx + half + colGap, cy + 1.73, half, 1.08, 'F8EDEC', 'F8EDEC', true);
    miniText(slide, 'Total estimado', cx + half + colGap + 0.14, cy + 1.87, half - 0.25, 0.16, { size: 6, color: C.muted });
    miniText(slide, 'S/ 38.00', cx + half + colGap + 0.14, cy + 2.12, half - 0.25, 0.3, { size: 16, bold: true, color: C.red });
    miniText(slide, 'base + sobrepeso + categoría', cx + half + colGap + 0.14, cy + 2.51, half - 0.25, 0.14, { size: 5.5, color: C.muted });
  } else if (kind === 'login') {
    miniRect(slide, cx, cy, half, ch - 0.1, 'F8EDEC', 'F8EDEC', true);
    miniText(slide, 'Tu próximo envío\nempieza contigo.', cx + 0.16, cy + 0.3, half - 0.28, 0.55, { size: 11, bold: true });
    miniText(slide, 'Un perfil, todos tus envíos.', cx + 0.16, cy + 0.98, half - 0.28, 0.2, { size: 6, color: C.muted });
    miniText(slide, 'Elige tu espacio', cx + half + colGap, cy + 0.08, half, 0.24, { size: 9, bold: true });
    ['Brayan Lopez · Cliente', 'María Quispe · Cliente', 'Administrador'].forEach((name, index) => {
      const yy = cy + 0.47 + index * 0.55;
      miniRect(slide, cx + half + colGap, yy, half, 0.43, C.white, C.line, true);
      miniText(slide, '◯', cx + half + colGap + 0.12, yy + 0.1, 0.18, 0.16, { size: 8, color: C.red });
      miniText(slide, name, cx + half + colGap + 0.39, yy + 0.1, half - 0.5, 0.16, { size: 6.5, bold: true });
    });
    miniButton(slide, 'Continuar', cx + half + colGap, cy + 2.23, half);
  } else if (kind === 'form') {
    miniText(slide, 'Registrar envío', cx, cy, cw, 0.25, { size: 10, bold: true });
    ['1  Destinatario', '2  Paquete', '3  Detalles'].forEach((step, index) => {
      miniRect(slide, cx + index * (cw / 3), cy + 0.35, cw / 3 - 0.08, 0.3, index === 0 ? 'F8EDEC' : mutedFill, C.line, true);
      miniText(slide, step, cx + index * (cw / 3) + 0.08, cy + 0.41, cw / 3 - 0.18, 0.12, { size: 5.7, bold: true });
    });
    miniField(slide, 'Destinatario', 'Nombre completo', cx, cy + 0.83, half);
    miniField(slide, 'DNI', '12345678', cx + half + colGap, cy + 0.83, half);
    miniField(slide, 'Agencia de origen', 'Lima Centro', cx, cy + 1.48, half);
    miniField(slide, 'Destino', 'Provincia / distrito', cx + half + colGap, cy + 1.48, half);
    miniField(slide, 'Categoría, peso y cantidad', 'Textil  ·  1 kg  ·  1', cx, cy + 2.13, cw);
    miniButton(slide, 'Guardar envío', cx + cw - 1.0, cy + 2.73, 1.0);
  } else if (kind === 'client' || kind === 'admin') {
    miniText(slide, kind === 'client' ? 'Hola, Brayan' : 'Panel administrativo', cx, cy, cw * 0.68, 0.25, { size: 10, bold: true });
    miniButton(slide, kind === 'client' ? 'Nuevo envío' : '+ Registrar', cx + cw - 1.0, cy, 1.0);
    const stats = kind === 'client' ? ['3 envíos', '1 en tránsito', '1 entregado'] : ['4 registrados', '1 en tránsito', '1 incidencia'];
    stats.forEach((stat, index) => {
      const tileX = cx + index * (cw / 3);
      miniRect(slide, tileX, cy + 0.4, cw / 3 - 0.08, 0.48, index === 1 ? 'F8EDEC' : mutedFill, C.line, true);
      miniText(slide, stat, tileX + 0.1, cy + 0.54, cw / 3 - 0.22, 0.16, { size: 6.5, bold: true });
    });
    if (kind === 'admin') {
      miniField(slide, 'Buscar guía o destinatario', 'PW-2026…', cx, cy + 1.03, half);
      miniField(slide, 'Filtrar estado / agencia', 'Todos', cx + half + colGap, cy + 1.03, half);
    } else {
      miniText(slide, 'Mis envíos', cx, cy + 1.02, cw, 0.2, { size: 7, bold: true });
    }
    miniTable(slide, cx, cy + 1.34, [cw * 0.32, cw * 0.36, cw * 0.32], ['GUÍA', 'DESTINATARIO', 'ESTADO'], [
      ['PW-2026-0001', 'Carlos Mendoza', 'En tránsito'],
      ['PW-2026-0002', 'Lucía Ramos', 'Entregado'],
      ['PW-2026-0003', 'Diego Salas', 'Incidencia'],
    ]);
  } else if (kind === 'detail' || kind === 'tracking') {
    miniText(slide, kind === 'detail' ? 'PW-2026-0001  ·  En tránsito' : '¿Cómo va tu envío?', cx, cy, cw, 0.25, { size: 9, bold: true });
    if (kind === 'tracking') {
      miniRect(slide, cx, cy + 0.36, cw, 0.38, C.white, C.line, true);
      miniText(slide, 'Número de guía  ·  PW-2026-0001', cx + 0.12, cy + 0.47, cw - 1.3, 0.15, { size: 6, color: C.muted });
      miniButton(slide, 'Buscar', cx + cw - 0.85, cy + 0.4, 0.72);
    }
    miniRect(slide, cx, cy + 0.84, half, 1.46, C.white, C.line, true);
    miniText(slide, 'DATOS DEL ENVÍO', cx + 0.12, cy + 0.96, half - 0.2, 0.17, { size: 6, bold: true, color: C.red });
    ['Origen · Lima Centro', 'Destino · Chancay', 'Paquetes · 1', 'Peso · 1 kg'].forEach((line, index) => miniText(slide, line, cx + 0.12, cy + 1.25 + index * 0.22, half - 0.2, 0.13, { size: 6, color: C.muted }));
    miniRect(slide, cx + half + colGap, cy + 0.84, half, 1.46, C.white, C.line, true);
    miniText(slide, 'HISTORIAL', cx + half + colGap + 0.12, cy + 0.96, half - 0.2, 0.17, { size: 6, bold: true, color: C.teal });
    ['● Registrado', '● En almacén', '● En tránsito'].forEach((line, index) => miniText(slide, line, cx + half + colGap + 0.14, cy + 1.28 + index * 0.28, half - 0.26, 0.16, { size: 6, color: index === 2 ? C.red : C.muted }));
    if (kind === 'tracking') {
      miniRect(slide, cx, cy + 2.45, cw, 0.7, 'E8EFEB', 'E8EFEB', true);
      miniText(slide, 'Punto de referencia del envío · ubicación referencial', cx + 0.15, cy + 2.68, cw - 0.3, 0.18, { size: 6, color: C.teal, bold: true, align: 'center' });
    } else {
      miniButton(slide, 'Actualizar estado', cx + cw - 1.25, cy + 2.48, 1.25);
      miniText(slide, 'Observación y cambio registrados en el historial', cx, cy + 2.56, cw - 1.4, 0.16, { size: 5.5, color: C.muted });
    }
  } else if (kind === 'complaint' || kind === 'complaintAdmin') {
    miniText(slide, kind === 'complaint' ? 'Libro de Reclamaciones' : 'Atención de reclamaciones', cx, cy, cw, 0.25, { size: 9, bold: true });
    if (kind === 'complaint') {
      miniRect(slide, cx, cy + 0.4, half * 0.72, ch - 0.55, 'F8EDEC', 'F8EDEC', true);
      miniText(slide, 'Te escuchamos', cx + 0.12, cy + 0.55, half * 0.72 - 0.24, 0.2, { size: 7, bold: true });
      miniText(slide, 'Reclamo o queja\nRespuesta hasta 15 días hábiles', cx + 0.12, cy + 0.9, half * 0.72 - 0.24, 0.45, { size: 6, color: C.muted });
      const fx = cx + half * 0.8;
      const fw = cw - half * 0.8;
      miniText(slide, '1. Identificación del consumidor', fx, cy + 0.43, fw, 0.18, { size: 6, bold: true, color: C.red });
      miniField(slide, 'Nombre completo', 'Nombre y apellidos', fx, cy + 0.68, fw);
      miniField(slide, 'Documento / teléfono', 'DNI · contacto', fx, cy + 1.24, fw);
      miniText(slide, '2. Producto o servicio  ·  3. Pedido', fx, cy + 1.82, fw, 0.18, { size: 6, bold: true, color: C.teal });
      miniRect(slide, fx, cy + 2.08, fw, 0.52, C.white, C.line, true);
      miniText(slide, 'Describe lo ocurrido y la solución que solicitas…', fx + 0.1, cy + 2.2, fw - 0.2, 0.24, { size: 5.5, color: C.muted });
      miniButton(slide, 'Registrar hoja', fx, cy + 2.71, 0.95);
    } else {
      miniRect(slide, cx, cy + 0.4, cw, 0.48, 'F8EDEC', 'F8EDEC', true);
      miniText(slide, 'PW-WEB-000001  ·  Reclamo  ·  Cliente: María Quispe', cx + 0.14, cy + 0.55, cw - 0.28, 0.16, { size: 6, bold: true });
      miniText(slide, 'Detalle del consumidor', cx, cy + 1.04, cw, 0.16, { size: 6, bold: true });
      miniRect(slide, cx, cy + 1.27, cw, 0.54, C.white, C.line, true);
      miniText(slide, 'Descripción del reclamo y solicitud registrada…', cx + 0.12, cy + 1.42, cw - 0.24, 0.18, { size: 5.7, color: C.muted });
      miniText(slide, 'Respuesta y acciones adoptadas', cx, cy + 1.98, cw, 0.16, { size: 6, bold: true });
      miniRect(slide, cx, cy + 2.2, cw, 0.56, C.white, C.line, true);
      miniText(slide, 'Escribir respuesta que se comunicó al consumidor…', cx + 0.12, cy + 2.36, cw - 0.24, 0.18, { size: 5.7, color: C.muted });
      miniButton(slide, 'Registrar respuesta enviada', cx + cw - 1.55, cy + 2.88, 1.55, C.teal);
    }
  } else if (kind === 'receipt') {
    miniRect(slide, cx + 0.65, cy, cw - 1.3, ch - 0.05, 'FCFCFB', C.line, true);
    miniText(slide, 'Paway  ·  HOJA DE RECLAMACIÓN', cx + 0.86, cy + 0.18, cw - 1.7, 0.22, { size: 8, bold: true, color: C.red, align: 'center' });
    miniText(slide, 'PW-WEB-000001   ·   Fecha de registro', cx + 0.86, cy + 0.48, cw - 1.7, 0.16, { size: 6, color: C.muted, align: 'center' });
    ['1. Identificación del consumidor', 'Nombre · Documento · Domicilio · Correo', '2. Bien contratado y descripción', '3. Detalle del reclamo y pedido', '4. Respuesta del proveedor'].forEach((line, index) => {
      const yy = cy + 0.83 + index * 0.38;
      miniText(slide, line, cx + 0.88, yy, cw - 1.75, 0.15, { size: 6.3, bold: index % 2 === 0, color: index % 2 === 0 ? C.ink : C.muted });
      if (index < 4) miniRect(slide, cx + 0.88, yy + 0.2, cw - 1.75, 0.01, C.line, C.line);
    });
    miniButton(slide, 'Imprimir / guardar PDF', cx + cw - 2.55, cy + ch - 0.45, 1.7);
  } else if (kind === 'help' || kind === 'legal' || kind === 'privacy') {
    miniText(slide, kind === 'help' ? 'Preguntas frecuentes' : kind === 'legal' ? 'Términos y condiciones' : 'Política de privacidad', cx, cy, cw, 0.25, { size: 9, bold: true });
    miniRect(slide, cx, cy + 0.4, 1.38, ch - 0.55, mutedFill, mutedFill, true);
    miniText(slide, kind === 'help' ? 'Centro de ayuda' : 'En esta página', cx + 0.1, cy + 0.54, 1.18, 0.18, { size: 6.5, bold: true, color: C.teal });
    ['Servicio', 'Tarifas', 'Seguimiento', 'Datos personales'].forEach((line, index) => miniText(slide, line, cx + 0.1, cy + 0.85 + index * 0.28, 1.17, 0.16, { size: 5.7, color: C.muted }));
    const tx = cx + 1.55;
    const tw = cw - 1.55;
    ['¿Qué destinos puedo cotizar?', '¿Cómo se calcula el precio?', '¿Cómo consulto el envío?'].forEach((line, index) => {
      const yy = cy + 0.42 + index * 0.75;
      miniRect(slide, tx, yy, tw, 0.62, C.white, C.line, true);
      miniText(slide, line, tx + 0.12, yy + 0.1, tw - 0.24, 0.16, { size: 6.5, bold: true });
      miniText(slide, 'Información y condiciones del servicio Paway…', tx + 0.12, yy + 0.34, tw - 0.24, 0.16, { size: 5.5, color: C.muted });
    });
  } else {
    miniRect(slide, cx + 0.72, cy + 0.46, cw - 1.44, 2.15, 'F8EDEC', 'F8EDEC', true);
    miniText(slide, '404', cx + 0.92, cy + 0.75, cw - 1.84, 0.55, { size: 26, bold: true, color: C.red, align: 'center' });
    miniText(slide, 'No pudimos completar tu solicitud', cx + 0.92, cy + 1.4, cw - 1.84, 0.25, { size: 8, bold: true, align: 'center' });
    miniText(slide, 'La página o el envío no existe o no está disponible.', cx + 0.92, cy + 1.75, cw - 1.84, 0.2, { size: 6, color: C.muted, align: 'center' });
    miniButton(slide, 'Volver al inicio', cx + cw / 2 - 0.55, cy + 2.08, 1.1);
  }
}

function addWindowSlide(page, item, index) {
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

  addPanel(slide, 0.62, 1.82, 4.32, 2.77);
  slide.addText('QUÉ PERMITE', {
    x: 0.9, y: 2.08, w: 3.8, h: 0.24,
    fontFace: 'Aptos', fontSize: 10, bold: true, color: C.red,
    charSpacing: 0.5, margin: 0,
  });
  addBulletList(slide, item.features, 0.9, 2.42, 3.78, {
    fontSize: 10.5,
    gap: item.features.length > 4 ? 0.4 : 0.48,
  });
  addPanel(slide, 0.62, 4.77, 4.32, 1.97);
  slide.addText('RECORRIDO', {
    x: 0.9, y: 4.98, w: 3.8, h: 0.22,
    fontFace: 'Aptos', fontSize: 9, bold: true, color: C.teal,
    charSpacing: 0.5, margin: 0,
  });
  addSteps(slide, item.steps, 0.94, 5.3, 3.65);
  slide.addText(item.note, {
    x: 0.72, y: 6.82, w: 11.95, h: 0.22,
    fontFace: 'Aptos', fontSize: 8.5, color: C.muted,
    margin: 0, valign: 'mid', fit: 'shrink',
  });
  miniText(slide, 'VISTA REPRESENTATIVA', 5.18, 1.62, 2.5, 0.15, { size: 7, bold: true, color: C.teal });
  addMockWindow(slide, ['home', 'quote', 'login', 'form', 'client', 'admin', 'detail', 'tracking', 'complaint', 'receipt', 'complaintAdmin', 'help', 'legal', 'privacy', 'error'][index], 5.18, 1.82, 7.53, 4.92);
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
    'Spring MVC conecta rutas, formularios y servicios.',
    'Thymeleaf renderiza vistas HTML del lado servidor.',
    'Hibernate/JPA persiste entidades mediante repositorios.',
    'La API JSON entrega distritos según la provincia.',
  ], 1.05, 4.32, 5.0, { fontSize: 12, gap: 0.46, dotColor: C.teal });
  slide.addText('Alcance de la demostración', {
    x: 7.08, y: 3.85, w: 5.0, h: 0.3,
    fontFace: 'Aptos Display', fontSize: 17, bold: true, color: C.ink, margin: 0,
  });
  addBulletList(slide, [
    'Perfiles sin contraseña; no es autenticación real.',
    'PostgreSQL conserva datos en el modo normal.',
    'El perfil local usa H2 en memoria y se reinicia.',
    'Las respuestas de reclamos no se envían automáticamente.',
  ], 7.11, 4.32, 5.0, { fontSize: 12, gap: 0.46, dotColor: C.red });
}

function addTechStack() {
  const slide = pptx.addSlide();
  addBase(slide, 3, 'Tecnologías y arquitectura');
  slide.addText('De la pantalla a los datos', {
    x: 0.62, y: 0.72, w: 8.8, h: 0.48,
    fontFace: 'Aptos Display', fontSize: 27, bold: true, color: C.ink, margin: 0,
  });
  slide.addText('Cada capa tiene una responsabilidad y se comunica con la siguiente.', {
    x: 0.65, y: 1.3, w: 11.6, h: 0.3,
    fontFace: 'Aptos', fontSize: 13, color: C.muted, margin: 0,
  });

  const layers = [
    { y: 1.9, color: C.gold, tag: 'UI', title: 'Navegador', desc: 'Thymeleaf · Bootstrap 5 · JavaScript · Leaflet / OpenStreetMap' },
    { y: 3.05, color: C.red, tag: 'MVC', title: 'Aplicación Spring Boot', desc: 'Controladores MVC · formularios · validación · API REST de distritos' },
    { y: 4.2, color: C.teal, tag: 'ORM', title: 'Servicios y persistencia', desc: 'Reglas de negocio · Spring Data JPA · Hibernate mapea entidades ↔ tablas' },
  ];
  layers.forEach((layer) => {
    addPanel(slide, 0.72, layer.y, 5.65, 0.9);
    slide.addShape(pptx.ShapeType.ellipse, {
      x: 0.95, y: layer.y + 0.2, w: 0.5, h: 0.5,
      line: { color: layer.color, transparency: 100 }, fill: { color: layer.color },
    });
    miniText(slide, layer.tag, 1.0, layer.y + 0.34, 0.4, 0.16, { size: 7, color: C.white, bold: true, align: 'center' });
    slide.addText(layer.title, {
      x: 1.7, y: layer.y + 0.17, w: 4.2, h: 0.22,
      fontFace: 'Aptos Display', fontSize: 14, bold: true, color: C.ink, margin: 0,
    });
    slide.addText(layer.desc, {
      x: 1.7, y: layer.y + 0.48, w: 4.35, h: 0.2,
      fontFace: 'Aptos', fontSize: 9, color: C.muted, margin: 0, fit: 'shrink',
    });
  });
  [2.83, 3.98].forEach((y) => slide.addShape(pptx.ShapeType.downArrow, {
    x: 3.25, y, w: 0.42, h: 0.2,
    line: { color: C.line, transparency: 100 }, fill: { color: C.line },
  }));

  addPanel(slide, 6.75, 1.9, 5.85, 1.8, C.paleTeal);
  miniText(slide, 'PRODUCCIÓN / PERSISTENTE', 7.08, 2.18, 4.9, 0.2, { size: 8, color: C.teal, bold: true });
  slide.addText('PostgreSQL 16  +  Flyway', {
    x: 7.08, y: 2.5, w: 4.9, h: 0.36,
    fontFace: 'Aptos Display', fontSize: 21, bold: true, color: C.ink, margin: 0,
  });
  slide.addText('Flyway crea el esquema (V1) y carga los datos de ejemplo (V2). Hibernate valida el esquema; los repositorios JPA leen y guardan clientes, envíos y reclamos.', {
    x: 7.08, y: 2.98, w: 5.0, h: 0.48,
    fontFace: 'Aptos', fontSize: 10, color: C.ink, margin: 0, fit: 'shrink', valign: 'top',
  });

  addPanel(slide, 6.75, 3.95, 5.85, 1.4, C.paleRed);
  miniText(slide, 'EJECUCIÓN LOCAL / SIN DOCKER', 7.08, 4.18, 4.9, 0.2, { size: 8, color: C.red, bold: true });
  slide.addText('H2 en memoria', {
    x: 7.08, y: 4.48, w: 4.9, h: 0.28,
    fontFace: 'Aptos Display', fontSize: 17, bold: true, color: C.ink, margin: 0,
  });
  slide.addText('El perfil local crea las tablas desde las entidades y carga datos demo. Al cerrar la app, la base temporal se reinicia.', {
    x: 7.08, y: 4.84, w: 5.0, h: 0.34,
    fontFace: 'Aptos', fontSize: 9, color: C.ink, margin: 0, fit: 'shrink',
  });

  addPanel(slide, 0.72, 5.55, 11.88, 0.92);
  const chips = [
    ['Java 21', 'lenguaje'], ['Maven', 'build'], ['Spring Boot', 'arranque'],
    ['Spring MVC', 'rutas'], ['Thymeleaf', 'HTML'], ['Hibernate / JPA', 'ORM'],
    ['PostgreSQL', 'persistencia'], ['Flyway', 'migraciones'],
  ];
  chips.forEach(([name, role], index) => {
    const col = index % 4;
    const row = Math.floor(index / 4);
    const xx = 0.98 + col * 2.92;
    const yy = 5.72 + row * 0.38;
    slide.addText(name, {
      x: xx, y: yy, w: 1.72, h: 0.17,
      fontFace: 'Aptos', fontSize: 9, bold: true, color: C.ink, margin: 0,
    });
    slide.addText(role, {
      x: xx + 1.78, y: yy + 0.01, w: 0.85, h: 0.15,
      fontFace: 'Aptos', fontSize: 7, color: C.muted, align: 'right', margin: 0,
    });
  });
}

addCover();
addOverview();
addTechStack();
screens.forEach((screen, index) => addWindowSlide(index + 4, screen, index));

pptx.writeFile({ fileName: output });