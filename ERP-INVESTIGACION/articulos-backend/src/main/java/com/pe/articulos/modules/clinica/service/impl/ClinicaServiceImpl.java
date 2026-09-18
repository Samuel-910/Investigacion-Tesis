package com.pe.articulos.modules.clinica.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.articulos.modules.clinica.entity.Clinica;
import com.pe.articulos.modules.clinica.entity.ClinicaAuditoria;
import com.pe.articulos.modules.clinica.repository.ClinicaAuditoriaRepository;
import com.pe.articulos.modules.clinica.repository.ClinicaRepository;
import com.pe.articulos.modules.clinica.service.ClinicaService;
import com.pe.articulos.core.enums.EstadoGeneral;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class ClinicaServiceImpl implements ClinicaService {

    private final ClinicaRepository clinicaRepository;
    private final ClinicaAuditoriaRepository auditoriaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Clinica obtenerPrincipal() {
        return clinicaRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontraron datos de la clínica"));
    }

    @Override
    @Transactional(readOnly = true)
    public Clinica obtenerPorId(Long id) {
        return clinicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clínica no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public Clinica crear(Clinica clinica) {
        if (clinicaRepository.count() > 0) {
            throw new RuntimeException("Ya existe un registro de la clínica. Use la opción de editar.");
        }
        Clinica nueva = clinicaRepository.save(clinica);
        registrarAuditoria(nueva, "CREATE", null);
        return nueva;
    }

    @Override
    @Transactional
    public Clinica actualizar(Long id, Clinica clinica) {
        Clinica existente = obtenerPorId(id);

        String datosAnteriores = serialize(existente);

        existente.setRazonSocial(clinica.getRazonSocial());
        existente.setRuc(clinica.getRuc());
        existente.setEmail(clinica.getEmail());
        existente.setRepresentante(clinica.getRepresentante());
        existente.setAuditor(clinica.getAuditor());
        existente.setLiquidador(clinica.getLiquidador());
        existente.setFinanciero(clinica.getFinanciero());
        existente.setWeb(clinica.getWeb());
        existente.setCtacte(clinica.getCtacte());
        existente.setCodigo(clinica.getCodigo());
        existente.setAbrev(clinica.getAbrev());
        existente.setLogoCuadrado(clinica.getLogoCuadrado());
        existente.setLogoRectangular(clinica.getLogoRectangular());
        existente.setLogoPrincipal(clinica.getLogoPrincipal());
        existente.setEstado(clinica.getEstado());

        Clinica actualizada = clinicaRepository.save(existente);
        registrarAuditoria(actualizada, "UPDATE", datosAnteriores);

        return actualizada;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Clinica existente = obtenerPorId(id);
        existente.setEstado(EstadoGeneral.ELIMINADO);
        clinicaRepository.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClinicaAuditoria> listarHistorial(Long idClinica, Pageable pageable) {
        Page<ClinicaAuditoria> page = auditoriaRepository.findByIdClinica(idClinica, pageable);
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }

    private void registrarAuditoria(Clinica clinica, String operacion, String datosAnteriores) {
        String usuario = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";

        ClinicaAuditoria auditoria = ClinicaAuditoria.builder()
                .idClinica(clinica.getId())
                .usuario(usuario)
                .operacion(operacion)
                .datosAnteriores(datosAnteriores)
                .build();

        auditoriaRepository.save(auditoria);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error serializando clínica para auditoría", e);
            return null;
        }
    }
}
