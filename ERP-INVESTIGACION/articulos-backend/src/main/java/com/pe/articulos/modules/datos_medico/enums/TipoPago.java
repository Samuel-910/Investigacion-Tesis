package com.pe.articulos.modules.datos_medico.enums;

public enum TipoPago {
    MENSUAL("MENSUAL", "Pago Mensual"),
    QUINCENAL("QUINCENAL", "Pago Quincenal"),
    SEMANAL("SEMANAL", "Pago Semanal");

    private final String codigo;
    private final String descripcion;

    TipoPago(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}