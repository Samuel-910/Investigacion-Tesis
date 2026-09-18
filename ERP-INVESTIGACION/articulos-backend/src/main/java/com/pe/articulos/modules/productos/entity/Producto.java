package com.pe.articulos.modules.productos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.almacen.entity.Almacen;
import com.pe.articulos.modules.atributos.entity.Ubicacion;
import com.pe.articulos.modules.atributos.entity.Laboratorio;
import com.pe.articulos.modules.proveedores.entity.Proveedor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "productos", indexes = {
        @Index(name = "idx_producto_sucursal", columnList = "id_sucursal"),
        @Index(name = "idx_producto_catalogo", columnList = "id_catalogo"),
        @Index(name = "idx_producto_codigo_barra", columnList = "codigo_barra"),
        @Index(name = "idx_producto_laboratorio", columnList = "id_laboratorio")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @Column(name = "id_catalogo", nullable = false)
    private Long idCatalogo;

    @Column(name = "codigo_barra", length = 50)
    private String codigoBarra;

    @Column(name = "cod_digemid", length = 50)
    private String codDigemid;

    @Column(name = "presentacion", length = 100)
    private String presentacion;

    @Column(name = "id_laboratorio")
    private Long idLaboratorio;
    // Empaque y Fraccionamiento
    @Column(name = "tipo_blister")
    private Integer tipoBlister;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_laboratorio", insertable = false, updatable = false)
    private Laboratorio laboratorio;

    @Column(name = "id_proveedor")
    private Long idProveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", insertable = false, updatable = false)
    private Proveedor proveedor;

    @Column(name = "id_ubicacion")
    private Long idUbicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion", insertable = false, updatable = false)
    private Ubicacion ubicacion;

    @Column(name = "precio_venta_unitario", precision = 12, scale = 2)
    private BigDecimal precioVentaUnitario;

    @Column(name = "tipo_ganancia_unidad")
    private String tipoGananciaUnidad;

    @Column(name = "ganancia_unidad", precision = 5, scale = 2)
    private BigDecimal gananciaUnidad;

    @Column(name = "ganancia_unidad_min", precision = 5, scale = 2)
    private BigDecimal gananciaUnidadMin;

    @Column(name = "precio_unitario_min", precision = 12, scale = 2)
    private BigDecimal precioUnitarioMin;

    @Column(name = "maneja_unidad")
    @Builder.Default
    private Boolean manejaUnidad = true;

    @Column(name = "maneja_blister")
    @Builder.Default
    private Boolean manejaBlister = false;

    @Column(name = "maneja_caja")
    @Builder.Default
    private Boolean manejaCaja = false;

    @Column(name = "stock", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stock = BigDecimal.ZERO;

    @Column(name = "factor_blister")
    @Builder.Default
    private Integer factorBlister = 1;

    @Column(name = "precio_blister_min", precision = 12, scale = 2)
    private BigDecimal precioBlisterMin;

    @Column(name = "ganancia_blister_min", precision = 5, scale = 2)
    private BigDecimal gananciaBlisterMin;

    @Column(name = "tipo_ganancia_blister")
    private String tipoGananciaBlister;

    @Column(name = "ganancia_blister", precision = 5, scale = 2)
    private BigDecimal gananciaBlister;

    @Column(name = "factor_caja")
    @Builder.Default
    private Integer factorCaja = 1;

    @Column(name = "precio_caja_min", precision = 12, scale = 2)
    private BigDecimal precioCajaMin;

    @Column(name = "ganancia_caja_min", precision = 5, scale = 2)
    private BigDecimal gananciaCajaMin;

    @Column(name = "tipo_ganancia_caja")
    private String tipoGananciaCaja;

    @Column(name = "ganancia_caja", precision = 5, scale = 2)
    private BigDecimal gananciaCaja;

    @Column(name = "precio_compra", precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "fecha_venc")
    private LocalDate fechaVencimiento;

    @Column(name = "registro_invima_lote", length = 50)
    private String registroInvimaLote;

    @Column(name = "nro_lote", length = 50)
    private String nroLote;

    @Column(name = "dias_alerta_vencimiento")
    @Builder.Default
    private Integer diasAlertaVencimiento = 90;

    @Column(name = "precio_venta_blister", precision = 12, scale = 2)
    private BigDecimal precioVentaBlister;

    @Column(name = "precio_venta_caja", precision = 12, scale = 2)
    private BigDecimal precioVentaCaja;

    @CreatedDate
    @Column(name = "fecha_reg", updatable = false)
    private LocalDateTime fechaReg;

    @Column(name = "usuario_crea", length = 50)
    private String usuarioCrea;

    @LastModifiedDate
    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    // Relación con ProductoServicio (catálogo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", insertable = false, updatable = false)
    private Catalogo catalogo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_almacen", insertable = false, updatable = false)
    private Almacen almacen;

    @Column(name = "id_almacen")
    private Long idAlmacen;

    @Column(name = "stock_minimo", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "stock_maximo", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockMaximo = BigDecimal.ZERO;

    @Column(name = "stock_real", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockReal = BigDecimal.ZERO;

    // Stocks Específicos
    @Column(name = "stock_unidad", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockUnidad = BigDecimal.ZERO;

    @Column(name = "stock_blister", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockBlister = BigDecimal.ZERO;

    @Column(name = "stock_caja", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockCaja = BigDecimal.ZERO;
    @Column(name = "factor_compra")
    @Builder.Default
    private Integer factorCompra = 1;

    @Column(name = "tipo_afectacion_compra")
    @Builder.Default
    private String tipoAfectacionCompra = "GRAVADO";

    @JsonProperty("tipo")
    private String tipo;

    @JsonProperty("idTipoTemperatura")
    private Long idTipoTemperatura;

    @JsonProperty("codigo")
    private String codigo;

    @Transient
    public String getCodigo() {
        if (this.codigo != null)
            return this.codigo;
        return this.catalogo != null ? this.catalogo.getCodigo() : null;
    }

}
