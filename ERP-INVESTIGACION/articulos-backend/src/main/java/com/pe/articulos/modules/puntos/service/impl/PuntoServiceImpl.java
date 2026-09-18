package com.pe.articulos.modules.puntos.service.impl;

import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.puntos.dto.PuntoRequestDTO;
import com.pe.articulos.modules.puntos.dto.PuntoResponseDTO;
import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.puntos.mapper.PuntoMapper;
import com.pe.articulos.modules.puntos.repository.PuntoRepository;
import com.pe.articulos.core.config.initializers.PuntoDocumentoBackup;
import com.pe.articulos.modules.puntos.service.PuntoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class PuntoServiceImpl implements PuntoService {

    private final PuntoRepository puntoRepository;
    private final PuntoMapper puntoMapper;
    private final PuntoDocumentoBackup puntoDocumentoBackup;

    @Override
    @Transactional
    public PuntoResponseDTO crearPunto(PuntoRequestDTO requestDTO) {
        log.info("Creando nuevo punto con nombre: {}", requestDTO.getNombre());

        // Verificar si ya existe un punto con el mismo nombre
        if (puntoRepository.existsByNombre(requestDTO.getNombre())) {
            throw new ValidationException("Ya existe un punto con el nombre: " + requestDTO.getNombre());
        }

        Punto punto = puntoMapper.toEntity(requestDTO);
        Punto puntoGuardado = puntoRepository.save(punto);

        try {
            puntoDocumentoBackup.asignarDocumentosPorDefecto(puntoGuardado);
            log.info("Documentos por defecto asignados al nuevo punto {}", puntoGuardado.getNombre());
        } catch (Exception e) {
            log.error("Error al asignar documentos por defecto al nuevo punto: {}", e.getMessage());
        }

        log.info("Punto creado exitosamente con ID: {}", puntoGuardado.getPunto());
        return puntoMapper.toResponseDTO(puntoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PuntoResponseDTO obtenerPuntoPorId(Long id) {
        log.info("Buscando punto con ID: {}", id);

        Punto punto = puntoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Punto no encontrado con ID: " + id));

        return puntoMapper.toResponseDTO(punto);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PuntoResponseDTO> obtenerTodosPuntos(Integer idSucursal, Pageable pageable) {
        log.info("Obteniendo todos los puntos paginados de la sucursal: {}", idSucursal);
        org.springframework.data.domain.Page<PuntoResponseDTO> page = puntoRepository
                .buscarGlobal("", "ALL", idSucursal, pageable).map(puntoMapper::toResponseDTO);
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PuntoResponseDTO> obtenerPuntosActivos(Integer idSucursal, Pageable pageable) {
        log.info("Obteniendo puntos activos paginados de la sucursal: {}", idSucursal);

        org.springframework.data.domain.Page<PuntoResponseDTO> page = puntoRepository
                .findByValidoAndIdSucursal("S", idSucursal, pageable).map(puntoMapper::toResponseDTO);
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoResponseDTO> obtenerTodosPuntosList(Integer idSucursal) {
        log.info("Obteniendo todos los puntos como lista de la sucursal: {}", idSucursal);
        List<Punto> puntos = puntoRepository.findByIdSucursal(idSucursal);
        return puntos.stream().map(puntoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoResponseDTO> obtenerPuntosActivosList(Integer idSucursal) {
        log.info("Obteniendo puntos activos como lista de la sucursal: {}", idSucursal);
        List<Punto> puntosActivos = puntoRepository.findByValidoAndIdSucursal("S", idSucursal);
        return puntosActivos.stream().map(puntoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional
    public PuntoResponseDTO actualizarPunto(Long id, PuntoRequestDTO requestDTO) {
        log.info("Actualizando punto con ID: {}", id);

        Punto puntoExistente = puntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Punto no encontrado con ID: " + id));

        // Verificar si el nuevo nombre ya existe en otro punto
        if (!puntoExistente.getNombre().equals(requestDTO.getNombre()) &&
                puntoRepository.existsByNombre(requestDTO.getNombre())) {
            throw new ValidationException("Ya existe otro punto con el nombre: " + requestDTO.getNombre());
        }

        puntoMapper.updateEntityFromDTO(requestDTO, puntoExistente);
        Punto puntoActualizado = puntoRepository.save(puntoExistente);

        log.info("Punto actualizado exitosamente con ID: {}", puntoActualizado.getPunto());
        return puntoMapper.toResponseDTO(puntoActualizado);
    }

    @Override
    @Transactional
    public void eliminarPunto(Long id) {
        log.info("Eliminando punto con ID: {}", id);

        if (!puntoRepository.existsById(id)) {
            throw new ValidationException("Punto no encontrado con ID: " + id);
        }

        puntoRepository.deleteById(id);
        log.info("Punto eliminado exitosamente con ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoResponseDTO> buscarPorNombre(String nombre) {
        log.info("Buscando puntos por nombre: {}", nombre);

        List<Punto> puntos = puntoRepository.findByNombreContainingIgnoreCase(nombre);
        return puntos.stream().map(puntoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoResponseDTO> buscarPorTipo(String tipo) {
        log.info("Buscando puntos por tipo: {}", tipo);

        List<Punto> puntos = puntoRepository.findByTipo(tipo);
        return puntos.stream().map(puntoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoResponseDTO> buscarPorSucursal(Integer idSucursal) {
        log.info("Buscando puntos por sucursal: {}", idSucursal);

        List<Punto> puntos = puntoRepository.findByIdSucursal(idSucursal);
        return puntos.stream().map(puntoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoResponseDTO> buscarPorCriterios(String nombre, String tipo, Integer idSucursal) {
        log.info("Buscando puntos con criterios - Nombre: {}, Tipo: {}, Sucursal: {}",
                nombre, tipo, idSucursal);

        List<Punto> puntos = puntoRepository.buscarPorCriterios(nombre, tipo, idSucursal);
        return puntos.stream().map(puntoMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        log.info("Verificando si existe punto con nombre: {}", nombre);
        return puntoRepository.existsByNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorSucursal(Integer idSucursal) {
        log.info("Contando puntos de la sucursal: {}", idSucursal);
        return puntoRepository.countByIdSucursal(idSucursal);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PuntoResponseDTO> buscar(String q, String tipo, Integer idSucursal, Pageable pageable) {
        log.info("Buscando puntos globalmente con término: '{}', tipo: '{}' y sucursal: {}", q, tipo, idSucursal);
        org.springframework.data.domain.Page<PuntoResponseDTO> page = puntoRepository
                .buscarGlobal(q, tipo, idSucursal, pageable).map(puntoMapper::toResponseDTO);
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }
}
