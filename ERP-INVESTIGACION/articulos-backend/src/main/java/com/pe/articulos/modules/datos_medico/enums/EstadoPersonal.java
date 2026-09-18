package com.pe.articulos.modules.datos_medico.enums;

public enum EstadoPersonal {
    ACTIVO("A", "Activo"),
    INACTIVO("I", "Inactivo"),
    SUSPENDIDO("S", "Suspendido");

    private final String codigo;
    private final String descripcion;

    EstadoPersonal(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static EstadoPersonal fromCodigo(String codigo) {
        for (EstadoPersonal estado : values()) {
            if (estado.getCodigo().equals(codigo)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + codigo);
    }
}