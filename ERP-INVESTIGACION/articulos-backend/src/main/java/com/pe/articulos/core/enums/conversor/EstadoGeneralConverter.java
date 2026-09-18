package com.pe.articulos.core.enums.conversor;

import com.pe.articulos.core.enums.EstadoGeneral;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoGeneralConverter implements AttributeConverter<EstadoGeneral, Integer> {
    @Override
    public Integer convertToDatabaseColumn(EstadoGeneral estado) {
        return (estado == null) ? null : estado.getValor();
    }

    @Override
    public EstadoGeneral convertToEntityAttribute(Integer valor) {
        return EstadoGeneral.fromInt(valor);
    }
}