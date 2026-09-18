package com.pe.articulos.modules.proveedores.service;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.proveedores.dto.CuentaProveedorResponse;
import com.pe.articulos.modules.proveedores.dto.CuentaProveedorMovimientoResponse;
import com.pe.articulos.modules.proveedores.dto.DeudaProveedorResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface CuentaProveedorService {
    CuentaProveedorResponse obtenerCuentaPorProveedor(Long idProveedor);

    PageResponse<CuentaProveedorMovimientoResponse> listarMovimientos(Long idProveedor, Pageable pageable);

    CuentaProveedorMovimientoResponse registrarMovimiento(Long idProveedor, String tipo, BigDecimal monto, String descripcion,
            String idUser, Long idSucursal, boolean pagarDesdeCajaGeneral, String metodoPago);

    List<CuentaProveedorResponse> listarCuentasConSaldoPendiente();

    List<DeudaProveedorResponse> listarDeudasPendientesPorProveedor(Long idProveedor);

    PageResponse<DeudaProveedorResponse> listarDeudasPaginadas(String searchTerm, Pageable pageable);
}
