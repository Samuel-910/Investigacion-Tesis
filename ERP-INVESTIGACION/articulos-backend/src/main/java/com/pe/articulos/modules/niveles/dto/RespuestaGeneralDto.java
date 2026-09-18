package com.pe.articulos.modules.niveles.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespuestaGeneralDto<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public RespuestaGeneralDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> RespuestaGeneralDto<T> exitoso(String mensaje, T datos) {
        return new RespuestaGeneralDto<>(true, mensaje, datos);
    }

    public static <T> RespuestaGeneralDto<T> exitoso(String mensaje) {
        return new RespuestaGeneralDto<>(true, mensaje, null);
    }

    public static <T> RespuestaGeneralDto<T> error(String mensaje) {
        return new RespuestaGeneralDto<>(false, mensaje, null);
    }
}