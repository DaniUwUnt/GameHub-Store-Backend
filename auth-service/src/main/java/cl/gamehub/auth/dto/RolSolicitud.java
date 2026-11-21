package cl.gamehub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RolSolicitud(
        @NotBlank(message = "Selecciona un rol.")
        @Pattern(regexp = "CLIENTE|VENDEDOR|ADMIN", message = "El rol no es válido.")
        String rol) {
}
