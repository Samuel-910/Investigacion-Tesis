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
public class MovimientoDiversoResponse {
    private Long id;
    private LocalDateTime fecha;
    private String motivo;
    private String estado;
    private String numDocumento;
    private String serie;
    private Integer numero;
    private Long idSucursal;
    private String sucursalNombre;
    private Long idUsuario;
    private String usuarioNombre;
    private List<MovimientoDiversoDetalleResponse> detalles;
}
