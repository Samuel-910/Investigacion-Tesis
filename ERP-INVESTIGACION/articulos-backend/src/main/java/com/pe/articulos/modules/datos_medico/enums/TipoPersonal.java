package com.pe.articulos.modules.datos_medico.enums;

public enum TipoPersonal {
    MEDICO("MEDICO", "Médico"),
    ENFERMERA("ENFERMERA", "Enfermera"),
    TECNICO("TECNICO", "Técnico"),
    ADMIN("ADMIN", "Administrativo");

    private final String codigo;
    private final String descripcion;

    TipoPersonal(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static TipoPersonal fromCodigo(String codigo) {
        for (TipoPersonal tipo : values()) {
            if (tipo.getCodigo().equals(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo no válido: " + codigo);
    }
}