package com.pe.articulos.modules.venta_registro.dto;

import java.util.List;
import lombok.Data;

@Data
public class DevolucionRequest {
    private Long idVenta;
    private String motivo;
    private String motivoSunat;
    private List<DevolucionDetalleRequest> detalles;
}
