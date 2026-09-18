package com.pe.articulos.modules.productos.dto;

import lombok.Data;
import java.util.List;

@Data
public class TransferenciaRequest {
    private Long idSucursalDestino; // Quien solicita
    private Long idSucursalOrigen; // A quien le pide
    private String motivo;
    private Long idUsuario;
    private List<TransferenciaDetalleRequest> detalles;
}
