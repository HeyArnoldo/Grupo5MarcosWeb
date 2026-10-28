package pe.edu.utp.techlab_web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CourseInputDto {

    @NotBlank(message = "{course.titulo.obligatorio}")
    @Size(max = 120, message = "{course.titulo.longitud}")
    private String titulo;

    @NotNull(message = "{course.horas.obligatorias}")
    @Min(value = 1, message = "{course.horas.minimo}")
    @Max(value = 500, message = "{course.horas.maximo}")
    private Integer horas;

    @NotNull(message = "{course.categoria.obligatoria}")
    private Long categoriaId;

    public CourseInputDto() {
    }

    public CourseInputDto(String titulo, Integer horas, Long categoriaId) {
        this.titulo = titulo;
        this.horas = horas;
        this.categoriaId = categoriaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getHoras() {
        return horas;
    }

    public void setHoras(Integer horas) {
        this.horas = horas;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
}
