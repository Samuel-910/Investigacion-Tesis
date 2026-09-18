package com.pe.articulos.modules.almacen.entity;

import com.pe.articulos.modules.sucursal.entity.Sucursal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "almacen", indexes = {
    @Index(name = "idx_almacen_sucursal", columnList = "id_sucursal"),
    @Index(name = "idx_almacen_estado", columnList = "estado"),
    @Index(name = "idx_almacen_sucursal_estado", columnList = "id_sucursal, estado")
})
@SQLRestriction("estado != 3")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Almacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String codigo;

    @Column(length = 200)
    private String ubicacion;

    @Column(name = "es_principal")
    @Builder.Default
    private Boolean esPrincipal = false;

    @Column(length = 100)
    private String responsable;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;
    @Column(name = "actualiza_precio", nullable = false, length = 1)
    private String actualizaPrecio;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @CreatedBy
    @Column(name = "usuario_creacion", length = 50, updatable = false)
    private String usuarioCreacion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 2)
    private String tipo;

    @Column(name = "id_encargado", length = 20)
    private String idEncargado;

    @Column(name = "fecha_crea", nullable = false)
    private LocalDate fechaCrea;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @CreatedBy
    @Column(name = "id_user_crea", nullable = false, length = 20, updatable = false)
    private String idUserCrea;

    @LastModifiedBy
    @Column(name = "id_user_ultimo", length = 20)
    private String idUserUltimo;

    @Column(name = "ultimo_acceso")
    private LocalDate ultimoAcceso;

    @Column(name = "id_inv")
    private Long idInv;

    @Column(name = "ped_interdep", nullable = false, length = 1)
    private String pedInterdep;

    @Column(name = "maneja_petitorio", nullable = false, length = 1)
    private String manejaPetitorio;

    @Column(nullable = false, length = 1)
    private String receta;

    @Column(name = "canje_stock_fijo", nullable = false)
    private Integer canjeStockFijo;

    @Column(name = "tabla_accesos", length = 20)
    private String tablaAccesos;

    @Column(name = "da_asistencia", length = 1)
    private String daAsistencia;

    @Column(name = "id_nivel", length = 4)
    private String idNivel;

    @Column(length = 1)
    private String farmacia;

    @Column(length = 1)
    private String interd;

    @Column(name = "reg_comp", length = 1)
    private String regComp;

    @PrePersist
    protected void onCreate() {
        if (this.actualizaPrecio == null) {
            this.actualizaPrecio = "S";
        }
        if (this.tipo == null) {
            this.tipo = "01";
        }
        if (this.fechaCrea == null) {
            this.fechaCrea = LocalDate.now();
        }
        if (this.fechaFin == null) {
            this.fechaFin = LocalDate.now().plusYears(10);
        }
        if (this.pedInterdep == null) {
            this.pedInterdep = "S";
        }
        if (this.manejaPetitorio == null) {
            this.manejaPetitorio = "S";
        }
        if (this.receta == null) {
            this.receta = "S";
        }
        if (this.canjeStockFijo == null) {
            this.canjeStockFijo = 1;
        }
    }
}
