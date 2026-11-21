package cl.gamehub.ordenes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import jakarta.validation.constraints.Size;

/** El carrito vive en el navegador; aquí solo se revisa contra el stock, los precios y el cupón. */
public record ValidarCarritoSolicitud(
        @NotNull(message = "Faltan las líneas del carrito.") @Size(max = 100) @Valid List<@NotNull LineaSolicitud> lineas,
        @Schema(example = "GAME10") @Size(max = 30) String cupon) {
}
