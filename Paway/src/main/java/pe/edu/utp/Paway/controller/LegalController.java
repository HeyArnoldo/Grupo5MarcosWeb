package pe.edu.utp.Paway.controller;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.UUID;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.Paway.dto.ComplaintForm;
import pe.edu.utp.Paway.service.ComplaintService;

@Controller
public class LegalController {
    private final ComplaintService complaints;
    public LegalController(ComplaintService complaints) { this.complaints = complaints; }

    public record Provider(String name, String ruc, String address, String email) {}
    @ModelAttribute("provider")
    public Provider provider(@Value("${paway.provider.name}") String name, @Value("${paway.provider.ruc}") String ruc,
            @Value("${paway.provider.address}") String address, @Value("${paway.provider.email}") String email) {
        return new Provider(name, ruc, address, email);
    }

    @GetMapping("/terminos-y-condiciones")
    public String terms() { return "terms"; }

    @GetMapping("/libro-de-reclamaciones")
    public String book(@ModelAttribute("complaintForm") ComplaintForm form, HttpSession session, Model model,
            HttpServletResponse response) {
        privatePage(response);
        String token = UUID.randomUUID().toString();
        session.setAttribute("complaintToken", token);
        model.addAttribute("submissionToken", token);
        return "complaints";
    }

    @PostMapping("/libro-de-reclamaciones")
    public String register(@Valid @ModelAttribute("complaintForm") ComplaintForm form, BindingResult errors,
            @RequestParam String submissionToken, HttpSession session, Model model, HttpServletResponse response) {
        privatePage(response);
        if (!submissionToken.equals(session.getAttribute("complaintToken"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        model.addAttribute("submissionToken", submissionToken);
        if (errors.hasErrors()) return "complaints";
        try {
            complaints.register(form, submissionToken);
            return "redirect:/libro-de-reclamaciones/constancia/" + submissionToken;
        } catch (IOException ex) {
            errors.reject("complaint.save", "No pudimos guardar la hoja. Tus datos siguen aquí; vuelve a intentar el envío.");
            response.setStatus(503);
            return "complaints";
        }
    }

    @GetMapping("/libro-de-reclamaciones/constancia/{token}")
    public String receipt(@PathVariable String token, Model model, HttpServletResponse response) throws IOException {
        privatePage(response);
        try { model.addAttribute("sheet", complaints.get(token)); }
        catch (NoSuchFileException | IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
        return "complaint-receipt";
    }

    @GetMapping("/admin/reclamaciones")
    public String administration(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        privatePage(response);
        if (session.getAttribute("complaintAdminToken") == null) session.setAttribute("complaintAdminToken", UUID.randomUUID().toString());
        model.addAttribute("adminToken", session.getAttribute("complaintAdminToken"));
        model.addAttribute("sheets", complaints.all());
        return "complaint-admin";
    }

    @PostMapping("/admin/reclamaciones/{token}/respuesta")
    public String respond(@PathVariable String token, @RequestParam String answer, @RequestParam String delivery,
            @RequestParam String adminToken, HttpSession session, RedirectAttributes redirect) throws IOException {
        if (!adminToken.equals(session.getAttribute("complaintAdminToken"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (answer.isBlank() || answer.length() > 4000 || delivery.isBlank() || delivery.length() > 500) {
            redirect.addFlashAttribute("error", "Completa la respuesta (hasta 4000 caracteres) y la referencia de entrega (hasta 500).");
        } else {
            try { complaints.respond(token, answer, delivery); }
            catch (NoSuchFileException | IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
            redirect.addFlashAttribute("message", "Respuesta y referencia de entrega registradas.");
        }
        return "redirect:/admin/reclamaciones";
    }

    private void privatePage(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Robots-Tag", "noindex, nofollow");
        response.setHeader("Referrer-Policy", "no-referrer");
    }
}
