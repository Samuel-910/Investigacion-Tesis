package com.pe.articulos.modules.documentos.entities;

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
@Table(name = "plantillas", indexes = {
        @Index(name = "idx_plantilla_formato", columnList = "formato_id"),
        @Index(name = "idx_plantilla_estado", columnList = "estado")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("estado != 3")
public class Plantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String htmlContenido;

    @Column(columnDefinition = "TEXT")
    private String htmlTraducido;

    @Column(columnDefinition = "TEXT")
    private String cssEstilo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "formato_id")
    private DocumentoFormato formato;

    private String orientacion; // Vertical, Horizontal

    @Enumerated(EnumType.STRING)
    private Modulo modulo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_doc", referencedColumnName = "tipo_doc")
    private TipoDocumento tipoDocumento;

    @Builder.Default
    private boolean isDefault = false;

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
