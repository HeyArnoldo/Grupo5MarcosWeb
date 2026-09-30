package pe.edu.utp.Paway.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.utp.Paway.dto.ShipmentForm;
import pe.edu.utp.Paway.model.Client;
import pe.edu.utp.Paway.model.Shipment;
import pe.edu.utp.Paway.model.ShipmentStatus;

@Service
public class ShipmentService {
    private final QuoteService quotes;
    private final Map<Long, Shipment> shipments = new LinkedHashMap<>();
    private long sequence;
    private final List<Client> clients = List.of(
            new Client(1, "Brayan Lopez", "12345678", "brayan@example.com", "931989909"),
            new Client(2, "María Quispe", "70123456", "maria@example.com", "987654321"));
    private final Map<String, String> agencyDistricts = new LinkedHashMap<>();

    public ShipmentService(QuoteService quotes) {
        this.quotes = quotes;
        agencyDistricts.put("Lima Centro", "Lima Centro");
        agencyDistricts.put("Huaral Centro", "Huaral");
        agencyDistricts.put("Matucana", "Matucana");
        agencyDistricts.put("San Vicente", "San Vicente");
        seed("Carlos Mendoza", "45678912", "huaral", "Chancay", 1, ShipmentStatus.TRANSIT);
        seed("Lucía Ramos", "71234567", "canete", "Asia", 1, ShipmentStatus.DELIVERED);
        seed("Diego Salas", "40987654", "huarochiri", "Matucana", 2, ShipmentStatus.INCIDENT);
        seed("Ana Flores", "72345678", "lima", "Surco", 1, ShipmentStatus.REGISTERED);
    }

    private void seed(String recipient, String document, String province, String district,
            long clientId, ShipmentStatus status) {
        ShipmentForm form = new ShipmentForm();
        form.setRecipient(recipient);
        form.setDocument(document);
        form.setProvince(province);
        form.setDistrict(district);
        form.setClientId(clientId);
        Shipment shipment = create(form);
        if (status != ShipmentStatus.REGISTERED) {
            changeStatus(shipment.id(), status, "Estado inicial de demostración.");
        }
    }

    public List<Client> clients() { return clients; }
    public List<String> agencies() { return List.copyOf(agencyDistricts.keySet()); }

    public Client client(long id) {
        return clients.stream().filter(c -> c.id() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));
    }

    public synchronized Shipment get(long id) {
        Shipment shipment = shipments.get(id);
        if (shipment == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El envío no existe.");
        return shipment;
    }

    public synchronized Optional<Shipment> track(String guide) {
        String normalized = guide.strip().toUpperCase(Locale.ROOT);
        return shipments.values().stream().filter(s -> s.guide().equals(normalized)).findFirst();
    }

    public synchronized List<Shipment> search(String query, ShipmentStatus status, String origin) {
        String text = query.strip().toLowerCase(Locale.ROOT);
        return shipments.values().stream()
                .filter(s -> text.isEmpty() || s.guide().toLowerCase(Locale.ROOT).contains(text)
                        || s.recipient().toLowerCase(Locale.ROOT).contains(text))
                .filter(s -> status == null || s.status() == status)
                .filter(s -> origin.isBlank() || s.origin().equals(origin))
                .sorted(Comparator.comparingLong(Shipment::id).reversed()).toList();
    }

    public synchronized List<Shipment> forClient(long id) {
        return search("", null, "").stream().filter(s -> s.clientId() == id).toList();
    }

    public synchronized Shipment create(ShipmentForm form) {
        validate(form);
        long id = ++sequence;
        String guide = "PW-%d-%04d".formatted(Year.now().getValue(), id);
        LocalDateTime now = LocalDateTime.now();
        Shipment shipment = build(id, guide, form, ShipmentStatus.REGISTERED, now,
                List.of(new Shipment.Event(ShipmentStatus.REGISTERED, now, form.getObservation().strip())));
        shipments.put(id, shipment);
        return shipment;
    }

    public synchronized Shipment update(long id, ShipmentForm form) {
        Shipment existing = get(id);
        requireActive(existing);
        validate(form);
        Shipment updated = build(id, existing.guide(), form, existing.status(), existing.createdAt(), existing.events());
        shipments.put(id, updated);
        return updated;
    }

    public synchronized void changeStatus(long id, ShipmentStatus status, String observation) {
        Shipment existing = get(id);
        requireActive(existing);
        if (status == null || observation == null || observation.length() > 500) {
            throw new IllegalArgumentException("Revisa el estado y la observación (máximo 500 caracteres).");
        }
        if (existing.status() == status) return;
        List<Shipment.Event> events = new ArrayList<>(existing.events());
        events.add(new Shipment.Event(status, LocalDateTime.now(), observation.strip()));
        shipments.put(id, new Shipment(existing.id(), existing.guide(), existing.clientId(), existing.recipient(),
                existing.document(), existing.origin(), existing.province(), existing.district(), existing.category(),
                existing.weight(), existing.quantity(), existing.total(), status, observation.strip(),
                existing.createdAt(), events));
    }

    private void requireActive(Shipment shipment) {
        if (shipment.status() == ShipmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Un envío anulado conserva su historial y no se puede modificar.");
        }
    }

    private void validate(ShipmentForm form) {
        client(form.getClientId());
        if (!agencyDistricts.containsKey(form.getOrigin())) throw new IllegalArgumentException("La agencia no existe.");
        if (agencyDistricts.get(form.getOrigin()).equals(form.getDistrict())) {
            throw new IllegalArgumentException("El destino debe ser distinto de la agencia de origen.");
        }
        if (form.getRecipient() == null || form.getRecipient().isBlank() || form.getRecipient().length() > 100
                || form.getDocument() == null || !form.getDocument().matches("[0-9]{8}")
                || form.getObservation() == null || form.getObservation().length() > 500) {
            throw new IllegalArgumentException("Revisa los datos del destinatario.");
        }
        quotes.calculate(form);
    }

    private Shipment build(long id, String guide, ShipmentForm form, ShipmentStatus status,
            LocalDateTime createdAt, List<Shipment.Event> events) {
        return new Shipment(id, guide, form.getClientId(), form.getRecipient().strip(), form.getDocument(),
                form.getOrigin(), form.getProvince(), form.getDistrict(), form.getCategory(), form.getWeight(),
                form.getQuantity(), quotes.calculate(form).total(), status, form.getObservation().strip(), createdAt, events);
    }

    public Map<ShipmentStatus, Long> statistics(List<Shipment> list) {
        Map<ShipmentStatus, Long> counts = new LinkedHashMap<>();
        for (ShipmentStatus status : ShipmentStatus.values()) {
            counts.put(status, list.stream().filter(s -> s.status() == status).count());
        }
        return counts;
    }
}
