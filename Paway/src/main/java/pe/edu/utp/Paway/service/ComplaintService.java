package pe.edu.utp.Paway.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.utp.Paway.dto.ComplaintForm;
import pe.edu.utp.Paway.entity.ComplaintEntity;
import pe.edu.utp.Paway.repository.ComplaintRepository;

/**
 * Libro de Reclamaciones stored in PostgreSQL (table {@code complaint}). The printable receipt
 * is still the HTML view {@code complaint-receipt} ("Imprimir / guardar PDF" in the browser),
 * so no files are written by the application anymore.
 */
@Service
@Transactional(readOnly = true)
public class ComplaintService {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss ' (Perú)'");

    private final ComplaintRepository complaints;

    public ComplaintService(ComplaintRepository complaints) {
        this.complaints = complaints;
    }

    /** View of a sheet; {@code details} keeps the form field names used by the templates. */
    public record Sheet(String token, String number, String date, Map<String, String> details,
            String response, String responseDate, String delivery) {}

    /**
     * Registers the sheet once per submission token. A repeated submit (double click, refresh)
     * returns the existing sheet; the unique constraint on {@code token} settles concurrent ones.
     * Not transactional on purpose: each repository call runs in its own transaction, so a
     * duplicate-token failure can be recovered by reading the sheet that won.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Sheet register(ComplaintForm form, String token) {
        UUID id = parse(token);
        var existing = complaints.findByToken(id);
        if (existing.isPresent()) return toView(existing.get());
        ComplaintEntity sheet = new ComplaintEntity(id, "PW-WEB-%06d".formatted(complaints.nextSheetNumber()),
                Instant.now());
        sheet.fill(strip(form.getName()), strip(form.getDocument()), strip(form.getAddress()), strip(form.getEmail()),
                strip(form.getPhone()), form.isMinor(), strip(form.getGuardian()), strip(form.getGuardianDocument()),
                strip(form.getGuardianContact()));
        String amount = strip(form.getAmount());
        sheet.describe(strip(form.getType()), strip(form.getItemType()), amount.isEmpty() ? null : new BigDecimal(amount).setScale(2),
                strip(form.getGuide()), strip(form.getDescription()), strip(form.getDetail()), strip(form.getRequest()),
                strip(form.getResponseChannel()));
        try {
            return toView(complaints.saveAndFlush(sheet));
        } catch (DataIntegrityViolationException duplicate) {
            return complaints.findByToken(id).map(this::toView).orElseThrow(() -> duplicate);
        }
    }

    public Sheet get(String token) {
        return toView(entity(token));
    }

    public List<Sheet> all() {
        return complaints.findAllByOrderBySheetNumberDesc().stream().map(this::toView).toList();
    }

    @Transactional
    public void respond(String token, String response, String delivery) {
        entity(token).respond(response.strip(), delivery.strip(), Instant.now());
    }

    private ComplaintEntity entity(String token) {
        return complaints.findByToken(parse(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La hoja no existe."));
    }

    private static UUID parse(String token) {
        try {
            return UUID.fromString(token);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La hoja no existe.");
        }
    }

    private Sheet toView(ComplaintEntity c) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("name", c.getConsumerName());
        details.put("document", c.getConsumerDocument());
        details.put("address", c.getAddress());
        details.put("email", c.getEmail());
        details.put("phone", c.getPhone());
        details.put("minor", String.valueOf(c.isMinor()));
        details.put("guardian", c.getGuardianName());
        details.put("guardianDocument", c.getGuardianDocument());
        details.put("guardianContact", c.getGuardianContact());
        details.put("type", c.getComplaintType());
        details.put("itemType", c.getItemType());
        details.put("amount", c.getAmount() == null ? "" : c.getAmount().toPlainString());
        details.put("guide", c.getGuide());
        details.put("description", c.getDescription());
        details.put("detail", c.getDetail());
        details.put("request", c.getRequest());
        details.put("responseChannel", c.getResponseChannel());
        return new Sheet(c.getToken().toString(), c.getSheetNumber(), format(c.getCreatedAt()), Map.copyOf(details),
                c.getResponse() == null ? "" : c.getResponse(), format(c.getRespondedAt()),
                c.getResponseDelivery() == null ? "" : c.getResponseDelivery());
    }

    private static String format(Instant instant) {
        return instant == null ? "" : instant.atZone(LIMA).format(DATE);
    }

    private static String strip(String value) {
        return value == null ? "" : value.strip();
    }
}
