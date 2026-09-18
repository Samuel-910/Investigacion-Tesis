package com.pe.articulos.modules.compania.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompaniaRegistroDTO {
    private String username;
    private String email;
    private String nombre;
    private String direcc;
    private String fonLocal;
    private String ruc;
    private String tipodoc;
    private String numdoc;
    private String codigo;
    private String codigoIafa;
    private String codGales;
    private String codEps;
    private String codCtr;
    private String codSec;
    private String idTipoCia;
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
    private LocalDate fechaCreacion;
    private String facturar;
    private Integer contador;
    private Integer clasifRepCobranzas;
}
