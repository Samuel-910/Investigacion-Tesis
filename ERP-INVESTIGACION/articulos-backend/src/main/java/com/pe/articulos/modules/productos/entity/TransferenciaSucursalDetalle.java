package com.pe.articulos.modules.productos.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pe.articulos.modules.catalogo.entity.Catalogo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventario_transferencias_detalle", indexes = {
        @Index(name = "idx_transferencia_detalle_trans", columnList = "id_transferencia"),
        @Index(name = "idx_transferencia_detalle_cat", columnList = "id_catalogo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaSucursalDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transferencia", nullable = false)
    @JsonIgnore
    private TransferenciaSucursal transferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", nullable = false)
    private Catalogo catalogo;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidadSolicitada;

    @Column(name = "cantidad_enviada", precision = 12, scale = 2)
    private BigDecimal cantidadEnviada;

    @Column(name = "cantidad_recibida", precision = 12, scale = 2)
    private BigDecimal cantidadRecibida;

    @Column(name = "nro_lote", length = 50)
    private String nroLote;

    @Column(name = "fecha_venc")
    private LocalDate fechaVenc;

    @Column(name = "costo_unitario", precision = 12, scale = 2)
    private BigDecimal costoUnitario;
}
