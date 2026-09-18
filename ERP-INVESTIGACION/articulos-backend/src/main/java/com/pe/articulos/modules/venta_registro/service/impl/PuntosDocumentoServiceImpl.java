package com.pe.articulos.modules.venta_registro.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.modules.puntos.entity.PuntoDocumento;
import com.pe.articulos.modules.puntos.repository.PuntoDocumentoRepository;
import com.pe.articulos.modules.venta_registro.dto.SerieCorrelativoDto;
import com.pe.articulos.modules.venta_registro.service.PuntosDocumentoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class PuntosDocumentoServiceImpl implements PuntosDocumentoService {

    private final PuntoDocumentoRepository puntoDocumentoRepository;

    @Override
    @Transactional(noRollbackFor = {Exception.class})
    public SerieCorrelativoDto generarCorrelativo(Long puntoId, String tipoDocId, com.pe.articulos.modules.documentos.entities.Modulo modulo) {
        log.info("Generando correlativo para punto {}, tipo documento {} y módulo {}", puntoId, tipoDocId, modulo);

        List<PuntoDocumento> docs = puntoDocumentoRepository.findByPuntoAndTipoDocAndModulo(puntoId, tipoDocId, modulo);

        if (docs.isEmpty()) {
            List<PuntoDocumento> disponibles = puntoDocumentoRepository.findByPuntoPunto(puntoId);
            log.error("❌ Correlativo NO encontrado. Punto: {}, Tipo buscado: '{}'", puntoId, tipoDocId);
            log.error("   📄 Documentos disponibles para este punto: {}",
                    disponibles.stream()
                            .map(d -> (d.getTipoDocumento() != null ? d.getTipoDocumento().getTipoDoc() : "NULL") + " ("
                                    + d.getSerie() + ")")
                            .toList());

            throw new ResourceNotFoundException("No se encontró configuración de correlativo para punto: "
                    + puntoId + " y tipo: " + tipoDocId);
        }


        PuntoDocumento config = docs.get(0);

        Integer nuevoNumero = (config.getNumero() != null ? config.getNumero() : 0) + 1;
        config.setNumero(nuevoNumero);

        puntoDocumentoRepository.save(config);

        return SerieCorrelativoDto.builder()
                .serie(config.getSerie())
                .numero(nuevoNumero)
                .build();
    }

    @Override
    @Transactional(noRollbackFor = {Exception.class})
    public SerieCorrelativoDto generarCorrelativoConPrefijo(Long puntoId, String tipoDocId, com.pe.articulos.modules.documentos.entities.Modulo modulo, String seriePrefix) {
        log.info("Generando correlativo para punto {}, tipo documento {}, módulo {} con prefijo {}", puntoId, tipoDocId, modulo, seriePrefix);

        List<PuntoDocumento> docs = puntoDocumentoRepository.findByPuntoAndTipoDocAndModulo(puntoId, tipoDocId, modulo);

        if (docs.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró configuración de correlativo para punto: "
                    + puntoId + " y tipo: " + tipoDocId);
        }

        PuntoDocumento config = docs.stream()
                .filter(d -> d.getSerie() != null && d.getSerie().startsWith(seriePrefix))
                .findFirst()
                .orElse(docs.get(0));

        Integer nuevoNumero = (config.getNumero() != null ? config.getNumero() : 0) + 1;
        config.setNumero(nuevoNumero);

        puntoDocumentoRepository.save(config);

        return SerieCorrelativoDto.builder()
                .serie(config.getSerie())
                .numero(nuevoNumero)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumento> findAll() {
        return puntoDocumentoRepository.findAll();
    }

    @Override
    @Transactional(noRollbackFor = {Exception.class})
    public PuntoDocumento save(PuntoDocumento puntosDocumento) {
        return puntoDocumentoRepository.save(puntosDocumento);
    }
}

