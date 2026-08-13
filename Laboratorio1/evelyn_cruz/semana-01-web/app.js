"use strict";
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