package com.pe.articulos.modules.clinica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "datos_clinica_auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ClinicaAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_clinica", nullable = false)
    private Long idClinica;

    @Column(name = "usuario", length = 100)
    private String usuario;

    @CreatedDate
    @Column(name = "fecha_cambio", updatable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "operacion", length = 20)
    private String operacion;

    @Column(name = "datos_anteriores", columnDefinition = "TEXT")
    private String datosAnteriores;
}
