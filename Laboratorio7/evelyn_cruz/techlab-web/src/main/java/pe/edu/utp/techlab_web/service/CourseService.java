package pe.edu.utp.techlab_web.service;

import java.util.List;
import java.util.Optional;
import pe.edu.utp.techlab_web.dto.CourseDTO;
import java.util.ArrayList;

public class CourseService {

    public List<CourseDTO> buscar(String q) {
        // Lógica de búsqueda básica
        return new ArrayList<>();
    }

    public Optional<CourseDTO> buscarPorId(Long id) {
        // Lógica para buscar por ID
        return Optional.empty();
    }
}
