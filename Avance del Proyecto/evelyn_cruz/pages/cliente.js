/* =====================================================
   PERSONA 4 - BRAYAN LÓPEZ
   MÓDULO: HISTORIAL Y PANEL DEL CLIENTE
   ===================================================== */


/*
    Los datos son internos y simulados.
    Actualmente el proyecto no utiliza
    una base de datos.
*/


document.addEventListener('DOMContentLoaded', () => {


    // =====================================================
    // DATOS DEL CLIENTE
    // =====================================================

    const cliente = {

        nombre: "Brayan López",

        dni: "12345678",

        correo: "brayanlopezjs29@gmail.com",

        telefono: "999 999 999"

    };



    // =====================================================
    // HISTORIAL INTERNO DE ENVÍOS
    // =====================================================

    const envios = [

        {
            codigo: "PW-98765432",
            fecha: "28/08/2026",
            origen: "Lima",
            destino: "Huancayo",
            servicio: "Paway Express",
            estado: "Entregado"
        },


        {
            codigo: "PW-45671238",
            fecha: "29/08/2026",
            origen: "Lima",
            destino: "Arequipa",
            servicio: "Carga Consolidada",
            estado: "En tránsito"
        },


        {
            codigo: "PW-11223344",
            fecha: "30/08/2026",
            origen: "Lima",
            destino: "Cusco",
            servicio: "Paway Express",
            estado: "Pendiente"
        },


        {
            codigo: "PW-55667788",
            fecha: "25/08/2026",
            origen: "Lima",
            destino: "Trujillo",
            servicio: "Paway Express",
            estado: "Entregado"
        },


        {
            codigo: "PW-99887766",
            fecha: "20/08/2026",
            origen: "Huancayo",
            destino: "Lima",
            servicio: "Carga Consolidada",
            estado: "Entregado"
        }

    ];



    // =====================================================
    // MOSTRAR INFORMACIÓN DEL CLIENTE
    // =====================================================

    document.getElementById("clienteNombre").textContent =
        cliente.nombre;


    document.getElementById("clienteCorreo").textContent =
        cliente.correo;



    // =====================================================
    // DATOS DEL MODAL
    // =====================================================

    document.getElementById("modalNombre").textContent =
        cliente.nombre;


    document.getElementById("modalDni").textContent =
        cliente.dni;


    document.getElementById("modalCorreo").textContent =
        cliente.correo;


    document.getElementById("modalTelefono").textContent =
        cliente.telefono;



    // =====================================================
    // ESTADÍSTICAS
    // =====================================================

    const total =
        envios.length;


    const entregados =
        envios.filter(
            envio => envio.estado === "Entregado"
        ).length;


    const transito =
        envios.filter(
            envio => envio.estado === "En tránsito"
        ).length;


    const pendientes =
        envios.filter(
            envio => envio.estado === "Pendiente"
        ).length;



    // =====================================================
    // MOSTRAR ESTADÍSTICAS
    // =====================================================

    document.getElementById("totalEnvios").textContent =
        total;


    document.getElementById("enviosEntregados").textContent =
        entregados;


    document.getElementById("enviosTransito").textContent =
        transito;


    document.getElementById("enviosPendientes").textContent =
        pendientes;



    // =====================================================
    // GENERAR TABLA DE HISTORIAL
    // =====================================================

    const historial =
        document.getElementById("historialEnvios");


    historial.innerHTML = "";



    envios.forEach((envio, index) => {


        // =================================================
        // COLOR DEL ESTADO
        // =================================================

        let claseEstado;


        if (envio.estado === "Entregado") {

            claseEstado = "bg-success";

        }

        else if (envio.estado === "En tránsito") {

            claseEstado = "bg-warning text-dark";

        }

        else {

            claseEstado = "bg-secondary";

        }



        // =================================================
        // CREAR FILA
        // =================================================

        const fila =
            document.createElement("tr");



        fila.innerHTML = `

            <td class="px-4 fw-bold text-paway-red">

                ${envio.codigo}

            </td>


            <td>

                ${envio.fecha}

            </td>


            <td>

                ${envio.origen}

            </td>


            <td>

                ${envio.destino}

            </td>


            <td>

                ${envio.servicio}

            </td>


            <td>

                <span class="badge ${claseEstado} rounded-pill">

                    ${envio.estado}

                </span>

            </td>


            <td class="text-center">

                <button
                    type="button"
                    class="btn btn-sm btn-outline-danger rounded-pill"
                    onclick="verDetalle(${index})">

                    <i class="bi bi-eye me-1"></i>

                    Ver

                </button>

            </td>

        `;



        historial.appendChild(fila);

    });



    // =====================================================
    // VER DETALLE DE ENVÍO
    // =====================================================

    window.verDetalle = function(index) {


        const envio =
            envios[index];


        document.getElementById("detalleCodigo").textContent =
            envio.codigo;


        document.getElementById("detalleFecha").textContent =
            envio.fecha;


        document.getElementById("detalleOrigen").textContent =
            envio.origen;


        document.getElementById("detalleDestino").textContent =
            envio.destino;


        document.getElementById("detalleServicio").textContent =
            envio.servicio;


        document.getElementById("detalleEstado").textContent =
            envio.estado;



        // =================================================
        // ABRIR MODAL
        // =================================================

        const modalElemento =
            document.getElementById("detalleModal");


        const modal =
            bootstrap.Modal.getOrCreateInstance(
                modalElemento
            );


        modal.show();

    };


});