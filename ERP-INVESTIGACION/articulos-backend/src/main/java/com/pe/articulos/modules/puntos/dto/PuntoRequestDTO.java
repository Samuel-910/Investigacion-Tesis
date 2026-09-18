package com.pe.articulos.modules.puntos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    private Integer idSucursal;

    private Long idTipo;
    private Long idProceso;

    private Integer idNivel;

    @Size(max = 10, message = "El campo excep no puede exceder 10 caracteres")
    private String excep;

    @Size(max = 255, message = "La impresión no puede exceder 255 caracteres")
    private String impresion;

    @Size(max = 10)
    private String imprimeComp;

    @Size(max = 10)
    private String agrupaServ;

    @Size(max = 10)
    private String emiteHonorMed;

    @Size(max = 50)
    private String tipo2;

    @Size(max = 50)
    private String abreviacion;

    private LocalDateTime fecini;

    private LocalDateTime fecfin;

    @Size(max = 50)
    private String cencos;

    @Size(max = 10)
    private String valido;

    private BigDecimal cargaPresup;

    private Integer ultConsultas;

    private BigDecimal acumulaPedidos;

    private Integer verNroCtas;

    private Long idAlmacen;

    @Size(max = 10)
    private String cobra;

    @Size(max = 10)
    private String buscaXIniciales;

    @Size(max = 10)
    private String cargaMedico;

    @Size(max = 100)
    private String ipAccesoModulo;

    @Size(max = 10)
    private String imprimePreventa;

    private Integer modulo;

    @Size(max = 50)
    private String orden;

    @Size(max = 10)
    private String receta;

    @Size(max = 50)
    private String terminal;

    @Size(max = 50)
    private String tipoAhe;

    @Size(max = 10)
    private String repRecep;

    @Size(max = 10)
    private String autorizado;

    @Size(max = 10)
    private String senc;

    @Size(max = 50)
    private String abrevSenc;

    @Size(max = 50)
    private String cencosOld;

    @Size(max = 10)
    private String arqueo;

    @Size(max = 10)
    private String fe;
}
