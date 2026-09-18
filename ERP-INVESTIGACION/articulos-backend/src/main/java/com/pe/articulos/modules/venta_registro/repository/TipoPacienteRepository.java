package com.pe.articulos.modules.venta_registro.repository;

import com.pe.articulos.modules.venta_registro.entity.TipoPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoPacienteRepository extends JpaRepository<TipoPaciente, Integer> {
}

