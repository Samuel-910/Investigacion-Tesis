package com.pe.articulos.modules.venta_registro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrelatividadDTO {
    private String tipoDoc;
    private String serie;
    private Integer desde;
    private Integer hasta;
    private Long cantidad;
    private String tipoDocDescripcion;

    private java.math.BigDecimal baseImp;
    private java.math.BigDecimal igv;
    private java.math.BigDecimal valorExo;
    private java.math.BigDecimal valorInaf;
    private java.math.BigDecimal total;
}

