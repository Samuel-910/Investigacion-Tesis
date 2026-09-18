package com.pe.articulos.modules.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;

@Entity
@Table(name = "datos_clinica", indexes = {
        @Index(name = "idx_clinica_estado", columnList = "estado"),
        @Index(name = "idx_clinica_ruc", columnList = "ruc")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("estado != 3")
@EntityListeners(AuditingEntityListener.class)
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "ruc", length = 11)
    private String ruc;

    @Column(name = "codigo_ipress")
    private String codigoIpress;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "representante", length = 100)
    private String representante;

    @Column(name = "auditor", length = 100)
    private String auditor;

    @Column(name = "liquidador", length = 100)
    private String liquidador;

    @Column(name = "financiero", length = 100)
    private String financiero;

    @Column(name = "web", length = 100)
    private String web;

    @Column(name = "ctacte", length = 50)
    private String ctacte;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "abrev", length = 50)
    private String abrev;

    @Column(name = "logo_cuadrado", columnDefinition = "TEXT")
    private String logoCuadrado;

    @Column(name = "logo_rectangular", columnDefinition = "TEXT")
    private String logoRectangular;

    @Column(name = "logo_principal", length = 20)
    private String logoPrincipal;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @CreatedBy
    @Column(name = "usuario_creacion", length = 50)
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

    @Column(name = "terminos_condiciones", columnDefinition = "TEXT")
    private String terminosCondiciones;

    @Column(name = "nombre_comercial", length = 200)
    private String nombreComercial;

    @Column(name = "nombre_sede", length = 100)
    private String nombreSede;

    @Column(name = "direccion_fiscal", length = 255)
    private String direccionFiscal;

    @Column(name = "telefonos", length = 100)
    private String telefonos;

    @Column(name = "email_contacto", length = 100)
    private String emailContacto;

    @Column(name = "contacto_header", columnDefinition = "TEXT")
    private String contactoHeader;

    @Column(name = "representante_legal", length = 200)
    private String representanteLegal;

    @Column(name = "web_url", length = 200)
    private String webUrl;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "logo_base64", columnDefinition = "TEXT")
    private String logoBase64;

}
