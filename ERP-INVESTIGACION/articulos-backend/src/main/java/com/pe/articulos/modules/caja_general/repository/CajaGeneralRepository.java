package com.pe.articulos.modules.caja_general.repository;

import com.pe.articulos.modules.caja_general.entity.CajaGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CajaGeneralRepository extends JpaRepository<CajaGeneral, Long> {
    Optional<CajaGeneral> findByIdSucursal(Long idSucursal);
}
