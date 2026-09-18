package com.pe.articulos.modules.niveles.dto;

import com.pe.articulos.core.enums.EstadoGeneral;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NivelCreateDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    private String numNivel;
    private String tipo;
    private String idTipoAte;
    private String centroCosto;
    private BigDecimal centCostLimite;
    private EstadoGeneral estado;

    // JERARQUÍA
    private Long idNivelPadre;
    private Integer orden;
}
