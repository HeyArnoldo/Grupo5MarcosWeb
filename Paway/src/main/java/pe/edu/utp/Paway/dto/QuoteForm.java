package pe.edu.utp.Paway.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuoteForm {
    @NotBlank(message = "Selecciona una provincia.")
    private String province = "";
    @NotBlank(message = "Selecciona un distrito.")
    private String district = "";
    @NotBlank(message = "Selecciona una categoría.")
    private String category = "textil";
    @NotNull(message = "Ingresa el peso.")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor que cero.")
    @DecimalMax(value = "1000", message = "El peso máximo es 1000 kg por paquete.")
    @Digits(integer = 4, fraction = 2, message = "Usa hasta dos decimales para el peso.")
    private BigDecimal weight = BigDecimal.ONE;
    @NotNull(message = "Ingresa la cantidad.")
    @Min(value = 1, message = "Ingresa al menos un paquete.")
    @Max(value = 100, message = "El máximo es 100 paquetes por envío.")
    private Integer quantity = 1;
}
