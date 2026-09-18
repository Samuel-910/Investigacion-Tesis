package com.pe.articulos.modules.venta_registro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaRegistroDTO {

    private Long idVenta;
    private String idPersonal;
    private LocalDate fecha;

    private String punto;
    private String tipoDoc;
    private String serie;
    private Integer numero;
    private String metodoPago;

    private String tipoPac;
    private String moneda;
    private BigDecimal tc;
    private BigDecimal impVta;
    private BigDecimal igv;
    private BigDecimal igvDescuento;
    private BigDecimal total;
    private BigDecimal baseImp;
    private BigDecimal valorInaf;
    private BigDecimal valorExo;
    private BigDecimal descuento; // Descuentos aplicados
    private String idMedico;

    private EstadoGeneral estado;
    private String idUser;
    private String observacion; // Notas adicionales

    private Long idSucursal;
    private String idAlmacen;
    private String nroDni;
    private String nombrePac;
    private String tipoDni;
    private String ruc;
    private String razon;
    private String direcRuc;
    private String nhc;
    private String codAfi;
    private String autorizador;
    private java.time.LocalDateTime fechaAutoriza;
    private String refMotivo;
    private String refPac;
    private String refDoc;
    private String refFono;
    private String voucher;

    private BigDecimal copago;
    private BigDecimal ivap;
    private BigDecimal descuentoEsp;

    private String caja;
    private String banco;
    private String cuenta;
    private BigDecimal importePago;
    private BigDecimal vuelto;
    private String totalLetras;

    private String idPersonalDig;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String ip;

    @Builder.Default
    private List<VentaDetalleDTO> detalles = new ArrayList<>();

    private String nombrePaciente;
    private String estadoDescripcion;
    private String numeroDocumento; // Formato: SERIE-NUMERO (ej. F001-5041)
    private String nombreVendedor;
    private String emailVendedor;
    private String rolVendedor;
    private String serieTicketera;
    private String concepto;
    private String obs;
    private String direccion;
    private String celularSucursal;
}

