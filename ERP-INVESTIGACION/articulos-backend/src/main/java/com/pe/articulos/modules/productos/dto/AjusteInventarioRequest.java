package com.pe.articulos.modules.productos.dto;

import lombok.Data;
import java.util.List;

@Data
public class AjusteInventarioRequest {
    private Long idSucursal;
    private String tipo; // INGRESO, SALIDA
    private String motivo;
    private Long idUsuario;
    private Long idClasificacion;
    private List<AjusteInventarioDetalleRequest> detalles;
}
