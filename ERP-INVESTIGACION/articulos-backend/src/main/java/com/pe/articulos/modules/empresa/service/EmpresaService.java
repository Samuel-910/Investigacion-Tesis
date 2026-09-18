package com.pe.articulos.modules.empresa.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pe.articulos.modules.empresa.dto.EmpresaDTO;
import com.pe.articulos.modules.empresa.dto.EmpresaPersonaVinculoDTO;
import com.pe.articulos.modules.empresa.dto.VinculoDTO;

public interface EmpresaService {
    EmpresaPersonaVinculoDTO crearVinculo(VinculoDTO vinculoDTO);

    EmpresaDTO create(EmpresaDTO empresaDTO);

    // CRUD Empresa
    List<EmpresaDTO> getAll();

    Page<EmpresaDTO> getAll(Pageable pageable);

    Page<EmpresaDTO> search(String q, Pageable pageable);

    EmpresaDTO getById(Long id);

    EmpresaDTO update(Long id, EmpresaDTO empresaDTO);

    void delete(Long id);

    // Relación
    List<EmpresaPersonaVinculoDTO> getVinculosByEmpresa(Long idEmpresa);
}
