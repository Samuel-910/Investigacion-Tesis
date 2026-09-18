package com.pe.articulos.modules.atributos.service;

import com.pe.articulos.modules.atributos.entity.Categoria;
import com.pe.articulos.modules.atributos.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class CategoriaService extends BaseAtributoService<Categoria> {

    public CategoriaService(CategoriaRepository repository) {
        super(repository);
    }
}
