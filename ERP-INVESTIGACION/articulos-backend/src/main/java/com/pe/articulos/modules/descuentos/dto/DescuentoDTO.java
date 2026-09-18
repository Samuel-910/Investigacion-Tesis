package com.pe.articulos.modules.descuentos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescuentoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Long idCompania;
    private String nombreCompania;
    private String usuariosAfectados;
    private String tipoAlcance;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activo;
    private List<DescuentoDetalleDTO> detalles;
}
