package com.pe.articulos.modules.kardex.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KardexDTO {
    private String idArticuloKardex;
    private Long idAlmacen;
    private Long idCatalogo;
    private Long idSucursal;
    private Long idUsuario;
    private UsuarioResumenDTO usuario;
    private String idDocumento;
    private String numDoc;
    private LocalDateTime fecha;
    private String detalle;
    private String operacion;
    private String signo;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private String observacion;
    private String origenId;
    private String origenTipo;
    private String nroLote;
    private LocalDate fechaVenc;
    private String presentacion;
    private Long idClasificacion;
    private BigDecimal saldoCantidad;
    private BigDecimal saldoCostoUnitario;
    private BigDecimal saldoCostoTotal;
}
