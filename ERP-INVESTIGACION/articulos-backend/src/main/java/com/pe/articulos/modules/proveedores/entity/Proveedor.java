package com.pe.articulos.modules.proveedores.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "proveedores", indexes = {
        @Index(name = "idx_proveedor_numdoc", columnList = "num_doc_ident"),
        @Index(name = "idx_proveedor_estado", columnList = "estado")
})
@SQLDelete(sql = "UPDATE proveedores SET estado = 0 WHERE id = ?")
@SQLRestriction("estado = 1")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_doc_ident", nullable = false, length = 50)
    private String tipoDocIdent;

    @Column(name = "num_doc_ident", nullable = false, length = 20)
    private String numDocIdent;

    @Column(name = "razon_social", nullable = false, length = 255)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 255)
    private String nombreComercial;

    @Column(length = 1000)
    private String direccion;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String telefono;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String provincia;

    @Column(length = 100)
    private String distrito;

    @Column(length = 1000)
    private String sedes;

    @Column(length = 255)
    private String representante;

    @Column(name = "cargo_representante", length = 100)
    private String cargoRepresentante;

    @Column(name = "plazo_dias")
    @Builder.Default
    private Integer plazoDias = 0;

    @Column(length = 100)
    private String banco;

    @Column(name = "cuenta_bancaria", length = 50)
    private String cuentaBancaria;

    @Column(length = 50)
    private String cci;

    @Column(name = "sitio_web", length = 255)
    private String sitioWeb;

    @Column(name = "tipo_proveedor", length = 50)
    private String tipoProveedor;

    @Column(length = 1000)
    private String observaciones;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @CreatedDate
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}