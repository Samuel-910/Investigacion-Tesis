package com.pe.articulos.modules.atributos.service;

import com.pe.articulos.modules.atributos.entity.Ubicacion;
import com.pe.articulos.modules.atributos.repository.UbicacionRepository;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class UbicacionService extends BaseAtributoService<Ubicacion> {

    public UbicacionService(UbicacionRepository repository) {
        super(repository);
    }
}
