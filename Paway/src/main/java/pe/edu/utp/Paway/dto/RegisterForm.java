package pe.edu.utp.Paway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Sign-up form: creates the customer (sender) profile and its CLIENT account. */
@Getter
@Setter
public class RegisterForm {
    @NotBlank(message = "Ingresa tu nombre completo.")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
    private String fullName = "";
    @NotNull
    @Pattern(regexp = "[0-9]{8}", message = "El DNI debe tener 8 dígitos.")
    private String document = "";
    @NotBlank(message = "Ingresa tu correo electrónico.")
    @Email(message = "Ingresa un correo electrónico válido.")
    @Size(max = 150, message = "El correo admite hasta 150 caracteres.")
    private String email = "";
    @NotNull
    @Pattern(regexp = "[+0-9 ()-]{7,25}", message = "Ingresa un teléfono válido (7 a 25 dígitos).")
    private String phone = "";
    @NotNull
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    private String password = "";
    @NotNull
    private String passwordConfirmation = "";

    @Override
    public String toString() {
        // Never include the password fields in logs or debug output.
        return "RegisterForm[email=" + email + "]";
    }
}
