package com.pe.articulos.modules.finanzas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlujoAprobacionRepository extends JpaRepository<FlujoAprobacion, Long> {
    List<FlujoAprobacion> findByEstado(EstadoAprobacion estado);
}
