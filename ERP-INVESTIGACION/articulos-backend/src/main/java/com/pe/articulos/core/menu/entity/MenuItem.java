package com.pe.articulos.core.menu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pe.articulos.modules.accesos.entity.AccesoMain;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_acceso")
    private AccesoMain acceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MenuItem parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<MenuItem> children = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(length = 255)
    private String ruta;

    @Column(columnDefinition = "TEXT")
    private String icono;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TipoMenuItem tipo = TipoMenuItem.ITEM;

    // ========================================
    // SISTEMA HÍBRIDO DE CONTROL DE ACCESO
    // ========================================

    /**
     * Tipo de control de acceso para este item
     */
    @Column(name = "tipo_control", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TipoControl tipoControl = TipoControl.MODULO;

    /**
     * Módulo requerido (usado cuando tipoControl = MODULO)
     */
    @Column(name = "modulo_requerido", length = 100)
    private String moduloRequerido;

    /**
     * Permisos requeridos como JSON array (usado cuando tipoControl = PERMISOS)
     * Formato: ["PERMISO_1", "PERMISO_2", "PERMISO_3"]
     */
    @Column(name = "permisos_requeridos", columnDefinition = "TEXT")
    private String permisosRequeridos;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // ========================================
    // ENUMS
    // ========================================

    public enum TipoMenuItem {
        GRUPO, // Sección principal (ej: CONFIGURACIONES)
        SUBMENU, // Subsección (ej: SEGURIDAD)
        ITEM // Item final con ruta
    }

    public enum TipoControl {
        PUBLICO, // No requiere verificación
        MODULO, // Requiere cualquier permiso del módulo
        PERMISOS, // Requiere AL MENOS UNO de los permisos
        PERMISOS_TODOS // Requiere TODOS los permisos
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    /**
     * Verifica si este item requiere algún control de acceso
     */
    public boolean requiereAcceso() {
        return tipoControl != null && tipoControl != TipoControl.PUBLICO;
    }

    /**
     * Obtiene la lista de permisos requeridos desde el JSON
     */
    public List<String> getPermisosRequeridosList() {
        if (permisosRequeridos == null || permisosRequeridos.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Parsear JSON simple: ["PERMISO_1", "PERMISO_2"]
            String cleaned = permisosRequeridos
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .trim();

            if (cleaned.isEmpty()) {
                return new ArrayList<>();
            }

            return Arrays.asList(cleaned.split("\\s*,\\s*"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Establece la lista de permisos como JSON
     */
    public void setPermisosRequeridosList(List<String> permisos) {
        if (permisos == null || permisos.isEmpty()) {
            this.permisosRequeridos = null;
            return;
        }

        // Crear JSON simple
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < permisos.size(); i++) {
            if (i > 0)
                json.append(", ");
            json.append("\"").append(permisos.get(i)).append("\"");
        }
        json.append("]");

        this.permisosRequeridos = json.toString();
    }

    /**
     * Agrega un hijo a este item
     */
    public void addChild(MenuItem child) {
        children.add(child);
        child.setParent(this);
    }

    /**
     * Remueve un hijo de este item
     */
    public void removeChild(MenuItem child) {
        children.remove(child);
        child.setParent(null);
    }

    // ========================================
    // MÉTODOS DE DESCRIPCIÓN
    // ========================================

    /**
     * Obtiene una descripción legible del control de acceso
     */
    public String getDescripcionControl() {
        if (tipoControl == null || tipoControl == TipoControl.PUBLICO) {
            return "Público - Sin restricciones";
        }

        switch (tipoControl) {
            case MODULO:
                return "Módulo: " + (moduloRequerido != null ? moduloRequerido : "No especificado");

            case PERMISOS:
                return "Permisos (al menos uno): " + getPermisosRequeridosList();

            case PERMISOS_TODOS:
                return "Permisos (todos): " + getPermisosRequeridosList();

            default:
                return "Desconocido";
        }
    }
}