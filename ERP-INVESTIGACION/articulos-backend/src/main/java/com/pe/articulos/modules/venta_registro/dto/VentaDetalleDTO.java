package com.pe.articulos.modules.venta_registro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.pe.articulos.core.enums.EstadoGeneral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalleDTO {

    private Long idDetalle;
    private Long idVenta;
    private Long idCatalogo;
    private Integer item;
    private String unidadMedida;

    private BigDecimal cantidad;
    private String descripcion;
    private BigDecimal precioUnitario;
    private BigDecimal precioVenta;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private BigDecimal baseImp;
    private BigDecimal valorInaf;
    private BigDecimal valorExo;
    private BigDecimal igv;
    private BigDecimal igvDescuento;
    private BigDecimal total;

    private BigDecimal porcentajeDescuento;
    private BigDecimal montoDescuento;
    private BigDecimal porcDsc;
    private BigDecimal descuento;

    private String nroLote;
    private LocalDate fechaVenc;
    private String codDigemid;
    private String idAlmart;

    private String idMedicoSer;
    private String idMedicoRec;
    private String consultorio;
    private String habit;
    private String cama;
    private String tipoAtencion;
    private String idCita;
    private String prioridad;
    private String muestra;

    private BigDecimal copago;
    private String tipoCopago;
    private BigDecimal copagoConIgv;
    private BigDecimal cobertura;

    private EstadoGeneral estado;
    private String idOrden; // Vinculación con orden de laboratorio
    private String idPersonalDig;
    private java.time.LocalDateTime createdAt;
    private String motivoModif;
    private String idArticulo;

    @Deprecated
    private Long idMovart; // Usar idDetalle
    @Deprecated
    private String glosa; // Usar descripcion

    private String nombreExamen;
    private String nombreMedico;
    private java.math.BigDecimal cantidadDevuelta;
}

