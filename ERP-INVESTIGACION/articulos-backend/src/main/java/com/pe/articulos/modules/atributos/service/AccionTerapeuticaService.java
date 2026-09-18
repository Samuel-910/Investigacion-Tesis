package com.pe.articulos.modules.atributos.service;

import com.pe.articulos.modules.atributos.entity.AccionTerapeutica;
import com.pe.articulos.modules.atributos.repository.AccionTerapeuticaRepository;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class AccionTerapeuticaService extends BaseAtributoService<AccionTerapeutica> {

    public AccionTerapeuticaService(AccionTerapeuticaRepository repository) {
        super(repository);
    }
}
