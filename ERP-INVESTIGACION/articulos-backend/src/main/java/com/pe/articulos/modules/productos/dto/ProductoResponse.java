package com.pe.articulos.modules.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pe.articulos.modules.catalogo.dto.CatalogoResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponse {

    private Long idProducto;
    private Long idSucursal;
    private Long idCatalogo;
    private String codigoBarra;
    private String codDigemid;
    private String presentacion;
    private Long idLaboratorio;
    private String laboratorio;

    // Gestión de Inventario RobustambreAlmacen;

    private Long idAlmacen;
    private String nombreAlmacen;
    private Long idUbicacion;

    private BigDecimal stock;

    private BigDecimal precioVentaUnitario;

    private String tipoGananciaUnidad;
    private BigDecimal gananciaUnidad;
    private BigDecimal gananciaUnidadMin;

    private BigDecimal precioUnitarioMin;

    private String tipoGananciaBlister;
    private BigDecimal gananciaBlister;
    private BigDecimal gananciaBlisterMin;

    private String tipoGananciaCaja;
    private BigDecimal gananciaCaja;
    private BigDecimal gananciaCajaMin;
    private Boolean manejaUnidad;
    private Boolean manejaBlister;
    private Boolean manejaCaja;
    private Boolean manejaLote;

    private Integer factorBlister;
    private BigDecimal precioVentaBlister;
    private BigDecimal precioBlisterMin;
    private Integer factorCaja;
    private BigDecimal precioVentaCaja;
    private BigDecimal precioCajaMin;

    private String nroLote;
    private LocalDate fechaVencimiento;
    private String registroInvimaLote;
    private Integer diasAlertaVencimiento;
    private LocalDateTime fechaReg;
    private LocalDateTime ultimaActualizacion;
    private String usuarioCrea;
    private CatalogoResponse catalogo;

    private BigDecimal precioCompra;
    private BigDecimal stockKardex;
    private BigDecimal pmp;

    private Long idProveedor;
    private String proveedorRazonSocial;
}
