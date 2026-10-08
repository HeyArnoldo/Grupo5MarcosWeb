package pe.edu.utp.techlab_web.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.entity.Course;
import pe.edu.utp.techlab_web.mapper.CourseMapper;
import pe.edu.utp.techlab_web.repository.CourseRepository;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository repository;
    private final CourseMapper mapper;

    public CourseService(CourseRepository repository, CourseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CourseDTO> buscar(String texto) {
        return buscar(texto, "");
    }

    public List<CourseDTO> buscar(String texto, String categoria) {
        String criterio = normalizar(texto);
        String filtro = normalizar(categoria);
        List<Course> entidades;
        if (filtro.isBlank()) {
            entidades = criterio.isBlank()
                    ? repository.findAllByOrderByIdAsc()
                    : repository.findByTituloContainingIgnoreCaseOrderByIdAsc(criterio);
        } else {
            entidades = criterio.isBlank()
                    ? repository.findByCategoriaIgnoreCaseOrderByIdAsc(filtro)
                    : repository.findByTituloContainingIgnoreCaseAndCategoriaIgnoreCaseOrderByIdAsc(
                            criterio, filtro);
        }
        return entidades.stream().map(mapper::toDto).toList();
    }

    public Optional<CourseDTO> buscarPorId(long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    public long cantidad() {
        return repository.count();
    }

    public int totalHoras() {
        Long total = repository.sumarHoras();
        return Math.toIntExact(total == null ? 0L : total);
    }

    private String normalizar(String texto) {
        return Optional.ofNullable(texto).orElse("").strip();
    }
}
