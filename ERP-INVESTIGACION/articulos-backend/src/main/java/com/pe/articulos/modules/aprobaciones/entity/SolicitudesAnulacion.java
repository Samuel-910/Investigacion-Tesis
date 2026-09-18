package com.pe.articulos.modules.aprobaciones.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "solicitudes_anulacion", indexes = {
        @Index(name = "idx_solic_anulacion_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SolicitudesAnulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoSolicitud tipo; // VENTA, COMPRA

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "usuario_creacion", updatable = false, length = 100)
    @CreatedBy
    private String usuarioCreacion;

    @Column(name = "fecha_creacion", updatable = false)
    @CreatedDate
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(name = "usuario_modificacion", length = 100)
    @LastModifiedBy
    private String usuarioModificacion;

    @Column(name = "fecha_modificacion")
    @LastModifiedDate
    private LocalDateTime fechaModificacion;

    @Column(name = "observacion_atiende", columnDefinition = "TEXT")
    private String observacionAtiende;

    public enum TipoSolicitud {
        VENTA, COMPRA, MOVIMIENTO_DIVERSO
    }

    public enum EstadoSolicitud {
        PENDIENTE, APROBADA, RECHAZADA
    }
}
