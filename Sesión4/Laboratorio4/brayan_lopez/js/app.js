"use strict";
const form = document.querySelector("#formRegistro");
const estado = document.querySelector("#estadoFormulario");
if (form && estado) {
    form.addEventListener("submit", (event) => {
        event.preventDefault();
        form.classList.add("was-validated");
        if (!form.checkValidity()) {
            form.querySelector(":invalid")?.focus();
            return;
        }
        estado.textContent = "Registro de demostración completado.";
        estado.classList.remove("d-none");
    });
}