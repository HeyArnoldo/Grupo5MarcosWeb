function showSection(sectionId) {
    const sections = document.querySelectorAll('.section');
    sections.forEach(sec => sec.classList.remove('active'));
    document.getElementById(sectionId).classList.add('active');
}

function handleLogin(event) {
    event.preventDefault();
    alert('¡Inicio de sesión exitoso en Paway!');
    showSection('home');
}