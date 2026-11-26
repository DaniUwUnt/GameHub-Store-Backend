package cl.gamehub.usuarios.contacto;

import cl.gamehub.comun.validacion.Identidad;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Contacto")
public class ContactoController {
    public record Solicitud(@NotBlank @Size(max=100) String nombre,
                            @Size(max=100) @Pattern(regexp=Identidad.CORREO) String correo,
                            @NotBlank @Size(max=500) String comentario) {}
    public record Acuse(Long id) {}
    private final ConsultaRepository consultas;
    public ContactoController(ConsultaRepository consultas) { this.consultas = consultas; }

    @PostMapping("/contacto")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una consulta; devuelve únicamente su número")
    public Acuse recibir(@Valid @RequestBody Solicitud s) {
        Consulta consulta = consultas.save(new Consulta(s.nombre(), s.correo(), s.comentario()));
        return new Acuse(consulta.getId());
    }

    @GetMapping("/admin/contacto")
    @Operation(summary = "Lista las últimas 100 consultas, de más nueva a más antigua (ADMIN)")
    public List<Consulta> listar() { return consultas.findTop100ByOrderByIdDesc(); }
}
