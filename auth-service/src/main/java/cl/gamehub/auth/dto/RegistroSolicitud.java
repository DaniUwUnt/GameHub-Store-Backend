package cl.gamehub.auth.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.PastOrPresent;
import cl.gamehub.comun.validacion.Identidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Formulario de registro. Nombre, correo y contraseña quedan en auth-service; teléfono, región, comuna y
 * dirección van al perfil en user-service.
 */
public record RegistroSolicitud(
        @Schema(example = "Rocío Navarro")
        @NotBlank(message = "Escribe tu nombre.")
        @Size(max = 50, message = "El nombre tiene hasta 50 caracteres.") String nombre,
        @Schema(example = "rocio.levelup@duoc.cl")
        @NotBlank(message = "Escribe tu correo.")
        @Size(max = 100) @Pattern(regexp = Identidad.CORREO, message = "Escribe un correo válido.") @Email(message = "Escribe un correo válido.") String email,
        @NotBlank(message = "Escribe una contraseña.")
        @Size(min = 8, max = 10, message = "La contraseña debe tener entre 8 y 10 caracteres.")
        @Pattern(regexp = ".*\\d.*", message = "La contraseña debe incluir al menos un número.") String clave,
        @Schema(example = "987654321")
        @NotBlank(message = "Escribe tu teléfono.")
        @Pattern(regexp = "^9\\d{8}$", message = "El teléfono debe tener 9 dígitos y empezar con 9.") String telefono,
        @Schema(example = "Región Metropolitana de Santiago")
        @NotBlank(message = "Elige una región.") String region,
        @Schema(example = "Ñuñoa")
        @NotBlank(message = "Elige una comuna.") String comuna,
        @Schema(example = "Av. Irarrázaval 3400, depto. 1203")
        @NotBlank(message = "Escribe tu dirección de despacho.")
        @Size(min = 5, max = 300, message = "La dirección debe tener entre 5 y 300 caracteres.") String direccion,
        @NotBlank(message = "Escribe tu RUN.") @Pattern(regexp = "[1-9][0-9]{5,7}[0-9kK]", message = "Escribe tu RUN sin puntos ni guion.") String run,
        @NotBlank(message = "Escribe tus apellidos.") @Size(max = 100) String apellidos,
        @PastOrPresent(message = "La fecha no puede estar en el futuro.") LocalDate nacimiento) {
}
