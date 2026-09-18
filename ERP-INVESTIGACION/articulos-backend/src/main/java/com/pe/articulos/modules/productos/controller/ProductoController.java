package com.pe.articulos.modules.productos.controller;

import com.pe.articulos.modules.productos.dto.ProductoRequest;
import com.pe.articulos.modules.productos.dto.ProductoResponse;
import com.pe.articulos.modules.productos.services.ProductoService;
import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.productos.dto.StockValorizadoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@Tag(name = "Productos por Sucursal", description = "API para gestionar inventario de productos por sucursal")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

        private final ProductoService service;

        @Operation(summary = "Crear producto", description = "Registra un producto en una sucursal específica")
        @PostMapping
        public ResponseEntity<ApiResponse<ProductoResponse>> crear(
                        @Valid @RequestBody ProductoRequest request) {

                ProductoResponse response = service.crear(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ProductoResponse>builder()
                                                .success(true)
                                                .message("Producto creado exitosamente")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Obtener producto por ID")
        @GetMapping("/{idProducto}")
        public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorId(@PathVariable Long idProducto) {

                ProductoResponse response = service.obtenerPorId(idProducto);

                return ResponseEntity.ok(ApiResponse.<ProductoResponse>builder()
                                .success(true)
                                .message("Producto encontrado")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Obtener producto por ID y sucursal")
        @GetMapping("/{idProducto}/sucursal/{idSucursal}")
        public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorIdYSucursal(
                        @PathVariable Long idProducto,
                        @PathVariable Long idSucursal) {

                ProductoResponse response = service.obtenerPorIdYSucursal(idProducto, idSucursal);

                return ResponseEntity.ok(ApiResponse.<ProductoResponse>builder()
                                .success(true)
                                .message("Producto encontrado")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Listar productos por sucursal")
        @GetMapping("/sucursal/{idSucursal}")
        public ResponseEntity<ApiResponse<PageResponse<ProductoResponse>>> listarPorSucursal(
                        @PathVariable Long idSucursal,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "false") boolean soloParaVenta,
                        @RequestParam(defaultValue = "idProducto") String sortBy,
                        @RequestParam(defaultValue = "ASC") String direction) {

                Sort.Direction sortDirection = Sort.Direction.fromString(direction != null ? direction : "ASC");
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

                PageResponse<ProductoResponse> response = service.listarPorSucursal(idSucursal, pageable, soloParaVenta);

                return ResponseEntity.ok(ApiResponse.<PageResponse<ProductoResponse>>builder()
                                .success(true)
                                .message("Productos obtenidos exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Listar productos (un solo lote representativo por catálogo) para configuración de descuentos")
        @GetMapping("/sucursal/{idSucursal}/para-descuentos")
        public ResponseEntity<ApiResponse<PageResponse<ProductoResponse>>> listarParaDescuentos(
                        @PathVariable Long idSucursal,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "50") int size,
                        @RequestParam(defaultValue = "idCatalogo") String sortBy,
                        @RequestParam(defaultValue = "ASC") String direction) {

                Sort.Direction sortDirection = Sort.Direction.fromString(direction != null ? direction : "ASC");
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

                PageResponse<ProductoResponse> response = service.listarParaDescuentos(idSucursal, pageable);

                return ResponseEntity.ok(ApiResponse.<PageResponse<ProductoResponse>>builder()
                                .success(true)
                                .message("Productos para descuentos obtenidos exitosamente")
                                .data(response)
                                .build());
        }


        @Operation(summary = "Listar vendedores (usuarios que crearon productos) por sucursal")
        @GetMapping("/sucursal/{idSucursal}/vendedores")
        public ResponseEntity<ApiResponse<List<String>>> listarVendedores(
                        @PathVariable Long idSucursal) {

                List<String> response = service.listarVendedores(idSucursal);

                return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                                .success(true)
                                .message("Vendedores obtenidos exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Obtener stock valorizado por sucursal")
        @GetMapping("/sucursal/{idSucursal}/stock-valorizado")
        public ResponseEntity<ApiResponse<?>> obtenerStockValorizado(
                        @PathVariable Long idSucursal,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size) {

                if (page != null && size != null) {
                        Pageable pageable = PageRequest.of(page, size);
                        PageResponse<StockValorizadoDTO> response = service
                                        .obtenerStockValorizadoPaginado(idSucursal, pageable);
                        return ResponseEntity.ok(ApiResponse.builder()
                                        .success(true)
                                        .message("Reporte de stock valorizado paginado obtenido exitosamente")
                                        .data(response)
                                        .build());
                } else {
                        List<StockValorizadoDTO> response = service
                                        .obtenerStockValorizado(idSucursal);
                        return ResponseEntity.ok(ApiResponse.builder()
                                        .success(true)
                                        .message("Reporte de stock valorizado obtenido exitosamente")
                                        .data(response)
                                        .build());
                }
        }

        @Operation(summary = "Listar productos por catálogo")
        @GetMapping("/catalogo/{idCatalogo}")
        public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarPorCatalogo(
                        @PathVariable Long idCatalogo) {

                List<ProductoResponse> response = service.listarPorCatalogo(idCatalogo);

                return ResponseEntity.ok(ApiResponse.<List<ProductoResponse>>builder()
                                .success(true)
                                .message("Productos del catálogo obtenidos exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Listar productos por catálogo y sucursal")
        @GetMapping("/catalogo/{idCatalogo}/sucursal/{idSucursal}")
        public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarPorCatalogoYSucursal(
                        @PathVariable Long idCatalogo,
                        @PathVariable Long idSucursal) {

                List<ProductoResponse> response = service.listarPorCatalogoYSucursal(idCatalogo, idSucursal);

                return ResponseEntity.ok(ApiResponse.<List<ProductoResponse>>builder()
                                .success(true)
                                .message("Productos del catálogo en la sucursal obtenidos exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Actualizar producto")
        @PutMapping("/{idProducto}")
        public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(
                        @PathVariable Long idProducto,
                        @Valid @RequestBody ProductoRequest request) {

                ProductoResponse response = service.actualizar(idProducto, request);

                return ResponseEntity.ok(ApiResponse.<ProductoResponse>builder()
                                .success(true)
                                .message("Producto actualizado exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Eliminar producto")
        @DeleteMapping("/{idProducto}")
        public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long idProducto) {

                service.eliminar(idProducto);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Producto eliminado exitosamente")
                                .data(null)
                                .build());
        }

        @Operation(summary = "Listar productos próximos a vencer")
        @GetMapping("/sucursal/{idSucursal}/proximos-vencer")
        public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarProductosProximosAVencer(
                        @PathVariable Long idSucursal,
                        @RequestParam(defaultValue = "90") Integer dias) {

                List<ProductoResponse> response = service.listarProductosProximosAVencer(idSucursal, dias);

                return ResponseEntity.ok(ApiResponse.<List<ProductoResponse>>builder()
                                .success(true)
                                .message("Productos próximos a vencer obtenidos exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Listar productos vencidos")
        @GetMapping("/sucursal/{idSucursal}/vencidos")
        public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarProductosVencidos(
                        @PathVariable Long idSucursal) {

                List<ProductoResponse> response = service.listarProductosVencidos(idSucursal);

                return ResponseEntity.ok(ApiResponse.<List<ProductoResponse>>builder()
                                .success(true)
                                .message("Productos vencidos obtenidos exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Listar productos sin stock")
        @GetMapping("/sucursal/{idSucursal}/sin-stock")
        public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarProductosSinStock(
                        @PathVariable Long idSucursal) {

                List<ProductoResponse> response = service.listarProductosSinStock(idSucursal);

                return ResponseEntity.ok(ApiResponse.<List<ProductoResponse>>builder()
                                .success(true)
                                .message("Productos sin stock obtenidos exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Buscar productos por sucursal")
        @GetMapping("/buscar")
        public ResponseEntity<ApiResponse<PageResponse<ProductoResponse>>> buscar(
                        @RequestParam String q,
                        @RequestParam Long idSucursal,
                        @RequestParam(required = false) String tipo,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "false") boolean soloParaVenta) {

                Pageable pageable = PageRequest.of(page, size);
                PageResponse<ProductoResponse> response = service.buscar(q, idSucursal, tipo, pageable, soloParaVenta);

                return ResponseEntity.ok(ApiResponse.<PageResponse<ProductoResponse>>builder()
                                .success(true)
                                .message("Búsqueda completada")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Búsqueda avanzada de productos")
        @GetMapping("/search-advanced")
        public ResponseEntity<ApiResponse<PageResponse<ProductoResponse>>> searchAdvanced(
                        @RequestParam Long idSucursal,
                        @RequestParam(required = false) String usuarioCrea,
                        @RequestParam(required = false) LocalDate fechaDesde,
                        @RequestParam(required = false) LocalDate fechaHasta,
                        @RequestParam(required = false) Boolean rotaMas,
                        @RequestParam(required = false) Boolean queEntra,
                        @RequestParam(required = false) Boolean cercaVencer,
                        @RequestParam(required = false) Boolean faltanPrecio,
                        @RequestParam(required = false) Boolean listosVender,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "idProducto") String sortBy,
                        @RequestParam(defaultValue = "ASC") String direction) {

                Sort.Direction sortDirection = Sort.Direction.fromString(direction != null ? direction : "ASC");
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
                
                PageResponse<ProductoResponse> response = service.buscarAvanzado(idSucursal, usuarioCrea, fechaDesde,
                                fechaHasta, rotaMas, queEntra, cercaVencer, faltanPrecio, listosVender, pageable);

                return ResponseEntity.ok(ApiResponse.<PageResponse<ProductoResponse>>builder()
                                .success(true)
                                .message("Búsqueda avanzada completada")
                                .data(response)
                                .build());
        }
}
