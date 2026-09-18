package com.pe.articulos.modules.niveles.dto;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NivelDto {

    private Long idNivel;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 50, message = "El número de nivel no puede exceder 50 caracteres")
    private String numNivel;

    @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
    private String tipo;

    private String idTipoAte;
    private String centroCosto;

    @DecimalMin(value = "0.0", inclusive = true, message = "El límite debe ser mayor o igual a 0")
    private BigDecimal centCostLimite;

    private EstadoGeneral estado;

    // JERARQUÍA
    private Long idNivelPadre;
    private Integer nivelJerarquia;
    private Integer orden;

    // DATOS ADICIONALES
    private String nombrePadre;
    private boolean tieneHijos;
    private int totalHijos;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}