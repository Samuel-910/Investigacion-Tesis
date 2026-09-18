package com.pe.articulos.modules.compras.service;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.compras.dto.CompraRequest;
import com.pe.articulos.modules.compras.dto.CompraResponse;
import com.pe.articulos.modules.compras.dto.DetalleCompraResponse;
import com.pe.articulos.modules.compras.dto.OrdenCreateRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CompraService {

    CompraResponse registrar(CompraRequest request, String userId);

    CompraResponse crearOrden(OrdenCreateRequest request, String userId);

    CompraResponse actualizar(Long id, CompraRequest request, String userId);

    CompraResponse actualizarOrden(Long id, OrdenCreateRequest request, String userId);

    CompraResponse obtener(Long id);

    PageResponse<CompraResponse> listar(Long idSucursal, String estado, Pageable pageable);

    PageResponse<CompraResponse> listarOrdenes(Long idSucursal, Pageable pageable);

    CompraResponse seleccionarGanadora(Long idCompra, String nombreGrupo);

    PageResponse<CompraResponse> buscar(Long idSucursal,
            Long idProveedor,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String estado,
            Pageable pageable);

    void solicitarAnulacion(Long id, String motivo);

    void confirmarAnulacion(Long id);

    PageResponse<DetalleCompraResponse> buscarDetallesPorProducto(String term, Long idSucursal, Pageable pageable);

    BigDecimal obtenerPrecioMaximo(Long idCatalogo);

    PageResponse<DetalleCompraResponse> obtenerHistorialPorProducto(Long idCatalogo, Pageable pageable);

    PageResponse<String> listarGrupos(Long idSucursal, Pageable pageable);

    PageResponse<DetalleCompraResponse> obtenerProductosPorGrupo(String nombreGrupo, Long idSucursal,
            Pageable pageable);

    PageResponse<Long> obtenerProveedoresPorGrupo(String nombreGrupo, Long idSucursal, Pageable pageable);
}
