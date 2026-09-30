package pe.edu.utp.Paway.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.utp.Paway.dto.QuoteForm;

@Service
public class QuoteService {
    private final List<Destination> destinations = List.of(
            new Destination("lima", "Lima", new BigDecimal("10.00"),
                    List.of("Ate", "San Borja", "Miraflores", "Lima Centro", "San Isidro", "Lince", "Surco")),
            new Destination("huarochiri", "Huarochirí", new BigDecimal("18.00"),
                    List.of("Matucana", "Antioquía", "San Bartolomé", "San Mateo")),
            new Destination("huaral", "Huaral", new BigDecimal("20.00"),
                    List.of("Huaral", "Chancay", "Aucallama")),
            new Destination("canete", "Cañete", new BigDecimal("22.00"),
                    List.of("San Vicente", "Asia", "Lunahuaná", "Mala")));
    private final List<Category> categories = List.of(
            new Category("textil", "Textil", BigDecimal.ZERO),
            new Category("otros", "Otros", BigDecimal.ZERO),
            new Category("hogar_higiene", "Hogar e higiene", new BigDecimal("2.00")),
            new Category("tecnologia", "Tecnología", new BigDecimal("5.00")),
            new Category("joyeria", "Joyería", new BigDecimal("13.00")));

    public List<Destination> destinations() { return destinations; }
    public List<Category> categories() { return categories; }

    public Destination destination(String code) {
        return destinations.stream().filter(d -> d.code().equals(code)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La provincia no pertenece a la cobertura."));
    }

    public Quote calculate(QuoteForm form) {
        Destination destination = destination(form.getProvince());
        if (!destination.districts().contains(form.getDistrict())) {
            throw new IllegalArgumentException("El distrito no pertenece a la provincia seleccionada.");
        }
        Category category = categories.stream().filter(c -> c.code().equals(form.getCategory())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La categoría no es válida."));
        BigDecimal weight = form.getWeight();
        Integer quantity = form.getQuantity();
        if (weight == null || weight.signum() <= 0 || weight.compareTo(new BigDecimal("1000")) > 0
                || weight.stripTrailingZeros().scale() > 2 || quantity == null || quantity < 1 || quantity > 100) {
            throw new IllegalArgumentException("Revisa el peso y la cantidad de paquetes.");
        }
        BigDecimal overweight = weight.subtract(new BigDecimal("2")).max(BigDecimal.ZERO)
                .multiply(new BigDecimal("3.00"));
        BigDecimal unit = destination.base().add(overweight).add(category.surcharge());
        return new Quote(destination.base(), overweight, category.surcharge(), unit.setScale(2, RoundingMode.HALF_UP),
                unit.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
    }

    public record Destination(String code, String name, BigDecimal base, List<String> districts) { }
    public record Category(String code, String name, BigDecimal surcharge) { }
    public record Quote(BigDecimal base, BigDecimal overweight, BigDecimal surcharge,
            BigDecimal unit, BigDecimal total) { }
}
