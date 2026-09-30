package pe.edu.utp.techlab_web; // o el paquete raíz que tengas

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"pe.edu.utp.techlab"}) // <--- Añade esto para que encuentre tus controladores
public class TechlabWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechlabWebApplication.class, args);
    }
}