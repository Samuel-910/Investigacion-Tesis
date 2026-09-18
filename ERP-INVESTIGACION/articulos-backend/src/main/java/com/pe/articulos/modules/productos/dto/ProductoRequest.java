package com.pe.articulos.modules.productos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long idProducto;

    @NotNull(message = "El ID de sucursal es obligatorio")
    private Long idSucursal;

    @NotNull(message = "El ID de catálogo es obligatorio")
    private Long idCatalogo;

    private String codigoBarra;
    private String codDigemid;
    private String presentacion;
    private Long idLaboratorio;

    // --- Ubicación ---
    private Long idUbicacion;

    private Long idAlmacen; // Restored

    @DecimalMin(value = "0.0", message = "El stock no puede ser negativo")
    private BigDecimal stock;

    @DecimalMin(value = "0.0", message = "El precio de venta unitario no puede ser negativo")
    private BigDecimal precioVentaUnitario;

    // Ganancia Unidad
    private String tipoGananciaUnidad;
    private BigDecimal gananciaUnidad;
    private BigDecimal gananciaUnidadMin;

    // Ganancias Blister/Caja
    private String tipoGananciaBlister;
    private BigDecimal gananciaBlister;

    private String tipoGananciaCaja;
    private BigDecimal gananciaCaja;

    // Precios Unitarios (Min/Max ya estaban en ProductoRequest? No, solo precioA/B
    // que parecen ser eso)
    // ProductoRequest lines 86, 87 are precioBlisterMin/Max.
    // I should add precioUnitarioMin/Max if not present.
    // It seems precioA/B were placeholders. I will verify if I should replacing
    // them or just adding.
    // Safe bet: Add new fields. Remove gananciaPorcentaje.

    private BigDecimal precioUnitarioMin;
    // private BigDecimal precioUnitarioMax; // Removed

    private Boolean manejaUnidad;
    private Boolean manejaBlister;
    private Boolean manejaCaja;
    private Boolean manejaLote;

    @Min(value = 1, message = "El factor de blister debe ser al menos 1")
    private Integer factorBlister;

    private BigDecimal precioBlisterMin;
    // private BigDecimal precioBlisterMax; // Removed
    private BigDecimal gananciaBlisterMin;

    @DecimalMin(value = "0.0", message = "El precio de venta blister no puede ser negativo")
    private BigDecimal precioVentaBlister;

    @Min(value = 1, message = "El factor de caja debe ser al menos 1")
    private Integer factorCaja;

    private BigDecimal precioCajaMin;
    // private BigDecimal precioCajaMax; // Removed
    private BigDecimal gananciaCajaMin;

    @DecimalMin(value = "0.0", message = "El precio de venta caja no puede ser negativo")
    private BigDecimal precioVentaCaja;

    @Size(max = 50, message = "El número de lote no puede exceder 50 caracteres")
    private String nroLote;

    @Future(message = "La fecha de vencimiento debe ser futura")
    private LocalDate fechaVencimiento;

    @Size(max = 50, message = "El registro INVIMA del lote no puede exceder 50 caracteres")
    private String registroInvimaLote;

    @Min(value = 0, message = "Los días de alerta de vencimiento no pueden ser negativos")
    private Integer diasAlertaVencimiento;
    private String usuarioCrea;

    private BigDecimal precioCompra;
    private Long idDetalleCompra;
    private Long idProveedor;
}
