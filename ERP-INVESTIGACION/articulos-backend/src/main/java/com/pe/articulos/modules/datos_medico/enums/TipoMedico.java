package com.pe.articulos.modules.datos_medico.enums;

public enum TipoMedico {
    GENERAL("GENERAL", "Médico General"),
    ESPECIALISTA("ESPECIALISTA", "Médico Especialista"),
    RESIDENTE("RESIDENTE", "Médico Residente");

    private final String codigo;
    private final String descripcion;

    TipoMedico(String codigo, String descripcion) {
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