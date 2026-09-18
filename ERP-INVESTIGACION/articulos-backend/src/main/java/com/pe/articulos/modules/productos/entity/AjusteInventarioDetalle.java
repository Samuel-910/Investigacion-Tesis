package com.pe.articulos.modules.productos.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pe.articulos.modules.catalogo.entity.Catalogo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "inventario_ajustes_detalle", indexes = {
        @Index(name = "idx_ajuste_detalle_ajuste", columnList = "id_ajuste"),
        @Index(name = "idx_ajuste_detalle_catalogo", columnList = "id_catalogo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjusteInventarioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ajuste", nullable = false)
    @JsonIgnore
    private AjusteInventario ajuste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIncludeProperties({ "id", "nombre" })
    private Catalogo producto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", precision = 12, scale = 2)
    private BigDecimal costoUnitario;
}
