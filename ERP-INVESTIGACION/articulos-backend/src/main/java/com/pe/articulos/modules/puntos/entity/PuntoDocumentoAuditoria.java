package com.pe.articulos.modules.puntos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "puntos_documento_auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@EntityListeners(AuditingEntityListener.class)
public class PuntoDocumentoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_punto_doc", nullable = false)
    private Long idPuntoDoc;

    @Column(name = "usuario", length = 100)
    private String usuario;

    @CreatedDate
    @Column(name = "fecha_cambio", updatable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "operacion", length = 20)
    private String operacion; // UPDATE, CREATE

    @Column(name = "datos_anteriores", columnDefinition = "TEXT")
    private String datosAnteriores;
}
