package com.pe.articulos.modules.accesos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.accesos.entity.AccesoMain;

@Repository
public interface AccesoMainRepository extends JpaRepository<AccesoMain, Long> {
    java.util.Optional<AccesoMain> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
