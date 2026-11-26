package cl.gamehub.auth.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import cl.gamehub.comun.validacion.Identidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginSolicitud(
        @Schema(example = "camila.levelup@duoc.cl")
        @NotBlank(message = "Escribe tu correo.")
        @Size(max = 100) @Pattern(regexp = Identidad.CORREO, message = "Escribe un correo válido.") @Email(message = "Escribe un correo válido.") String email,
        @NotBlank(message = "Escribe tu contraseña.") @Size(min = 4, max = 10, message = "Usa entre 4 y 10 caracteres.") String clave) {
}
