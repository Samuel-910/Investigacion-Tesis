package com.pe.articulos.modules.caja_chica.repository;

import com.pe.articulos.modules.caja_chica.entity.CajaChica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CajaChicaRepository extends JpaRepository<CajaChica, Long> {
    Optional<CajaChica> findByNombreAndIdSucursal(String nombre, Long idSucursal);

    Optional<CajaChica> findTopByEstadoAndIdPuntoVentaAndIdUsuarioCajeroOrderByIdDesc(
            CajaChica.EstadoCaja estado, Long idPuntoVenta, String idUsuarioCajero);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM CajaChica c WHERE c.idSucursal = :idSucursal AND c.estado = 'CERRADA' AND YEAR(c.fechaCierre) = :anio AND MONTH(c.fechaCierre) = :mes")
    java.util.List<CajaChica> findCajasArqueadas(
            @org.springframework.data.repository.query.Param("idSucursal") Long idSucursal,
            @org.springframework.data.repository.query.Param("mes") Integer mes,
            @org.springframework.data.repository.query.Param("anio") Integer anio);
}
