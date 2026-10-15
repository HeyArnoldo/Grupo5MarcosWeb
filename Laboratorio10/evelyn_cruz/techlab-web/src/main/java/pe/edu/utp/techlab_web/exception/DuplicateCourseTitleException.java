package pe.edu.utp.techlab_web.exception;

public class DuplicateCourseTitleException extends RuntimeException {
    public DuplicateCourseTitleException(String titulo) {
        super("Ya existe un curso con el título " + titulo);
    }

}
