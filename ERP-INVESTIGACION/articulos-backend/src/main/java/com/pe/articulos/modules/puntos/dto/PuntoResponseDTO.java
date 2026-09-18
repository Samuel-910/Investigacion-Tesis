package com.pe.articulos.modules.puntos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoResponseDTO {

    private Long punto;
    private String nombre;
    private Integer idSucursal;
    private String nombreSucursal;
    private Long idTipo;
    private String nombreTipo;
    private Long idProceso;
    private String nombreProceso;
    private Integer idNivel;
    private String excep;
    private String impresion;
    private String imprimeComp;
    private String agrupaServ;
    private String emiteHonorMed;
    private String tipo2;
    private String abreviacion;
    private LocalDateTime fecini;
    private LocalDateTime fecfin;
    private String cencos;
    private String valido;
    private LocalDateTime creaCuenta;
    private BigDecimal cargaPresup;
    private Integer ultConsultas;
    private BigDecimal acumulaPedidos;
    private Integer verNroCtas;
    private Long idAlmacen;
    private String nombreAlmacen;
    private String cobra;
    private String buscaXIniciales;
    private String cargaMedico;
    private String ipAccesoModulo;
    private String imprimePreventa;
    private Integer modulo;
    private String orden;
    private String receta;
    private String terminal;
    private String tipoAhe;
    private String repRecep;
    private String autorizado;
    private String senc;
    private String abrevSenc;
    private String cencosOld;
    private String arqueo;
    private String fe;
}
