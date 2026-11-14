package cl.musicmania.api.repository;

import cl.musicmania.api.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, String> {
    List<Orden> findAllByOrderByFechaDesc();
    List<Orden> findByIdUsuarioOrderByFechaDesc(Long idUsuario);
}
