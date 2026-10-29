package pe.edu.utp.Paway.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.edu.utp.Paway.config.AccountSession;
import pe.edu.utp.Paway.entity.AccountEntity.Role;
import pe.edu.utp.Paway.model.ShipmentStatus;
import pe.edu.utp.Paway.service.AccountService;
import pe.edu.utp.Paway.service.QuoteService;
import pe.edu.utp.Paway.service.ShipmentService;

@ControllerAdvice(basePackageClasses = {PublicController.class, PanelController.class, LegalController.class})
public class ViewData {
    private final QuoteService quotes;
    private final ShipmentService shipments;
    private final AccountService accounts;

    public ViewData(QuoteService quotes, ShipmentService shipments, AccountService accounts) {
        this.quotes = quotes;
        this.shipments = shipments;
        this.accounts = accounts;
    }

    @ModelAttribute("destinations")
    public List<QuoteService.Destination> destinations() { return quotes.destinations(); }
    @ModelAttribute("categories")
    public List<QuoteService.Category> categories() { return quotes.categories(); }
    @ModelAttribute("agencies")
    public List<String> agencies() { return shipments.agencies(); }
    @ModelAttribute("statuses")
    public ShipmentStatus[] statuses() { return ShipmentStatus.values(); }
    /** "" when anonymous, "admin" for the ADMIN account and "client" for a CLIENT account. */
    @ModelAttribute("profile")
    public String profile(HttpServletRequest request) {
        return AccountSession.current(request, accounts)
                .map(account -> account.role() == Role.ADMIN ? "admin" : "client").orElse("");
    }
    @ModelAttribute("profileName")
    public String profileName(HttpServletRequest request) {
        return AccountSession.current(request, accounts).map(AccountService.SignedIn::displayName).orElse("");
    }
}
