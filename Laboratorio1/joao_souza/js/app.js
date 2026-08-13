"use strict";

/* Consejo: el botón muestra u oculta un párrafo.
   aria-expanded comunica a un lector de pantalla si el contenido está visible. */
const botonConsejo = document.querySelector("#boton-consejo");
const consejo = document.querySelector("#consejo");

if (botonConsejo && consejo) {
  botonConsejo.addEventListener("click", () => {
    const seMostrara = consejo.hidden;

    consejo.hidden = !seMostrara;
    botonConsejo.setAttribute("aria-expanded", String(seMostrara));
    botonConsejo.textContent = seMostrara ? "Ocultar consejo" : "Mostrar consejo";
  });
}

/* Tema: el botón activa o desactiva una clase en body.
   El CSS reasigna los roles de color; el JavaScript no toca estilos directamente.
   aria-pressed comunica si el modo oscuro está activado. */
const botonTema = document.querySelector("#boton-tema");

if (botonTema) {
  botonTema.addEventListener("click", () => {
    const temaOscuroActivo = document.body.classList.toggle("tema-oscuro");

    botonTema.setAttribute("aria-pressed", String(temaOscuroActivo));
  });
}
