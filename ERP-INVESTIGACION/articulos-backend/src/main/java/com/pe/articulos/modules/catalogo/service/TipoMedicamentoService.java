package com.pe.articulos.modules.catalogo.service;

import com.pe.articulos.modules.atributos.service.BaseAtributoService;
import com.pe.articulos.modules.catalogo.entity.TipoMedicamento;
import com.pe.articulos.modules.catalogo.repository.TipoMedicamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TipoMedicamentoService extends BaseAtributoService<TipoMedicamento> {

    public TipoMedicamentoService(TipoMedicamentoRepository repository) {
        super(repository);
    }
}
