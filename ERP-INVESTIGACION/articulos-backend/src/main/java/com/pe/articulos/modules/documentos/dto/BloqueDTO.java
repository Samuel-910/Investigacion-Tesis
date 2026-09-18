package com.pe.articulos.modules.documentos.dto;

import com.pe.articulos.modules.documentos.entities.Modulo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloqueDTO {
    private Long id;
    private String nombre;
    private String htmlContenido;
    private String cssEstilo;
    private String categoria;
    private Set<Modulo> modulos;
    private Integer estado;
}
