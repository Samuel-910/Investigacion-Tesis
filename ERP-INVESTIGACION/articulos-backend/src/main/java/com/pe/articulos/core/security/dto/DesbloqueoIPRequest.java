package com.pe.articulos.core.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesbloqueoIPRequest {

    private String ip;
    private String motivo;
}
