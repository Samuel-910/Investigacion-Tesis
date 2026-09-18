package com.pe.articulos.modules.proveedores.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuenta_proveedor_movimiento", indexes = {
        @Index(name = "idx_cuenta_prov_mov", columnList = "id_cuenta")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaProveedorMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_cuenta", nullable = false)
    private CuentaProveedor cuenta;

    @Column(nullable = false, length = 20)
    private String tipo; // CARGO, ABONO

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(length = 255)
    private String descripcion;

    @CreatedDate
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "id_user")
    private String idUser;
}
