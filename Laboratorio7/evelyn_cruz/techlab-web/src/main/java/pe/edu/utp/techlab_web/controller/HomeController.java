package main.java.pe.edu.utp.techlab_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", "TechLab Web - UTP");
        return "index"; 
    }

    @GetMapping("/saludo")
    public String saludar(@RequestParam(name = "nombre", required = false, defaultValue = "Estudiante") String nombre, Model model) {
        model.addAttribute("mensaje", "¡Bienvenido al sistema, " + nombre + "!");
        return "saludo"; 
    }
}