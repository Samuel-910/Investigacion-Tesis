package com.pe.articulos.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColaEventoRepository extends JpaRepository<ColaEvento, Long> {
    List<ColaEvento> findByEstadoOrderByFechaCreacionAsc(String estado);
}
