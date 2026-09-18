package com.pe.articulos.modules.almacen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenResponse {
    private Long id;
    private String nombre;
    private String codigo;
    private String ubicacion;
    private Boolean esPrincipal;
    private String responsable;
    private EstadoGeneral estado;
    private Long idSucursal;
    private String nombreSucursal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
