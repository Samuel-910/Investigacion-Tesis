package com.pe.articulos.modules.catalogo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoRequest {

    private String codigo;

    private String codigoSeus;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String detalle;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    private String tipoMedicamento;

    private Long idPrincipioActivo;

    private Long idAccionTerapeutica;

    @Size(max = 50)
    private String regSanitario;

    private Boolean esGenerico;
    private Boolean esControlado;
    private Boolean ventaConReceta;
    private Boolean manejaLotes;

    private Long idCategoria;

    private Long idNivel;

    @Size(max = 100, message = "La presentación no puede exceder 100 caracteres")
    private String presentacion;

    @Min(value = 0, message = "El tiempo de entrega no puede ser negativo")
    private Integer tiempoEntregaMinutos;

    // Jerarquía
    private Long idUnidadBase;
    private Long idUnidadIntermedia;
    private Long idUnidadMayor;

    // Unidades
    private Boolean manejaUnidad;
    private Boolean manejaBlister;
    private Integer factorBlister;
    private Boolean manejaCaja;
    private Integer factorCaja;

    private String tipoAfectacion;
    private java.math.BigDecimal precioKairos;

    @Size(max = 255, message = "La foto no puede exceder 255 caracteres")
    private String foto;

    private EstadoGeneral estado;
    private String usuarioCreacion;
}
