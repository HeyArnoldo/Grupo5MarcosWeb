package pe.edu.utp.techlab_web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cursos")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "horas", nullable = false)
    private int horas;

    @Column(name = "categoria", nullable = false, length = 40)
    private String categoria;

    protected Course() {
    }

    public static Course crear(String titulo, int horas, String categoria) {
        Course course = new Course();
        course.titulo = titulo;
        course.horas = horas;
        course.categoria = categoria;
        return course;
    }

    public void actualizar(String titulo, int horas, String categoria) {
        this.titulo = titulo;
        this.horas = horas;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getHoras() {
        return horas;
    }

    public String getCategoria() {
        return categoria;
    }
}
