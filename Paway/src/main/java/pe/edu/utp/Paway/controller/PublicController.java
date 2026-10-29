package pe.edu.utp.Paway.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pe.edu.utp.Paway.config.AccountSession;
import pe.edu.utp.Paway.dto.QuoteForm;
import pe.edu.utp.Paway.dto.RegisterForm;
import pe.edu.utp.Paway.entity.AccountEntity.Role;
import pe.edu.utp.Paway.service.AccountService;
import pe.edu.utp.Paway.service.AccountService.SignedIn;
import pe.edu.utp.Paway.service.QuoteService;
import pe.edu.utp.Paway.service.ShipmentService;
import pe.edu.utp.Paway.service.TrackingLocationService;

@Controller
public class PublicController {
    private final QuoteService quotes;
    private final ShipmentService shipments;
    private final TrackingLocationService locations;
    private final AccountService accounts;

    public PublicController(QuoteService quotes, ShipmentService shipments, TrackingLocationService locations,
            AccountService accounts) {
        this.quotes = quotes;
        this.shipments = shipments;
        this.locations = locations;
        this.accounts = accounts;
    }

    @GetMapping("/")
    public String home() { return "home"; }

    @GetMapping("/ayuda")
    public String help() { return "help"; }

    @GetMapping("/terminos")
    public String terms() { return "redirect:/terminos-y-condiciones"; }

    @GetMapping("/privacidad")
    public String privacy(Model model, @Value("${paway.provider.name}") String providerName,
            @Value("${paway.provider.ruc}") String providerRuc,
            @Value("${paway.provider.address}") String providerAddress,
            @Value("${paway.provider.email}") String providerEmail) {
        model.addAttribute("privacyProviderName", providerName);
        model.addAttribute("privacyProviderRuc", providerRuc);
        model.addAttribute("privacyProviderAddress", providerAddress);
        model.addAttribute("privacyProviderEmail", providerEmail);
        return "privacy";
    }

    @GetMapping("/cotizar")
    public String quote(@ModelAttribute("quoteForm") QuoteForm form, Model model) {
        addDistricts(form, model);
        return "quote";
    }

    @PostMapping("/cotizar")
    public String calculate(@Valid @ModelAttribute("quoteForm") QuoteForm form, BindingResult errors,
            @RequestParam(defaultValue = "false") boolean requestShipment, Model model, HttpSession session,
            HttpServletRequest request) {
        if (!errors.hasErrors()) {
            try {
                model.addAttribute("quote", quotes.calculate(form));
                if (requestShipment) {
                    session.setAttribute("pendingQuote", form);
                    boolean client = AccountSession.current(request, accounts)
                            .filter(account -> account.role() == Role.CLIENT).isPresent();
                    return "redirect:" + (client ? "/cliente/envios/nuevo" : "/login");
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
                shipments.track(normalized).ifPresentOrElse(s -> {
                    model.addAttribute("shipment", s);
                    locations.locate(s).ifPresent(location -> model.addAttribute("trackingLocation", location));
                },
                        () -> model.addAttribute("trackingError", "No encontramos un envío con esa guía."));
            }
        }
        return "tracking";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginEmail", "");
        return "login";
    }

    @PostMapping("/login")
    public String signIn(@RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String password, HttpServletRequest request, Model model) {
        return accounts.authenticate(email, password).map(account -> start(request, account)).orElseGet(() -> {
            // Same message for an unknown email and a wrong password (no account enumeration).
            model.addAttribute("loginError", "Correo o contraseña incorrectos.");
            model.addAttribute("loginEmail", email.strip());
            return "login";
        });
    }

    @GetMapping("/registro")
    public String register(@ModelAttribute("registerForm") RegisterForm form) {
        return "register";
    }

    @PostMapping("/registro")
    public String createAccount(@Valid @ModelAttribute("registerForm") RegisterForm form, BindingResult errors,
            HttpServletRequest request) {
        if (!errors.hasFieldErrors("password") && !AccountService.encodable(form.getPassword())) {
            errors.rejectValue("password", "password.tooLong", "La contraseña es demasiado larga.");
        }
        if (!form.getPassword().equals(form.getPasswordConfirmation())) {
            errors.rejectValue("passwordConfirmation", "password.mismatch", "Las contraseñas no coinciden.");
        }
        if (!errors.hasErrors()) {
            try {
                return start(request, accounts.register(form));
            } catch (AccountService.DuplicateAccountException ex) {
                errors.rejectValue(ex.field(), "account.duplicate", "email".equals(ex.field())
                        ? "Este correo ya está registrado." : "Este DNI ya está registrado.");
            }
        }
        // Passwords are never sent back to the browser.
        form.setPassword("");
        form.setPasswordConfirmation("");
        return "register";
    }

    private String start(HttpServletRequest request, SignedIn account) {
        AccountSession.signIn(request, account);
        if (account.role() == Role.ADMIN) return "redirect:/admin";
        return "redirect:" + (request.getSession().getAttribute("pendingQuote") == null
                ? "/cliente" : "/cliente/envios/nuevo");
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
