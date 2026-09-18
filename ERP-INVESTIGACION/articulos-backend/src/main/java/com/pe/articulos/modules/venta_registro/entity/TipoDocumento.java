package com.pe.articulos.modules.venta_registro.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_documento")
    private Long idTipoDocumento;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nombre", length = 200)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "requiere_ruc", nullable = false)
    @Builder.Default
    private Boolean requiereRuc = false;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "tipo_doc", length = 2, unique = true)
    private String tipoDoc;

    @Column(name = "base_imp", length = 4)
    private String baseImp;

    @Column(length = 1)
    private String compras;

    @Column(length = 1)
    private String ventas;

    @Column(length = 1)
    private String contado;

    @Column(length = 1)
    private String credito;

    @Column(length = 1)
    @Builder.Default
    private String clinic = "1";

    @Column(length = 1)
    private String igv;

    @Column(length = 5)
    private String retenc;

    @Column(length = 1)
    private String regcom;

    @Column(name = "codsun", length = 2)
    private String codsun;

    @Column(length = 1)
    private String crefis;

    @Column(nullable = false)
    private Integer factor;

    @Column(length = 10)
    private String prefij;

    @Column(length = 1)
    private String regven;

    @Column(length = 12)
    private String nombre2;

    @Column(name = "compra_art", length = 1)
    @Builder.Default
    private String compraArt = "0";

    @Column(length = 1)
    @Builder.Default
    private String selpag = "0";

    @Column(length = 1)
    private String detdoc;

    @Column(length = 1)
    private String modo;

    @Column(length = 2)
    private String codeps;

    @Column(length = 1)
    @Builder.Default
    private String ctacte = "1";

    @Column(length = 1)
    private String ctapag;

    @Column(length = 1)
    @Builder.Default
    private String electr = "0";
}

