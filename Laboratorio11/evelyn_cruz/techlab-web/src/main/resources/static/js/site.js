document.querySelectorAll("[data-confirm-delete]").forEach((form) => {
 form.addEventListener("submit", (event) => {
 const title = form.dataset.courseTitle ?? "este curso";
 const confirmed = window.confirm(
 "¿Desea eliminar el curso \"" + title + "\"?"
 );
 if (!confirmed) {
 event.preventDefault();
 }
 });
 });
