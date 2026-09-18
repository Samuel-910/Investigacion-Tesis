package com.pe.articulos.modules.atributos.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("estado != 3")
public abstract class BaseAtributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    private EstadoGeneral estado = EstadoGeneral.ACTIVO; // A: Activo, I: Inactivo

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_creacion", updatable = false, length = 100)
    @CreatedBy
    private String usuarioCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_modificacion", length = 100)
    @LastModifiedBy
    private String usuarioModificacion;
}
