"use strict";
document.addEventListener("DOMContentLoaded", () => {
    const yearElement = document.querySelector("[data-current-year]");
    const statusElement = document.querySelector("#estado-registro");
    const workshopButtons = document.querySelectorAll("[data-taller]");
    if (yearElement) {
        yearElement.textContent = String(new Date().getFullYear());
    }
    if (!statusElement) {
        return;
    }
    workshopButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const workshopName = button.dataset.taller ?? "el taller seleccionado";
            statusElement.textContent =
                `Has elegido ${workshopName}. El registro de esta práctica es simulado.`;
            statusElement.classList.remove("d-none");
        });
    });
});