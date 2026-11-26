package pe.edu.utp.techlab_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalController {
    @GetMapping("/")
    public String inicio() {
        return "redirect:/cursos";
    }
    @GetMapping("/portal")
    public String portal() {
        return "redirect:/cursos";
    }
}