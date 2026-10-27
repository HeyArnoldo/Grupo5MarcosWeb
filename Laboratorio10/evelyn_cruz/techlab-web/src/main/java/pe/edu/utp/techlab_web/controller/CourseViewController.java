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
import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.exception.CourseNotFoundException;
import pe.edu.utp.techlab_web.exception.DuplicateCourseTitleException;
import pe.edu.utp.techlab_web.exception.InvalidCourseDataException;
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
        CourseDTO curso = service.buscarPorId(id).orElseThrow(() -> notFound(id));
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
                model);
    }

    @PostMapping
    public String crear(
            @ModelAttribute("form") CourseInputDto form,
            Model model,
            RedirectAttributes redirect) {
        try {
            CourseDTO creado = service.crear(form);
            redirect.addFlashAttribute(
                    "mensaje",
                    "Curso registrado correctamente");
            return "redirect:/cursos/" + creado.id();
        } catch (InvalidCourseDataException
                | DuplicateCourseTitleException exception) {
            return mostrarFormulario(
                    form,
                    null,
                    "Nuevo curso",
                    "Registrar curso",
                    exception.getMessage(),
                    model);
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        CourseDTO curso = service.buscarPorId(id)
                .orElseThrow(() -> notFound(id));
        CourseInputDto form = new CourseInputDto(
                curso.titulo(),
                curso.horas(),
                curso.categoria());
        return mostrarFormulario(
                form,
                id,
                "Editar curso",
                "Guardar cambios",
                null,
                model);
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable long id,
            @ModelAttribute("form") CourseInputDto form,
            Model model,
            RedirectAttributes redirect) {
        try {
            service.actualizar(id, form);
            redirect.addFlashAttribute(
                    "mensaje",
                    "Curso actualizado correctamente");
            return "redirect:/cursos/" + id;
        } catch (InvalidCourseDataException
                | DuplicateCourseTitleException exception) {
            return mostrarFormulario(
                    form,
                    id,
                    "Editar curso",
                    "Guardar cambios",
                    exception.getMessage(),
                    model);
        } catch (CourseNotFoundException exception) {
            throw notFound(id);
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable long id,
            RedirectAttributes redirect) {
        try {
            service.eliminar(id);
            redirect.addFlashAttribute(
                    "mensaje",
                    "Curso eliminado correctamente");
            return "redirect:/cursos";
        } catch (CourseNotFoundException exception) {
            throw notFound(id);
        }
    }

    private String mostrarFormulario(
            CourseInputDto form,
            Long cursoId,
            String tituloPagina,
            String textoBoton,
            String error,
            Model model) {
        model.addAttribute("form", form);
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("tituloPagina", tituloPagina);
        model.addAttribute("textoBoton", textoBoton);
        model.addAttribute("error", error);
        return "cursos/formulario";
    }

    private String normalizarConsulta(String q) {
        String consulta = q.strip();
        if (consulta.length() > MAX_QUERY_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La búsqueda admite hasta 60 caracteres");
        }
        return consulta;
    }

    private ResponseStatusException notFound(long id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No existe un curso con el identificador " + id);
    }
}