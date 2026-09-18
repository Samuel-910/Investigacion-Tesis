package com.pe.articulos.modules.productos.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.pe.articulos.modules.catalogo.entity.Catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "movimiento_diverso_detalle", indexes = {
        @Index(name = "idx_movimiento_detalle_mov", columnList = "id_movimiento"),
        @Index(name = "idx_movimiento_detalle_cat", columnList = "id_catalogo")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoDiversoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movimiento", nullable = false)
    @JsonIgnoreProperties("detalles")
    private MovimientoDiverso movimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Catalogo catalogo;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo; // INGRESO, SALIDA

    @Column(name = "nro_lote", length = 50)
    private String nroLote;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;
    
    @Column(name = "observacion", length = 255)
    private String observacion;

    @Column(name = "id_almacen")
    private Long idAlmacen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clasificacion")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ClasificacionMovimiento clasificacion;
}
