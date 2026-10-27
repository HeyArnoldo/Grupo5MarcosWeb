package pe.edu.utp.techlab_web.mapper;

import org.springframework.stereotype.Component;

import pe.edu.utp.techlab_web.dto.CourseDto;
import pe.edu.utp.techlab_web.entity.Course;

@Component
public class CourseMapper {

    public CourseDto toDto(Course course) {
        return new CourseDto(
                course.getId(),
                course.getTitulo(),
                course.getHoras(),
                course.getCategoria().getId(),
                course.getCategoria().getNombre()
        );
    }
}
