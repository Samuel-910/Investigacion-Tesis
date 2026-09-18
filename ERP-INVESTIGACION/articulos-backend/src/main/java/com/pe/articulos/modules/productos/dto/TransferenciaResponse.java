package com.pe.articulos.modules.productos.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaResponse {
    private Long id;
    private Long idSucursalOrigen;
    private String sucursalOrigenNombre;
    private Long idSucursalDestino;
    private String sucursalDestinoNombre;
    private String estado;
    
    private Long idUsuarioSolicita;
    private String usuarioSolicitaNombre;
    private Long idUsuarioEnvia;
    private String usuarioEnviaNombre;
    private Long idUsuarioRecibe;
    private String usuarioRecibeNombre;
    private Long idUsuarioCancela;
    private String usuarioCancelaNombre;
    
    private String motivo;
    
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRecepcion;
    private LocalDateTime fechaCancelacion;
    
    private List<TransferenciaDetalleResponse> detalles;
}
