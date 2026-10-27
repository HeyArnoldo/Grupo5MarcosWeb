package pe.edu.utp.techlab_web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pe.edu.utp.techlab_web.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOrderByIdAsc();

    List<Course> findByTituloContainingIgnoreCaseOrderByIdAsc(String titulo);

    List<Course> findByCategoriaIgnoreCaseOrderByIdAsc(String categoria);

    List<Course> findByTituloContainingIgnoreCaseAndCategoriaIgnoreCaseOrderByIdAsc(
            String titulo, String categoria);

    @Query("select sum(c.horas) from Course c")
    Long sumarHoras();
}
