package com.pe.articulos.modules.datos_medico.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.pe.articulos.core.enums.EstadoGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosMedicoDto {
    private Long id;

    private Long idPersonal;

    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    private String codigo;

    @Pattern(regexp = "^[0-9]{6}$", message = "El CMP debe tener 6 dígitos")
    private String nroCmp;

    @Size(max = 20, message = "El RNE no puede exceder 20 caracteres")
    private String nroRne;

    @NotBlank(message = "El nombre del médico es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombreMed;

    @DecimalMin(value = "0.0", inclusive = false, message = "Los honorarios deben ser mayor a 0")
    private BigDecimal honorarios;

    @NotNull(message = "El estado es obligatorio")
    private EstadoGeneral estado;

    @Pattern(regexp = "[SN]", message = "Planilla debe ser S o N")
    private String planilla;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCese;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ultimaLiquidacion;

    @Size(max = 20, message = "Tipo de pago no puede exceder 20 caracteres")
    private String tipoPago;

    @Size(max = 20, message = "Forma de pago no puede exceder 20 caracteres")
    private String formaPago;

    @Size(max = 30, message = "Tipo de recibo no puede exceder 30 caracteres")
    private String tipoRecibo;

    @Size(max = 1000, message = "La observación no puede exceder 1000 caracteres")
    private String observacion;

    @Size(max = 50, message = "ID usuario no puede exceder 50 caracteres")
    private String idPersonalUser;

    @Size(max = 30, message = "Tipo no puede exceder 30 caracteres")
    private String tipo;

    @Size(max = 50, message = "Cuenta corriente no puede exceder 50 caracteres")
    private String cuentaCorriente;

    @Size(max = 30, message = "Tipo de plan no puede exceder 30 caracteres")
    private String tipoPlan;

    @Size(max = 50, message = "Usuario actualización no puede exceder 50 caracteres")
    private String usuarioActualizacionObs;

    @Size(max = 30, message = "Número de cuenta no puede exceder 30 caracteres")
    private String numeroCuenta;

    @Pattern(regexp = "[SN]", message = "Sueldo paquete debe ser S o N")
    private String sueldoPaquete;

    @Size(max = 20, message = "Código contrato no puede exceder 20 caracteres")
    private String codigoContrato;

    @Pattern(regexp = "[SN]", message = "Imprimir turno debe ser S o N")
    private String imprimirTurno;

    @Pattern(regexp = "[SN]", message = "Emergencia debe ser S o N")
    private String emergencia;

    private Long idArea;

    @Size(max = 200, message = "Página web no puede exceder 200 caracteres")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "URL no válida", flags = {
            Pattern.Flag.CASE_INSENSITIVE })
    private String paginaWeb;

    @Size(max = 2000, message = "Pregrado no puede exceder 2000 caracteres")
    private String preGrado;

    @Size(max = 2000, message = "Postgrado no puede exceder 2000 caracteres")
    private String postGrado;

    @Size(max = 2000, message = "Cargos no puede exceder 2000 caracteres")
    private String cargos;

    @Size(max = 200, message = "Idiomas no puede exceder 200 caracteres")
    private String idiomas;

    @Size(max = 2000, message = "Experiencia no puede exceder 2000 caracteres")
    private String experiencia;

    @Pattern(regexp = "[SN]", message = "Acepta FT debe ser S o N")
    private String aceptaFacturaTributaria;

    @Pattern(regexp = "[SN]", message = "Capacitado debe ser S o N")
    private String capacitado;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCapacitacion;

    private Long idSucursal;

    @Pattern(regexp = "[SN]", message = "Vacaciones debe ser S o N")
    private String vacaciones;

    @Min(value = 0, message = "Días de vacaciones debe ser mayor o igual a 0")
    private Integer diasVacaciones;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicioVacaciones;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFinVacaciones;

    @Size(max = 50, message = "Tipo médico no puede exceder 50 caracteres")
    private String tipoMedico;

    @Pattern(regexp = "[SN]", message = "Anestesia debe ser S o N")
    private String anestesia;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaActualizacion;

    // Datos adicionales (desde relaciones)
    private String nombreArea;
    private String nombreSucursal;
}
