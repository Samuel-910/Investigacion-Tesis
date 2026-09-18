package com.pe.articulos.modules.empresa.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.modules.empresa.dto.EmpresaDTO;
import com.pe.articulos.modules.empresa.dto.EmpresaMapper;
import com.pe.articulos.modules.empresa.dto.EmpresaPersonaVinculoDTO;
import com.pe.articulos.modules.empresa.dto.EmpresaPersonaVinculoMapper;
import com.pe.articulos.modules.empresa.dto.VinculoDTO;
import com.pe.articulos.modules.empresa.entity.Empresa;
import com.pe.articulos.modules.empresa.entity.EmpresaPersonaVinculo;
import com.pe.articulos.modules.empresa.repository.EmpresaPersonaVinculoRepository;
import com.pe.articulos.modules.empresa.repository.EmpresaRepository;
import com.pe.articulos.modules.empresa.service.EmpresaService;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.core.enums.EstadoGeneral;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final EmpresaPersonaVinculoRepository vinculoRepository;
    private final com.pe.articulos.core.reports.audit.service.AuditService auditService;
    private final EmpresaMapper empresaMapper;
    private final EmpresaPersonaVinculoMapper vinculoMapper;

    @Override
    @Transactional
    public EmpresaPersonaVinculoDTO crearVinculo(VinculoDTO vinculoDTO) {
        log.info("Creando vínculo para personal ID: {} con empresa ID: {}", vinculoDTO.getIdPersonal(), vinculoDTO.getIdEmpresa());
        Empresa empresa = empresaRepository.findById(vinculoDTO.getIdEmpresa())
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + vinculoDTO.getIdEmpresa()));

        DatosPersonales personal = datosPersonalesRepository.findById(vinculoDTO.getIdPersonal())
                .orElseThrow(() -> new EntityNotFoundException("Personal no encontrado con ID: " + vinculoDTO.getIdPersonal()));

        EmpresaPersonaVinculo vinculo = EmpresaPersonaVinculo.builder()
                .empresa(empresa)
                .personal(personal)
                .cargo(vinculoDTO.getCargo())
                .fechaInicio(vinculoDTO.getFechaInicio() != null ? vinculoDTO.getFechaInicio() : java.time.LocalDate.now())
                .estado(vinculoDTO.getEstado() != null ? vinculoDTO.getEstado() : EstadoGeneral.ACTIVO)
                .build();

        EmpresaPersonaVinculo guardado = vinculoRepository.save(vinculo);

        auditService.logUserAction("EMPRESA", "INSERT_VINCULO", "Vinculó personal: " + personal.getNombre() + " a empresa: " + empresa.getCodigo());

        return vinculoMapper.toDto(guardado);
    }

    @Override
    @Transactional
    public EmpresaDTO create(EmpresaDTO empresaDTO) {
        log.info("Creando nueva empresa: {}", empresaDTO.getNombre());
        Empresa empresa = empresaMapper.toEntity(empresaDTO);
        
        if (empresa.getEstado() == null) {
            empresa.setEstado(EstadoGeneral.ACTIVO);
        }

        Empresa saved = empresaRepository.save(empresa);

        auditService.logUserAction("EMPRESA", "INSERT", "Creó empresa: " + (saved.getCodigo() != null ? saved.getCodigo() : "ID " + saved.getIdEmpresa()));

        return empresaMapper.toDto(saved);
    }

    @Override
    public List<EmpresaDTO> getAll() {
        return empresaRepository.findAll().stream()
                .map(empresaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<EmpresaDTO> getAll(Pageable pageable) {
        return empresaRepository.findAll(pageable).map(empresaMapper::toDto);
    }

    @Override
    public Page<EmpresaDTO> search(String q, Pageable pageable) {
        return empresaRepository.search(q, pageable).map(empresaMapper::toDto);
    }

    @Override
    public EmpresaDTO getById(Long id) {
        return empresaRepository.findById(id)
                .map(empresaMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public EmpresaDTO update(Long id, EmpresaDTO empresaDTO) {
        log.info("Actualizando empresa con ID: {}", id);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + id));

        empresa.setRepresentante(empresaDTO.getRepresentante());
        empresa.setAuditor(empresaDTO.getAuditor());
        empresa.setLiquidador(empresaDTO.getLiquidador());
        empresa.setFinanciero(empresaDTO.getFinanciero());
        empresa.setFax(empresaDTO.getFax());
        empresa.setEmpresasChana(empresaDTO.getEmpresasChana());
        empresa.setDireccion(empresaDTO.getDireccion());
        empresa.setTelefono(empresaDTO.getTelefono());
        empresa.setCodigo(empresaDTO.getCodigo());
        empresa.setAbrev(empresaDTO.getAbrev());
        empresa.setEstado(empresaDTO.getEstado());
        empresa.setCtacte(empresaDTO.getCtacte());
        empresa.setNombre(empresaDTO.getNombre());

        Empresa actualizado = empresaRepository.save(empresa);

        auditService.logUserAction("EMPRESA", "UPDATE", "Actualizó empresa ID: " + id);

        return empresaMapper.toDto(actualizado);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Eliminando empresa con ID: {}", id);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + id));
        
        empresa.setEstado(EstadoGeneral.ELIMINADO);
        empresaRepository.save(empresa);

        auditService.logUserAction("EMPRESA", "DELETE", "Eliminó empresa ID: " + id);
    }

    @Override
    public List<EmpresaPersonaVinculoDTO> getVinculosByEmpresa(Long idEmpresa) {
        return vinculoRepository.findByEmpresaIdEmpresa(idEmpresa).stream()
                .map(vinculoMapper::toDto)
                .collect(Collectors.toList());
    }
}
