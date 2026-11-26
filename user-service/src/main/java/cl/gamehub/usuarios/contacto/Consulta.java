package cl.gamehub.usuarios.contacto;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas_contacto")
public class Consulta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(length = 100) private String correo;
    @Column(nullable = false, length = 500) private String comentario;
    @Column(nullable = false) private LocalDateTime creada;
    protected Consulta() { }
    Consulta(String nombre, String correo, String comentario) {
        this.nombre = nombre.trim();
        this.correo = correo == null || correo.isBlank() ? null : correo.trim();
        this.comentario = comentario.trim();
        this.creada = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getComentario() { return comentario; }
    public LocalDateTime getCreada() { return creada; }
}
