package com.pe.articulos.modules.productos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "inventario_ajustes", indexes = {
        @Index(name = "idx_ajuste_sucursal", columnList = "id_sucursal"),
        @Index(name = "idx_ajuste_fecha", columnList = "fecha_registro")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjusteInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @Column(nullable = false, length = 10)
    private String tipo; // INGRESO, SALIDA

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @OneToMany(mappedBy = "ajuste", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AjusteInventarioDetalle> detalles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clasificacion")
    private ClasificacionMovimiento clasificacion;

    @Column(length = 20)
    private String correlativo;

    @CreatedDate
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
}
