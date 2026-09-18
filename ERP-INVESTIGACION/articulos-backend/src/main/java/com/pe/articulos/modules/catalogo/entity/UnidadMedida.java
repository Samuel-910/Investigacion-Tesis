package com.pe.articulos.modules.catalogo.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "unidad_medida", indexes = {
        @Index(name = "idx_unidad_medida_estado", columnList = "estado"),
        @Index(name = "idx_unidad_medida_nombre", columnList = "nombre")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("estado != 3")
@EntityListeners(AuditingEntityListener.class)
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre; // Ej: "Caja", "Blister", "Unidad"

    @Column(nullable = false, length = 10)
    private String simbolo; // Ej: "CJA", "BLS", "UND"

    @Column(name = "codigo_sunat", length = 10)
    private String codigoSunat; // Ej: "BX" (Box), "NIU" (Unit)

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @Column(name = "es_agrupador", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean esAgrupador = false;

    @CreatedBy
    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @LastModifiedBy
    @Column(name = "usuario_modificacion")
    private String usuarioModificacion;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
