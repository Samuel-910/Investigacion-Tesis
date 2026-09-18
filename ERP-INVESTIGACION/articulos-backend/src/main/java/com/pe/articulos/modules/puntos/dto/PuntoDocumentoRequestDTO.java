package com.pe.articulos.modules.puntos.dto;

import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.core.enums.EstadoGeneral;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoDocumentoRequestDTO {

    @NotNull(message = "El punto es obligatorio")
    private Long puntoId;

    @Size(max = 50, message = "El tipo de documento no puede exceder 50 caracteres")
    private String tipoDoc;

    @Size(max = 20, message = "La serie no puede exceder 20 caracteres")
    private String serie;

    private Integer numero;

    @Size(max = 100, message = "La IP no puede exceder 100 caracteres")
    private String ip;

    private Integer idDocimp;

    @Size(max = 50)
    private String x;

    private Integer idPersonalUser;

    @Size(max = 10)
    private String selecc;

    private String nota;

    @Size(max = 50)
    private String serieTicketera;

    private EstadoGeneral estado;

    @Size(max = 50)
    private String lpt;

    @Size(max = 50)
    private String detNc;

    @Size(max = 10)
    private String refact;

    private Integer refactDia;

    private Modulo modulo;

    private Long idPlantilla;
}
