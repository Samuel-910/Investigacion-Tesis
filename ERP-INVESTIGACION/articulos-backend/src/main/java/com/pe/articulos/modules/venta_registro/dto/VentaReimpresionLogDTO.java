package com.pe.articulos.modules.venta_registro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaReimpresionLogDTO {
    private Long id;
    private Long idVenta;
    private Integer idUsuario;
    private String nombreUsuario;
    private LocalDateTime fechaReimpresion;
    private String ip;
    private String motivo;
}
