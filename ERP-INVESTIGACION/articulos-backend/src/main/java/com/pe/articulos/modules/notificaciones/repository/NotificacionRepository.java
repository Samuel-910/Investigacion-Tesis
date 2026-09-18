package com.pe.articulos.modules.notificaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.notificaciones.entity.Notificacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    List<Notificacion> findByLeidoFalseOrderByIdNotificacionDesc();

    List<Notificacion> findByLeidoFalseAndIdSucursalOrderByIdNotificacionDesc(Long idSucursal);

    Optional<Notificacion> findByTipoAndReferenciaId(String tipo, String referenciaId);
}
