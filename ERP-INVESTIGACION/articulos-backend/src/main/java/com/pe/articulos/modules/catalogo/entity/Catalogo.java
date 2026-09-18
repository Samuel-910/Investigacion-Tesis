package com.pe.articulos.modules.catalogo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import com.pe.articulos.modules.niveles.entity.Nivel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "catalogo", indexes = {
        @Index(name = "idx_catalogo_estado", columnList = "estado"),
        @Index(name = "idx_catalogo_codigo", columnList = "codigo"),
        @Index(name = "idx_catalogo_categoria", columnList = "id_categoria"),
        @Index(name = "idx_catalogo_nivel", columnList = "id_nivel")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@SQLRestriction("estado != 3")
@EntityListeners(AuditingEntityListener.class)
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String codigo;

    @Column(name = "codigo_seus", length = 50)
    private String codigoSeus;

    @Column(nullable = false)
    private String nombre;
    @Column(name = "nombre_catalogo", nullable = false, length = 255)
    private String nombreCatalogo;
    @Column(columnDefinition = "TEXT")
    private String detalle;
    @Column(name = "tipo_catalogo", nullable = false, length = 50) // Eliminado @Enumerated
    private String tipoCatalogo;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private Tipo tipo;

    @Column(name = "tipo_medicamento", length = 50)
    private String tipoMedicamento;

    @Column(name = "id_principio_activo")
    private Long idPrincipioActivo;

    @Column(name = "id_accion_terapeutica")
    private Long idAccionTerapeutica;

    @Column(name = "reg_sanitario", length = 50)
    private String regSanitario;

    @Column(name = "es_generico")
    @Builder.Default
    private Boolean esGenerico = false;

    @Column(name = "es_controlado")
    @Builder.Default
    private Boolean esControlado = false;

    @Column(name = "venta_con_receta")
    @Builder.Default
    private Boolean ventaConReceta = false;

    @Column(name = "maneja_lotes")
    @Builder.Default
    private Boolean manejaLotes = false;

    @Column(name = "id_categoria")
    private Long idCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivel", nullable = false)
    @JsonIgnoreProperties({ "nivelesHijos", "nivelPadre", "hibernateLazyInitializer", "handler" })
    private Nivel nivel;

    @Column(length = 100)
    private String presentacion;

    @Column(name = "tiempo_entrega_minutos")
    private Integer tiempoEntregaMinutos;

    // --- Jerarquía de Unidades (Artículos) ---
    @Column(name = "id_unidad_base")
    private Long idUnidadBase;

    @Column(name = "id_unidad_intermedia")
    private Long idUnidadIntermedia;

    @Column(name = "id_unidad_mayor")
    private Long idUnidadMayor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_atencion")
    private TipoAtencion tipoAtencion;
    // --- Configuración Médica y de facturación (Finanzas) ---
    @Column(name = "nombre_servicio", length = 255)
    private String nombreServicio;

    @Column(name = "unidad", precision = 10, scale = 2)
    private BigDecimal unidad;

    @Column(name = "tipo_unidad", length = 50)
    private String tipoUnidad;

    @Column(name = "honorarios_medicos")
    @Builder.Default
    private Boolean honorariosMedicos = false;

    @Column(name = "tarifa", precision = 10, scale = 2)
    private BigDecimal tarifa;

    @Column(name = "precio_sin_igv", precision = 10, scale = 2)
    private BigDecimal precioSinIgv;

    @Column(name = "precio_con_igv", precision = 12, scale = 4)
    private BigDecimal precioConIgv;

    @Column(name = "precio_compra", precision = 12, scale = 4)
    private BigDecimal precioCompra;

    @Column(name = "precio_kairos", precision = 12, scale = 4)
    private BigDecimal precioKairos;

    @Column(name = "margen_ganancia", precision = 12, scale = 4)
    private BigDecimal margenGanancia;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "id_tipo_atencion")
    // private TipoAtencion tipoAtencion; // Falta entidad en artículos

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_precio", length = 50)
    private TipoPrecio tipoPrecio;

    @Column(name = "moneda", length = 3)
    private String moneda;

    @Column(name = "codigo_barra", length = 50)
    private String codigoBarra;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "cod_digemid", length = 50)
    private String codDigemid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_temperatura")
    private TipoTemperatura tipoTemperatura;

    @Column(name = "id_laboratorio")
    private Long idLaboratorio;

    @Column(name = "id_ubicacion")
    private Long idUbicacion;

    // --- Units Config (Stocks y Ganancias por Niveles) ---
    @Column(name = "maneja_unidad")
    @Builder.Default
    private Boolean manejaUnidad = true;
    @Column(name = "stock_unidad")
    private BigDecimal stockUnidad;
    @Column(name = "precio_venta_unitario")
    private BigDecimal precioVentaUnitario;
    @Column(name = "tipo_ganancia_unidad", length = 20)
    private String tipoGananciaUnidad;
    @Column(name = "ganancia_unidad")
    private BigDecimal gananciaUnidad;
    @Column(name = "precio_unitario_min")
    private BigDecimal precioUnitarioMin;
    @Column(name = "precio_unitario_max")
    private BigDecimal precioUnitarioMax;

    @Column(name = "maneja_blister")
    @Builder.Default
    private Boolean manejaBlister = false;
    @Column(name = "stock_blister")
    private BigDecimal stockBlister;
    @Column(name = "precio_blister_min")
    private BigDecimal precioBlisterMin;
    @Column(name = "precio_blister_max")
    private BigDecimal precioBlisterMax;
    @Column(name = "precio_venta_blister")
    private BigDecimal precioVentaBlister;
    @Column(name = "factor_blister")
    @Builder.Default
    private Integer factorBlister = 1;
    @Column(name = "tipo_ganancia_blister", length = 20)
    private String tipoGananciaBlister;
    @Column(name = "ganancia_blister")
    private BigDecimal gananciaBlister;

    @Column(name = "maneja_caja")
    @Builder.Default
    private Boolean manejaCaja = false;
    @Column(name = "stock_caja")
    private BigDecimal stockCaja;
    @Column(name = "precio_caja_min")
    private BigDecimal precioCajaMin;
    @Column(name = "precio_caja_max")
    private BigDecimal precioCajaMax;
    @Column(name = "precio_venta_caja")
    private BigDecimal precioVentaCaja;
    @Column(name = "factor_caja")
    @Builder.Default
    private Integer factorCaja = 1;
    @Column(name = "tipo_ganancia_caja", length = 20)
    private String tipoGananciaCaja;
    @Column(name = "ganancia_caja")
    private BigDecimal gananciaCaja;

    // --- Stock Reservado ---
    @Column(name = "stock_reservado_unidad")
    @Builder.Default
    private BigDecimal stockReservadoUnidad = BigDecimal.ZERO;

    @Column(name = "stock_reservado_blister")
    @Builder.Default
    private BigDecimal stockReservadoBlister = BigDecimal.ZERO;

    @Column(name = "stock_reservado_caja")
    @Builder.Default
    private BigDecimal stockReservadoCaja = BigDecimal.ZERO;

    @Column(name = "tipo_afectacion")
    @Builder.Default
    private String tipoAfectacion = "GRAVADO";

    @Column(length = 255)
    @Builder.Default
    private String foto = "sinfoto.png";

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @CreatedBy
    @Column(name = "usuario_creacion", length = 50)
    private String usuarioCreacion;

    @LastModifiedBy
    @Column(name = "usuario_modificacion", length = 50)
    private String usuarioModificacion;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TipoPrecio {
        PRECIO_FACTOR("Precio Factor."),
        PRECIO_FIJO("Precio fijo."),
        PRECIO_VARIABLE("Precio Variable.");

        private final String descripcion;

        TipoPrecio(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum Tipo {
        SERVICIO("Servicio"),
        PAQUETE("Paquete"),
        PRODUCTO("Producto");

        private final String descripcion;

        Tipo(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    @JsonProperty("idTipoTemperatura")
    public Long getIdTipoTemperatura() {
        return tipoTemperatura != null ? tipoTemperatura.getId() : null;
    }

    @JsonProperty("tipoTemperaturaNombre")
    public String getTipoTemperaturaNombre() {
        return tipoTemperatura != null ? tipoTemperatura.getNombre() : null;
    }

    @JsonProperty("tempMin")
    public BigDecimal getTempMin() {
        return tipoTemperatura != null ? tipoTemperatura.getTempMin() : null;
    }

    @JsonProperty("tempMax")
    public BigDecimal getTempMax() {
        return tipoTemperatura != null ? tipoTemperatura.getTempMax() : null;
    }

    @JsonProperty("idNivel")
    public Long getIdNivel() {
        return nivel != null ? nivel.getIdNivel() : null;
    }
}
