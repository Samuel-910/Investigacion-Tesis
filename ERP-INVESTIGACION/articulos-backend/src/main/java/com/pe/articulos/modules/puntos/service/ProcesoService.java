package com.pe.articulos.modules.puntos.service;

import com.pe.articulos.modules.atributos.service.BaseAtributoService;
import com.pe.articulos.modules.puntos.entity.Proceso;
import com.pe.articulos.modules.puntos.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcesoService extends BaseAtributoService<Proceso> {

    public ProcesoService(ProcesoRepository repository) {
        super(repository);
    }
}
