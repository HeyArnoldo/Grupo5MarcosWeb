package pe.edu.utp.techlab_web.service;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.entity.Course;
import pe.edu.utp.techlab_web.exception.CourseNotFoundException;
import pe.edu.utp.techlab_web.exception.DuplicateCourseTitleException;
import pe.edu.utp.techlab_web.exception.InvalidCourseDataException;
import pe.edu.utp.techlab_web.mapper.CourseMapper;
import pe.edu.utp.techlab_web.repository.CourseRepository;

@Service
@Transactional(readOnly = true)
public class CourseService {
    private static final int MAX_QUERY_LENGTH = 60;
    private static final int MAX_TITLE_LENGTH = 120;
    private static final int MIN_HOURS = 1;
    private static final int MAX_HOURS = 500;

    private final CourseRepository repository;
    private final CourseMapper mapper;

    public CourseService(
            CourseRepository repository,
            CourseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CourseDTO> buscar(String texto) {
        String criterio = Optional.ofNullable(texto)
                .orElse("")
                .strip();
        if (criterio.length() > MAX_QUERY_LENGTH) {
            throw new InvalidCourseDataException(
                    "La búsqueda admite hasta 60 caracteres");
        }
        List<Course> entidades = criterio.isBlank()
                ? repository.findAllByOrderByIdAsc()
                : repository.findByTituloContainingIgnoreCaseOrderByIdAsc(criterio);
        return entidades.stream()
                .map(mapper::toDto)
                .toList();
    }

    public Optional<CourseDTO> buscarPorId(long id) {
        return repository.findById(id)
                .map(mapper::toDto);
    }

    public long cantidad() {
        return repository.count();
    }

    public int totalHoras() {
        Long total = repository.sumarHoras();
        return Math.toIntExact(total == null ? 0L : total);
    }

    @Transactional
    public CourseDTO crear(CourseInputDto input) {
        CourseData data = normalizar(input);
        if (repository.existsByTituloIgnoreCase(data.titulo())) {
            throw new DuplicateCourseTitleException(data.titulo());
        }
        try {
            Course entity = Course.crear(data.titulo(), data.horas());
            return mapper.toDto(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCourseTitleException(data.titulo());
        }
    }

    @Transactional
    public CourseDTO actualizar(long id, CourseInputDto input) {
        Course entity = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        CourseData data = normalizar(input);
        boolean repetido = repository.existsByTituloIgnoreCaseAndIdNot(data.titulo(), id);
        if (repetido) {
            throw new DuplicateCourseTitleException(data.titulo());
        }
        entity.actualizar(data.titulo(), data.horas());
        try {
            repository.flush();
            return mapper.toDto(entity);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCourseTitleException(data.titulo());
        }
    }

    @Transactional
    public void eliminar(long id) {
        Course entity = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        repository.delete(entity);
        repository.flush();
    }

    private CourseData normalizar(CourseInputDto input) {
        if (input == null) {
            throw new InvalidCourseDataException(
                    "Debe enviar los datos del curso");
        }
        String titulo = Optional.ofNullable(input.getTitulo())
                .orElse("")
                .strip()
                .replaceAll("\\s+", " ");
        if (titulo.isBlank()) {
            throw new InvalidCourseDataException(
                    "El título es obligatorio");
        }
        if (titulo.length() > MAX_TITLE_LENGTH) {
            throw new InvalidCourseDataException(
                    "El título admite hasta 120 caracteres");
        }
        Integer horas = input.getHoras();
        if (horas == null) {
            throw new InvalidCourseDataException(
                    "La cantidad de horas es obligatoria");
        }
        if (horas < MIN_HOURS || horas > MAX_HOURS) {
            throw new InvalidCourseDataException(
                    "Las horas deben estar entre 1 y 500");
        }
        return new CourseData(titulo, horas);
    }

    private record CourseData(String titulo, int horas) {
    }
}
