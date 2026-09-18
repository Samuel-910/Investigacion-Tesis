package com.pe.articulos.modules.venta_registro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearVentaRequest {

    private String idPersonal; // Paciente
    private LocalDate fecha;

    private String punto; // Código del punto de venta (si no se envía, se usa del usuario)
    private String tipoDoc; // 01=Factura, 03=Boleta (Requerido)


    private String tipoPac; // Tipo de paciente (opcional)
    private String moneda; // PEN, USD (default: PEN)
    private BigDecimal descuento; // Descuentos a aplicar (opcional)
    private String idMedico; // Médico que deriva (opcional)
    private String observacion; // Notas adicionales (opcional)
    private EstadoGeneral estado; // VIGENTE=Venta, PENDIENTE=Cotizacion
    private String metodoPago; // EFECTIVO, TARJETA, etc.
    private Long idPlantilla; // Plantilla de impresión seleccionada

    private String nroDni;
    private String nombrePac;
    private String tipoDni;
    private BigDecimal importePago;
    private BigDecimal vuelto;
    private Long canjeNotaVentaId;

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Long getIdPlantilla() {
        return idPlantilla;
    }

    public void setIdPlantilla(Long idPlantilla) {
        this.idPlantilla = idPlantilla;
    }

    @NotEmpty(message = "La venta debe tener al menos un detalle")
    @Valid
    private List<DetalleVentaRequest> detalles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleVentaRequest {
        private String idExamen;
        private String idCatalogo;
        private String idArticulo;
        private String glosa;
        private String idMedicoSer;
        private BigDecimal cantidad;
        private BigDecimal precioUnitario;
        private String unidadMedida;
        private BigDecimal total;

        private BigDecimal descuento;
        private BigDecimal porcDsc;
        private BigDecimal porcentajeDescuento;
        private BigDecimal montoDescuento;
        private String nroLote;
        private LocalDate fechaVenc;
    }
}

