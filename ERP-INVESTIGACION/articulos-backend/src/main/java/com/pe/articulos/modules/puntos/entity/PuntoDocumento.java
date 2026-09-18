package com.pe.articulos.modules.puntos.entity;

import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "puntos_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE puntos_documento SET estado = 3 WHERE id = ?")
@SQLRestriction("estado != 3")
public class PuntoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto", referencedColumnName = "punto")
    private Punto punto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento")
    private TipoDocumento tipoDocumento;

    @Column(name = "serie", length = 20)
    private String serie;

    @Column(name = "ip", length = 100)
    private String ip;

    @Column(name = "id_docimp")
    private Integer idDocimp;

    @Column(name = "x", length = 50)
    private String x;

    @Column(name = "id_personal_user")
    private Integer idPersonalUser;

    @Column(name = "ultimo_numero")
    @Builder.Default
    private Integer numero = 0;

    @Column(name = "selecc", length = 10)
    private String selecc;

    @Column(name = "nota", columnDefinition = "TEXT")
    private String nota;

    @Column(name = "serie_ticketera", length = 50)
    private String serieTicketera;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @Column(name = "lpt", length = 50)
    private String lpt;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "det_nc", length = 50)
    private String detNc;

    @Column(name = "refact", length = 10)
    private String refact;

    @Column(name = "refact_dia")
    private Integer refactDia;

    @Enumerated(EnumType.STRING)
    @Column(name = "modulo", length = 20)
    private Modulo modulo;

    @Column(name = "id_plantilla")
    private Long idPlantilla;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "numero_actual", nullable = false)
    @Builder.Default
    private Integer numeroActual = 1;
}
