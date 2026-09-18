package com.pe.articulos.modules.sucursal.service;

import org.springframework.data.domain.Pageable;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.sucursal.dto.SucursalDto;

import java.util.List;
import java.util.Optional;

public interface SucursalService {

    // ========== CRUD BÁSICO ==========

    SucursalDto crear(SucursalDto dto);

    SucursalDto actualizar(Long id, SucursalDto dto);

    void eliminar(Long id);

    Optional<SucursalDto> obtenerPorId(Long id);

    // ========== LISTADOS CON PAGINACIÓN ==========

    PageResponse<SucursalDto> obtenerTodas(Pageable pageable);

    PageResponse<SucursalDto> obtenerPorEstado(String estado, Pageable pageable);

    PageResponse<SucursalDto> buscar(String q, String type, Pageable pageable);

    PageResponse<SucursalDto> obtenerConPersonal(Pageable pageable);

    // ========== LISTADOS SIN PAGINACIÓN ==========

    List<SucursalDto> obtenerTodasAsList();

    List<SucursalDto> obtenerActivasAsList();

    List<SucursalDto> obtenerPorEstadoAsList(String estado);

    List<SucursalDto> obtenerSinPersonalAsList();

    // ========== BÚSQUEDAS ESPECIALIZADAS ==========

    Optional<SucursalDto> obtenerPorNombre(String nombre);

    List<SucursalDto> obtenerConCantidadPersonal();

    List<SucursalDto> obtenerSinPersonal();

    // ========== UTILIDADES ==========

    Long contarPorEstado(String estado);

    Long contarActivas();

    Long contarTotal();

    boolean existePorNombre(String nombre);

    // ========== OPERACIONES ESPECIALES ==========

    void cambiarEstado(Long id, String nuevoEstado);

    void activar(Long id);

    void inactivar(Long id);
}