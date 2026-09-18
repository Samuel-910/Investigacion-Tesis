package com.pe.articulos.modules.puntos.service;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.puntos.dto.PuntoRequestDTO;
import com.pe.articulos.modules.puntos.dto.PuntoResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PuntoService {

    /**
     * Crear un nuevo punto
     */
    PuntoResponseDTO crearPunto(PuntoRequestDTO requestDTO);

    /**
     * Obtener un punto por ID
     */
    PuntoResponseDTO obtenerPuntoPorId(Long id);

    /**
     * Obtener todos los puntos con paginación filtrados por sucursal
     */
    PageResponse<PuntoResponseDTO> obtenerTodosPuntos(Integer idSucursal, Pageable pageable);

    /**
     * Obtener puntos activos con paginación filtrados por sucursal
     */
    PageResponse<PuntoResponseDTO> obtenerPuntosActivos(Integer idSucursal, Pageable pageable);

    /**
     * Obtener todos los puntos como lista filtrados por sucursal (para combos)
     */
    List<PuntoResponseDTO> obtenerTodosPuntosList(Integer idSucursal);

    /**
     * Obtener puntos activos como lista filtrados por sucursal (para combos)
     */
    List<PuntoResponseDTO> obtenerPuntosActivosList(Integer idSucursal);

    /**
     * Actualizar un punto existente
     */
    PuntoResponseDTO actualizarPunto(Long id, PuntoRequestDTO requestDTO);

    /**
     * Eliminar un punto
     */
    void eliminarPunto(Long id);

    /**
     * Buscar puntos por nombre
     */
    List<PuntoResponseDTO> buscarPorNombre(String nombre);

    /**
     * Buscar puntos por tipo
     */
    List<PuntoResponseDTO> buscarPorTipo(String tipo);

    /**
     * Buscar puntos por sucursal
     */
    List<PuntoResponseDTO> buscarPorSucursal(Integer idSucursal);

    /**
     * Buscar puntos por múltiples criterios
     */
    List<PuntoResponseDTO> buscarPorCriterios(String nombre, String tipo, Integer idSucursal);

    /**
     * Verificar si existe un punto por nombre
     */
    boolean existePorNombre(String nombre);

    /**
     * Contar puntos por sucursal
     */
    long contarPorSucursal(Integer idSucursal);

    /**
     * Buscar puntos globalmente con paginación, filtro de tipo y sucursal
     */
    PageResponse<PuntoResponseDTO> buscar(String q, String tipo, Integer idSucursal, Pageable pageable);
}
