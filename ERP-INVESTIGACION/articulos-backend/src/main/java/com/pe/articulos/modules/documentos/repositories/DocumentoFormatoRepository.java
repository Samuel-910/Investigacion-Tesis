package com.pe.articulos.modules.documentos.repositories;

import com.pe.articulos.modules.documentos.entities.DocumentoFormato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DocumentoFormatoRepository extends JpaRepository<DocumentoFormato, Long> {
    Optional<DocumentoFormato> findByNombre(String nombre);
}
