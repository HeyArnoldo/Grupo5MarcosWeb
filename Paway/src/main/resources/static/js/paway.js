(() => {
    const root = document.documentElement;
    const button = document.querySelector('[data-theme-toggle]');
    const storageKey = 'paway-theme';
    let theme = 'light';
    try { theme = localStorage.getItem(storageKey) === 'dark' ? 'dark' : 'light'; } catch (_) { /* Storage may be disabled. */ }

    function applyTheme(value) {
        theme = value;
        root.dataset.theme = theme;
        root.dataset.bsTheme = theme;
        if (!button) return;
        const dark = theme === 'dark';
        button.setAttribute('aria-pressed', String(dark));
        button.setAttribute('aria-label', dark ? 'Activar modo claro' : 'Activar modo oscuro');
        button.querySelector('i')?.classList.toggle('bi-moon-stars', !dark);
        button.querySelector('i')?.classList.toggle('bi-sun', dark);
        const label = button.querySelector('span');
        if (label) label.textContent = dark ? 'Modo claro' : 'Modo oscuro';
    }

    applyTheme(theme);
    button?.addEventListener('click', () => {
        applyTheme(theme === 'dark' ? 'light' : 'dark');
        try { localStorage.setItem(storageKey, theme); } catch (_) { /* Keep the current page theme. */ }
    });
})();

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

const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
if (window.AOS && !reducedMotion.matches) {
    document.querySelectorAll('.banner-content, .quick-actions, .section-heading, .info-card, .service-feature, .coverage-card, .page-heading').forEach((element, index) => {
        element.dataset.aos = 'fade-up';
        element.dataset.aosDelay = String((index % 3) * 50);
    });
    try {
        window.AOS.init({ duration: 650, easing: 'ease-out-cubic', once: true, offset: 35 });
        document.documentElement.classList.add('aos-ready');
    } catch {
        document.documentElement.classList.remove('aos-ready');
    }
}
reducedMotion.addEventListener('change', (event) => {
    if (event.matches) document.documentElement.classList.remove('aos-ready');
});
document.querySelector('[data-form-errors]')?.focus();
document.querySelectorAll('[data-print-receipt]').forEach((button) => button.addEventListener('click', () => window.print()));
const minor = document.querySelector('[data-minor]');
if (minor) {
    const updateGuardian = () => document.querySelectorAll('[data-guardian]').forEach((field) => { field.required = minor.checked; });
    minor.addEventListener('change', updateGuardian);
    updateGuardian();
}

const mapElement = document.querySelector('[data-tracking-map]');
if (mapElement && window.L) {
    const latitude = Number(mapElement.dataset.lat);
    const longitude = Number(mapElement.dataset.lng);
    if (Number.isFinite(latitude) && Number.isFinite(longitude)) {
        const map = window.L.map(mapElement, { scrollWheelZoom: false }).setView([latitude, longitude], 13);
        const tiles = window.L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);
        const popup = document.createElement('strong');
        popup.textContent = mapElement.dataset.label;
        const pin = window.L.divIcon({ className: 'paway-map-marker', html: '<span aria-hidden="true">📦</span>', iconSize: [42, 42], iconAnchor: [21, 42], popupAnchor: [0, -44] });
        window.L.marker([latitude, longitude], { icon: pin, title: mapElement.dataset.label, alt: 'Punto de referencia del envío' }).addTo(map).bindPopup(popup).openPopup();
        tiles.on('tileerror', () => { document.querySelector('[data-map-fallback]').hidden = false; });
        // A failed tile request keeps the external location link available.
        mapElement.addEventListener('focusin', () => mapElement.scrollIntoView({ block: 'nearest' }));
    }
}
