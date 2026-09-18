package com.pe.articulos.modules.productos.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.productos.dto.TransferenciaDetalleRequest;
import com.pe.articulos.modules.productos.dto.TransferenciaRequest;
import com.pe.articulos.modules.productos.dto.TransferenciaResponse;
import com.pe.articulos.modules.productos.services.TransferenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario/transferencia")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("Transferencia encontrada", transferenciaService.obtenerPorId(id), 200, true));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransferenciaResponse>>> listar(@RequestParam Long idSucursal) {
        return ResponseEntity.ok(new ApiResponse<>("Lista de transferencias",
                transferenciaService.listar(idSucursal), 200, true));
    }

    @PostMapping("/solicitar")
    public ResponseEntity<ApiResponse<Void>> solicitar(@RequestBody TransferenciaRequest request) {
        transferenciaService.solicitar(request);
        return ResponseEntity.ok(new ApiResponse<>("Solicitud de transferencia creada", null, 200, true));
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<ApiResponse<Void>> enviar(
            @PathVariable Long id,
            @RequestParam Long idUsuario,
            @RequestBody List<TransferenciaDetalleRequest> detalles) {
        transferenciaService.enviar(id, idUsuario, detalles);
        return ResponseEntity.ok(new ApiResponse<>("Envío de transferencia registrado", null, 200, true));
    }

    @PostMapping("/{id}/recibir")
    public ResponseEntity<ApiResponse<Void>> recibir(
            @PathVariable Long id,
            @RequestParam Long idUsuario,
            @RequestParam Long idAlmacenDestino) {
        transferenciaService.recibir(id, idUsuario, idAlmacenDestino);
        return ResponseEntity.ok(new ApiResponse<>("Recepción de transferencia registrada", null, 200, true));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<Void>> rechazar(
            @PathVariable Long id,
            @RequestParam Long idUsuario,
            @RequestParam(required = false) String motivo) {
        transferenciaService.rechazar(id, idUsuario, motivo);
        return ResponseEntity.ok(new ApiResponse<>("Transferencia rechazada", null, 200, true));
    }
}
