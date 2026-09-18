package com.pe.articulos.modules.documentos.entities;

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
@Table(name = "bloques", indexes = {
        @Index(name = "idx_bloque_estado", columnList = "estado")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("estado != 3")
public class Bloque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String htmlContenido;

    @Column(columnDefinition = "TEXT")
    private String cssEstilo;

    private String categoria;

    @ElementCollection(targetClass = Modulo.class)
    @CollectionTable(name = "bloque_modulos", joinColumns = @JoinColumn(name = "bloque_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "modulo")
    private java.util.Set<Modulo> modulos;

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
