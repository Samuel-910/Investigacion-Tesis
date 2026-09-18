package com.pe.articulos.modules.clinica.repository;

import com.pe.articulos.modules.clinica.entity.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {
    Optional<Clinica> findByRuc(String ruc);
}
