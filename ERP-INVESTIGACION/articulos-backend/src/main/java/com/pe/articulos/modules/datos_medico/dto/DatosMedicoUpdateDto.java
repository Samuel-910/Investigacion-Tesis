package com.pe.articulos.modules.datos_medico.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.pe.articulos.core.enums.EstadoGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosMedicoUpdateDto {

    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombreMed;

    private EstadoGeneral estado;

    private String tipo;
    private String nroCmp;
    private String nroRne;

    private BigDecimal honorarios;
    private String planilla;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCese;

    private String tipoPago;
    private String formaPago;

    private Long idArea;
    private Long idSucursal;

    private String observacion;
}