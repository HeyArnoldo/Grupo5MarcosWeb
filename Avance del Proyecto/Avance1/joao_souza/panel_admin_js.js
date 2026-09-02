/* =====================================================
   PERSONA 5 - JOAO SOUZA
   MÓDULO: PANEL ADMINISTRATIVO
   ===================================================== */


/*
    Los datos son internos y simulados.
    Actualmente el proyecto no utiliza
    una base de datos.
*/


document.addEventListener('DOMContentLoaded', () => {


    // =====================================================
    // AGENCIAS
    // =====================================================

    const agencias = [

        { nombre: "Lima Centro", ciudad: "Lima", capacidad: 400 },

        { nombre: "Arequipa Norte", ciudad: "Arequipa", capacidad: 250 },

        { nombre: "Cusco Wanchaq", ciudad: "Cusco", capacidad: 180 },

        { nombre: "Trujillo Centro", ciudad: "La Libertad", capacidad: 220 },

        { nombre: "Piura Este", ciudad: "Piura", capacidad: 150 }

    ];


    // =====================================================
    // ENVÍOS REGISTRADOS
    // =====================================================

    const envios = [

        {
            id: 1,
            guia: "PW-2026-0001",
            destinatario: "María Quispe",
            documento: "70123456",
            origen: "Lima Centro",
            destino: "Cusco Wanchaq",
            peso: 12.5,
            fecha: "2026-08-24",
            estado: "Entregado",
            observacion: "Entregado en recepción."
        },

        {
            id: 2,
            guia: "PW-2026-0002",
            destinatario: "Carlos Mendoza",
            documento: "45678912",
            origen: "Arequipa Norte",
            destino: "Lima Centro",
            peso: 3.2,
            fecha: "2026-08-26",
            estado: "En tránsito",
            observacion: ""
        },

        {
            id: 3,
            guia: "PW-2026-0003",
            destinatario: "Lucía Ramos",
            documento: "71234567",
            origen: "Trujillo Centro",
            destino: "Piura Este",
            peso: 45,
            fecha: "2026-08-27",
            estado: "Registrado",
            observacion: "Carga pesada, requiere montacargas."
        },

        {
            id: 4,
            guia: "PW-2026-0004",
            destinatario: "Diego Salas",
            documento: "40987654",
            origen: "Lima Centro",
            destino: "Arequipa Norte",
            peso: 8.7,
            fecha: "2026-08-27",
            estado: "Incidencia",
            observacion: "Dirección incompleta, pendiente de contacto."
        },

        {
            id: 5,
            guia: "PW-2026-0005",
            destinatario: "Ana Flores",
            documento: "72345678",
            origen: "Cusco Wanchaq",
            destino: "Lima Centro",
            peso: 1.4,
            fecha: "2026-08-28",
            estado: "Entregado",
            observacion: ""
        },

        {
            id: 6,
            guia: "PW-2026-0006",
            destinatario: "Jorge Huamán",
            documento: "43219876",
            origen: "Piura Este",
            destino: "Trujillo Centro",
            peso: 22,
            fecha: "2026-08-28",
            estado: "En tránsito",
            observacion: ""
        },

        {
            id: 7,
            guia: "PW-2026-0007",
            destinatario: "Rosa Ccahuana",
            documento: "76543210",
            origen: "Lima Centro",
            destino: "Trujillo Centro",
            peso: 5.9,
            fecha: "2026-08-29",
            estado: "Registrado",
            observacion: ""
        },

        {
            id: 8,
            guia: "PW-2026-0008",
            destinatario: "Fernando Vargas",
            documento: "41236547",
            origen: "Arequipa Norte",
            destino: "Cusco Wanchaq",
            peso: 17.3,
            fecha: "2026-08-30",
            estado: "En tránsito",
            observacion: ""
        },

        {
            id: 9,
            guia: "PW-2026-0009",
            destinatario: "Patricia León",
            documento: "77889900",
            origen: "Lima Centro",
            destino: "Piura Este",
            peso: 2.1,
            fecha: "2026-08-30",
            estado: "Registrado",
            observacion: ""
        },

        {
            id: 10,
            guia: "PW-2026-0010",
            destinatario: "Miguel Ortiz",
            documento: "44556677",
            origen: "Trujillo Centro",
            destino: "Lima Centro",
            peso: 60,
            fecha: "2026-08-31",
            estado: "Registrado",
            observacion: "Carga volumétrica."
        }

    ];


    // Secuencia lógica del seguimiento

    const FLUJO_ESTADOS = [
        "Registrado",
        "En almacén",
        "En tránsito",
        "En reparto",
        "Entregado"
    ];


    const POR_PAGINA = 5;


    // =====================================================
    // ESTADO DE LA INTERFAZ
    // =====================================================

    let paginaActual = 1;

    let idParaAnular = null;


    // =====================================================
    // REFERENCIAS DEL DOM
    // =====================================================

    const $ = (selector) => document.querySelector(selector);

    const cuerpoTabla = $('#cuerpoTabla');

    const resumenTabla = $('#resumenTabla');

    const paginacion = $('#paginacion');

    const listaAgencias = $('#listaAgencias');

    const filtroBusqueda = $('#filtroBusqueda');

    const filtroEstado = $('#filtroEstado');

    const filtroAgencia = $('#filtroAgencia');

    const formEnvio = $('#formEnvio');


    const modalEnvio = new bootstrap.Modal('#modalEnvio');

    const modalDetalle = new bootstrap.Modal('#modalDetalle');

    const modalConfirmar = new bootstrap.Modal('#modalConfirmar');


    // =====================================================
    // UTILIDADES
    // =====================================================

    /* Evita que un texto ingresado rompa el HTML generado */

    function escapar(texto) {

        const temporal = document.createElement('div');

        temporal.textContent = texto ?? '';

        return temporal.innerHTML;

    }


    function formatearFecha(fecha) {

        const [anio, mes, dia] = fecha.split('-');

        return `${dia}/${mes}/${anio}`;

    }


    function claseEstado(estado) {

        const clases = {
            "Registrado": "badge-registrado",
            "En tránsito": "badge-transito",
            "Entregado": "badge-entregado",
            "Incidencia": "badge-incidencia"
        };

        return clases[estado] || "badge-registrado";

    }


    function siguienteGuia() {

        const numero = envios.reduce((mayor, envio) => {

            const actual = parseInt(envio.guia.split('-')[2], 10);

            return actual > mayor ? actual : mayor;

        }, 0) + 1;

        return `PW-2026-${String(numero).padStart(4, '0')}`;

    }


    function mostrarAviso(mensaje, tipo = 'success') {

        const contenedor = $('#contenedorAvisos');

        const aviso = document.createElement('div');

        aviso.className = `toast align-items-center text-bg-${tipo} border-0`;

        aviso.setAttribute('role', 'alert');

        aviso.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${escapar(mensaje)}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto"
                    data-bs-dismiss="toast" aria-label="Cerrar"></button>
            </div>`;

        contenedor.appendChild(aviso);

        const instancia = new bootstrap.Toast(aviso, { delay: 3000 });

        instancia.show();

        aviso.addEventListener('hidden.bs.toast', () => aviso.remove());

    }


    // =====================================================
    // INDICADORES
    // =====================================================

    function actualizarIndicadores() {

        const contar = (estado) => envios.filter(e => e.estado === estado).length;

        $('#kpiTotal').textContent = envios.length;

        $('#kpiTransito').textContent = contar("En tránsito");

        $('#kpiEntregado').textContent = contar("Entregado");

        $('#kpiIncidencia').textContent = contar("Incidencia");

    }


    // =====================================================
    // FILTRADO
    // =====================================================

    function enviosFiltrados() {

        const texto = filtroBusqueda.value.trim().toLowerCase();

        const estado = filtroEstado.value;

        const agencia = filtroAgencia.value;

        return envios.filter(envio => {

            const coincideTexto = !texto
                || envio.guia.toLowerCase().includes(texto)
                || envio.destinatario.toLowerCase().includes(texto);

            const coincideEstado = !estado || envio.estado === estado;

            const coincideAgencia = !agencia || envio.origen === agencia;

            return coincideTexto && coincideEstado && coincideAgencia;

        });

    }


    // =====================================================
    // TABLA DE ENVÍOS
    // =====================================================

    function renderTabla() {

        const resultados = enviosFiltrados();

        const totalPaginas = Math.max(1, Math.ceil(resultados.length / POR_PAGINA));

        if (paginaActual > totalPaginas) {

            paginaActual = totalPaginas;

        }

        const inicio = (paginaActual - 1) * POR_PAGINA;

        const pagina = resultados.slice(inicio, inicio + POR_PAGINA);


        if (pagina.length === 0) {

            cuerpoTabla.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center tabla-vacia">
                        <i class="bi bi-inbox fs-3 d-block mb-2"></i>
                        No se encontraron envíos con esos criterios.
                    </td>
                </tr>`;

        } else {

            cuerpoTabla.innerHTML = pagina.map(envio => `
                <tr>
                    <td class="guia-codigo">${escapar(envio.guia)}</td>
                    <td>
                        <span class="fw-semibold">${escapar(envio.destinatario)}</span>
                        <br>
                        <span class="small text-muted">DNI ${escapar(envio.documento)}</span>
                    </td>
                    <td class="small">
                        ${escapar(envio.origen)}
                        <i class="bi bi-arrow-right mx-1 text-muted"></i>
                        ${escapar(envio.destino)}
                    </td>
                    <td>${envio.peso} kg</td>
                    <td class="small">${formatearFecha(envio.fecha)}</td>
                    <td>
                        <span class="badge badge-estado ${claseEstado(envio.estado)}">
                            ${escapar(envio.estado)}
                        </span>
                    </td>
                    <td class="text-end text-nowrap">
                        <button class="btn btn-sm btn-outline-secondary"
                            data-accion="detalle" data-id="${envio.id}"
                            title="Ver detalle">
                            <i class="bi bi-eye"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-paway"
                            data-accion="editar" data-id="${envio.id}"
                            title="Editar">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger"
                            data-accion="anular" data-id="${envio.id}"
                            title="Anular">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>`).join('');

        }


        const desde = resultados.length === 0 ? 0 : inicio + 1;

        const hasta = inicio + pagina.length;

        resumenTabla.textContent =
            `Mostrando ${desde}-${hasta} de ${resultados.length} envíos`;

        renderPaginacion(totalPaginas);

    }


    function renderPaginacion(totalPaginas) {

        let html = `
            <li class="page-item ${paginaActual === 1 ? 'disabled' : ''}">
                <button class="page-link" data-pagina="${paginaActual - 1}">
                    Anterior
                </button>
            </li>`;

        for (let i = 1; i <= totalPaginas; i++) {

            html += `
                <li class="page-item ${i === paginaActual ? 'active' : ''}">
                    <button class="page-link" data-pagina="${i}">${i}</button>
                </li>`;

        }

        html += `
            <li class="page-item ${paginaActual === totalPaginas ? 'disabled' : ''}">
                <button class="page-link" data-pagina="${paginaActual + 1}">
                    Siguiente
                </button>
            </li>`;

        paginacion.innerHTML = html;

    }


    // =====================================================
    // AGENCIAS
    // =====================================================

    function renderAgencias() {

        listaAgencias.innerHTML = agencias.map(agencia => {

            const enviosAgencia = envios.filter(e => e.origen === agencia.nombre).length;

            const ocupacion = Math.min(
                100,
                Math.round((enviosAgencia * 100) / agencia.capacidad * 10)
            );

            return `
                <div class="col-12 col-md-6 col-xl-4">
                    <div class="card-paway p-3 h-100">

                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <div>
                                <h3 class="h6 fw-bold mb-1">
                                    <i class="bi bi-geo-alt text-paway-red me-1"></i>
                                    ${escapar(agencia.nombre)}
                                </h3>
                                <p class="small text-muted mb-0">
                                    ${escapar(agencia.ciudad)}
                                </p>
                            </div>
                            <span class="badge badge-estado badge-registrado">
                                ${enviosAgencia} envíos
                            </span>
                        </div>

                        <p class="small text-muted mb-1">
                            Capacidad utilizada
                        </p>

                        <div class="progress" style="height: 8px;"
                            role="progressbar"
                            aria-label="Capacidad de ${escapar(agencia.nombre)}"
                            aria-valuenow="${ocupacion}"
                            aria-valuemin="0" aria-valuemax="100">
                            <div class="progress-bar bg-danger"
                                style="width: ${ocupacion}%"></div>
                        </div>

                        <p class="small text-muted mt-2 mb-0">
                            ${ocupacion}% de ${agencia.capacidad} paquetes
                        </p>

                    </div>
                </div>`;

        }).join('');

    }


    // =====================================================
    // REPORTES
    // =====================================================

    function renderReportes() {

        const estados = ["Registrado", "En tránsito", "Entregado", "Incidencia"];

        $('#reporteEstados').innerHTML = estados.map(estado => {

            const cantidad = envios.filter(e => e.estado === estado).length;

            const porcentaje = envios.length === 0
                ? 0
                : Math.round((cantidad * 100) / envios.length);

            return `
                <div class="mb-3">
                    <div class="d-flex justify-content-between small mb-1">
                        <span class="fw-semibold">${escapar(estado)}</span>
                        <span class="text-muted">${cantidad} (${porcentaje}%)</span>
                    </div>
                    <div class="progress" style="height: 8px;"
                        role="progressbar"
                        aria-label="Envíos en estado ${escapar(estado)}"
                        aria-valuenow="${porcentaje}"
                        aria-valuemin="0" aria-valuemax="100">
                        <div class="progress-bar bg-danger"
                            style="width: ${porcentaje}%"></div>
                    </div>
                </div>`;

        }).join('');


        $('#reporteAgencias').innerHTML = `
            <ul class="list-group list-group-flush">
                ${agencias.map(agencia => {

                    const cantidad = envios.filter(e => e.origen === agencia.nombre).length;

                    return `
                        <li class="list-group-item d-flex justify-content-between align-items-center px-0">
                            <span>
                                <i class="bi bi-shop text-paway-red me-2"></i>
                                ${escapar(agencia.nombre)}
                            </span>
                            <span class="badge bg-danger rounded-pill">${cantidad}</span>
                        </li>`;

                }).join('')}
            </ul>`;

    }


    // =====================================================
    // SELECTORES DE AGENCIA
    // =====================================================

    function cargarSelectoresAgencia() {

        const opciones = agencias
            .map(a => `<option value="${escapar(a.nombre)}">${escapar(a.nombre)}</option>`)
            .join('');

        filtroAgencia.innerHTML = `<option value="">Todas</option>${opciones}`;

        $('#campoOrigen').innerHTML =
            `<option value="">Seleccione...</option>${opciones}`;

        $('#campoDestino').innerHTML =
            `<option value="">Seleccione...</option>${opciones}`;

    }


    // =====================================================
    // ALTA Y EDICIÓN
    // =====================================================

    function abrirFormulario(envio = null) {

        formEnvio.reset();

        formEnvio.classList.remove('was-validated');

        if (envio) {

            $('#tituloModalEnvio').textContent = `Editar envío ${envio.guia}`;

            $('#envioId').value = envio.id;

            $('#campoDestinatario').value = envio.destinatario;

            $('#campoDocumento').value = envio.documento;

            $('#campoOrigen').value = envio.origen;

            $('#campoDestino').value = envio.destino;

            $('#campoPeso').value = envio.peso;

            $('#campoFecha').value = envio.fecha;

            $('#campoEstado').value = envio.estado;

            $('#campoObservacion').value = envio.observacion;

        } else {

            $('#tituloModalEnvio').textContent = 'Registrar envío';

            $('#envioId').value = '';

            $('#campoFecha').value = new Date().toISOString().slice(0, 10);

        }

        modalEnvio.show();

    }


    formEnvio.addEventListener('submit', (evento) => {

        evento.preventDefault();

        const origen = $('#campoOrigen').value;

        const destino = $('#campoDestino').value;


        // Regla de negocio: origen y destino no pueden coincidir

        $('#campoDestino').setCustomValidity(
            origen && origen === destino ? 'invalido' : ''
        );

        if (!formEnvio.checkValidity()) {

            formEnvio.classList.add('was-validated');

            if (origen === destino && origen) {

                mostrarAviso('El destino debe ser distinto del origen.', 'danger');

            }

            return;

        }


        const datos = {
            destinatario: $('#campoDestinatario').value.trim(),
            documento: $('#campoDocumento').value.trim(),
            origen: origen,
            destino: destino,
            peso: parseFloat($('#campoPeso').value),
            fecha: $('#campoFecha').value,
            estado: $('#campoEstado').value,
            observacion: $('#campoObservacion').value.trim()
        };

        const id = $('#envioId').value;


        if (id) {

            const envio = envios.find(e => e.id === Number(id));

            Object.assign(envio, datos);

            mostrarAviso(`Envío ${envio.guia} actualizado.`);

        } else {

            const nuevoId = envios.reduce((mayor, e) => Math.max(mayor, e.id), 0) + 1;

            envios.push({ id: nuevoId, guia: siguienteGuia(), ...datos });

            mostrarAviso('Envío registrado correctamente.');

        }

        modalEnvio.hide();

        refrescarPanel();

    });


    // Limpia la validación personalizada al cambiar el destino

    $('#campoDestino').addEventListener('change', (evento) => {

        evento.target.setCustomValidity('');

    });


    // =====================================================
    // DETALLE Y SEGUIMIENTO
    // =====================================================

    function mostrarDetalle(envio) {

        const indiceActual = envio.estado === "Incidencia"
            ? FLUJO_ESTADOS.indexOf("En tránsito")
            : FLUJO_ESTADOS.indexOf(envio.estado);

        const pasos = FLUJO_ESTADOS.map((paso, indice) => {

            let clase = '';

            if (indice <= indiceActual) {

                clase = 'completado';

            }

            if (envio.estado === "Incidencia" && indice === indiceActual) {

                clase = 'alerta';

            }

            return `
                <li class="${clase}">
                    <span class="fw-semibold">${paso}</span>
                    ${indice <= indiceActual
                        ? '<br><span class="small text-muted">Completado</span>'
                        : '<br><span class="small text-muted">Pendiente</span>'}
                </li>`;

        }).join('');


        $('#tituloModalDetalle').textContent = `Envío ${envio.guia}`;

        $('#cuerpoDetalle').innerHTML = `
            <div class="mb-3">
                <span class="badge badge-estado ${claseEstado(envio.estado)}">
                    ${escapar(envio.estado)}
                </span>
            </div>

            <ul class="list-group list-group-flush mb-3">
                <li class="list-group-item d-flex justify-content-between px-0">
                    <span class="text-muted">Destinatario</span>
                    <span class="fw-semibold">${escapar(envio.destinatario)}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between px-0">
                    <span class="text-muted">Documento</span>
                    <span>${escapar(envio.documento)}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between px-0">
                    <span class="text-muted">Ruta</span>
                    <span>${escapar(envio.origen)} &rarr; ${escapar(envio.destino)}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between px-0">
                    <span class="text-muted">Peso</span>
                    <span>${envio.peso} kg</span>
                </li>
                <li class="list-group-item d-flex justify-content-between px-0">
                    <span class="text-muted">Fecha</span>
                    <span>${formatearFecha(envio.fecha)}</span>
                </li>
            </ul>

            ${envio.observacion
                ? `<div class="alert alert-warning small">
                        <i class="bi bi-info-circle me-1"></i>
                        ${escapar(envio.observacion)}
                   </div>`
                : ''}

            <p class="fw-bold small text-uppercase text-muted mb-3">
                Seguimiento
            </p>

            <ul class="timeline">${pasos}</ul>`;

        modalDetalle.show();

    }


    // =====================================================
    // ANULACIÓN
    // =====================================================

    $('#btnConfirmarEliminar').addEventListener('click', () => {

        const indice = envios.findIndex(e => e.id === idParaAnular);

        if (indice !== -1) {

            const [eliminado] = envios.splice(indice, 1);

            mostrarAviso(`Envío ${eliminado.guia} anulado.`, 'danger');

        }

        idParaAnular = null;

        modalConfirmar.hide();

        refrescarPanel();

    });


    // =====================================================
    // EVENTOS DE LA TABLA
    // =====================================================

    cuerpoTabla.addEventListener('click', (evento) => {

        const boton = evento.target.closest('button[data-accion]');

        if (!boton) {

            return;

        }

        const envio = envios.find(e => e.id === Number(boton.dataset.id));

        if (!envio) {

            return;

        }

        if (boton.dataset.accion === 'detalle') {

            mostrarDetalle(envio);

        }

        if (boton.dataset.accion === 'editar') {

            abrirFormulario(envio);

        }

        if (boton.dataset.accion === 'anular') {

            idParaAnular = envio.id;

            $('#textoConfirmar').textContent =
                `¿Desea anular el envío ${envio.guia}?`;

            modalConfirmar.show();

        }

    });


    paginacion.addEventListener('click', (evento) => {

        const boton = evento.target.closest('button[data-pagina]');

        if (!boton || boton.parentElement.classList.contains('disabled')) {

            return;

        }

        paginaActual = Number(boton.dataset.pagina);

        renderTabla();

    });


    // =====================================================
    // EVENTOS DE FILTROS Y NAVEGACIÓN
    // =====================================================

    [filtroBusqueda, filtroEstado, filtroAgencia].forEach(control => {

        control.addEventListener('input', () => {

            paginaActual = 1;

            renderTabla();

        });

    });


    $('#btnLimpiarFiltros').addEventListener('click', () => {

        filtroBusqueda.value = '';

        filtroEstado.value = '';

        filtroAgencia.value = '';

        paginaActual = 1;

        renderTabla();

    });


    $('#btnNuevoEnvio').addEventListener('click', () => abrirFormulario());


    // Marca la opción activa de la barra lateral

    document.querySelectorAll('#menuLateral .nav-link').forEach(enlace => {

        enlace.addEventListener('click', () => {

            document.querySelectorAll('#menuLateral .nav-link')
                .forEach(item => item.classList.remove('active'));

            enlace.classList.add('active');

        });

    });


    // =====================================================
    // INICIO
    // =====================================================

    function refrescarPanel() {

        actualizarIndicadores();

        renderTabla();

        renderAgencias();

        renderReportes();

    }


    cargarSelectoresAgencia();

    refrescarPanel();


});
