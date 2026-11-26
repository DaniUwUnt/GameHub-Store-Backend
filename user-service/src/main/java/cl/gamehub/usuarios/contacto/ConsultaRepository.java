package cl.gamehub.usuarios.contacto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findTop100ByOrderByIdDesc();
}
