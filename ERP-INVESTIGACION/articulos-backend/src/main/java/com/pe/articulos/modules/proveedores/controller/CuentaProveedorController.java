package com.pe.articulos.modules.proveedores.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.proveedores.dto.CuentaProveedorResponse;
import com.pe.articulos.modules.proveedores.dto.CuentaProveedorMovimientoResponse;
import com.pe.articulos.modules.proveedores.dto.DeudaProveedorResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.proveedores.service.CuentaProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuentas-proveedor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CuentaProveedorController {

        private final CuentaProveedorService cuentaService;

        @GetMapping("/proveedor/{id}")
        public ResponseEntity<ApiResponse<CuentaProveedorResponse>> obtenerCuenta(@PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.<CuentaProveedorResponse>builder()
                                .success(true)
                                .data(cuentaService.obtenerCuentaPorProveedor(id))
                                .build());
        }

        @GetMapping("/proveedor/{id}/movimientos")
        public ResponseEntity<ApiResponse<PageResponse<CuentaProveedorMovimientoResponse>>> listarMovimientos(
                        @PathVariable Long id, Pageable pageable) {
                return ResponseEntity.ok(ApiResponse.<PageResponse<CuentaProveedorMovimientoResponse>>builder()
                                .success(true)
                                .data(cuentaService.listarMovimientos(id, pageable))
                                .build());
        }

        @PostMapping("/proveedor/{id}/movimiento")
        public ResponseEntity<ApiResponse<CuentaProveedorMovimientoResponse>> registrarMovimiento(
                        @PathVariable Long id,
                        @RequestBody Map<String, Object> payload) {

                String tipo = (String) payload.get("tipo");
                BigDecimal monto = new BigDecimal(payload.get("monto").toString());
                String descripcion = (String) payload.get("descripcion");

                String idUser = (String) payload.get("idUser");
                Long idSucursal = payload.get("idSucursal") != null ? Long.valueOf(payload.get("idSucursal").toString())
                                : null;
                boolean pagarDesdeCajaGeneral = payload.get("pagarDesdeCajaGeneral") != null
                                && (boolean) payload.get("pagarDesdeCajaGeneral");
                String metodoPago = (String) payload.get("metodoPago");

                return ResponseEntity.ok(ApiResponse.<CuentaProveedorMovimientoResponse>builder()
                                .success(true)
                                .message("Movimiento registrado correctamente")
                                .data(cuentaService.registrarMovimiento(id, tipo, monto, descripcion, idUser,
                                                idSucursal,
                                                pagarDesdeCajaGeneral, metodoPago))
                                .build());
        }

        @GetMapping("/pendientes")
        public ResponseEntity<ApiResponse<List<CuentaProveedorResponse>>> listarPendientes() {
                return ResponseEntity.ok(ApiResponse.<List<CuentaProveedorResponse>>builder()
                                .success(true)
                                .data(cuentaService.listarCuentasConSaldoPendiente())
                                .build());
        }

        @GetMapping("/proveedor/{id}/deudas")
        public ResponseEntity<ApiResponse<List<DeudaProveedorResponse>>> listarDeudasPendientes(@PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.<List<DeudaProveedorResponse>>builder()
                                .success(true)
                                .data(cuentaService.listarDeudasPendientesPorProveedor(id))
                                .build());
        }

        @GetMapping("/deudas/pendientes/paginadas")
        public ResponseEntity<ApiResponse<PageResponse<DeudaProveedorResponse>>> listarDeudasPendientesPaginadas(
                        @RequestParam(required = false) String searchTerm,
                        Pageable pageable) {
                return ResponseEntity.ok(ApiResponse.<PageResponse<DeudaProveedorResponse>>builder()
                                .success(true)
                                .data(cuentaService.listarDeudasPaginadas(searchTerm, pageable))
                                .build());
        }
}
