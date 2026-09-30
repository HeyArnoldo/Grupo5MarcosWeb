package pe.edu.utp.Paway.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.Paway.dto.QuoteForm;
import pe.edu.utp.Paway.service.QuoteService;
import pe.edu.utp.Paway.service.ShipmentService;

@Controller
public class PublicController {
    private final QuoteService quotes;
    private final ShipmentService shipments;

    public PublicController(QuoteService quotes, ShipmentService shipments) {
        this.quotes = quotes;
        this.shipments = shipments;
    }

    @GetMapping("/")
    public String home() { return "home"; }

    @GetMapping("/cotizar")
    public String quote(@ModelAttribute("quoteForm") QuoteForm form, Model model) {
        addDistricts(form, model);
        return "quote";
    }

    @PostMapping("/cotizar")
    public String calculate(@Valid @ModelAttribute("quoteForm") QuoteForm form, BindingResult errors,
            @RequestParam(defaultValue = "false") boolean requestShipment, Model model, HttpSession session) {
        if (!errors.hasErrors()) {
            try {
                model.addAttribute("quote", quotes.calculate(form));
                if (requestShipment) {
                    session.setAttribute("pendingQuote", form);
                    String profile = (String) session.getAttribute("profile");
                    return "redirect:" + ("client1".equals(profile) || "client2".equals(profile)
                            ? "/cliente/envios/nuevo" : "/login");
                }
            } catch (IllegalArgumentException ex) {
                errors.reject("quote.invalid", ex.getMessage());
            }
        }
        addDistricts(form, model);
        return "quote";
    }

    private void addDistricts(QuoteForm form, Model model) {
        List<String> districts = quotes.destinations().stream()
                .filter(d -> d.code().equals(form.getProvince())).findFirst()
                .map(QuoteService.Destination::districts).orElse(List.of());
        model.addAttribute("districts", districts);
    }

    @GetMapping("/rastrear")
    public String tracking(@RequestParam(defaultValue = "") String guide, Model model) {
        String normalized = guide.strip();
        model.addAttribute("guide", normalized);
        if (!normalized.isEmpty()) {
            if (normalized.length() > 40) {
                model.addAttribute("trackingError", "El código de guía admite hasta 40 caracteres.");
            } else {
                shipments.track(normalized).ifPresentOrElse(s -> model.addAttribute("shipment", s),
                        () -> model.addAttribute("trackingError", "No encontramos un envío con esa guía."));
            }
        }
        return "tracking";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("clients", shipments.clients());
        return "login";
    }

    @PostMapping("/login")
    public String selectProfile(@RequestParam String profile, HttpSession session, RedirectAttributes redirect) {
        if (!List.of("admin", "client1", "client2").contains(profile)) {
            redirect.addFlashAttribute("error", "Seleccione un perfil de demostración válido.");
            return "redirect:/login";
        }
        session.setAttribute("profile", profile);
        if (profile.equals("admin")) return "redirect:/admin";
        return "redirect:" + (session.getAttribute("pendingQuote") == null ? "/cliente" : "/cliente/envios/nuevo");
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
