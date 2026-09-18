package com.pe.articulos.modules.puntos.service;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.puntos.dto.PuntoDocumentoRequestDTO;
import com.pe.articulos.modules.puntos.dto.PuntoDocumentoResponseDTO;
import com.pe.articulos.modules.puntos.entity.PuntoDocumentoAuditoria;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PuntoDocumentoService {

    /**
     * Crear un nuevo documento para un punto
     */
    PuntoDocumentoResponseDTO crearDocumento(PuntoDocumentoRequestDTO requestDTO);

    /**
     * Obtener un documento por ID
     */
    PuntoDocumentoResponseDTO obtenerDocumentoPorId(Long id);

    /**
     * Obtener todos los documentos paginados
     */
    PageResponse<PuntoDocumentoResponseDTO> obtenerTodosDocumentos(Long puntoId, Pageable pageable);

    /**
     * Obtener todos los documentos como lista
     */
    List<PuntoDocumentoResponseDTO> obtenerTodosDocumentosList();

    /**
     * Actualizar un documento existente
     */
    PuntoDocumentoResponseDTO actualizarDocumento(Long id, PuntoDocumentoRequestDTO requestDTO);

    /**
     * Eliminar un documento
     */
    void eliminarDocumento(Long id);

    /**
     * Obtener todos los documentos de un punto específico
     */
    List<PuntoDocumentoResponseDTO> obtenerDocumentosPorPunto(Long puntoId);

    /**
     * Buscar documentos por tipo
     */
    List<PuntoDocumentoResponseDTO> buscarPorTipoDocumento(String tipoDoc);

    /**
     * Buscar documentos por serie
     */
    List<PuntoDocumentoResponseDTO> buscarPorSerie(String serie);

    /**
     * Buscar documentos por estado
     */
    List<PuntoDocumentoResponseDTO> buscarPorEstado(String estado);

    /**
     * Obtener documentos activos de un punto
     */
    List<PuntoDocumentoResponseDTO> obtenerDocumentosActivosPorPunto(Long puntoId, List<String> modulos);

    List<PuntoDocumentoResponseDTO> obtenerDocumentosActivosPorSucursal(Long sucursalId, List<String> modulos);

    /**
     * Buscar documentos por múltiples criterios
     */
    List<PuntoDocumentoResponseDTO> buscarPorCriterios(Long puntoId, String tipoDoc, String estado);

    /**
     * Verificar si existe un documento con serie y número
     */
    boolean existePorSerieYNumero(String serie, Integer numero);

    /**
     * Contar documentos de un punto
     */
    long contarDocumentosPorPunto(Long puntoId);

    Integer obtenerUltimoNumeroPorSerieYTipo(String serie, String tipoDoc);

    /**
     * Obtener tipos de documentos asignados por Módulo y Sucursal
     */
    List<com.pe.articulos.modules.venta_registro.entity.TipoDocumento> obtenerTiposDocumentosAsignados(String modulo, Long sucursalId);

    List<PuntoDocumentoAuditoria> listarHistorial(Long id);
}
