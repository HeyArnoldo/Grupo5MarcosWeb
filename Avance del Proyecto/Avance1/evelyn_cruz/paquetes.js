document.addEventListener('DOMContentLoaded', () => {
    const trackingForm = document.getElementById('trackingForm');
    const trackingInput = document.getElementById('trackingInput');
    const displayGuia = document.getElementById('displayGuia');
    const trackingErrorAlert = document.getElementById('trackingErrorAlert');
    const trackingResult = document.getElementById('trackingResult');

    if (trackingForm) {
        trackingForm.addEventListener('submit', (e) => {
            e.preventDefault();

            if (!trackingForm.checkValidity()) {
                e.stopPropagation();
                trackingForm.classList.add('was-validated');
                return;
            }

            const codigoIngresado = trackingInput.value.trim().toUpperCase();

            // Ejemplo de simulación de respuesta
            if (codigoIngresado.length < 5) {
                trackingErrorAlert.classList.remove('d-none');
                trackingResult.classList.add('d-none');
            } else {
                trackingErrorAlert.classList.add('d-none');
                trackingResult.classList.remove('d-none');
                if (displayGuia) {
                    displayGuia.textContent = codigoIngresado;
                }
            }

            trackingForm.classList.add('was-validated');
        });
    }
});