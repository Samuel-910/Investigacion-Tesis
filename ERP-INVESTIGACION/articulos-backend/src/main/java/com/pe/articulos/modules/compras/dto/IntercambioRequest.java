package com.pe.articulos.modules.compras.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class IntercambioRequest {
    private Long idProductoOrigen; // id_producto (stock entry)
    private BigDecimal cantidad; // Cantidad a entregar (origen)
    private BigDecimal cantidadDestino; // Cantidad a recibir (destino)
    private Long idCatalogoDestino; // id_catalogo (master product)
    private Long idProveedor; // id_proveedor (original provider)
    private String loteDestino;
    private LocalDate fechaVencDestino;
    private BigDecimal precioCompraDestino;
    private String motivo;
}
