package com.pe.articulos.core.shared.notificaciones;

import com.pe.articulos.core.shared.notificaciones.NotificacionPendiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionPendienteRepository extends JpaRepository<NotificacionPendiente, Long> {
    List<NotificacionPendiente> findBySucursalIdAndEstadoOrderByFechaCreacionDesc(Long sucursalId, String estado);
    Optional<NotificacionPendiente> findByReferenciaIdAndTituloAndEstado(Long referenciaId, String titulo, String estado);
}
