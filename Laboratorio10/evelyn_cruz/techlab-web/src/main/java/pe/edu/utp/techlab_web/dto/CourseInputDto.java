public class CourseInputDto {
    
    private String titulo;
    private Integer horas;

    public CourseInputDto() {
    }

    public CourseInputDto(String titulo, Integer horas) {
        this.titulo = titulo;
        this.horas = horas;
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

}
