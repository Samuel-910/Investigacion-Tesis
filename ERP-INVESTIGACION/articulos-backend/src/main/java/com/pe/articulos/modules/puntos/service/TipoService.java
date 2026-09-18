package com.pe.articulos.modules.puntos.service;

import com.pe.articulos.modules.atributos.service.BaseAtributoService;
import com.pe.articulos.modules.puntos.entity.Tipo;
import com.pe.articulos.modules.puntos.repository.TipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TipoService extends BaseAtributoService<Tipo> {

    public TipoService(TipoRepository repository) {
        super(repository);
    }
}
