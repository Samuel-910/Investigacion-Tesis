package com.pe.articulos.modules.puntos.dto;

import com.pe.articulos.modules.documentos.entities.Modulo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoDocumentoResponseDTO {

    private Long id;
    private Long puntoId;
    private String puntoNombre;
    private String tipoDoc;
    private String serie;
    private Integer numero;
    private String ip;
    private Integer idDocimp;
    private String x;
    private Integer idPersonalUser;
    private String selecc;
    private String nota;
    private String serieTicketera;
    private EstadoGeneral estado;
    private String lpt;
    private String detNc;
    private String refact;
    private Integer refactDia;
    private Modulo modulo;
    private Long idPlantilla;
    private String plantillaNombre;
    private String tipoDocumentoNombre;
    private String plantillaFormato;
    private String plantillaOrientacion;
}
