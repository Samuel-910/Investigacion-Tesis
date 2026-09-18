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
public class DatosMedicoCreateDto {

    @NotBlank(message = "El nombre del médico es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombreMed;

    @NotNull(message = "El estado es obligatorio")
    private EstadoGeneral estado;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @Pattern(regexp = "^[0-9]{6}$", message = "El CMP debe tener 6 dígitos")
    private String nroCmp;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;

    private Long idArea;
    private Long idSucursal;

    private BigDecimal honorarios;
    private String tipoPago;
    private String planilla;
}
