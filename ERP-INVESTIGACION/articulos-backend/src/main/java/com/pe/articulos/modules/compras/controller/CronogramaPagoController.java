package com.pe.articulos.modules.compras.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.compras.dto.CronogramaPagoResponseDTO;
import com.pe.articulos.modules.compras.service.CronogramaPagosService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/cronograma-pagos")
@RequiredArgsConstructor
public class CronogramaPagoController {

    private final CronogramaPagosService cronogramaPagoService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CronogramaPagoResponseDTO>>> listarDeudas(
            @RequestParam Map<String, Object> params,
            Pageable pageable) {
        PageResponse<CronogramaPagoResponseDTO> page = cronogramaPagoService.listarDeudas(params, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PostMapping("/{idCronograma}/pagar")
    public ResponseEntity<ApiResponse<CronogramaPagoResponseDTO>> pagarCuota(
            @PathVariable Long idCronograma,
            @RequestParam("numeroOperacion") String numeroOperacion,
            @RequestParam(value = "voucher", required = false) MultipartFile voucher) {

        CronogramaPagoResponseDTO cuota = cronogramaPagoService.registrarPagoCuota(idCronograma, numeroOperacion, voucher);
        return ResponseEntity.ok(ApiResponse.success(cuota));
    }

    @PostMapping("/{idCronograma}/solicitar")
    public ResponseEntity<ApiResponse<CronogramaPagoResponseDTO>> solicitarPago(@PathVariable Long idCronograma) {
        CronogramaPagoResponseDTO cuota = cronogramaPagoService.solicitarPago(idCronograma);
        return ResponseEntity.ok(ApiResponse.success(cuota));
    }

    @PostMapping("/solicitar/compra/{idCompra}")
    public ResponseEntity<ApiResponse<CronogramaPagoResponseDTO>> solicitarPagoCompra(
            @PathVariable Long idCompra,
            @RequestBody(required = false) com.pe.articulos.modules.compras.dto.SolicitudPagoCompraRequest request) {
        CronogramaPagoResponseDTO cuota = cronogramaPagoService.generarCronograma(idCompra, request);
        return ResponseEntity.ok(ApiResponse.success(cuota));
    }
}
