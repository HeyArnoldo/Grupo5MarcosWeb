package pe.edu.utp.Paway.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.utp.Paway.dto.ComplaintForm;

@Service
public class ComplaintService {
    private static final List<String> FIELDS = List.of("name", "document", "address", "email", "phone",
            "minor", "guardian", "guardianDocument", "guardianContact", "type", "itemType", "amount",
            "guide", "description", "detail", "request", "responseChannel");
    private final Path directory;

    public ComplaintService(@Value("${paway.complaints.directory:./data/complaints}") String directory) {
        this.directory = Path.of(directory);
    }

    public record Sheet(String token, String number, String date, Map<String, String> details,
            String response, String responseDate, String delivery) {}

    public synchronized Sheet register(ComplaintForm form, String token) throws IOException {
        Path file = path(token);
        if (Files.exists(file)) return get(token);
        Files.createDirectories(directory);
        long next = all().stream().mapToLong(s -> Long.parseLong(s.number().substring(7))).max().orElse(0) + 1;
        Properties values = new Properties();
        values.setProperty("number", "PW-WEB-" + String.format("%06d", next));
        values.setProperty("date", now());
        BeanWrapperImpl bean = new BeanWrapperImpl(form);
        for (String field : FIELDS) values.setProperty(field, String.valueOf(bean.getPropertyValue(field)).strip());
        save(file, values);
        return get(token);
    }

    public synchronized Sheet get(String token) throws IOException {
        Properties values = load(path(token));
        Map<String, String> details = FIELDS.stream().collect(Collectors.toUnmodifiableMap(f -> f, f -> values.getProperty(f, "")));
        return new Sheet(token, values.getProperty("number"), values.getProperty("date"), details,
                values.getProperty("response", ""), values.getProperty("responseDate", ""), values.getProperty("delivery", ""));
    }

    public synchronized List<Sheet> all() throws IOException {
        if (!Files.exists(directory)) return List.of();
        try (var files = Files.list(directory)) {
            List<Path> paths = files.filter(p -> p.getFileName().toString().endsWith(".xml")).toList();
            var sheets = new java.util.ArrayList<Sheet>();
            for (Path file : paths) sheets.add(get(file.getFileName().toString().replace(".xml", "")));
            return sheets.stream().sorted(Comparator.comparing(Sheet::number).reversed()).toList();
        }
    }

    public synchronized void respond(String token, String response, String delivery) throws IOException {
        Path file = path(token);
        Properties values = load(file);
        values.setProperty("response", response.strip());
        values.setProperty("delivery", delivery.strip());
        values.setProperty("responseDate", now());
        save(file, values);
    }

    private Path path(String token) { return directory.resolve(UUID.fromString(token) + ".xml"); }
    private String now() {
        return ZonedDateTime.now(ZoneId.of("America/Lima")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss ' (Perú)'"));
    }
    private Properties load(Path file) throws IOException {
        Properties values = new Properties();
        try (var input = Files.newInputStream(file)) { values.loadFromXML(input); }
        return values;
    }
    private void save(Path file, Properties values) throws IOException {
        Path temporary = Files.createTempFile(directory, "sheet-", ".tmp");
        try {
            try (var output = Files.newOutputStream(temporary)) { values.storeToXML(output, "Paway - Hoja de reclamación", "UTF-8"); }
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
    }
}
