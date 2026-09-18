package com.pe.articulos.modules.caja_general.entity;

import com.pe.articulos.modules.atributos.entity.MetodoPago;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_general_movimiento", indexes = {
        @Index(name = "idx_caja_gen_mov_estado", columnList = "estado"),
        @Index(name = "idx_caja_gen_mov_caja", columnList = "id_caja_general"),
        @Index(name = "idx_caja_gen_mov_metodo", columnList = "id_metodo_pago")
})
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("estado != 3")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaGeneralMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja_general", nullable = false)
    private CajaGeneral cajaGeneral;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private TipoMovimiento tipo;

    @Column(nullable = false, precision = 15, scale = 5)
    private BigDecimal monto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(name = "estado")
    @Builder.Default
    private Integer estado = 1;

    @CreatedBy
    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @LastModifiedBy
    @Column(name = "usuario_modificacion")
    private String usuarioModificacion;

    @CreatedDate
    @Column(name = "fecha_creacion", updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    public enum TipoMovimiento {
        INGRESO, EGRESO, TRANSFERENCIA_RECIBIDA, PAGO_PROVEEDOR, TRANSFERENCIA_ENTRE_METODOS
    }
}
