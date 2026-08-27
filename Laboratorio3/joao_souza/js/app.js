"use strict";

/* =========================================================
   Campus Activo — comportamiento
   Responsabilidades de este archivo:
   1. Poblar el modal con la actividad del botón que lo abrió.
   2. Gestionar el foco al abrir y al cerrar.
   3. Validar el formulario con la Constraint Validation API.
   4. Anunciar el resultado sin interrumpir al usuario.
   ========================================================= */

const modalInscripcion = document.querySelector("#modalInscripcion");
const formInscripcion = document.querySelector("#formInscripcion");
const actividadInput = document.querySelector("#actividad");
const nombreInput = document.querySelector("#nombre");
const estadoFormulario = document.querySelector("#estadoFormulario");
const estadoGlobal = document.querySelector("#estadoGlobal");
const anioActual = document.querySelector("#anioActual");

const ACTIVIDAD_POR_DEFECTO = "Orientación general";

/* Contador de la sesión: alimenta el estado dinámico no urgente. */
let registrosDemo = 0;

if (anioActual) {
  anioActual.textContent = new Date().getFullYear();
}

/** Deja el formulario en su estado inicial, sin marcas de validación. */
function limpiarEstadoDelFormulario() {
  formInscripcion.classList.remove("was-validated");
  estadoFormulario.classList.add("d-none");
  estadoFormulario.textContent = "";
}

/** Actualiza el aviso general de la página (role="status", cortés). */
function anunciarEnLaPagina(mensaje) {
  if (!estadoGlobal) {
    return;
  }
  estadoGlobal.textContent = mensaje;
  estadoGlobal.classList.remove("d-none");
}

if (modalInscripcion && formInscripcion && actividadInput && nombreInput && estadoFormulario) {
  /* show.bs.modal ocurre ANTES de mostrarse: momento correcto
     para preparar el contenido que el usuario verá. */
  modalInscripcion.addEventListener("show.bs.modal", (event) => {
    const botonOrigen = event.relatedTarget;
    actividadInput.value = botonOrigen?.dataset.actividad ?? ACTIVIDAD_POR_DEFECTO;
    limpiarEstadoDelFormulario();
  });

  /* shown.bs.modal ocurre DESPUÉS de la transición: recién aquí
     el campo es visible y enfocable (autofocus no sirve antes). */
  modalInscripcion.addEventListener("shown.bs.modal", () => {
    nombreInput.focus();
  });

  formInscripcion.addEventListener("submit", (event) => {
    event.preventDefault();
    event.stopPropagation();

    formInscripcion.classList.add("was-validated");

    if (!formInscripcion.checkValidity()) {
      const primerCampoInvalido = formInscripcion.querySelector(":invalid");
      primerCampoInvalido?.focus();
      return;
    }

    registrosDemo += 1;

    estadoFormulario.textContent =
      `Inscripción de demostración registrada para ${actividadInput.value}. Puedes cerrar esta ventana.`;
    estadoFormulario.classList.remove("d-none");
    estadoFormulario.focus();

    anunciarEnLaPagina(
      registrosDemo === 1
        ? `Registraste 1 inscripción de demostración en esta sesión. La última fue ${actividadInput.value}.`
        : `Registraste ${registrosDemo} inscripciones de demostración en esta sesión. La última fue ${actividadInput.value}.`
    );
  });

  /* Al cerrar, Bootstrap devuelve el foco al botón activador.
     Aquí solo limpiamos datos para que el siguiente uso empiece limpio. */
  modalInscripcion.addEventListener("hidden.bs.modal", () => {
    formInscripcion.reset();
    limpiarEstadoDelFormulario();
  });
}
