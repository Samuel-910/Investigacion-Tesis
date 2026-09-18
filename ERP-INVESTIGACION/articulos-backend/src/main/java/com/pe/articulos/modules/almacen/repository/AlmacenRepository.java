package com.pe.articulos.modules.almacen.repository;

import com.pe.articulos.modules.almacen.entity.Almacen;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.pe.articulos.core.enums.EstadoGeneral;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
    List<Almacen> findBySucursalIdSucursal(Long idSucursal);

    Page<Almacen> findBySucursalIdSucursal(Long idSucursal, Pageable pageable);

    long countBySucursal(Sucursal sucursal);

    Optional<Almacen> findFirstByEsPrincipalTrueAndEstado(EstadoGeneral estado);

    Optional<Almacen> findFirstBySucursal(Sucursal sucursal);

    Optional<Almacen> findFirstBySucursalIdSucursal(Long idSucursal);
}
