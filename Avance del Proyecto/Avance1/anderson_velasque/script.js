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

// El botón "Iniciar Sesión" del navbar compartido apunta a esta
// página con #login. Al llegar desde otro módulo se abre directo
// el formulario en lugar de la portada.
window.addEventListener('DOMContentLoaded', function () {
    if (window.location.hash === '#login') {
        showSection('login');
    }
});