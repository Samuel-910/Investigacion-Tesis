package com.pe.articulos.modules.notificaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {
    private Long idNotificacion;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String referenciaId;
    private Boolean leido;
    private LocalDateTime fechaCreacion;
    private Long idSucursal;
}
