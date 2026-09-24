package pe.edu.utp.techlab_web.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.service.CourseService;

@RestController
@RequestMapping(value = "/api/v1/cursos",
        produces = "application/json")
public class CourseController {
    private final CourseService service;
    public CourseController(CourseService service) {
        this.service = service;
    }
    @GetMapping
    public List<CourseDTO> listar(
            @RequestParam(name = "q", defaultValue = "") String q) {
        if (q.length() > 60) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Consulta demasiado larga");
        }
        return service.buscar(q);
    }
    @GetMapping("/{id}")
    public CourseDTO detalle(@PathVariable("id") long id) {
        return service.buscarPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Curso no encontrado"));
    }
}