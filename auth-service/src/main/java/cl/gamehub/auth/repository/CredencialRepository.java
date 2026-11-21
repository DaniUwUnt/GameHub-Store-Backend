package cl.gamehub.auth.repository;

import cl.gamehub.auth.model.Credencial;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {

    Optional<Credencial> findByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Credencial c where c.id = :id")
    Optional<Credencial> bloquearPorId(@Param("id") Long id);

    boolean existsByEmailIgnoreCase(String email);

    /** Para el panel de administración: por rol (ADMIN, CLIENTE, VENDEDOR) y, dentro de cada rol, por nombre. */
    List<Credencial> findAllByOrderByRolAscNombreAsc();
}
