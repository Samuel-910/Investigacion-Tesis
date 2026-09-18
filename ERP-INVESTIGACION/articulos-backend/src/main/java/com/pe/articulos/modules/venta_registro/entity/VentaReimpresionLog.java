package com.pe.articulos.modules.venta_registro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "venta_reimpresion_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaReimpresionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    private VentaRegistro venta;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "nombre_usuario", length = 100)
    private String nombreUsuario;

    @Column(name = "fecha_reimpresion", nullable = false)
    private LocalDateTime fechaReimpresion;

    @Column(name = "ip", length = 50)
    private String ip;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

}
