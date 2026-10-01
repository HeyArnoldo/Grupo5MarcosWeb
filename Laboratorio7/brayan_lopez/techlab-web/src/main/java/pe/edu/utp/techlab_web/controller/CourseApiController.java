package pe.edu.utp.techlab_web.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.service.CourseService;

@RestController
@RequestMapping("/api/v1/cursos")
public class CourseApiController {

    private final CourseService service;

    public CourseApiController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourseDTO> listarApi(@RequestParam(name = "q", defaultValue = "") String q) {
        return service.buscar(q);
    }
}
