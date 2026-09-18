package com.pe.articulos.core.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemDTO {
    private Long id;
    private String titulo;
    private String ruta;
    private String icono;
    private Integer orden;
    private String tipo;

    @Builder.Default
    private List<MenuItemDTO> children = new ArrayList<>();
}