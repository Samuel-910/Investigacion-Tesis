package com.pe.articulos.core.enums;

import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum EstadoGeneral {
    INACTIVO(0),
    ACTIVO(1),
    PENDIENTE(2),
    ELIMINADO(3),
    REGISTRADO(4),
    SOLICITADO(5),
    ENVIADO(6),
    RECIBIDO(7),
    CANCELADO(8),
    VIGENTE(9),
    ANULADO(10),
    SUSPENDIDO(11),
    PENDIENTE_ANULACION(12),
    PROCESADO(13),
    EN_ESPERA(14),
    VALIDADO(15),
    PAGADO(16),
    COTIZACION(17);

    private final int valor;

    EstadoGeneral(int valor) {
        this.valor = valor;
    }

    public String getName() {
        return this.name();
    }

    public static EstadoGeneral fromInt(Integer valor) {
        if (valor == null)
            return INACTIVO;
        for (EstadoGeneral estado : EstadoGeneral.values()) {
            if (estado.getValor() == valor) {
                return estado;
            }
        }
        return INACTIVO;
    }

    @JsonCreator
    public static EstadoGeneral fromCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty())
            return ACTIVO;
        String c = codigo.trim().toUpperCase();
        return switch (c) {
            case "A", "ACTIVO", "1" -> ACTIVO;
            case "I", "INACTIVO", "0" -> INACTIVO;
            case "P", "PENDIENTE", "2" -> PENDIENTE;
            case "C", "COTIZACION", "17" -> COTIZACION;
            case "E", "ELIMINADO", "3" -> ELIMINADO;
            case "R", "REGISTRADO", "4" -> REGISTRADO;
            case "S", "SOLICITADO", "5" -> SOLICITADO;
            case "V", "VIGENTE", "9" -> VIGENTE;
            case "X", "ANULADO", "10" -> ANULADO;
            case "PA", "PENDIENTE_ANULACION", "12" -> PENDIENTE_ANULACION;
            case "VAL", "VALIDADO", "15" -> VALIDADO;
            case "PAG", "PAGADO", "16" -> PAGADO;
            default -> ACTIVO;
        };
    }
}