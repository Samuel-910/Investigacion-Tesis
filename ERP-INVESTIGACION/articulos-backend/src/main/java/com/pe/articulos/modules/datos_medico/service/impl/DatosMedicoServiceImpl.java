package com.pe.articulos.modules.datos_medico.service.impl;

import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.core.reports.audit.service.AuditService;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.datos_medico.dto.*;
import com.pe.articulos.modules.datos_medico.entity.DatosMedico;
import com.pe.articulos.modules.datos_medico.mapper.DatosMedicoMapper;
import com.pe.articulos.modules.datos_medico.repository.DatosMedicoRepository;
import com.pe.articulos.modules.datos_medico.service.DatosMedicoService;

import com.pe.articulos.core.enums.EstadoGeneral;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@SuppressWarnings("null")
public class DatosMedicoServiceImpl implements DatosMedicoService {

    private final DatosMedicoRepository medicoRepository;
    private final DatosMedicoMapper medicoMapper;

    private final AuditService auditService;
    // ========================================
    // CONSTANTES
    // ========================================

    private static final int MIN_NOMBRE_LENGTH = 3;
    private static final int MAX_NOMBRE_LENGTH = 200;
    private static final int MIN_CMP_LENGTH = 6;
    private static final int MAX_CMP_LENGTH = 20;

    private static final BigDecimal MIN_HONORARIOS = BigDecimal.ZERO;
    private static final BigDecimal MAX_HONORARIOS = new BigDecimal("99999.99");
    private static final int MAX_DECIMALES = 2;

    private static final int MIN_DIAS_VACACIONES = 1;
    private static final int MAX_DIAS_VACACIONES = 60;

    // private static final List<String> ESTADOS_VALIDOS = Arrays.asList("A", "I",
    // "S"); // Removido por el enum
    private static final List<String> TIPOS_VALIDOS = Arrays.asList("MEDICO", "ENFERMERA", "TECNICO", "ADMIN");
    private static final List<String> TIPOS_MEDICO_VALIDOS = Arrays.asList("GENERAL", "ESPECIALISTA", "RESIDENTE");
    private static final List<String> SI_NO_VALIDOS = Arrays.asList("S", "N");

    // ========================================
    // CRUD BÁSICO
    // ========================================

    @Override
    public DatosMedicoDto crear(DatosMedicoCreateDto dto) {
        log.info("Creating medical personnel: {}", dto.getNombreMed());

        // Validaciones
        validateNombreMed(dto.getNombreMed());
        validateEstado(dto.getEstado());
        validateTipo(dto.getTipo());
        validateNroCmp(dto.getNroCmp(), dto.getTipo(), null);
        validateFechas(dto.getFechaIngreso(), null);
        validateHonorarios(dto.getHonorarios());

        if (dto.getPlanilla() != null) {
            validateCampoSN(dto.getPlanilla(), "planilla");
        }
        DatosMedico medico = medicoMapper.createDtoToEntity(dto);
        medico.setNombreMed(dto.getNombreMed().trim());
        medico.setEstado(dto.getEstado());
        medico.setTipo(dto.getTipo().trim().toUpperCase());

        if (dto.getNroCmp() != null) {
            medico.setNroCmp(dto.getNroCmp().trim().toUpperCase());
        }

        if (dto.getPlanilla() != null) {
            medico.setPlanilla(dto.getPlanilla().trim().toUpperCase());
        }

        DatosMedico medicoGuardado = medicoRepository.save(medico);

        log.info("Medical personnel created successfully - ID: {}, CMP: {}",
                medicoGuardado.getId(), medicoGuardado.getNroCmp());

        auditService.logUserAction("DATOS_MEDICO", "INSERT",
                "Creó médico: " + medicoGuardado.getNombreMed() + " (CMP: " + medicoGuardado.getNroCmp() + ")");

        return medicoMapper.toDto(medicoGuardado);
    }

