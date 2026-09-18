package com.pe.articulos.modules.compras.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cronograma_pagos", indexes = @Index(name = "idx_cronograma_pagos_estado", columnList = "estado"))
@SQLRestriction("estado != 3")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CronogramaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cronograma")
    private Long idCronograma;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_compra", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "cronograma", "hibernateLazyInitializer",
            "handler" })
    private Compra compra;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota; // 1, 2, 3...

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "monto_cuota", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoCuota;

    @Column(nullable = false, length = 20)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @Column(name = "fecha_pago_real")
    private LocalDateTime fechaPagoReal;

    @Column(name = "comprobante_pago")
    private String comprobantePago;

    @CreatedBy
    @Column(name = "usuario_creacion", updatable = false, length = 50)
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
}
