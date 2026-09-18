package com.pe.articulos.modules.aprobaciones.repository;

import com.pe.articulos.modules.aprobaciones.entity.SolicitudesAnulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudAnulacionRepository extends JpaRepository<SolicitudesAnulacion, Long> {
    List<SolicitudesAnulacion> findByEstado(SolicitudesAnulacion.EstadoSolicitud estado);

    Optional<SolicitudesAnulacion> findByTipoAndReferenciaIdAndEstado(
            SolicitudesAnulacion.TipoSolicitud tipo,
            Long referenciaId,
            SolicitudesAnulacion.EstadoSolicitud estado);
}
