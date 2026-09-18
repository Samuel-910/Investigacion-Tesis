package com.pe.articulos.service;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cola_eventos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColaEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String modulo;

    @Column(nullable = false, length = 100)
    private String topico;

    @Column(length = 100)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(length = 50)
    private String tipo;

    @Column(length = 20)
    private String estado;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_procesado")
    private LocalDateTime fechaProcesado;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}
