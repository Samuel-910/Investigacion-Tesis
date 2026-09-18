package com.pe.articulos.modules.empresa.dto;

import java.time.LocalDate;
import com.pe.articulos.core.enums.EstadoGeneral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaDTO {
    private Long idEmpresa;
    private String representante;
    private String auditor;
    private String liquidador;
    private String financiero;
    private String idPersonalUser;
    private String fax;
    private String empresasChana;
    private String direccion;
    private String telefono;
    private String codigo;
    private String abrev;
    private String nombre;
    private EstadoGeneral estado;
    private LocalDate fechaCre;
    private String ctacte;
}
