package pe.edu.utp.Paway.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.Paway.dto.QuoteForm;
import pe.edu.utp.Paway.dto.ShipmentForm;
import pe.edu.utp.Paway.model.Shipment;
import pe.edu.utp.Paway.model.ShipmentStatus;
import pe.edu.utp.Paway.service.QuoteService;
import pe.edu.utp.Paway.service.ShipmentService;

@Controller
public class PanelController {
    private static final int PAGE_SIZE = 5;
    private final ShipmentService shipments;
    private final QuoteService quotes;

    public PanelController(ShipmentService shipments, QuoteService quotes) {
        this.shipments = shipments;
        this.quotes = quotes;
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) ShipmentStatus status, @RequestParam(defaultValue = "") String origin,
            @RequestParam(defaultValue = "1") int page, Model model) {
        if (q.length() > 60) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Búsqueda demasiado larga.");
        List<Shipment> results = shipments.search(q, status, origin);
        int pages = Math.max(1, (results.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.max(1, Math.min(page, pages));
        int start = (current - 1) * PAGE_SIZE;
        model.addAttribute("shipments", results.subList(start, Math.min(start + PAGE_SIZE, results.size())));
        model.addAttribute("page", current);
        model.addAttribute("pages", pages);
        model.addAttribute("resultCount", results.size());
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedOrigin", origin);
        List<Shipment> all = shipments.search("", null, "");
        model.addAttribute("stats", shipments.statistics(all));
        model.addAttribute("total", all.size());
        model.addAttribute("agencyCounts", shipments.agencies().stream()
                .map(a -> new AgencyCount(a, all.stream().filter(s -> s.origin().equals(a)).count())).toList());
        return "admin";
    }

    @GetMapping("/cliente")
    public String client(HttpSession session, Model model) {
        long clientId = clientId(session);
        List<Shipment> list = shipments.forClient(clientId);
        model.addAttribute("client", shipments.client(clientId));
        model.addAttribute("shipments", list);
        model.addAttribute("stats", shipments.statistics(list));
        model.addAttribute("total", list.size());
        return "client";
    }

    @GetMapping("/admin/envios/nuevo")
    public String newAdmin(@ModelAttribute("shipmentForm") ShipmentForm form, Model model) {
        return formView(form, true, null, model);
    }

    @GetMapping("/cliente/envios/nuevo")
    public String newClient(HttpSession session, Model model) {
        ShipmentForm form = new ShipmentForm();
        form.setClientId(clientId(session));
        if (session.getAttribute("pendingQuote") instanceof QuoteForm quote) {
            form.setProvince(quote.getProvince());
            form.setDistrict(quote.getDistrict());
            form.setCategory(quote.getCategory());
            form.setWeight(quote.getWeight());
            form.setQuantity(quote.getQuantity());
        }
        model.addAttribute("shipmentForm", form);
        return formView(form, false, null, model);
    }

    @PostMapping("/admin/envios")
    public String createAdmin(@Valid @ModelAttribute("shipmentForm") ShipmentForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        return save(form, errors, true, null, model, redirect);
    }

    @PostMapping("/cliente/envios")
    public String createClient(@Valid @ModelAttribute("shipmentForm") ShipmentForm form, BindingResult errors,
            HttpSession session, Model model, RedirectAttributes redirect) {
        form.setClientId(clientId(session));
        String result = save(form, errors, false, null, model, redirect);
        if (result.startsWith("redirect:")) session.removeAttribute("pendingQuote");
        return result;
    }

    @GetMapping("/admin/envios/{id}/editar")
    public String edit(@PathVariable long id, Model model) {
        Shipment shipment = shipments.get(id);
        if (shipment.status() == ShipmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El envío está anulado.");
        }
        ShipmentForm form = ShipmentForm.from(shipment);
        model.addAttribute("shipmentForm", form);
        return formView(form, true, id, model);
    }

    @PostMapping("/admin/envios/{id}")
    public String update(@PathVariable long id, @Valid @ModelAttribute("shipmentForm") ShipmentForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        shipments.get(id);
        return save(form, errors, true, id, model, redirect);
    }

    private String save(ShipmentForm form, BindingResult errors, boolean admin, Long id,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                Shipment shipment = id == null ? shipments.create(form) : shipments.update(id, form);
                redirect.addFlashAttribute("message", "Envío " + shipment.guide()
                        + (id == null ? " registrado correctamente." : " actualizado correctamente."));
                return "redirect:" + (admin ? "/admin" : "/cliente");
            } catch (IllegalArgumentException ex) {
                errors.reject("shipment.invalid", ex.getMessage());
            }
        }
        return formView(form, admin, id, model);
    }

    private String formView(ShipmentForm form, boolean admin, Long id, Model model) {
        model.addAttribute("adminForm", admin);
        model.addAttribute("editing", id != null);
        model.addAttribute("formAction", admin ? (id == null ? "/admin/envios" : "/admin/envios/" + id) : "/cliente/envios");
        model.addAttribute("clients", shipments.clients());
        model.addAttribute("districts", quotes.destinations().stream()
                .filter(d -> d.code().equals(form.getProvince())).findFirst()
                .map(QuoteService.Destination::districts).orElse(List.of()));
        return "shipment-form";
    }

    @GetMapping("/admin/envios/{id}")
    public String detail(@PathVariable long id, Model model) {
        Shipment shipment = shipments.get(id);
        model.addAttribute("shipment", shipment);
        model.addAttribute("client", shipments.client(shipment.clientId()));
        return "shipment-detail";
    }

    @PostMapping("/admin/envios/{id}/estado")
    public String status(@PathVariable long id, @RequestParam ShipmentStatus status,
            @RequestParam(defaultValue = "") String observation, RedirectAttributes redirect) {
        try {
            shipments.changeStatus(id, status, observation);
            redirect.addFlashAttribute("message", "Estado actualizado. El cambio ya está disponible en rastreo.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/envios/" + id;
    }

    @PostMapping("/admin/envios/{id}/anular")
    public String cancel(@PathVariable long id, RedirectAttributes redirect) {
        try {
            shipments.changeStatus(id, ShipmentStatus.CANCELLED, "Envío anulado desde el panel administrativo.");
            redirect.addFlashAttribute("message", "Envío anulado. La guía y el historial se conservan.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin";
    }

    private long clientId(HttpSession session) {
        return "client2".equals(session.getAttribute("profile")) ? 2L : 1L;
    }

    public record AgencyCount(String name, long count) { }
}
