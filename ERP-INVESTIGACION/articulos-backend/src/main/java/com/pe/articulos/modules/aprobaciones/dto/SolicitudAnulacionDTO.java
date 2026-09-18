package com.pe.articulos.modules.aprobaciones.dto;

import com.pe.articulos.modules.aprobaciones.entity.SolicitudesAnulacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SolicitudAnulacionDTO {
    private Long id;
    private SolicitudesAnulacion.TipoSolicitud tipo;
    private Long referenciaId;
    private String motivo;
    private String usuarioSolicita;
    private LocalDateTime fechaSolicitud;
    private SolicitudesAnulacion.EstadoSolicitud estado;
    private String usuarioAtiende;
    private LocalDateTime fechaAtiende;
    private String observacionAtiende;

    // Información extra para la vista de aprobación
    private String documentoReferencia; // Serie-Numero
    private String clienteProveedor;
    private java.math.BigDecimal monto;
    private String tipoDocumento;
    private Long sucursalId;
    private Long puntoId;
}
