package com.pe.articulos.modules.venta_registro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_paciente")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_paciente")
    private Long idTipoPaciente;

    @Column(name = "codigo", length = 30, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "dsc", precision = 5, scale = 2)
    private BigDecimal dsc;

    @Column(name = "cob", precision = 5, scale = 2)
    private BigDecimal cob;

    @Column(name = "frq", length = 1)
    private String frq;

    @Column(name = "sf", length = 1)
    private String sf;

    @Column(name = "tarif", length = 10)
    private String tarif;

    @Column(name = "ambctc", length = 20)
    private String ambctc;

    @Column(name = "hosctc", length = 20)
    private String hosctc;

    @Column(name = "nombre2", length = 100)
    private String nombre2;

    @Column(name = "codmon", length = 1)
    private String codmon;

    @Column(name = "tipo_tarif", length = 1)
    private String tipoTarif;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActivo() {
        return this.estado == EstadoGeneral.ACTIVO;
    }

    public boolean tieneDescuento() {
        return dsc != null && dsc.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean tieneCobertura() {
        return cob != null && cob.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean esFrecuente() {
        return "1".equals(frq);
    }
}

