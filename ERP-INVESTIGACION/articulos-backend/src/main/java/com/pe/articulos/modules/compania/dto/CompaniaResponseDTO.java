package com.pe.articulos.modules.compania.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pe.articulos.core.enums.EstadoGeneral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompaniaResponseDTO {
    private Long id;
    private String codigo;
    private String codigoIafa;
    private String codGales;
    private String codEps;
    private String codCtr;
    private String codSec;
    private String idTipoCia;
    private String codPlan;
    private String representante;
    private String auditor;
    private String liquidador;
    private String financieroNombre;
    private String idPersonalUser;
    private String reqAmb;
    private String reqHos;
    private String reqEme;
    private String reqAcc;
    private Long daysWaiting;
    private String ctaCte;
    private String pluctc;
    private String diasPlazo;
    private String tipoTarif;
    private BigDecimal trabajaCpm;
    private Long importeCpm;
    private String afectaRecargoEspecial;
    private EstadoGeneral estado;
    private String nombre;
    private String nomCorto;
    private String tipoPac;
    private String chana;
    private String manejaCob;
    private String tiptra;
    private String borrar;
    private String mostrar;
    private String mostrarDirec;
    private String mostrarDiasPlazo;
    private String razSol;
    private String usuarioCreacion;
    private String usuarioModificacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String facturar;
    private Integer contador;
    private Integer clasifRepCobranzas;
    
    // DatosPersonales mapping
    private String ruc;
    private String email;
    private String direcc;
    private String fonLocal;
}
