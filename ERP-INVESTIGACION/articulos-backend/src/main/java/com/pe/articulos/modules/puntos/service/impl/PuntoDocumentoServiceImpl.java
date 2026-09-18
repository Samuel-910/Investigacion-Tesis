package com.pe.articulos.modules.puntos.service.impl;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.articulos.modules.puntos.dto.PuntoDocumentoRequestDTO;
import com.pe.articulos.modules.puntos.dto.PuntoDocumentoResponseDTO;
import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.puntos.entity.PuntoDocumento;
import com.pe.articulos.modules.puntos.entity.PuntoDocumentoAuditoria;
import com.pe.articulos.modules.puntos.mapper.PuntoDocumentoMapper;
import com.pe.articulos.modules.documentos.repositories.PlantillaRepository;
import com.pe.articulos.modules.puntos.repository.PuntoDocumentoAuditoriaRepository;
import com.pe.articulos.modules.puntos.repository.PuntoDocumentoRepository;
import com.pe.articulos.modules.puntos.repository.PuntoRepository;
import com.pe.articulos.modules.puntos.service.PuntoDocumentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class PuntoDocumentoServiceImpl implements PuntoDocumentoService {

    private final PuntoDocumentoRepository puntoDocumentoRepository;
    private final PuntoRepository puntoRepository;
    private final PuntoDocumentoMapper puntoDocumentoMapper;
    private final PuntoDocumentoAuditoriaRepository auditoriaRepository;
    private final PlantillaRepository plantillaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PuntoDocumentoResponseDTO crearDocumento(PuntoDocumentoRequestDTO requestDTO) {
        log.info("Creando nuevo documento para el punto ID: {}", requestDTO.getPuntoId());

        // Verificar que el punto existe
        Punto punto = puntoRepository.findById(requestDTO.getPuntoId())
                .orElseThrow(() -> new RuntimeException("Punto no encontrado con ID: " + requestDTO.getPuntoId()));

        // Verificar si ya existe un documento con la misma serie y número
        if (requestDTO.getSerie() != null && requestDTO.getNumero() != null) {
            if (puntoDocumentoRepository.existsBySerieAndNumero(requestDTO.getSerie(), requestDTO.getNumero())) {
                throw new RuntimeException("Ya existe un documento con la serie " + requestDTO.getSerie() +
                        " y número " + requestDTO.getNumero());
            }
        }

        PuntoDocumento documento = puntoDocumentoMapper.toEntity(requestDTO, punto);
        PuntoDocumento documentoGuardado = puntoDocumentoRepository.save(documento);

        registrarAuditoria(documentoGuardado, "CREATE", null);

        log.info("Documento creado exitosamente con ID: {}", documentoGuardado.getId());
        return enrichResponse(puntoDocumentoMapper.toResponseDTO(documentoGuardado));
    }

    @Override
    @Transactional(readOnly = true)
    public PuntoDocumentoResponseDTO obtenerDocumentoPorId(Long id) {
        log.info("Buscando documento con ID: {}", id);

        PuntoDocumento documento = puntoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        return enrichResponse(puntoDocumentoMapper.toResponseDTO(documento));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PuntoDocumentoResponseDTO> obtenerTodosDocumentos(Long puntoId, Pageable pageable) {
        log.info("Obteniendo todos los documentos paginados para puntoId: {}", puntoId);
        Page<PuntoDocumento> page;
        if (puntoId != null) {
            page = puntoDocumentoRepository.findByPuntoPunto(puntoId, pageable);
        } else {
            page = puntoDocumentoRepository.findAll(pageable);
        }
        org.springframework.data.domain.Page<PuntoDocumentoResponseDTO> pageDTO = page.map(puntoDocumentoMapper::toResponseDTO);
        return new PageResponse<>(
                pageDTO.getContent(),
                pageDTO.getTotalElements(),
                pageDTO.getTotalPages(),
                pageDTO.getSize(),
                pageDTO.getNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumentoResponseDTO> obtenerTodosDocumentosList() {
        log.info("Obteniendo todos los documentos como lista");
        List<PuntoDocumento> documentos = puntoDocumentoRepository.findAll();
        return documentos.stream()
                .map(puntoDocumentoMapper::toResponseDTO)
                .map(this::enrichResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PuntoDocumentoResponseDTO actualizarDocumento(Long id, PuntoDocumentoRequestDTO requestDTO) {
        log.info("Actualizando documento con ID: {}", id);

        PuntoDocumento documentoExistente = puntoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

        // Verificar que el punto existe si se está actualizando
        Punto punto = null;
        if (requestDTO.getPuntoId() != null) {
            punto = puntoRepository.findById(requestDTO.getPuntoId())
                    .orElseThrow(() -> new RuntimeException("Punto no encontrado con ID: " + requestDTO.getPuntoId()));
        }

        // Verificar serie y número si se están actualizando
        if (requestDTO.getSerie() != null && requestDTO.getNumero() != null) {
            boolean esNuevoSerieNumero = !requestDTO.getSerie().equals(documentoExistente.getSerie()) ||
                    !requestDTO.getNumero().equals(documentoExistente.getNumero());

            if (esNuevoSerieNumero &&
                    puntoDocumentoRepository.existsBySerieAndNumero(requestDTO.getSerie(), requestDTO.getNumero())) {
                throw new RuntimeException("Ya existe otro documento con la serie " + requestDTO.getSerie() +
                        " y número " + requestDTO.getNumero());
            }
        }

        // Guardar estado anterior para auditoría
        String datosAnteriores = serialize(documentoExistente);

        puntoDocumentoMapper.updateEntityFromDTO(requestDTO, documentoExistente, punto);
        PuntoDocumento documentoActualizado = puntoDocumentoRepository.save(documentoExistente);

        registrarAuditoria(documentoActualizado, "UPDATE", datosAnteriores);

        log.info("Documento actualizado exitosamente con ID: {}", documentoActualizado.getId());
        return enrichResponse(puntoDocumentoMapper.toResponseDTO(documentoActualizado));
    }

    @Override
    @Transactional
    public void eliminarDocumento(Long id) {
        log.info("Eliminando documento con ID: {}", id);

        if (!puntoDocumentoRepository.existsById(id)) {
            throw new RuntimeException("Documento no encontrado con ID: " + id);
        }

        puntoDocumentoRepository.deleteById(id);
        log.info("Documento eliminado exitosamente con ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumentoResponseDTO> obtenerDocumentosPorPunto(Long puntoId) {
        log.info("Obteniendo documentos del punto ID: {}", puntoId);

        // Verificar que el punto existe
        if (!puntoRepository.existsById(puntoId)) {
            throw new RuntimeException("Punto no encontrado con ID: " + puntoId);
        }

        List<PuntoDocumento> documentos = puntoDocumentoRepository.findByPuntoPunto(puntoId);
        return documentos.stream()
                .map(puntoDocumentoMapper::toResponseDTO)
                .map(this::enrichResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumentoResponseDTO> buscarPorTipoDocumento(String tipoDoc) {
        log.info("Buscando documentos por tipo: {}", tipoDoc);

        List<PuntoDocumento> documentos = puntoDocumentoRepository.findByTipoDocumentoTipoDoc(tipoDoc);
        return documentos.stream().map(puntoDocumentoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumentoResponseDTO> buscarPorSerie(String serie) {
        log.info("Buscando documentos por serie: {}", serie);

        List<PuntoDocumento> documentos = puntoDocumentoRepository.findBySerie(serie);
        return documentos.stream().map(puntoDocumentoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumentoResponseDTO> buscarPorEstado(String estado) {
        log.info("Buscando documentos por estado: {}", estado);

        List<PuntoDocumento> documentos = puntoDocumentoRepository
                .findByEstado(com.pe.articulos.core.enums.EstadoGeneral.fromCodigo(estado));
        return documentos.stream().map(puntoDocumentoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumentoResponseDTO> obtenerDocumentosActivosPorPunto(Long puntoId, List<String> modulosStr) {
        log.info("Obteniendo documentos activos del punto ID: {}", puntoId);

        // Verificar que el punto existe
        if (!puntoRepository.existsById(puntoId)) {
            throw new RuntimeException("Punto no encontrado con ID: " + puntoId);
        }
        
        List<com.pe.articulos.modules.documentos.entities.Modulo> modulos = parseModulos(modulosStr);
        List<PuntoDocumento> documentos = puntoDocumentoRepository.findDocumentosActivosByPunto(puntoId, modulos.isEmpty() ? null : modulos);
        return documentos.stream().map(puntoDocumentoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumentoResponseDTO> obtenerDocumentosActivosPorSucursal(Long sucursalId, List<String> modulosStr) {
        log.info("Obteniendo documentos activos de la sucursal ID: {}", sucursalId);
        
        List<com.pe.articulos.modules.documentos.entities.Modulo> modulos = parseModulos(modulosStr);
        List<PuntoDocumento> documentos = puntoDocumentoRepository.findDocumentosActivosBySucursal(sucursalId, modulos.isEmpty() ? null : modulos);
        return documentos.stream().map(puntoDocumentoMapper::toResponseDTO).toList();
    }

    private List<com.pe.articulos.modules.documentos.entities.Modulo> parseModulos(List<String> modulosStr) {
        if (modulosStr == null || modulosStr.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return modulosStr.stream()
            .filter(m -> m != null && !m.trim().isEmpty())
            .map(m -> {
                try {
                    return com.pe.articulos.modules.documentos.entities.Modulo.valueOf(m.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Módulo inválido: {}", m);
                    return null;
                }
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumentoResponseDTO> buscarPorCriterios(Long puntoId, String tipoDoc, String estado) {
        log.info("Buscando documentos con criterios - Punto: {}, Tipo: {}, Estado: {}",
                puntoId, tipoDoc, estado);

        com.pe.articulos.core.enums.EstadoGeneral estadoEnum = (estado != null)
                ? com.pe.articulos.core.enums.EstadoGeneral.fromCodigo(estado)
                : null;
        List<PuntoDocumento> documentos = puntoDocumentoRepository.buscarPorCriterios(puntoId, tipoDoc, estadoEnum);
        return documentos.stream().map(puntoDocumentoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorSerieYNumero(String serie, Integer numero) {
        log.info("Verificando si existe documento con serie: {} y número: {}", serie, numero);
        return puntoDocumentoRepository.existsBySerieAndNumero(serie, numero);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarDocumentosPorPunto(Long puntoId) {
        log.info("Contando documentos del punto ID: {}", puntoId);
        return puntoDocumentoRepository.countByPuntoPunto(puntoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer obtenerUltimoNumeroPorSerieYTipo(String serie, String tipoDoc) {
        log.info("Obteniendo último número de documento para serie: {} y tipo: {}", serie, tipoDoc);

        List<PuntoDocumento> documentos = puntoDocumentoRepository
                .findTopBySerieAndTipoDocOrderByNumeroDesc(serie, tipoDoc);

        if (documentos.isEmpty()) {
            return 0;
        }

        return documentos.get(0).getNumero();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.pe.articulos.modules.venta_registro.entity.TipoDocumento> obtenerTiposDocumentosAsignados(String moduloStr, Long sucursalId) {
        log.info("Obteniendo tipos de documentos asignados para modulo: {} y sucursal: {}", moduloStr, sucursalId);
        com.pe.articulos.modules.documentos.entities.Modulo modulo = null;
        if (moduloStr != null && !moduloStr.trim().isEmpty()) {
            try {
                modulo = com.pe.articulos.modules.documentos.entities.Modulo.valueOf(moduloStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Módulo inválido: {}", moduloStr);
            }
        }
        
        if (modulo == null) {
            return new java.util.ArrayList<>();
        }
        
        return puntoDocumentoRepository.findDistinctTipoDocumentoByModuloAndSucursal(modulo, sucursalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoDocumentoAuditoria> listarHistorial(Long id) {
        log.info("Obteniendo historial para el punto documento ID: {}", id);
        return auditoriaRepository.findByIdPuntoDocOrderByFechaCambioDesc(id);
    }

    private void registrarAuditoria(PuntoDocumento doc, String operacion, String datosAnteriores) {
        String usuario = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";

        PuntoDocumentoAuditoria auditoria = PuntoDocumentoAuditoria.builder()
                .idPuntoDoc(doc.getId())
                .usuario(usuario)
                .operacion(operacion)
                .datosAnteriores(datosAnteriores)
                .build();

        auditoriaRepository.save(auditoria);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error serializando punto documento para auditoría", e);
            return null;
        }
    }

    private PuntoDocumentoResponseDTO enrichResponse(PuntoDocumentoResponseDTO dto) {
        if (dto != null && dto.getIdPlantilla() != null) {
            plantillaRepository.findById(dto.getIdPlantilla())
                    .ifPresent(p -> dto.setPlantillaNombre(p.getNombre()));
        }
        return dto;
    }
}
