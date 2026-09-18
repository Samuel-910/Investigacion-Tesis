package com.pe.articulos.modules.compras.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.compras.dto.DetalleCompraResponse;
import com.pe.articulos.modules.compras.service.DetalleCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detalle-compra")
@RequiredArgsConstructor
public class DetalleCompraController {

    private final DetalleCompraService detalleCompraService;

    @GetMapping("/compra/{idCompra}")
    public ResponseEntity<ApiResponse<PageResponse<DetalleCompraResponse>>> listarPorCompra(@PathVariable Long idCompra, Pageable pageable) {
        PageResponse<DetalleCompraResponse> response = detalleCompraService.listarPorCompra(idCompra, pageable);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DetalleCompraResponse>> obtener(@PathVariable Long id) {
        DetalleCompraResponse response = detalleCompraService.obtener(id);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.OK);
    }
}
