package com.pe.articulos.modules.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SireVentaDTO {
    private String periodo;
    private String carSunat;
    private LocalDate fecha;
    private LocalDate fechaVencimiento;
    private String tipoDoc;
    private String serie;
    private String numero;
    private String numeroFinal;
    private String nroDocCli;
    private String cliente;
    
    // Importes
    private BigDecimal exportacion;
    private BigDecimal baseImponible;
    private BigDecimal descuentoBase;
    private BigDecimal igv;
    private BigDecimal descuentoIgv;
    private BigDecimal exonerado;
    private BigDecimal inafecto;
    private BigDecimal isc;
    private BigDecimal baseArroz;
    private BigDecimal igvArroz;
    private BigDecimal icbper;
    private BigDecimal otrosConceptos;
    private BigDecimal total;
    
    // Moneda y Cambio
    private String moneda;
    private BigDecimal tipoCambio;
    
    // Referencia (Notas de Crédito)
    private LocalDate fechaRef;
    private String tipoRef;
    private String serieRef;
    private String numeroRef;

    private EstadoGeneral estado;
}
