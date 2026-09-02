
/**
 * ============================================================
 * INICIALIZACIÓN DEL MÓDULO DE COTIZACIÓN
 * ============================================================
 */
document.addEventListener('DOMContentLoaded', () => {
    
    const calcProvincia = document.getElementById('calcProvincia');
    const calcDistrito = document.getElementById('calcDistrito');
    const calcCategoria = document.getElementById('calcCategoria');
    const calcPeso = document.getElementById('calcPeso');
    const calcCantidad = document.getElementById('calcCantidad');
    const calcTotalOutput = document.getElementById('calcTotalOutput');
    const panelResultado = document.querySelector('.calc-result-panel');
    const btnSolicitarEnvio = document.getElementById('btnSolicitarEnvio');

    if (!calcProvincia || !calcDistrito || !calcTotalOutput) return;

    // Diccionario Geográfico de Tarifas Base
    const dbTarifas = {
        lima: {
            tarifaBase: 10.00,
            distritos: ['Ate', 'San Borja', 'Miraflores', 'Lima Centro', 'San Isidro', 'Lince', 'Surco']
        },
        huarochiri: {
            tarifaBase: 18.00,
            distritos: ['Matucana', 'Antioquía', 'San Bartolomé', 'San Mateo']
        },
        huaral: {
            tarifaBase: 20.00,
            distritos: ['Huaral', 'Chancay', 'Aucallama']
        },
        canete: {
            tarifaBase: 22.00,
            distritos: ['San Vicente', 'Asia', 'Lunahuaná', 'Mala']
        }
    };

    // Constantes Operativas
    const PESO_BASE_KG = 2.0;       
    const COSTO_KG_EXTRA = 3.00;

    // Matriz de Recargos Operativos por Categoría
    const recargosCategorias = {
        textil: 0.00,
        otros: 0.00,
        hogar_higiene: 2.00,
        tecnologia: 5.00,
        joyeria: 13.00 // S/ 8.00 custodia especial + S/ 5.00 base de seguro obligatorio
    };

    /**
     * ============================================================
     * GESTIÓN DINÁMICA DE DESTINOS
     * ============================================================
     */
    const actualizarDistritos = () => {
        const provinciaSeleccionada = calcProvincia.value;
        
        // 1. Limpiamos las opciones previas y reseteamos el valor
        calcDistrito.innerHTML = '<option value="" disabled selected>Selecciona el distrito...</option>';
        calcDistrito.value = "";
        
        if (provinciaSeleccionada && dbTarifas[provinciaSeleccionada]) {
            // 2. Inyectamos los nuevos nodos option
            const distritos = dbTarifas[provinciaSeleccionada].distritos;
            distritos.forEach(distrito => {
                const opcion = document.createElement('option');
                opcion.value = distrito.toLowerCase().replace(/\s+/g, '-'); 
                opcion.textContent = distrito;
                calcDistrito.appendChild(opcion);
            });

            // 3. Habilitamos el componente al final para refrescar el renderizado
            calcDistrito.disabled = false;
        } else {
            calcDistrito.disabled = true;
        }

        // Ejecutamos cálculo seguro
        calcularTarifaFinal();
    };

    /**
     * ============================================================
     * MOTOR DE CÁLCULO DE COTIZACIONES
     * ============================================================
     */
    const calcularTarifaFinal = () => {
        const provincia = calcProvincia.value;
        const distrito = calcDistrito.value;
        const categoria = calcCategoria.value;
        const peso = parseFloat(calcPeso.value);
        const cantidad = parseInt(calcCantidad.value);

        if (!provincia || !distrito || !categoria || isNaN(peso) || isNaN(cantidad) || peso <= 0 || cantidad <= 0) {
            calcTotalOutput.textContent = 'S/ 0.00';
            return;
        }

        const tarifaBase = dbTarifas[provincia].tarifaBase;
        
        // Algoritmo de penalización por sobrepeso
        let costoSobrepeso = 0;
        if (peso > PESO_BASE_KG) {
            const kilosExtra = peso - PESO_BASE_KG;
            costoSobrepeso = kilosExtra * COSTO_KG_EXTRA;
        }

        // Obtención del recargo comercial indexado
        const recargoComercial = recargosCategorias[categoria] || 0.00;

        // Ecuación final integrada
        const costoPorPaquete = tarifaBase + costoSobrepeso + recargoComercial;
        const totalFinal = costoPorPaquete * cantidad;

        calcTotalOutput.textContent = `S/ ${totalFinal.toFixed(2)}`;
        animarResultado();
    };

    const animarResultado = () => {
        panelResultado.style.transform = 'scale(1.02)';
        panelResultado.style.borderColor = 'var(--secundario)';
        
        setTimeout(() => {
            panelResultado.style.transform = 'scale(1)';
            panelResultado.style.borderColor = 'var(--primario)';
        }, 250);
    };

    // Actualización en vivo de monto
    calcProvincia.addEventListener('change', actualizarDistritos);
    calcDistrito.addEventListener('change', calcularTarifaFinal);
    calcCategoria.addEventListener('change', calcularTarifaFinal);
    calcPeso.addEventListener('input', calcularTarifaFinal);
    calcCantidad.addEventListener('input', calcularTarifaFinal);

    /**
     * ============================================================
     * SOLICITUD DE ENVÍO
     * ============================================================
     */
    if (btnSolicitarEnvio) {
        btnSolicitarEnvio.addEventListener('click', () => {
            const provincia = calcProvincia.value;
            const distrito = calcDistrito.value;
            const categoria = calcCategoria.value;
            const peso = calcPeso.value;
            const cantidad = calcCantidad.value;

            if (provincia && distrito && categoria && peso && cantidad) {
                window.location.href = '../auth/login-client.html'; 
            } else {
                alert('Por favor, cotice su envío completando todos los campos requeridos antes de solicitar el recojo.');
            }
        });
    }
});