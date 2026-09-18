package com.pe.articulos.modules.productos.repository;

import com.pe.articulos.modules.productos.entity.TransferenciaSucursal;
import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferenciaRepository extends JpaRepository<TransferenciaSucursal, Long> {

    @Query("SELECT t FROM TransferenciaSucursal t " +
            "LEFT JOIN FETCH t.sucursalOrigen " +
            "LEFT JOIN FETCH t.sucursalDestino " +
            "LEFT JOIN FETCH t.usuarioSolicita " +
            "LEFT JOIN FETCH t.usuarioEnvia " +
            "LEFT JOIN FETCH t.usuarioRecibe " +
            "WHERE t.idSucursalOrigen = :idSucursal OR t.idSucursalDestino = :idSucursal " +
            "ORDER BY t.fechaSolicitud DESC")
    List<TransferenciaSucursal> findAllBySucursal(@Param("idSucursal") Long idSucursal);

    List<TransferenciaSucursal> findByIdSucursalDestinoAndEstado(Long idSucursalDestino, EstadoGeneral estado);

    List<TransferenciaSucursal> findByIdSucursalOrigenAndEstado(Long idSucursalOrigen, EstadoGeneral estado);
}
