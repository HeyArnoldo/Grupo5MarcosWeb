"use strict";
const modalRegistro = document.querySelector("#modalRegistro");
const formRegistro = document.querySelector("#formRegistro");
const cursoInput = document.querySelector("#curso");
const nombreInput = document.querySelector("#nombre");
const estadoFormulario = document.querySelector("#estadoFormulario");
const anioActual = document.querySelector("#anioActual");
if (anioActual) {
    anioActual.textContent = new Date().getFullYear();
}
if (modalRegistro && formRegistro && cursoInput && nombreInput && estadoFormulario) {
    modalRegistro.addEventListener("show.bs.modal", (event) => {
        const botonOrigen = event.relatedTarget;
        const curso = botonOrigen?.dataset.curso ?? "Orientación general";
        cursoInput.value = curso;
        formRegistro.classList.remove("was-validated");
        estadoFormulario.classList.add("d-none");
        estadoFormulario.textContent = "";
    });
    modalRegistro.addEventListener("shown.bs.modal", () => {
        nombreInput.focus();
    });
    formRegistro.addEventListener("submit", (event) => {
        event.preventDefault();
        event.stopPropagation();
        formRegistro.classList.add("was-validated");
        if (!formRegistro.checkValidity()) {
            const primerCampoInvalido = formRegistro.querySelector(":invalid");
            primerCampoInvalido?.focus();
            return;
        }
        estadoFormulario.textContent =
            `Registro de demostración completado para ${cursoInput.value}.`;
        estadoFormulario.classList.remove("d-none");
        estadoFormulario.focus();
    });
    modalRegistro.addEventListener("hidden.bs.modal", () => {
        formRegistro.reset();
        formRegistro.classList.remove("was-validated");
        estadoFormulario.classList.add("d-none");
        estadoFormulario.textContent = "";
    });
}