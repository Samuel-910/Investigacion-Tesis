package com.pe.articulos.modules.datos_medico.service;

import org.springframework.data.domain.Pageable;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.datos_medico.dto.*;

import java.time.LocalDate;
import java.util.Optional;

public interface DatosMedicoService {

    // ========== CRUD BÁSICO ==========

    DatosMedicoDto crear(DatosMedicoCreateDto dto);

    DatosMedicoDto actualizar(Long id, DatosMedicoUpdateDto dto);

    void eliminar(Long id);

    Optional<DatosMedicoDto> obtenerPorId(Long id);

    // ========== LISTADOS CON PAGINACIÓN ==========

    PageResponse<DatosMedicoDto> obtenerTodos(Pageable pageable);

    PageResponse<DatosMedicoDto> obtenerPorEstado(String estado, Pageable pageable);

    PageResponse<DatosMedicoDto> obtenerPorTipo(String tipo, Pageable pageable);

    PageResponse<DatosMedicoDto> obtenerPorArea(Long idArea, Pageable pageable);

    PageResponse<DatosMedicoDto> obtenerPorSucursal(Long idSucursal, Pageable pageable);

    PageResponse<DatosMedicoDto> buscarConFiltros(FiltroMedicoDto filtro, Pageable pageable);

    PageResponse<DatosMedicoDto> obtenerPorTipoMedico(String tipoMedico, Pageable pageable);

    // ========== LISTADOS SIN PAGINACIÓN (para selects/dropdowns) ==========

    PageResponse<DatosMedicoDto> obtenerActivos(Pageable pageable);

    // ========== BÚSQUEDAS ESPECIALIZADAS ==========

    Optional<DatosMedicoDto> obtenerPorCmp(String nroCmp);

    PageResponse<DatosMedicoDto> obtenerEnVacaciones(LocalDate fecha, Pageable pageable);

    PageResponse<DatosMedicoDto> obtenerPendientesCapacitacion(int meses, Pageable pageable);

    PageResponse<DatosMedicoDto> obtenerMedicosEmergencia(Pageable pageable);

    PageResponse<DatosMedicoDto> obtenerProximosALiquidar(LocalDate fechaLimite, Pageable pageable);

    PageResponse<DatosMedicoDto> obtenerConAniosServicio(int anios, Pageable pageable);

    // ========== UTILIDADES ==========

    EstadisticasDto obtenerEstadisticas();

    Long contarPorEstado(String estado);

    Long contarPorTipo(String tipo);

    Long contarPorArea(Long idArea);

    Long contarPorSucursal(Long idSucursal);

    boolean existePorCmp(String nroCmp);

    // ========== OPERACIONES ESPECIALES ==========

    void cambiarEstado(Long id, String nuevoEstado);

    void asignarArea(Long id, Long idArea);

    void asignarSucursal(Long id, Long idSucursal);

    void programarVacaciones(Long id, LocalDate inicio, LocalDate fin, Integer dias);

    void actualizarHonorarios(Long id, java.math.BigDecimal nuevoMonto);
}