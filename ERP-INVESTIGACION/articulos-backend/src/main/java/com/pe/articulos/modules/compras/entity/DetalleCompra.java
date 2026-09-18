
package com.pe.articulos.modules.compras.entity;

import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.catalogo.entity.UnidadMedida;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_compra", indexes = @Index(name = "idx_detalle_compra_estado", columnList = "estado"))
@SQLRestriction("estado != 3")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Catalogo producto;

    @Column(name = "producto_nombre", length = 255)
    private String productoNombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "id_stock_producto")
    private Long idProductoStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad_medida")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private UnidadMedida unidadMedida;

    @Column(name = "factor_conversion")
    @Builder.Default
    private Integer factorConversion = 1; // Factor para convertir a unidad base

    @Column(length = 255)
    private String descripcion;

    // -- Datos de Farmacia --
    @Column(length = 50)
    private String lote;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(length = 100)
    private String presentacion;

    // -- Precios y Descuentos --
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioUnitario; // Sin IGV, Valor Unitario

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(name = "porcentaje_descuento2", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuento2 = BigDecimal.ZERO;

    @Column(name = "es_bonificacion")
    @Builder.Default
    private Boolean esBonificacion = false;

    @Column(name = "tipo_afectacion", length = 20)
    @Builder.Default
    private String tipoAfectacion = "GRAVADO_ONEROSO"; // GRAVADO_ONEROSO, INAFECTO_ONEROSO

    @Column(name = "valor_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorVenta; // Total línea

    @Column(name = "base_imp", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal baseImp = BigDecimal.ZERO;

    @Column(name = "igv", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(name = "valor_exo", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorExo = BigDecimal.ZERO;

    @Column(name = "valor_inaf", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorInaf = BigDecimal.ZERO;

    @Column(name = "igv_descuento", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal igvDescuento = BigDecimal.ZERO;

    @Column(name = "registrado_en_almacen")
    @Builder.Default
    private Boolean registradoEnAlmacen = false;

    @Column(nullable = false, length = 20)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @CreatedBy
    @Column(name = "usuario_creacion", updatable = false, length = 50)
    private String usuarioCreacion;

    @LastModifiedBy
    @Column(name = "usuario_modificacion", length = 50)
    private String usuarioModificacion;

    @CreatedDate
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
