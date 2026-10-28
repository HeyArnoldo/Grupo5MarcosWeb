package pe.edu.utp.Paway.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.utp.Paway.dto.ShipmentForm;
import pe.edu.utp.Paway.entity.AgencyEntity;
import pe.edu.utp.Paway.entity.CustomerEntity;
import pe.edu.utp.Paway.entity.ShipmentEntity;
import pe.edu.utp.Paway.model.Client;
import pe.edu.utp.Paway.model.Shipment;
import pe.edu.utp.Paway.model.ShipmentStatus;
import pe.edu.utp.Paway.repository.AgencyRepository;
import pe.edu.utp.Paway.repository.CustomerRepository;
import pe.edu.utp.Paway.repository.ShipmentRepository;
import pe.edu.utp.Paway.repository.ShipmentSpecifications;

import static pe.edu.utp.Paway.repository.ShipmentSpecifications.origin;
import static pe.edu.utp.Paway.repository.ShipmentSpecifications.status;
import static pe.edu.utp.Paway.repository.ShipmentSpecifications.text;

/**
 * Shipment use cases on top of PostgreSQL. Controllers and templates keep receiving the
 * immutable {@link Shipment}/{@link Client} view records; JPA entities never leave this
 * service, so lazy associations are always read inside a transaction (open-in-view is off).
 */
@Service
@Transactional(readOnly = true)
public class ShipmentService {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final QuoteService quotes;
    private final ShipmentRepository shipments;
    private final CustomerRepository customers;
    private final AgencyRepository agencies;

    public ShipmentService(QuoteService quotes, ShipmentRepository shipments, CustomerRepository customers,
            AgencyRepository agencies) {
        this.quotes = quotes;
        this.shipments = shipments;
        this.customers = customers;
        this.agencies = agencies;
    }

    public List<Client> clients() {
        return customers.findAllByOrderByIdAsc().stream().map(ShipmentService::toView).toList();
    }

    public List<String> agencies() {
        return agencies.findAllByOrderByIdAsc().stream().map(AgencyEntity::getName).toList();
    }

    public Client client(long id) {
        return toView(customer(id));
    }

    public Shipment get(long id) {
        return toView(entity(id));
    }

    public Optional<Shipment> track(String guide) {
        return shipments.findByGuide(guide.strip().toUpperCase(Locale.ROOT)).map(this::toView);
    }

    public List<Shipment> search(String query, ShipmentStatus status, String origin) {
        return shipments.findAll(text(query).and(status(status)).and(origin(origin)), Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(this::toView).toList();
    }

    public List<Shipment> forClient(long id) {
        return shipments.findAll(ShipmentSpecifications.customer(id), Sort.by(Sort.Direction.DESC, "id")).stream().map(this::toView).toList();
    }

    @Transactional
    public Shipment create(ShipmentForm form) {
        CustomerEntity customer = customer(form.getClientId());
        AgencyEntity origin = validate(form);
        long id = shipments.nextId();
        String guide = "PW-%d-%04d".formatted(Year.now(LIMA).getValue(), id);
        ShipmentEntity shipment = ShipmentEntity.register(id, guide, customer, origin, details(form),
                quotes.calculate(form).total(), Instant.now());
        return toView(shipments.saveAndFlush(shipment));
    }

    @Transactional
    public Shipment update(long id, ShipmentForm form) {
        ShipmentEntity shipment = entity(id);
        if (form.getVersion() != null && !form.getVersion().equals(shipment.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(ShipmentEntity.class, id);
        }
        if (shipment.isCancelled()) {
            throw new IllegalArgumentException("Un envío anulado conserva su historial y no se puede modificar.");
        }
        CustomerEntity customer = customer(form.getClientId());
        AgencyEntity origin = validate(form);
        shipment.update(customer, origin, details(form), quotes.calculate(form).total(), Instant.now());
        return toView(shipments.saveAndFlush(shipment));
    }

    @Transactional
    public void changeStatus(long id, ShipmentStatus status, String observation) {
        entity(id).changeStatus(status, observation, Instant.now());
    }

    public Map<ShipmentStatus, Long> statistics(List<Shipment> list) {
        Map<ShipmentStatus, Long> counts = new LinkedHashMap<>();
        for (ShipmentStatus status : ShipmentStatus.values()) {
            counts.put(status, list.stream().filter(s -> s.status() == status).count());
        }
        return counts;
    }

    private ShipmentEntity entity(long id) {
        return shipments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El envío no existe."));
    }

    private CustomerEntity customer(long id) {
        return customers.findById(id).orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));
    }

    /** Catalog and business checks that Bean Validation on the form cannot express. */
    private AgencyEntity validate(ShipmentForm form) {
        AgencyEntity origin = agencies.findByName(form.getOrigin() == null ? "" : form.getOrigin())
                .orElseThrow(() -> new IllegalArgumentException("La agencia no existe."));
        if (origin.getDistrict().equals(form.getDistrict())) {
            throw new IllegalArgumentException("El destino debe ser distinto de la agencia de origen.");
        }
        if (form.getRecipient() == null || form.getRecipient().isBlank() || form.getRecipient().length() > 100
                || form.getDocument() == null || !form.getDocument().matches("[0-9]{8}")
                || form.getObservation() == null || form.getObservation().length() > 500) {
            throw new IllegalArgumentException("Revisa los datos del destinatario.");
        }
        quotes.calculate(form);
        return origin;
    }

    private static ShipmentEntity.Details details(ShipmentForm form) {
        return new ShipmentEntity.Details(form.getRecipient(), form.getDocument(), form.getProvince(),
                form.getDistrict(), form.getCategory(), form.getWeight(), form.getQuantity(), form.getObservation());
    }

    private Shipment toView(ShipmentEntity s) {
        List<Shipment.Event> events = s.getEvents().stream()
                .map(e -> new Shipment.Event(e.getStatus(), local(e.getOccurredAt()), e.getObservation())).toList();
        return new Shipment(s.getId(), s.getGuide(), s.getCustomer().getId(), s.getRecipient(),
                s.getRecipientDocument(), s.getOriginAgency().getName(), s.getProvince(), s.getDistrict(),
                s.getCategory(), s.getWeightKg(), s.getQuantity(), s.getTotal(), s.getStatus(), s.getObservation(),
                local(s.getCreatedAt()), events, s.getVersion());
    }

    private static Client toView(CustomerEntity c) {
        return new Client(c.getId(), c.getFullName(), c.getDocument(), c.getEmail(), c.getPhone());
    }

    private static LocalDateTime local(Instant instant) {
        return LocalDateTime.ofInstant(instant, LIMA);
    }
}
