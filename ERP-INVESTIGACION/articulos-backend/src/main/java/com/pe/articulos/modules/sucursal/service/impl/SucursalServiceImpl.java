package com.pe.articulos.modules.sucursal.service.impl;

import com.pe.articulos.modules.sucursal.dto.SucursalDto;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.pe.articulos.modules.sucursal.mapper.SucursalMapper;
import com.pe.articulos.modules.sucursal.repository.SucursalRepository;
import com.pe.articulos.modules.sucursal.service.SucursalService;
import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.core.enums.EstadoGeneral;

import com.pe.articulos.modules.puntos.service.PuntoService;
import com.pe.articulos.modules.puntos.dto.PuntoRequestDTO;
import com.pe.articulos.modules.reportes.service.DashboardConfigService;

import com.pe.articulos.modules.almacen.service.AlmacenService;
import com.pe.articulos.modules.almacen.dto.AlmacenRequest;
import com.pe.articulos.modules.almacen.dto.AlmacenResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@SuppressWarnings("null")
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;
    private final SucursalMapper sucursalMapper;
    private final AlmacenService almacenService;
    private final PuntoService puntoService;
    private final DashboardConfigService dashboardConfigService;

    // ========================================
    // CONSTANTES
    // ========================================

    private static final int MIN_NOMBRE_LENGTH = 3;
    private static final int MAX_NOMBRE_LENGTH = 100;
    private static final int MAX_DIRECCION_LENGTH = 200;
    private static final int MAX_TELEFONO_LENGTH = 9;
    private static final int MIN_TELEFONO_LENGTH = 1;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    @Override
    public SucursalDto crear(SucursalDto dto) {
        log.info("Creating branch: {}", dto.getNombreSucursal());

        // Validaciones
        validateNombreSucursal(dto.getNombreSucursal(), null);
        validateDireccion(dto.getDireccion());
        validateTelefono(dto.getTelefono());
        validateTelefono(dto.getCelular()); // Validar celular también

        // Verificar nombre único
        if (sucursalRepository.existsByNombreSucursal(dto.getNombreSucursal().trim())) {
            throw new ValidationException("Ya existe una sucursal con el nombre: " + dto.getNombreSucursal());
        }

        Sucursal sucursal = sucursalMapper.toEntity(dto);
        sucursal.setEstado(EstadoGeneral.ACTIVO); // Por defecto

        // Normalizar
        sucursal.setNombreSucursal(dto.getNombreSucursal().trim());
        if (dto.getDireccion() != null) {
            sucursal.setDireccion(dto.getDireccion().trim());
        }
        if (dto.getTelefono() != null) {
            sucursal.setTelefono(dto.getTelefono().trim());
        }
        if (dto.getCelular() != null) {
            sucursal.setCelular(dto.getCelular().trim());
        }

        Sucursal guardada = sucursalRepository.save(sucursal);

        log.info("Branch created successfully - ID: {}, Name: {}",
                guardada.getIdSucursal(), guardada.getNombreSucursal());


        // Crear almacén principal por defecto
        Long idAlmacenGuardado = null;
        try {
            AlmacenRequest almacenReq = new AlmacenRequest();
            almacenReq.setNombre("ALMACÉN PRINCIPAL - " + guardada.getNombreSucursal());
            almacenReq.setEsPrincipal(true);
            almacenReq.setEstado(EstadoGeneral.ACTIVO);
            almacenReq.setIdSucursal(guardada.getIdSucursal());
            
            AlmacenResponse almacenGuardado = almacenService.createAlmacen(almacenReq);
            idAlmacenGuardado = almacenGuardado.getId();
            log.info("Almacén principal por defecto creado para la sucursal: {}", guardada.getNombreSucursal());
        } catch (Exception e) {
            log.error("No se pudo crear el almacén principal por defecto para la sucursal", e);
        }

        // Crear punto de venta por defecto
        try {
            PuntoRequestDTO puntoReq = new PuntoRequestDTO();
            puntoReq.setNombre("Punto Principal - " + guardada.getNombreSucursal());
            puntoReq.setIdSucursal(Math.toIntExact(guardada.getIdSucursal()));
            if (idAlmacenGuardado != null) {
                puntoReq.setIdAlmacen(idAlmacenGuardado);
            }
            puntoReq.setValido("S");
            puntoService.crearPunto(puntoReq);
            log.info("Punto de venta por defecto creado para la sucursal: {}", guardada.getNombreSucursal());
        } catch (Exception e) {
            log.error("No se pudo crear el punto de venta por defecto para la sucursal", e);
        }

        // Crear dashboards por defecto desde JSON
        try {
            dashboardConfigService.inicializarParaSucursal(guardada.getIdSucursal());
            log.info("Dashboards por defecto inicializados para la sucursal: {}", guardada.getNombreSucursal());
        } catch (Exception e) {
            log.error("No se pudieron inicializar los dashboards por defecto para la sucursal", e);
        }

        return sucursalMapper.toDto(guardada);
    }

    @Override
    public SucursalDto actualizar(Long id, SucursalDto dto) {
        log.info("Updating branch with ID: {}", id);

        // Validar ID
        validateId(id);

        // Buscar existente
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        // Validaciones
        if (dto.getNombreSucursal() != null) {
            validateNombreSucursal(dto.getNombreSucursal(), id);

            // Validar nombre único solo si cambia
            if (!sucursal.getNombreSucursal().equalsIgnoreCase(dto.getNombreSucursal().trim())) {
                if (sucursalRepository.existsByNombreSucursal(dto.getNombreSucursal().trim())) {
                    throw new ValidationException("Ya existe una sucursal con el nombre: " + dto.getNombreSucursal());
                }
            }

            dto.setNombreSucursal(dto.getNombreSucursal().trim());
        }

        if (dto.getDireccion() != null) {
            validateDireccion(dto.getDireccion());
            dto.setDireccion(dto.getDireccion().trim());
        }

        if (dto.getTelefono() != null) {
            validateTelefono(dto.getTelefono());
            dto.setTelefono(dto.getTelefono().trim());
        }

        if (dto.getCelular() != null) {
            validateTelefono(dto.getCelular()); // Reusing phone validation for cell phone
            dto.setCelular(dto.getCelular().trim());
        }

        if (dto.getEstado() != null) {
            validateEstado(dto.getEstado());
        }

        sucursalMapper.updateEntityFromDto(dto, sucursal);

        Sucursal actualizada = sucursalRepository.save(sucursal);

        log.info("Branch updated successfully: {}", id);
        return sucursalMapper.toDto(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        log.info("Deleting branch with ID: {}", id);

        // Validar ID
        validateId(id);

        // Buscar existente
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        // Verificar si tiene personal activo
        Long personalActivo = sucursalRepository.countPersonalActivoBySucursal(id);
        if (personalActivo > 0) {
            throw new ValidationException(
                    "No se puede eliminar la sucursal porque tiene " +
                            personalActivo + " personal activo asignado");
        }

        // Soft delete manejado por Hibernate via @SQLDelete en la entidad
        sucursalRepository.delete(sucursal);

        log.info("Branch marked as inactive: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SucursalDto> obtenerPorId(Long id) {
        log.debug("Getting branch by ID: {}", id);

        // Validar ID
        validateId(id);

        return sucursalRepository.findById(id)
                .map(sucursalMapper::toDto);
    }

    // ========================================
    // LISTADOS CON PAGINACIÓN
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SucursalDto> obtenerTodas(Pageable pageable) {
        log.info("Getting all branches with pagination - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        validatePageable(pageable);

        Page<Sucursal> page = sucursalRepository.findAll(pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SucursalDto> obtenerPorEstado(String estado, Pageable pageable) {
        log.info("Getting branches by state: {} with pagination", estado);

        EstadoGeneral estadoEnum = EstadoGeneral.fromCodigo(estado);
        validateEstado(estadoEnum);
        validatePageable(pageable);

        Page<Sucursal> page = sucursalRepository.findByEstado(estadoEnum, pageable);
        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SucursalDto> buscar(String q, String type, Pageable pageable) {
        log.info("Searching branches with q: {}, type: {} with pagination", q, type);

        if (q == null || q.trim().isEmpty()) {
            throw new BadRequestException("El término de búsqueda no puede estar vacío");
        }

        validatePageable(pageable);

        Page<Sucursal> page;
        String term = q.trim();

        switch (type.toUpperCase()) {
            case "NOMBRE":
                page = sucursalRepository.findByNombreSucursalContainingIgnoreCase(term, pageable);
                break;
            case "TELEFONO":
                page = sucursalRepository.findByTelefonoContaining(term, pageable);
                break;
            case "DIRECCION":
                page = sucursalRepository.findByDireccionContainingIgnoreCase(term, pageable);
                break;
            default: // ALL o cualquier otro
                page = sucursalRepository.findByNombreSucursalContainingIgnoreCase(term, pageable);
                break;
        }

        return mapToPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SucursalDto> obtenerConPersonal(Pageable pageable) {
        log.info("Getting branches with staff count with pagination");

        validatePageable(pageable);

        Page<Object[]> page = sucursalRepository.findSucursalesConCantidadPersonal(pageable);

        List<SucursalDto> content = page.getContent().stream()
                .map(obj -> {
                    Sucursal sucursal = (Sucursal) obj[0];
                    Long cantidad = (Long) obj[1];
                    SucursalDto dto = sucursalMapper.toDto(sucursal);
                    dto.setCantidadPersonal(cantidad.intValue());
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }

    // ========================================
    // LISTADOS SIN PAGINACIÓN
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerTodasAsList() {
        log.debug("Getting all branches as list");

        return sucursalRepository.findAll().stream()
                .map(sucursalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerActivasAsList() {
        log.debug("Getting active branches as list");

        return sucursalRepository.findAllActivas().stream()
                .map(sucursalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerPorEstadoAsList(String estado) {
        log.debug("Getting branches by state: {} as list", estado);

        EstadoGeneral estadoEnum = EstadoGeneral.fromCodigo(estado);
        validateEstado(estadoEnum);

        return sucursalRepository.findByEstado(estadoEnum).stream()
                .map(sucursalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerSinPersonalAsList() {
        log.debug("Getting branches without staff as list");

        return sucursalRepository.findSucursalesSinPersonal().stream()
                .map(sucursalMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========================================
    // BÚSQUEDAS ESPECIALIZADAS
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Optional<SucursalDto> obtenerPorNombre(String nombre) {
        log.debug("Getting branch by name: {}", nombre);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre no puede estar vacío");
        }

        return sucursalRepository.findByNombreSucursal(nombre.trim())
                .map(sucursalMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerConCantidadPersonal() {
        log.debug("Getting branches with staff count");

        List<Object[]> resultados = sucursalRepository.findSucursalesConCantidadPersonal();

        return resultados.stream()
                .map(obj -> {
                    Sucursal sucursal = (Sucursal) obj[0];
                    Long cantidad = (Long) obj[1];
                    SucursalDto dto = sucursalMapper.toDto(sucursal);
                    dto.setCantidadPersonal(cantidad.intValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalDto> obtenerSinPersonal() {
        log.debug("Getting branches without staff");

        return sucursalRepository.findSucursalesSinPersonal().stream()
                .map(sucursalMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========================================
    // UTILIDADES
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Long contarPorEstado(String estado) {
        log.debug("Counting branches by state: {}", estado);

        EstadoGeneral estadoEnum = EstadoGeneral.fromCodigo(estado);
        validateEstado(estadoEnum);

        return sucursalRepository.countByEstado(estadoEnum);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarActivas() {
        log.debug("Counting active branches");

        return sucursalRepository.countActivas();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarTotal() {
        log.debug("Counting total branches");

        return sucursalRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        log.debug("Checking if branch exists with name: {}", nombre);

        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        return sucursalRepository.existsByNombreSucursal(nombre.trim());
    }

    // ========================================
    // OPERACIONES ESPECIALES
    // ========================================

    @Override
    public void cambiarEstado(Long id, String nuevoEstado) {
        log.info("Changing state for branch {} to: {}", id, nuevoEstado);

        validateId(id);
        EstadoGeneral estadoEnum = EstadoGeneral.fromCodigo(nuevoEstado);
        validateEstado(estadoEnum);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        sucursal.setEstado(estadoEnum);
        sucursalRepository.save(sucursal);

        log.info("State updated for branch: {}", id);
    }

    @Override
    public void activar(Long id) {
        log.info("Activating branch: {}", id);

        validateId(id);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        sucursal.setEstado(EstadoGeneral.ACTIVO);
        sucursalRepository.save(sucursal);

        log.info("Branch activated: {}", id);
    }

    @Override
    public void inactivar(Long id) {
        log.info("Inactivating branch: {}", id);

        validateId(id);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", "id", id));

        // Verificar si tiene personal activo
        Long personalActivo = sucursalRepository.countPersonalActivoBySucursal(id);
        if (personalActivo > 0) {
            throw new ValidationException(
                    "No se puede inactivar la sucursal porque tiene " +
                            personalActivo + " personal activo asignado");
        }

        sucursal.setEstado(EstadoGeneral.INACTIVO);
        sucursalRepository.save(sucursal);

        log.info("Branch inactivated: {}", id);
    }

    // ========================================
    // MÉTODOS DE VALIDACIÓN PRIVADOS
    // ========================================

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }
    }

    private void validateNombreSucursal(String nombre, Long idExcluir) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre de la sucursal es obligatorio");
        }

        String nombreTrim = nombre.trim();

        if (nombreTrim.length() < MIN_NOMBRE_LENGTH) {
            throw new BadRequestException("El nombre debe tener al menos " +
                    MIN_NOMBRE_LENGTH + " caracteres");
        }

        if (nombreTrim.length() > MAX_NOMBRE_LENGTH) {
            throw new BadRequestException("El nombre no puede superar los " +
                    MAX_NOMBRE_LENGTH + " caracteres");
        }

        // No puede contener solo números
        if (nombreTrim.matches("^[0-9]+$")) {
            throw new BadRequestException("El nombre no puede contener solo números");
        }
    }

    private void validateDireccion(String direccion) {
        if (direccion != null && !direccion.trim().isEmpty()) {
            if (direccion.trim().length() > MAX_DIRECCION_LENGTH) {
                throw new BadRequestException("La dirección no puede superar los " +
                        MAX_DIRECCION_LENGTH + " caracteres");
            }
        }
    }

    private void validateTelefono(String telefono) {
        if (telefono != null && !telefono.trim().isEmpty()) {
            String telefonoTrim = telefono.trim();

            if (telefonoTrim.length() < MIN_TELEFONO_LENGTH) {
                throw new BadRequestException("El teléfono debe tener al menos " +
                        MIN_TELEFONO_LENGTH + " dígitos");
            }

            if (telefonoTrim.length() > MAX_TELEFONO_LENGTH) {
                throw new BadRequestException("El teléfono no puede superar los " +
                        MAX_TELEFONO_LENGTH + " caracteres");
            }

            // Solo números
            if (!telefonoTrim.matches("^[0-9]+$")) {
                throw new BadRequestException(
                        "Solo se permiten números");
            }
        }
    }

    private void validateEstado(EstadoGeneral estado) {
        if (estado == null) {
            throw new BadRequestException("El estado es obligatorio o no es válido");
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
    // MÉTODOS DE MAPEO
    // ========================================

    private PageResponse<SucursalDto> mapToPageResponse(Page<Sucursal> page) {
        List<SucursalDto> content = page.getContent().stream()
                .map(sucursalMapper::toDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }
}