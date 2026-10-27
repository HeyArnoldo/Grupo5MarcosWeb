package pe.edu.utp.techlab_web.service;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import pe.edu.utp.techlab_web.dto.CategoryDto;
import pe.edu.utp.techlab_web.dto.CourseDto;
import pe.edu.utp.techlab_web.dto.CourseInputDto;
import pe.edu.utp.techlab_web.entity.Category;
import pe.edu.utp.techlab_web.entity.Course;
import pe.edu.utp.techlab_web.exception.CategoryNotFoundException;
import pe.edu.utp.techlab_web.exception.CourseNotFoundException;
import pe.edu.utp.techlab_web.exception.DuplicateCourseTitleException;
import pe.edu.utp.techlab_web.exception.InvalidCourseDataException;
import pe.edu.utp.techlab_web.mapper.CourseMapper;
import pe.edu.utp.techlab_web.repository.CategoryRepository;
import pe.edu.utp.techlab_web.repository.CourseRepository;

@Service
@Validated
@Transactional(readOnly = true)
public class CourseService {

    private static final int MAX_QUERY_LENGTH = 60;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CourseMapper mapper;

    public CourseService(
            CourseRepository courseRepository,
            CategoryRepository categoryRepository,
            CourseMapper mapper) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    public List<CourseDto> buscar(String texto) {
        String criterio = Optional.ofNullable(texto)
                .orElse("")
                .strip();
        if (criterio.length() > MAX_QUERY_LENGTH) {
            throw new InvalidCourseDataException(
                    "La búsqueda admite hasta 60 caracteres"
            );
        }
        List<Course> entidades = criterio.isBlank()
                ? courseRepository.findAllByOrderByIdAsc()
                : courseRepository.findByTituloContainingIgnoreCaseOrderByIdAsc(criterio);
        return entidades.stream()
                .map(mapper::toDto)
                .toList();
    }

    public Optional<CourseDto> buscarPorId(long id) {
        return courseRepository.findDetailById(id)
                .map(mapper::toDto);
    }

    public List<CategoryDto> listarCategorias() {
        return categoryRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(category -> new CategoryDto(
                        category.getId(),
                        category.getNombre()
                ))
                .toList();
    }

    public long cantidad() {
        return courseRepository.count();
    }

    public int totalHoras() {
        Long total = courseRepository.sumarHoras();
        return Math.toIntExact(total == null ? 0L : total);
    }

    @Transactional
    public CourseDto crear(
            @NotNull(message = "Debe enviar los datos del curso")
            @Valid CourseInputDto input) {
        CourseData data = normalizar(input);
        validarTituloDisponible(data.titulo(), null);
        Category categoria = buscarCategoria(data.categoriaId());
        try {
            Course entity = Course.crear(
                    data.titulo(),
                    data.horas(),
                    categoria
            );
            return mapper.toDto(courseRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCourseTitleException(data.titulo());
        }
    }

    @Transactional
    public CourseDto actualizar(
            long id,
            @NotNull(message = "Debe enviar los datos del curso")
            @Valid CourseInputDto input) {
        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        CourseData data = normalizar(input);
        validarTituloDisponible(data.titulo(), id);
        Category categoria = buscarCategoria(data.categoriaId());
        entity.actualizar(
                data.titulo(),
                data.horas(),
                categoria
        );
        try {
            courseRepository.flush();
            return mapper.toDto(entity);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCourseTitleException(data.titulo());
        }
    }

    @Transactional
    public void eliminar(long id) {
        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        courseRepository.delete(entity);
        courseRepository.flush();
    }

    private CourseData normalizar(CourseInputDto input) {
        String titulo = input.getTitulo()
                .strip()
                .replaceAll("\\s+", " ");
        return new CourseData(
                titulo,
                input.getHoras(),
                input.getCategoriaId()
        );
    }

    private Category buscarCategoria(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private void validarTituloDisponible(String titulo, Long cursoId) {
        boolean repetido = cursoId == null
                ? courseRepository.existsByTituloIgnoreCase(titulo)
                : courseRepository.existsByTituloIgnoreCaseAndIdNot(titulo, cursoId);
        if (repetido) {
            throw new DuplicateCourseTitleException(titulo);
        }
    }

    private record CourseData(
            String titulo,
            int horas,
            long categoriaId
            ) {

    }
}
