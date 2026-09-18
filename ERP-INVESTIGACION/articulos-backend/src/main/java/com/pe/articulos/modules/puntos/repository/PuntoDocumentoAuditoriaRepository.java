package com.pe.articulos.modules.puntos.repository;

import com.pe.articulos.modules.puntos.entity.PuntoDocumentoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PuntoDocumentoAuditoriaRepository extends JpaRepository<PuntoDocumentoAuditoria, Long> {
    List<PuntoDocumentoAuditoria> findByIdPuntoDocOrderByFechaCambioDesc(Long idPuntoDoc);
}
