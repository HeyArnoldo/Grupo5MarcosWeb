package pe.edu.utp.techlab_web.dto;

public class CourseInputDto {

    private String titulo;
    private Integer horas;
    private String categoria;

    public CourseInputDto() {
    }

    public CourseInputDto(String titulo, Integer horas, String categoria) {
        this.titulo = titulo;
        this.horas = horas;
        this.categoria = categoria;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