    @Override
    public DatosMedicoDto actualizar(Long id, DatosMedicoUpdateDto dto) {
        log.info("Updating medical personnel with ID: {}", id);

        validateId(id);

        DatosMedico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosMedico", "id", id));

        // Validar y actualizar campos
        if (dto.getNombreMed() != null) {
            validateNombreMed(dto.getNombreMed());
            medico.setNombreMed(dto.getNombreMed().trim());
        }

        if (dto.getEstado() != null) {
            validateEstado(dto.getEstado());
            medico.setEstado(dto.getEstado());
        }

        if (dto.getTipo() != null) {
            validateTipo(dto.getTipo());
            medico.setTipo(dto.getTipo().trim().toUpperCase());
        }

        if (dto.getNroCmp() != null) {
            validateNroCmp(dto.getNroCmp(), medico.getTipo(), id);
            medico.setNroCmp(dto.getNroCmp().trim().toUpperCase());
        }

        if (dto.getHonorarios() != null) {
            validateHonorarios(dto.getHonorarios());
            medico.setHonorarios(dto.getHonorarios());
        }

        if (dto.getFechaCese() != null) {
            validateFechas(medico.getFechaIngreso(), dto.getFechaCese());
            medico.setFechaCese(dto.getFechaCese());
        }
        String nombreAnterior = medico.getNombreMed();
        medicoMapper.updateEntityFromDto(dto, medico);

        DatosMedico medicoActualizado = medicoRepository.save(medico);

        log.info("Medical personnel updated successfully: {}", id);
        auditService.logUserAction("DATOS_MEDICO", "UPDATE",
                "Actualizó médico: " + nombreAnterior + " -> " + medicoActualizado.getNombreMed() + " (ID: " + id
                        + ")");
        return medicoMapper.toDto(medicoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        log.info("Deleting medical personnel with ID: {}", id);

        validateId(id);

        DatosMedico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosMedico", "id", id));

        // Soft delete
        medico.setEstado(EstadoGeneral.INACTIVO);
        medico.setFechaCese(LocalDate.now());
        medicoRepository.save(medico);

        log.info("Medical personnel marked as inactive: {}", id);
        auditService.logUserAction("DATOS_MEDICO", "DELETE",
                "Desactivó (eliminación lógica) médico: " + medico.getNombreMed() + " (ID: " + id + ")");

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DatosMedicoDto> obtenerPorId(Long id) {
        log.debug("Getting medical personnel by ID: {}", id);

        validateId(id);

        return medicoRepository.findByIdWithRelaciones(id)
                .map(medicoMapper::toDto);
    }

    // ========================================
    // LISTADOS CON PAGINACIÓN
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerTodos(Pageable pageable) {
        log.info("Getting all medical personnel with pagination");

        validatePageable(pageable);

        Page<DatosMedico> page = medicoRepository.findAll(pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerPorEstado(String estado, Pageable pageable) {
        log.info("Getting medical personnel by state: {}", estado);

        EstadoGeneral estadoEnum = EstadoGeneral.fromCodigo(estado);
        validateEstado(estadoEnum);
        validatePageable(pageable);

        Page<DatosMedico> page = medicoRepository.findByEstado(estadoEnum, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerPorTipo(String tipo, Pageable pageable) {
        log.info("Getting medical personnel by type: {}", tipo);

        validateTipo(tipo);
        validatePageable(pageable);

        Page<DatosMedico> page = medicoRepository.findByTipo(tipo.trim().toUpperCase(), pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerPorArea(Long idArea, Pageable pageable) {
        log.info("Getting medical personnel by area: {}", idArea);

        validateId(idArea);
        validatePageable(pageable);

        Page<DatosMedico> page = medicoRepository.findByIdArea(idArea, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerPorSucursal(Long idSucursal, Pageable pageable) {
        log.info("Getting medical personnel by branch: {}", idSucursal);

        validateId(idSucursal);
        validatePageable(pageable);

        Page<DatosMedico> page = medicoRepository.findByIdSucursal(idSucursal, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerPorTipoMedico(String tipoMedico, Pageable pageable) {
        log.info("Getting physicians by type: {}", tipoMedico);

        if (tipoMedico != null && !TIPOS_MEDICO_VALIDOS.contains(tipoMedico.trim().toUpperCase())) {
            throw new BadRequestException("Tipo médico no válido. Debe ser: GENERAL, ESPECIALISTA o RESIDENTE");
        }

        validatePageable(pageable);

        Page<DatosMedico> page = medicoRepository.findByTipoMedico(tipoMedico.trim().toUpperCase(), pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> buscarConFiltros(FiltroMedicoDto filtro, Pageable pageable) {
        log.info("Searching medical personnel with filters");

        validatePageable(pageable);

        String nombreMed = filtro.getNombreMed() != null ? filtro.getNombreMed().trim() : null;
        String nroCmp = filtro.getNroCmp() != null ? filtro.getNroCmp().trim() : null;
        EstadoGeneral estado = filtro.getEstado() != null ? filtro.getEstado() : null;
        String tipo = filtro.getTipo() != null ? filtro.getTipo().trim().toUpperCase() : null;
        String vacaciones = filtro.getVacaciones() != null ? filtro.getVacaciones().trim().toUpperCase() : null;
        String tipoMedico = filtro.getTipoMedico() != null ? filtro.getTipoMedico().trim().toUpperCase() : null;

        Page<DatosMedico> page = medicoRepository.buscarConFiltros(
                nombreMed, nroCmp, estado, tipo, filtro.getIdArea(),
                filtro.getIdSucursal(), vacaciones, tipoMedico, pageable);

        return mapToPageResponse(page);
    }

    // ========================================
    // LISTADOS SIN PAGINACIÓN -> AHORA PAGINADOS Y LIMPIOS
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerActivos(Pageable pageable) {
        log.debug("Getting active medical personnel with pagination");
        validatePageable(pageable);

        Page<DatosMedico> page = medicoRepository.findAllActivos(pageable);
        return mapToPageResponse(page);
    }

    // ========================================
    // BÚSQUEDAS ESPECIALIZADAS
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Optional<DatosMedicoDto> obtenerPorCmp(String nroCmp) {
        log.debug("Getting physician by CMP: {}", nroCmp);

        if (nroCmp == null || nroCmp.trim().isEmpty()) {
            throw new BadRequestException("El CMP no puede estar vacío");
        }

        return medicoRepository.findByNroCmp(nroCmp.trim())
                .map(medicoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerEnVacaciones(LocalDate fecha, Pageable pageable) {
        log.debug("Getting personnel on vacation for date: {}", fecha);
        validatePageable(pageable);

        if (fecha == null) {
            fecha = LocalDate.now();
        }

        Page<DatosMedico> page = medicoRepository.findEnVacaciones(fecha, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerPendientesCapacitacion(int meses, Pageable pageable) {
        log.debug("Getting personnel pending training (last {} months)", meses);
        validatePageable(pageable);

        if (meses <= 0) {
            throw new BadRequestException("Los meses deben ser positivos");
        }

        LocalDate fechaLimite = LocalDate.now().minusMonths(meses);
        Page<DatosMedico> page = medicoRepository.findPendientesCapacitacion(fechaLimite, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerMedicosEmergencia(Pageable pageable) {
        log.debug("Getting emergency physicians");
        validatePageable(pageable);

        Page<DatosMedico> page = medicoRepository.findMedicosEmergencia(pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerProximosALiquidar(LocalDate fechaLimite, Pageable pageable) {
        log.debug("Getting personnel due for payment by: {}", fechaLimite);
        validatePageable(pageable);

        if (fechaLimite == null) {
            fechaLimite = LocalDate.now();
        }

        Page<DatosMedico> page = medicoRepository.findProximosALiquidar(fechaLimite, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DatosMedicoDto> obtenerConAniosServicio(int anios, Pageable pageable) {
        log.debug("Getting personnel with {} years of service", anios);
        validatePageable(pageable);

        if (anios <= 0) {
            throw new BadRequestException("Los años deben ser positivos");
        }

        Page<DatosMedico> page = medicoRepository.findConAniosServicio(anios, pageable);
        return mapToPageResponse(page);
    }

    // ========================================
    // UTILIDADES
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public EstadisticasDto obtenerEstadisticas() {
        log.debug("Getting statistics");

        EstadisticasDto stats = new EstadisticasDto();
        stats.setTotalActivos(medicoRepository.countByEstado(EstadoGeneral.ACTIVO));
        stats.setTotalInactivos(medicoRepository.countByEstado(EstadoGeneral.INACTIVO));
        stats.setTotalSuspendidos(medicoRepository.countByEstado(EstadoGeneral.SUSPENDIDO));
        stats.setTotalMedicos(medicoRepository.countActivosByTipo("MEDICO"));
        stats.setTotalEnfermeras(medicoRepository.countActivosByTipo("ENFERMERA"));
        stats.setTotalTecnicos(medicoRepository.countActivosByTipo("TECNICO"));
        stats.setTotalEnVacaciones(medicoRepository.countEnVacaciones(LocalDate.now()));
        stats.setTotalPorCapacitar(medicoRepository.countPendientesCapacitacion(LocalDate.now().minusMonths(12)));

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarPorEstado(String estado) {
        log.debug("Counting by state: {}", estado);

        EstadoGeneral estadoEnum = EstadoGeneral.fromCodigo(estado);
        validateEstado(estadoEnum);

        return medicoRepository.countByEstado(estadoEnum);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarPorTipo(String tipo) {
        log.debug("Counting by type: {}", tipo);

        validateTipo(tipo);

        return medicoRepository.countByTipo(tipo.trim().toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarPorArea(Long idArea) {
        log.debug("Counting by area: {}", idArea);

        validateId(idArea);

        return medicoRepository.countByIdArea(idArea);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarPorSucursal(Long idSucursal) {
        log.debug("Counting by branch: {}", idSucursal);

        validateId(idSucursal);

        return medicoRepository.countByIdSucursal(idSucursal);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCmp(String nroCmp) {
        log.debug("Checking if CMP exists: {}", nroCmp);

        if (nroCmp == null || nroCmp.trim().isEmpty()) {
            return false;
        }

        return medicoRepository.existsByNroCmp(nroCmp.trim());
    }

    // ========================================
    // OPERACIONES ESPECIALES
    // ========================================

    @Override
    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado) {
        log.info("Changing state for personnel {} to: {}", id, nuevoEstado);

        validateId(id);
        EstadoGeneral estadoEnum = EstadoGeneral.fromCodigo(nuevoEstado);
        validateEstado(estadoEnum);

        DatosMedico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosMedico", "id", id));

        medico.setEstado(estadoEnum);

        if (EstadoGeneral.INACTIVO.equals(estadoEnum)) {
            medico.setFechaCese(LocalDate.now());
        }

        medicoRepository.save(medico);
        log.info("State updated for personnel {} to: {}", id, nuevoEstado);
        auditService.logUserAction("DATOS_MEDICO", "UPDATE",
                "Cambio de estado médico: " + medico.getNombreMed() + " a " + nuevoEstado);
    }

    @Override
    public void asignarArea(Long id, Long idArea) {
        log.info("Assigning area {} to personnel {}", idArea, id);

        validateId(id);
        validateId(idArea);

        DatosMedico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosMedico", "id", id));

        medico.setIdArea(idArea);
        medicoRepository.save(medico);

        log.info("Area {} assigned to personnel {}", idArea, id);
        auditService.logUserAction("DATOS_MEDICO", "UPDATE",
                "Asignó área " + idArea + " a médico: " + medico.getNombreMed());
    }

    @Override
    public void asignarSucursal(Long id, Long idSucursal) {
        log.info("Assigning branch {} to personnel {}", idSucursal, id);

        validateId(id);
        validateId(idSucursal);

        DatosMedico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosMedico", "id", id));

        medico.setIdSucursal(idSucursal);
        medicoRepository.save(medico);

        log.info("Branch {} assigned to personnel {}", idSucursal, id);
        auditService.logUserAction("DATOS_MEDICO", "UPDATE",
                "Asignó sucursal " + idSucursal + " a médico: " + medico.getNombreMed());
    }

    @Override
    public void programarVacaciones(Long id, LocalDate inicio, LocalDate fin, Integer dias) {
        log.info("Scheduling vacation for personnel {}: {} to {}", id, inicio, fin);

        validateId(id);
        validateVacaciones(id, inicio, fin, dias);

        DatosMedico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosMedico", "id", id));

        medico.setVacaciones("S");
        medico.setFechaInicioVacaciones(inicio);
        medico.setFechaFinVacaciones(fin);
        medico.setDiasVacaciones(dias);
        medico.setEstado(EstadoGeneral.SUSPENDIDO); // Suspendido durante vacaciones

        medicoRepository.save(medico);

        log.info("Vacation scheduled for personnel {}: {} to {}", id, inicio, fin);
        auditService.logUserAction("DATOS_MEDICO", "UPDATE",
                "Programó vacaciones para médico: " + medico.getNombreMed() + " (Inicio: " + inicio + ")");
    }

    @Override
    public void actualizarHonorarios(Long id, BigDecimal nuevoMonto) {
        log.info("Updating fees for personnel {}: {}", id, nuevoMonto);

        validateId(id);
        validateHonorarios(nuevoMonto);

        DatosMedico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosMedico", "id", id));

        medico.setHonorarios(nuevoMonto);
        medicoRepository.save(medico);

        log.info("Fees updated for personnel {}: {}", id, nuevoMonto);
    }

    // ========================================
    // MÉTODOS DE VALIDACIÓN PRIVADOS
    // ========================================

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }
    }

    private void validateNombreMed(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre del personal es obligatorio");
        }

        String nombreTrim = nombre.trim();

        if (nombreTrim.length() < MIN_NOMBRE_LENGTH) {
            throw new BadRequestException("El nombre debe tener al menos " + MIN_NOMBRE_LENGTH + " caracteres");
        }

        if (nombreTrim.length() > MAX_NOMBRE_LENGTH) {
            throw new BadRequestException("El nombre no puede superar los " + MAX_NOMBRE_LENGTH + " caracteres");
        }

        if (nombreTrim.matches("^[0-9]+$")) {
            throw new BadRequestException("El nombre no puede contener solo números");
        }
    }

    private void validateNroCmp(String nroCmp, String tipo, Long idExcluir) {
        if ("MEDICO".equals(tipo)) {
            if (nroCmp == null || nroCmp.trim().isEmpty()) {
                throw new BadRequestException("El personal médico debe tener número de CMP");
            }
        }

        if (nroCmp != null && !nroCmp.trim().isEmpty()) {
            String cmpTrim = nroCmp.trim();

            if (cmpTrim.length() < MIN_CMP_LENGTH || cmpTrim.length() > MAX_CMP_LENGTH) {
                throw new BadRequestException(
                        "El CMP debe tener entre " + MIN_CMP_LENGTH + " y " + MAX_CMP_LENGTH + " caracteres");
            }

            if (!cmpTrim.matches("^[0-9]+$")) {
                throw new BadRequestException("El CMP debe contener solo números");
            }

            Optional<DatosMedico> existente = medicoRepository.findByNroCmp(cmpTrim);
            if (existente.isPresent() && !existente.get().getId().equals(idExcluir)) {
                throw new ValidationException("Ya existe un médico registrado con el CMP: " + cmpTrim);
            }
        }
    }

    private void validateEstado(EstadoGeneral estado) {
        if (estado == null) {
            throw new BadRequestException("El estado es obligatorio o no es válido");
        }
    }

    private void validateTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new BadRequestException("El tipo de personal es obligatorio");
        }

        String tipoUpper = tipo.trim().toUpperCase();

        if (!TIPOS_VALIDOS.contains(tipoUpper)) {
            throw new BadRequestException("Tipo no válido. Debe ser: MEDICO, ENFERMERA, TECNICO o ADMIN");
        }
    }

    private void validateHonorarios(BigDecimal honorarios) {
        if (honorarios != null) {
            if (honorarios.compareTo(MIN_HONORARIOS) < 0) {
                throw new BadRequestException("Los honorarios no pueden ser negativos");
            }

            if (honorarios.compareTo(MAX_HONORARIOS) > 0) {
                throw new BadRequestException("Los honorarios no pueden superar " + MAX_HONORARIOS);
            }

            if (honorarios.scale() > MAX_DECIMALES) {
                throw new BadRequestException("Los honorarios no pueden tener más de " + MAX_DECIMALES + " decimales");
            }
        }
    }

    private void validateFechas(LocalDate fechaIngreso, LocalDate fechaCese) {
        if (fechaIngreso == null) {
            throw new BadRequestException("La fecha de ingreso es obligatoria");
        }

        if (fechaIngreso.isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de ingreso no puede ser futura");
        }

        if (fechaCese != null && fechaCese.isBefore(fechaIngreso)) {
            throw new BadRequestException("La fecha de cese debe ser posterior a la fecha de ingreso");
        }
    }

    private void validateVacaciones(Long id, LocalDate inicio, LocalDate fin, Integer dias) {
        if (inicio == null || fin == null) {
            throw new BadRequestException("Las fechas de inicio y fin son obligatorias");
        }

        if (dias == null || dias <= 0) {
            throw new BadRequestException("Los días de vacaciones deben ser mayor a 0");
        }

        if (dias < MIN_DIAS_VACACIONES || dias > MAX_DIAS_VACACIONES) {
            throw new BadRequestException(
                    "Los días de vacaciones deben estar entre " + MIN_DIAS_VACACIONES + " y " + MAX_DIAS_VACACIONES);
        }

        if (fin.isBefore(inicio)) {
            throw new BadRequestException("La fecha fin debe ser posterior a la fecha inicio");
        }

        DatosMedico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosMedico", "id", id));

        if (medico.getVacaciones() != null && "S".equals(medico.getVacaciones())) {
            if (medico.getFechaInicioVacaciones() != null && medico.getFechaFinVacaciones() != null) {
                boolean solapa = !(fin.isBefore(medico.getFechaInicioVacaciones()) ||
                        inicio.isAfter(medico.getFechaFinVacaciones()));

                if (solapa) {
                    throw new ValidationException("Las vacaciones se solapan con un período existente: " +
                            medico.getFechaInicioVacaciones() + " a " + medico.getFechaFinVacaciones());
                }
            }
        }
    }

    private void validateCampoSN(String valor, String nombreCampo) {
        if (valor != null && !valor.trim().isEmpty()) {
            String valorUpper = valor.trim().toUpperCase();

            if (!SI_NO_VALIDOS.contains(valorUpper)) {
                throw new BadRequestException("El campo " + nombreCampo + " debe ser S (Sí) o N (No)");
            }
        }
    }

    private void validatePageable(Pageable pageable) {
        if (pageable == null) {
            throw new BadRequestException("Los parámetros de paginación son requeridos");
        }

        if (pageable.getPageSize() > 100) {
            throw new BadRequestException("El tamaño de página no puede ser mayor a 100");
        }

        if (pageable.getPageSize() <= 0) {
            throw new BadRequestException("El tamaño de página debe ser mayor a 0");
        }
    }

    // ========================================
    // MAPEO
    // ========================================

    private PageResponse<DatosMedicoDto> mapToPageResponse(Page<DatosMedico> page) {
        return PageResponse.fromPage(page.map(medicoMapper::toDto));
    }
}