package com.pe.articulos.modules.niveles.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "niveles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@SQLDelete(sql = "UPDATE niveles SET deleted = true WHERE id_nivel = ?")
@SQLRestriction("deleted = false")
public class Nivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nivel")
    private Long idNivel;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "num_nivel", length = 50)
    private String numNivel;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @Column(name = "id_tipo_ate", length = 50)
    private String idTipoAte;

    @Column(name = "centro_costo", length = 100)
    private String centroCosto;

    @Column(name = "cent_cost_limite", precision = 15, scale = 2)
    private BigDecimal centCostLimite;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    // JERARQUÍA
    @Column(name = "nivel_jerarquia")
    @Builder.Default
    private Integer nivelJerarquia = 1;

    @Column(name = "orden")
    @Builder.Default
    private Integer orden = 0;

    // AUDITORÍA
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)

    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")

    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    @CreatedBy
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    @LastModifiedBy
    private String updatedBy;

    // RELACIÓN RECURSIVA
    @Column(name = "id_nivel_padre")
    private Long idNivelPadre;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivel_padre", referencedColumnName = "id_nivel", insertable = false, updatable = false)
    private Nivel nivelPadre;

    @OneToMany(mappedBy = "nivelPadre", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Nivel> nivelesHijos = new ArrayList<>();

    // SOFT DELETE
    @Column(name = "deleted")
    @Builder.Default
    private boolean deleted = false;

    // CAMPO TRANSIENT PARA FRONTEND
    @Transient
    private boolean tieneHijos;

    @Transient
    private int totalHijos;
}
