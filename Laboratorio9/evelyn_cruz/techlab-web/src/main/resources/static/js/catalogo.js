"use strict";
const formulario = document.querySelector("#busqueda");
const consulta = document.querySelector("#consulta");
const boton = document.querySelector("#buscar");
const lista = document.querySelector("#cursos");
const estado = document.querySelector("#estado");

function mostrarCursos(cursos) {
    lista.replaceChildren();
    for (const curso of cursos) {
        const item = document.createElement("li");
        item.className = "list-group-item";

        const titulo = document.createElement("h2");
        titulo.className = "h5";
        titulo.textContent = curso.titulo;

        const detalle = document.createElement("p");
        detalle.className = "mb-1";
        detalle.textContent = `${curso.horas} horas`;

        const enlace = document.createElement("a");
        enlace.href = `/api/v1/cursos/${encodeURIComponent(curso.id)}`;
        enlace.textContent = `Ver datos de ${curso.titulo}`;

        item.append(titulo, detalle, enlace);
        lista.append(item);
    }
}

async function cargarCursos() {
    boton.disabled = true;
    formulario.setAttribute("aria-busy", "true");
    estado.textContent = "Consultando cursos...";
    lista.replaceChildren();

    try {
        const parametros = new URLSearchParams({q: consulta.value});
        const respuesta = await fetch(`/api/v1/cursos?${parametros}`, {
            headers: {Accept: "application/json"}
        });

        if (!respuesta.ok) {
            throw new Error(`HTTP ${respuesta.status}`);
        }

        const cursos = await respuesta.json();
        if (!Array.isArray(cursos)) {
            throw new Error("Formato de respuesta inesperado");
        }

        mostrarCursos(cursos);
        estado.textContent = cursos.length
            ? `${cursos.length} cursos encontrados.`
            : "No hay cursos que coincidan.";

    } catch (error) {
        estado.textContent = "No se pudo cargar el catálogo. Reintente.";
        console.error("Fallo al consultar el catálogo", error);
    } finally {
        boton.disabled = false;
        formulario.removeAttribute("aria-busy");
    }
}

formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    if (!boton.disabled) cargarCursos();
});

cargarCursos();