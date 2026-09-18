package com.pe.articulos.modules.atributos.service;

import com.pe.articulos.modules.atributos.entity.PrincipioActivo;
import com.pe.articulos.modules.atributos.repository.PrincipioActivoRepository;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class PrincipioActivoService extends BaseAtributoService<PrincipioActivo> {

    public PrincipioActivoService(PrincipioActivoRepository repository) {
        super(repository);
    }
}
