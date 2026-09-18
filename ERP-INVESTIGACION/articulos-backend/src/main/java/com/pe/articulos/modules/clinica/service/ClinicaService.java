package com.pe.articulos.modules.clinica.service;

import com.pe.articulos.modules.clinica.entity.Clinica;
import com.pe.articulos.modules.clinica.entity.ClinicaAuditoria;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

public interface ClinicaService {
    Clinica obtenerPrincipal();

    Clinica obtenerPorId(Long id);

    Clinica crear(Clinica clinica);

    Clinica actualizar(Long id, Clinica clinica);

    void eliminar(Long id);

    PageResponse<ClinicaAuditoria> listarHistorial(Long idClinica, Pageable pageable);
}
