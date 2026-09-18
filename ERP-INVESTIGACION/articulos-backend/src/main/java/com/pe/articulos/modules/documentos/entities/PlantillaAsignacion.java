package com.pe.articulos.modules.documentos.entities;

import com.pe.articulos.modules.puntos.entity.PuntoDocumento;
import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;
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
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "plantilla_asignaciones", indexes = {
        @Index(name = "idx_pa_plantilla", columnList = "plantilla_id"),
        @Index(name = "idx_pa_punto_doc", columnList = "punto_documento_id"),
        @Index(name = "idx_pa_estado", columnList = "estado")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("estado != 3")
public class PlantillaAsignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String modulo; // VENTAS, COMPRAS, etc.

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plantilla_id", nullable = false)
    private Plantilla plantilla;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "punto_documento_id")
    private PuntoDocumento puntoDocumento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_doc")
    private TipoDocumento tipoDocumento;

    private String descripcion;

    @Builder.Default
    @Column(nullable = false)
    private Integer estado = 1;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    private String modifiedBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
// Refactor: Seguimiento de plantillas
