package com.pe.articulos.modules.caja_chica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CierreCajaRequest {
    private BigDecimal saldoCierreReal;
    private boolean transferirACajaGeneral;
    private List<DiferenciaArqueoDTO> diferencias;
}
