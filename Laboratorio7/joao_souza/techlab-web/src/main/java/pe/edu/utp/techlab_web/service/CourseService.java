package pe.edu.utp.techlab_web.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import pe.edu.utp.techlab_web.dto.CourseDTO;

@Service
public class CourseService {

    private final List<CourseDTO> cursos = List.of(
            new CourseDTO(1L, "HTML y CSS", 12),
            new CourseDTO(2L, "Bootstrap", 16),
            new CourseDTO(3L, "Spring Boot", 20)
    );

    public List<CourseDTO> buscar(String texto) {
        String criterio = Optional.ofNullable(texto)
                .orElse("")
                .strip()
                .toLowerCase(Locale.ROOT);

        if (criterio.isBlank()) {
            return cursos;
        }

        return cursos.stream()
                .filter(curso -> curso.titulo()
                        .toLowerCase(Locale.ROOT)
                        .contains(criterio))
                .toList();
    }

    public Optional<CourseDTO> buscarPorId(long id) {
        return cursos.stream()
                .filter(curso -> curso.id() == id)
                .findFirst();
    }
}