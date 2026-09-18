package com.pe.articulos.modules.descuentos.entity;

import com.pe.articulos.modules.venta_registro.entity.VentaRegistro;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venta_beneficios", indexes = {
        @Index(name = "idx_venta_benef_venta", columnList = "id_venta"),
        @Index(name = "idx_venta_benef_detalle", columnList = "id_descuento_detalle")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class VentaBeneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta_beneficio")
    private Long idVentaBeneficio;

    @Column(nullable = false, length = 100)
    private String parentesco;

    @Column(name = "nombre_paciente", nullable = false)
    private String nombrePaciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    private VentaRegistro venta;

    @Column(name = "id_descuento_detalle")
    private Long idDescuentoDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_descuento_detalle", insertable = false, updatable = false)
    @lombok.ToString.Exclude
    private DescuentoDetalle descuentoDetalle;

    @Column(name = "nombre_beneficio", nullable = false)
    private String nombreBeneficio;
}
