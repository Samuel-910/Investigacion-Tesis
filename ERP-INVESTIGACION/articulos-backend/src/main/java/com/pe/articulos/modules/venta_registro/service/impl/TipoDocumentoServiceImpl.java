package com.pe.articulos.modules.venta_registro.service.impl;

import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;
import com.pe.articulos.modules.venta_registro.repository.TipoDocumentoRepository;
import com.pe.articulos.modules.venta_registro.service.TipoDocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumento> findAll() {
        return tipoDocumentoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoDocumento> findById(String id) {
        return tipoDocumentoRepository.findByTipoDoc(id);
    }

    @Override
    @Transactional
    public TipoDocumento save(TipoDocumento tipoDocumento) {
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        tipoDocumentoRepository.findByTipoDoc(id).ifPresent(td -> {
            tipoDocumentoRepository.deleteById(td.getIdTipoDocumento());
        });
    }
}
