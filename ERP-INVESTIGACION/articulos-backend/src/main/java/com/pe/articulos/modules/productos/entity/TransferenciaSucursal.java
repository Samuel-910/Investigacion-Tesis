package com.pe.articulos.modules.productos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "inventario_transferencias", indexes = {
        @Index(name = "idx_transferencia_origen", columnList = "id_sucursal_origen"),
        @Index(name = "idx_transferencia_destino", columnList = "id_sucursal_destino"),
        @Index(name = "idx_transferencia_estado", columnList = "estado")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TransferenciaSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_sucursal_origen", nullable = false)
    private Long idSucursalOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_origen", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIncludeProperties({ "idSucursal", "nombreSucursal" })
    private com.pe.articulos.modules.sucursal.entity.Sucursal sucursalOrigen;

    @Column(name = "id_sucursal_destino", nullable = false)
    private Long idSucursalDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_destino", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIncludeProperties({ "idSucursal", "nombreSucursal" })
    private com.pe.articulos.modules.sucursal.entity.Sucursal sucursalDestino;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.SOLICITADO; // SOLICITADO, ENVIADO, RECIBIDO, CANCELADO

    @Column(name = "id_usuario_solicita", nullable = false)
    private Long idUsuarioSolicita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_solicita", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIncludeProperties({ "id", "nombreCompleto" })
    private com.pe.articulos.modules.users.entity.DatosPersonales usuarioSolicita;

    @Column(name = "id_usuario_envia")
    private Long idUsuarioEnvia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_envia", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIncludeProperties({ "id", "nombreCompleto" })
    private com.pe.articulos.modules.users.entity.DatosPersonales usuarioEnvia;

    @Column(name = "id_usuario_recibe")
    private Long idUsuarioRecibe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_recibe", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIncludeProperties({ "id", "nombreCompleto" })
    private com.pe.articulos.modules.users.entity.DatosPersonales usuarioRecibe;

    @Column(name = "id_usuario_cancela")
    private Long idUsuarioCancela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cancela", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIncludeProperties({ "id", "nombreCompleto" })
    private com.pe.articulos.modules.users.entity.DatosPersonales usuarioCancela;

    @Column(length = 500)
    private String motivo;

    @OneToMany(mappedBy = "transferencia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransferenciaSucursalDetalle> detalles;

    @CreatedDate
    @Column(name = "fecha_solicitud", updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;
}
