package com.pe.articulos.modules.kardex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.pe.articulos.modules.productos.entity.ClasificacionMovimiento;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.pe.articulos.modules.users.entity.DatosPersonales;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "kardex", indexes = {
        @Index(name = "idx_kardex_catalogo", columnList = "id_catalogo"),
        @Index(name = "idx_kardex_sucursal", columnList = "id_sucursal"),
        @Index(name = "idx_kardex_fecha", columnList = "fecha")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kardex {

    @Id
    @Column(name = "id_articulo_kardex", length = 50)
    private String idArticuloKardex; // PK como String según diagrama

    @Column(name = "id_alm_articulo", nullable = false)
    private Long idAlmArticulo; // FK Almacen / Lote específico

    @Column(name = "id_catalogo", nullable = false)
    private Long idCatalogo; // FK Catalogo (Para consolidación)

    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal; // FK Sucursal

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario; // FK Usuario

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIncludeProperties({ "id", "nombre" })
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private com.pe.articulos.modules.catalogo.entity.Catalogo producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", insertable = false, updatable = false)
    @JsonIncludeProperties({ "idSucursal", "nombreSucursal" })
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", insertable = false, updatable = false)
    @JsonIncludeProperties({ "id", "nombreCompleto" })
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private DatosPersonales usuario;

    @Column(name = "id_documento", nullable = false, length = 20)
    private String idDocumento; // FK Tipo Documento

    @Column(name = "num_doc", nullable = false, length = 100)
    private String numDoc;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "fecha_venc")
    private LocalDate fechaVenc;

    @Column(name = "nro_lote", length = 100)
    private String nroLote;

    @Column(nullable = false, length = 100)
    private String operacion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String observacion;

    @Column(nullable = false, length = 2)
    private String signo; // + o -

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", nullable = false, precision = 12, scale = 4)
    private BigDecimal costoTotal;

    @Column(name = "saldo_cantidad", nullable = false, precision = 12, scale = 4)
    private BigDecimal saldoCantidad;

    @Column(name = "saldo_costo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal saldoCostoUnitario; // PMP

    @Column(name = "saldo_costo_total", nullable = false, precision = 12, scale = 4)
    private BigDecimal saldoCostoTotal;

    @Column(name = "origen_id", nullable = false, length = 100)
    private String origenId;

    @Column(name = "id_mov_detalle")
    private Long idMovimientoDetalle;

    @Column(name = "origen_tipo", nullable = false, length = 100)
    private String origenTipo; // FAC, BOL, etc.

    @Column(nullable = false, length = 50)
    private String presentacion;

    @Column(name = "id_clasificacion")
    private Long idClasificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clasificacion", insertable = false, updatable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private ClasificacionMovimiento clasificacion;

    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
