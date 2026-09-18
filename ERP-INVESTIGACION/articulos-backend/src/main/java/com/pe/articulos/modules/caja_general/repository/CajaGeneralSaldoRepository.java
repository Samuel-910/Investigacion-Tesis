package com.pe.articulos.modules.caja_general.repository;

import com.pe.articulos.modules.caja_general.entity.CajaGeneralSaldo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CajaGeneralSaldoRepository extends JpaRepository<CajaGeneralSaldo, Long> {
    List<CajaGeneralSaldo> findByCajaGeneralId(Long idCajaGeneral);
    Optional<CajaGeneralSaldo> findByCajaGeneralIdAndMetodoPagoId(Long idCajaGeneral, Long idMetodoPago);
}
