package pe.edu.utp.techlab_web.dto;

public record CourseDTO(
        Long id,
        String titulo,
        int horas,
        String categoria
        ) {}
