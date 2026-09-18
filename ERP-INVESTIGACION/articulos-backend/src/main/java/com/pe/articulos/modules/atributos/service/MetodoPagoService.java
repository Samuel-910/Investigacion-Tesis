package com.pe.articulos.modules.atributos.service;

import com.pe.articulos.modules.atributos.entity.MetodoPago;
import com.pe.articulos.modules.atributos.repository.MetodoPagoRepository;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class MetodoPagoService extends BaseAtributoService<MetodoPago> {
    public MetodoPagoService(MetodoPagoRepository repository) {
        super(repository);
    }
}
