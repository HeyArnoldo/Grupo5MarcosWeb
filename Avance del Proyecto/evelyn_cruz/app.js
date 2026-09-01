document.addEventListener('DOMContentLoaded', () => {

    // 1. Inicializar Popovers de Bootstrap
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    popoverTriggerList.forEach(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));

    // 2. Validación de Formularios Nativos de Bootstrap y Disparo de Modal
    const contactForm = document.getElementById('contactForm');
    const confirmModal = new bootstrap.Modal(document.getElementById('confirmModal'));

    if (contactForm) {
        contactForm.addEventListener('submit', (event) => {
            event.preventDefault();

            if (!contactForm.checkValidity()) {
                event.stopPropagation();
            } else {
                // Mostrar Modal si la validación es correcta
                confirmModal.show();
                contactForm.reset();
                contactForm.classList.remove('was-validated');
                return;
            }

            contactForm.classList.add('was-validated');
        });
    }

    // 3. Validación rápida del formulario de seguimiento del Hero
    const fastForms = document.querySelectorAll('.needs-validation-fast');
    fastForms.forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

});