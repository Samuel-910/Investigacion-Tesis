package com.pe.articulos.modules.puntos.repository;

import com.pe.articulos.modules.atributos.repository.BaseAtributoRepository;
import com.pe.articulos.modules.puntos.entity.Tipo;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoRepository extends BaseAtributoRepository<Tipo> {
}
