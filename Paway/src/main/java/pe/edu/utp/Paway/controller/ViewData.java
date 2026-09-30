package pe.edu.utp.Paway.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.edu.utp.Paway.model.ShipmentStatus;
import pe.edu.utp.Paway.service.QuoteService;
import pe.edu.utp.Paway.service.ShipmentService;

@ControllerAdvice(basePackageClasses = {PublicController.class, PanelController.class})
public class ViewData {
    private final QuoteService quotes;
    private final ShipmentService shipments;

    public ViewData(QuoteService quotes, ShipmentService shipments) {
        this.quotes = quotes;
        this.shipments = shipments;
    }

    @ModelAttribute("destinations")
    public List<QuoteService.Destination> destinations() { return quotes.destinations(); }
    @ModelAttribute("categories")
    public List<QuoteService.Category> categories() { return quotes.categories(); }
    @ModelAttribute("agencies")
    public List<String> agencies() { return shipments.agencies(); }
    @ModelAttribute("statuses")
    public ShipmentStatus[] statuses() { return ShipmentStatus.values(); }
    @ModelAttribute("profile")
    public String profile(HttpSession session) {
        Object profile = session.getAttribute("profile");
        return profile == null ? "" : profile.toString();
    }
    @ModelAttribute("profileName")
    public String profileName(HttpSession session) {
        Object profile = session.getAttribute("profile");
        if (profile == null) return "";
        if ("admin".equals(profile)) return "Administrador";
        if ("client1".equals(profile)) return shipments.client(1L).name();
        if ("client2".equals(profile)) return shipments.client(2L).name();
        return "";
    }
}
