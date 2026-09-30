package pe.edu.utp.Paway.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplaintForm {
    @NotBlank(message = "Ingresa tu nombre completo.") @Size(max = 150)
    private String name = "";
    @NotBlank(message = "Ingresa tu documento.")
    @Pattern(regexp = "[A-Za-z0-9-]{6,20}", message = "Ingresa un DNI, CE o pasaporte válido (6 a 20 caracteres).")
    private String document = "";
    @NotBlank(message = "Ingresa tu domicilio.") @Size(max = 250)
    private String address = "";
    @NotBlank(message = "Ingresa tu correo.") @Email(message = "Ingresa un correo válido.") @Size(max = 150)
    private String email = "";
    @NotBlank(message = "Ingresa tu teléfono.")
    @Pattern(regexp = "[+0-9 ()-]{7,25}", message = "Ingresa un teléfono válido.")
    private String phone = "";
    private boolean minor;
    @Size(max = 150) private String guardian = "";
    @Size(max = 20) private String guardianDocument = "";
    @Size(max = 250) private String guardianContact = "";
    @Pattern(regexp = "RECLAMO|QUEJA", message = "Selecciona reclamo o queja.")
    private String type = "RECLAMO";
    @Pattern(regexp = "SERVICIO|PRODUCTO", message = "Selecciona producto o servicio.")
    private String itemType = "SERVICIO";
    @Pattern(regexp = "(?:[0-9]{1,8}(?:\\.[0-9]{1,2})?)?", message = "Ingresa un monto en soles válido, sin signo negativo.")
    private String amount = "";
    @Size(max = 40) private String guide = "";
    @NotBlank(message = "Describe el producto o servicio.") @Size(max = 1000)
    private String description = "";
    @NotBlank(message = "Cuéntanos lo ocurrido.") @Size(max = 4000)
    private String detail = "";
    @NotBlank(message = "Indica qué solución solicitas.") @Size(max = 2000)
    private String request = "";
    @Pattern(regexp = "EMAIL|DOMICILIO", message = "Selecciona el medio de respuesta.")
    private String responseChannel = "EMAIL";

    @AssertTrue(message = "Si eres menor, completa el nombre, documento y contacto de tu padre, madre o representante.")
    public boolean isGuardianComplete() {
        return !minor || (guardian != null && !guardian.isBlank()
                && guardianDocument != null && guardianDocument.matches("[A-Za-z0-9-]{6,20}")
                && guardianContact != null && !guardianContact.isBlank());
    }
}
