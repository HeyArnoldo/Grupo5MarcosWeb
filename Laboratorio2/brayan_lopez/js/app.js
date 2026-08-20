"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const anioElemento = document.querySelector("[data-anio-actual]");
    const estadoElemento = document.querySelector("#estado-reserva");
    const botonesReserva = document.querySelectorAll("[data-servicio]");

    if (anioElemento) {
        anioElemento.textContent = String(new Date().getFullYear());
    }

    if (!estadoElemento) {
        return;
    }

    botonesReserva.forEach((boton) => {
        boton.addEventListener("click", () => {
            const servicio = boton.dataset.servicio ?? "el servicio seleccionado";
            const horario = boton.dataset.horario ?? "el horario publicado";

            estadoElemento.textContent =
                `Reservaste ${servicio} para ${horario}. Esta confirmación es simulada.`;
            estadoElemento.classList.remove("d-none");
        });
    });
});