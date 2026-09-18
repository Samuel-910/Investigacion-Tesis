
package com.pe.articulos.modules.compras.entity;

import com.pe.articulos.modules.proveedores.entity.Proveedor;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "compras", indexes = @Index(name = "idx_compras_estado", columnList = "estado"))
@SQLRestriction("estado != 3")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Proveedor proveedor;

    @Column(name = "id_sucursal")
    private Long idSucursal;

    @Column(name = "tipo_comprobante", length = 20)
    private String tipoComprobante; // FACTURA, BOLETA, GUIA

    @Column(length = 10)
    private String serie;

    @Column(length = 20)
    private String correlativo;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "condicion_pago", length = 50)
    private String condicionPago;

    @Column(length = 3)
    private String moneda; // PEN, USD

    // -- Totales Tributarios --
    @Column(name = "valor_venta_gravado", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorVentaGravado = BigDecimal.ZERO;

    @Column(name = "valor_venta_exonerado", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorVentaExonerado = BigDecimal.ZERO;

    @Column(name = "valor_venta_inafecto", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorVentaInafecto = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(name = "igv_descuento", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal igvDescuento = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal percepcion = BigDecimal.ZERO;

    @Column(name = "ajuste_redondeo", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal ajusteRedondeo = BigDecimal.ZERO;

    @Column(name = "total_pagar", precision = 12, scale = 2)
    private BigDecimal totalPagar;

    @Column(name = "imp_dolar", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal impDolar = BigDecimal.ZERO;

    @Column(name = "tipo_cambio", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal tipoCambio = BigDecimal.ONE;

    @Column(name = "imp_bruto", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal impBruto = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "otros_tributos", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal otrosTributos = BigDecimal.ZERO;

    @Column(columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean detraccion = false;

    @Column(length = 20)
    private String retencion;

    @Column(name = "medio_pago", length = 50)
    private String medioPago;

    @Column(name = "porcentaje_igv", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeIgv = new BigDecimal("18.00");

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.REGISTRADO;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private List<CronogramaPago> cronograma;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private List<DetalleCompra> detalles;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "id_user_anul", length = 50)
    private String idUserAnul;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "nombre_grupo")
    private String nombreGrupo;

    @Column(name = "solicitar_fondo")
    private Boolean solicitarFondo;

    @Column(name = "solicitante_id")
    private Long solicitanteId;

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
