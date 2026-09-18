package com.pe.articulos.modules.productos.dto;

import lombok.Data;
import java.util.List;

@Data
public class MovimientoDiversoBatchRequest {
    private Long idSucursal;
    private Long idUsuario;
    private String motivo;
    private String serie;
    private Integer numero;
    private String estado;
    private Long idMovimiento;
    private Long idPlantilla;
    private List<MovimientoDiversoDetalleRequest> detalles;
}
