package pe.edu.utp.techlab_web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.techlab.dto.CourseDto;
import pe.edu.utp.techlab.dto.CourseInputDto;
import pe.edu.utp.techlab.exception.CourseNotFoundException;
import pe.edu.utp.techlab.exception.DuplicateCourseTitleException;
import pe.edu.utp.techlab.exception.InvalidCourseDataException;
import pe.edu.utp.techlab.service.CourseService;

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
 String consulta = normalizarConsulta(q);
 model.addAttribute("cursos", service.buscar(consulta));
 model.addAttribute("q", consulta);
 return "cursos/lista";
 }

@GetMapping("/resumen")
 public String resumen(Model model) {
 model.addAttribute("cantidadCursos", service.cantidad());
 model.addAttribute("totalHoras", service.totalHoras());
 return "cursos/resumen";
}
@GetMapping("/{id}")
public String detalle(@PathVariable long id, Model model) {
 CourseDto curso = service.buscarPorId(id)
 .orElseThrow(() -> notFound(id));
 model.addAttribute("curso", curso);
 return "cursos/detalle";
}
@GetMapping("/nuevo")
public String nuevo(Model model) {
 return mostrarFormulario(
 new CourseInputDto(),
 null,
 "Nuevo curso",
 "Registrar curso",
 null,
 model
 );
}
@PostMapping
public String crear(
 @ModelAttribute("form") CourseInputDto form,
 Model model,
 RedirectAttributes redirect) {
 try {
 CourseDto creado = service.crear(form);
 redirect.addFlashAttribute(
 "mensaje",
 "Curso registrado correctamente"
 );
 return "redirect:/cursos/" + creado.id();
 } catch (InvalidCourseDataException
 | DuplicateCourseTitleException exception) {
 return mostrarFormulario(
 form,
 null,
 "Nuevo curso",
 "Registrar curso",
 exception.getMessage(),
 model
 );
 }
}
@GetMapping("/{id}/editar")
