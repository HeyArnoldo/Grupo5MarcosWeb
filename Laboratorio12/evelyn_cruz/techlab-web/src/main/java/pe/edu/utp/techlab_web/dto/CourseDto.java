package pe.edu.utp.techlab_web.dto;

public record CourseDto(
        Long id,
        String titulo,
        int horas,
        long categoriaId,
        String categoriaNombre
        ) {

}
