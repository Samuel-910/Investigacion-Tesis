package com.pe.articulos.modules.caja_chica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_chica", indexes = {
        @Index(name = "idx_caja_chica_estado", columnList = "estado"),
        @Index(name = "idx_caja_chica_sucursal", columnList = "id_sucursal"),
        @Index(name = "idx_caja_chica_punto", columnList = "id_punto_venta")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaChica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "saldo_actual", precision = 15, scale = 5)
    @Builder.Default
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(name = "id_sucursal")
    private Long idSucursal;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoCaja estado = EstadoCaja.ABIERTA;

    @CreatedBy
    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @LastModifiedBy
    @Column(name = "usuario_modificacion")
    private String usuarioModificacion;

    @Column(name = "saldo_inicial", precision = 15, scale = 5)
    @Builder.Default
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @Column(name = "saldo_cierre_real", precision = 15, scale = 5)
    private BigDecimal saldoCierreReal;

    @Column(name = "id_punto_venta")
    private Long idPuntoVenta;

    @Column(name = "id_usuario_cajero")
    private String idUsuarioCajero;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum EstadoCaja {
        ABIERTA, CERRADA
    }
}
