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
public class AjusteInventarioResponse {
    private Long id;
    private Long idSucursal;
    private String tipo;
    private String motivo;
    private Long idUsuario;
    private String correlativo;
    private LocalDateTime fechaRegistro;
    private ClasificacionMovimientoResponse clasificacion;
    private List<AjusteInventarioDetalleResponse> detalles;
}
