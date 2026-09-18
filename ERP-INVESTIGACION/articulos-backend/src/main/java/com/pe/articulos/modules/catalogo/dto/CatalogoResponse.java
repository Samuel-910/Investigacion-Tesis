package com.pe.articulos.modules.catalogo.dto;

import com.pe.articulos.core.enums.EstadoGeneral;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoResponse {

    private Long id;
    private String codigo;
    private String codigoSeus;
    private String nombre;
    private String detalle;
    private String tipo;
    private String tipoMedicamento;
    private String principioActivo;
    private Long idPrincipioActivo;
    private String accionTerapeutica;
    private Long idAccionTerapeutica;
    private String regSanitario;
    private EstadoGeneral estado;
    private Boolean esGenerico;
    private Boolean esControlado;
    private Boolean ventaConReceta;
    private Boolean manejaLotes;
    private String categoria;
    private Long idCategoria;
    private Long idNivel;
    private String nivel;
    private String presentacion;
    private Integer tiempoEntregaMinutos;

    // Jerarquía
    private Long idUnidadBase;
    private String unidadBase;
    private Long idUnidadIntermedia;
    private String unidadIntermedia;
    private Long idUnidadMayor;
    private String unidadMayor;

    // Unidades
    private Boolean manejaUnidad;
    private Boolean manejaBlister;
    private Integer factorBlister;
    private Boolean manejaCaja;
    private Integer factorCaja;
    private String tipoAfectacion;
    private String foto;
    private java.math.BigDecimal precioKairos;
    private String usuarioCreacion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
