package com.pe.articulos.modules.descuentos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta_descuentos", indexes = {
        @Index(name = "idx_descuentos_compania", columnList = "id_compania"),
        @Index(name = "idx_descuentos_activo", columnList = "activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "id_compania")
    private Long idCompania;

    @Column(name = "usuarios_afectados", columnDefinition = "TEXT")
    private String usuariosAfectados;

    @Column(name = "tipo_alcance", length = 30)
    private String tipoAlcance; // GLOBAL, POR_PRODUCTO

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Builder.Default
    private Boolean activo = true;

    @OneToMany(mappedBy = "descuento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DescuentoDetalle> detalles = new ArrayList<>();

    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
