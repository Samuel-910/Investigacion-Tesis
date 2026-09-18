package com.pe.articulos.modules.reportes.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_widgets")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardWidget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Enumerated(EnumType.STRING)
    private TipoGrafico tipoGrafico;

    @Enumerated(EnumType.STRING)
    private MetricaReporte metrica;

    @Enumerated(EnumType.STRING)
    private DimensionReporte dimension;

    private Integer orden;
    private Integer columns; // Ancho en grid (1-12)
    @Column(name = "height_rows")
    @Builder.Default
    private Integer height = 1;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_config_id")
    @JsonBackReference
    private DashboardConfig dashboardConfig;

    public enum TipoGrafico {
        BAR, LINE, DONUT, AREA, PIE, COLUMN, SCATTER, RADAR, POLAR_AREA, GAUGE, TREEMAP, HEATMAP, FUNNEL, WATERFALL,
        CARD, TABLE
    }

    public enum MetricaReporte {
        VENTAS, VENTAS_CANTIDAD, COMPRAS, COMPRAS_CANTIDAD, DEUDA, PRODUCTOS, GASTOS, INGRESOS, STOCK_FISICO,
        VALOR_INVENTARIO, PRODUCTOS_VENCIDOS, SALDO_CAJA, VENTAS_VS_COMPRAS,
        UTILIDAD_NETA, TICKET_PROMEDIO, DESCUENTOS_TOTALES, TICKET_COMPRA_PROMEDIO, STOCK_CRITICO,
        RENTABILIDAD, ARTICULOS_PERDIDA
    }

    public enum DimensionReporte {
        TIEMPO_MES, TIEMPO_DIA, PROVEEDOR, CATEGORIA, PRODUCTO_NOMBRE, SUCURSAL, USUARIO_REGISTRO,
        METODO_PAGO, CLIENTE, DIA_SEMANA, MARCA, CONCEPTO_GASTO
    }
}
