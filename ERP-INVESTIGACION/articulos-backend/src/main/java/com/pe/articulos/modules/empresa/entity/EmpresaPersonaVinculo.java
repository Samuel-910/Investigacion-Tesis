package com.pe.articulos.modules.empresa.entity;

import java.time.LocalDate;

import com.pe.articulos.modules.users.entity.DatosPersonales;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;

import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Index;

@Entity
@Table(name = "empresa_persona_vinculo", indexes = {
        @Index(name = "idx_empresa_vinculo_empresa", columnList = "id_empresa"),
        @Index(name = "idx_empresa_vinculo_personal", columnList = "id_personal"),
        @Index(name = "idx_empresa_vinculo_estado", columnList = "estado")
})
@SQLRestriction("estado != 3")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaPersonaVinculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_empresa", referencedColumnName = "id_empresa", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "id_personal", referencedColumnName = "id", nullable = false)
    private DatosPersonales personal;

    @Column(name = "cargo")
    private String cargo;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;
}
