document.querySelectorAll('[data-destination-fields]').forEach((fields) => {
    const province = fields.querySelector('[data-province]');
    const district = fields.querySelector('[data-district]');
    const error = fields.querySelector('[data-destination-error]');
    const groups = Array.from(district.querySelectorAll('optgroup'));
    let controller;

    function setOptions(values, selected = '') {
        district.replaceChildren(new Option('Selecciona un distrito', ''));
        values.forEach((value) => district.add(new Option(value, value, false, value === selected)));
        district.disabled = values.length === 0;
    }

    const initial = groups.find((group) => group.dataset.province === province.value);
    setOptions(initial ? Array.from(initial.children).map((option) => option.value) : [], district.value);

    province.addEventListener('change', async () => {
        controller?.abort();
        controller = new AbortController();
        error.textContent = '';
        setOptions([]);
        if (!province.value) return;
        try {
            const response = await fetch(`${province.dataset.apiBase}${encodeURIComponent(province.value)}/distritos`, {
                signal: controller.signal,
                headers: { Accept: 'application/json' }
            });
            if (!response.ok) throw new Error('Destination lookup failed');
            setOptions(await response.json());
        } catch (failure) {
            if (failure.name === 'AbortError') return;
            const fallback = groups.find((group) => group.dataset.province === province.value);
            setOptions(fallback ? Array.from(fallback.children).map((option) => option.value) : []);
            error.textContent = 'No se pudo consultar la API. Puedes usar los distritos cargados con la página.';
        }
    });
});

document.querySelectorAll('form[data-confirm]').forEach((form) => {
    form.addEventListener('submit', (event) => {
        if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
});
