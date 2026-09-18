package com.pe.articulos.modules.atributos.service;

import com.pe.articulos.modules.atributos.entity.Laboratorio;
import com.pe.articulos.modules.atributos.repository.LaboratorioRepository;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class LaboratorioService extends BaseAtributoService<Laboratorio> {

    public LaboratorioService(LaboratorioRepository repository) {
        super(repository);
    }
}
