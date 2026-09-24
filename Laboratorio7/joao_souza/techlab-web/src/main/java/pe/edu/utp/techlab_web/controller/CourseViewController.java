package pe.edu.utp.techlab_web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.service.CourseService;

@Controller
@RequestMapping("/cursos")
public class CourseViewController {

    private static final int MAX_QUERY_LENGTH = 60;

    private final CourseService service;

    public CourseViewController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "") String q,
            Model model) {
        String consulta = normalizar(q);
        model.addAttribute("cursos", service.buscar(consulta));
        model.addAttribute("q", consulta);
        return "cursos/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable long id, Model model) {
        CourseDTO curso = service.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Curso no encontrado"));
        model.addAttribute("curso", curso);
        return "cursos/detalle";
    }

    private String normalizar(String q) {
        String consulta = q.strip();
        if (consulta.length() > MAX_QUERY_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La búsqueda admite hasta 60 caracteres");
        }
        return consulta;
    }
}