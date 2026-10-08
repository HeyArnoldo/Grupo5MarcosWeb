package pe.edu.utp.techlab_web.mapper;

import org.springframework.stereotype.Component;

import pe.edu.utp.techlab_web.dto.CourseDTO;
import pe.edu.utp.techlab_web.entity.Course;

@Component
public class CourseMapper {

    public CourseDTO toDto(Course course) {
        return new CourseDTO(course.getId(), course.getTitulo(),
                course.getHoras(), course.getCategoria());
    }
}
