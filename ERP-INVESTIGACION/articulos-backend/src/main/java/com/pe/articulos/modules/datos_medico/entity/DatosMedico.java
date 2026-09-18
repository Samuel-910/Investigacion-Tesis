package com.pe.articulos.modules.datos_medico.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.pe.articulos.modules.niveles.entity.Nivel;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "datos_medico", indexes = {
        @Index(name = "idx_nro_cmp", columnList = "nro_cmp", unique = true),
        @Index(name = "idx_nombremed", columnList = "nombremed"),
        @Index(name = "idx_estado", columnList = "estado"),
        @Index(name = "idx_tipo", columnList = "tipo"),
        @Index(name = "idx_area_estado", columnList = "id_area, estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DatosMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "nro_cmp", length = 20)
    private String nroCmp;

    @Column(name = "nro_rne", length = 20)
    private String nroRne;

    @Column(name = "nombremed", nullable = false, length = 200)
    private String nombreMed;

    @Column(name = "honorarios", precision = 10, scale = 2)
    private BigDecimal honorarios;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    private EstadoGeneral estado = EstadoGeneral.ACTIVO; // A=Activo, I=Inactivo, S=Suspendido

    @Column(name = "planilla", length = 1)
    private String planilla; // S=Sí, N=No

    @Column(name = "fecha_ing")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_cese")
    private LocalDate fechaCese;

    @Column(name = "ult_liq")
    private LocalDate ultimaLiquidacion;

    @Column(name = "tip_pag", length = 20)
    private String tipoPago; // MENSUAL, QUINCENAL, SEMANAL

    @Column(name = "fion_paq", length = 20)
    private String formaPago; // EFECTIVO, TRANSFERENCIA, CHEQUE

    @Column(name = "tipo_rec", length = 30)
    private String tipoRecibo; // HONORARIOS, PLANILLA, MIXTO

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "id_personal_user", length = 50)
    private String idPersonalUser;

    @Column(name = "tipo", length = 30)
    private String tipo; // MEDICO, ENFERMERA, TECNICO, ADMIN

    @Column(name = "ctacte", length = 50)
    private String cuentaCorriente;

    @Column(name = "tipo_plan", length = 30)
    private String tipoPlan; // COMPLETO, PARCIAL, GUARDIA

    @Column(name = "user_upd_obs", length = 50)
    private String usuarioActualizacionObs;

    @Column(name = "numcta", length = 30)
    private String numeroCuenta;

    @Column(name = "suepaq", length = 1)
    private String sueldoPaquete; // S=Sí, N=No

    @Column(name = "cod_cont", length = 20)
    private String codigoContrato;

    @Column(name = "imprimir_tur", length = 1)
    private String imprimirTurno; // S=Sí, N=No

    @Column(name = "emerge", length = 1)
    private String emergencia; // S=Sí, N=No

    @Column(name = "id_area")
    private Long idArea;

    @Column(name = "pagina_web", length = 200)
    private String paginaWeb;

    @Column(name = "pre_grado", columnDefinition = "TEXT")
    private String preGrado;

    @Column(name = "post_grado", columnDefinition = "TEXT")
    private String postGrado;

    @Column(name = "cargos", columnDefinition = "TEXT")
    private String cargos;

    @Column(name = "idiomas", length = 200)
    private String idiomas;

    @Column(name = "experiencia", columnDefinition = "TEXT")
    private String experiencia;

    @Column(name = "acepta_ft", length = 1)
    private String aceptaFacturaTributaria; // S=Sí, N=No

    @Column(name = "capacitado", length = 1)
    private String capacitado; // S=Sí, N=No

    @Column(name = "fecha_capac")
    private LocalDate fechaCapacitacion;

    @Column(name = "id_sucursal")
    private Long idSucursal;

    @Column(name = "vacaciones", length = 1)
    private String vacaciones; // S=Sí, N=No

    @Column(name = "dias_vaca")
    private Integer diasVacaciones;

    @Column(name = "fecini_vaca")
    private LocalDate fechaInicioVacaciones;

    @Column(name = "fecfin_vaca")
    private LocalDate fechaFinVacaciones;

    @Column(name = "tipo_med", length = 50)
    private String tipoMedico; // GENERAL, ESPECIALISTA, RESIDENTE

    @Column(name = "anest", length = 1)
    private String anestesia; // S=Sí, N=No

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivel", referencedColumnName = "id_nivel", insertable = false, updatable = false)
    private Nivel area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", referencedColumnName = "id_sucursal", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Sucursal sucursal;
}
