package pe.edu.utp.Paway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pe.edu.utp.Paway.model.Shipment;

@Getter
@Setter
public class ShipmentForm extends QuoteForm {
    @Positive(message = "Selecciona un cliente.")
    private long clientId = 1;
    @NotBlank(message = "Ingresa el nombre del destinatario.")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
    private String recipient = "";
    @NotNull
    @Pattern(regexp = "[0-9]{8}", message = "El DNI debe tener 8 dígitos.")
    private String document = "";
    @NotBlank(message = "Selecciona la agencia de origen.")
    private String origin = "Lima Centro";
    @NotNull
    @Size(max = 500, message = "La observación admite hasta 500 caracteres.")
    private String observation = "";

    public static ShipmentForm from(Shipment shipment) {
        ShipmentForm form = new ShipmentForm();
        form.setClientId(shipment.clientId());
        form.setRecipient(shipment.recipient());
        form.setDocument(shipment.document());
        form.setOrigin(shipment.origin());
        form.setProvince(shipment.province());
        form.setDistrict(shipment.district());
        form.setCategory(shipment.category());
        form.setWeight(shipment.weight());
        form.setQuantity(shipment.quantity());
        form.setObservation(shipment.observation());
        return form;
    }
}
