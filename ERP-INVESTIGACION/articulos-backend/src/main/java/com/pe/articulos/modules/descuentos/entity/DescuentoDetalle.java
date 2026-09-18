package com.pe.articulos.modules.descuentos.entity;

import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_descuento_detalles", indexes = {
        @Index(name = "idx_desc_det_descuento", columnList = "id_descuento"),
        @Index(name = "idx_desc_det_catalogo", columnList = "id_catalogo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescuentoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_descuento", nullable = false)
    @JsonIgnore
    private Descuento descuento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", nullable = true)
    private Catalogo catalogo;

    @Column(name = "id_catalogo", insertable = false, updatable = false)
    private Long idCatalogo;

    @Column(name = "tipo_descuento", length = 30)
    private String tipoDescuento;

    @Column(name = "valor_descuento", precision = 12, scale = 2)
    private BigDecimal valorDescuento;

    @Column(name = "cantidad")
    private Integer cantidad;
}
