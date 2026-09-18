package com.pe.articulos.modules.almacen.service.impl;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.almacen.dto.AlmacenRequest;
import com.pe.articulos.modules.almacen.dto.AlmacenResponse;
import com.pe.articulos.modules.almacen.entity.Almacen;
import com.pe.articulos.modules.almacen.mapper.AlmacenMapper;
import com.pe.articulos.modules.almacen.repository.AlmacenRepository;
import com.pe.articulos.modules.almacen.service.AlmacenService;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.pe.articulos.modules.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class AlmacenServiceImpl implements AlmacenService {

    private final AlmacenRepository almacenRepository;
    private final SucursalRepository sucursalRepository;
    private final AlmacenMapper almacenMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlmacenResponse> getAlmacenesBySucursal(Long idSucursal, Pageable pageable) {
        log.info("Obteniendo almacenes paginados de la sucursal: {}", idSucursal);
        Page<AlmacenResponse> page = almacenRepository.findBySucursalIdSucursal(idSucursal, pageable)
                .map(almacenMapper::toResponse);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional
    public AlmacenResponse createAlmacen(AlmacenRequest request) {
        log.info("Creando nuevo almacen: {}", request.getNombre());
        Sucursal sucursal = sucursalRepository.findById(request.getIdSucursal())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

        Almacen almacen = almacenMapper.toEntity(request, sucursal);
        almacen.setEstado(EstadoGeneral.ACTIVO);

        if (Boolean.TRUE.equals(request.getEsPrincipal())) {
            almacenRepository.findBySucursalIdSucursal(sucursal.getIdSucursal())
                    .forEach(a -> {
                        if (Boolean.TRUE.equals(a.getEsPrincipal())) {
                            a.setEsPrincipal(false);
                            almacenRepository.save(a);
                        }
                    });
        }

        return almacenMapper.toResponse(almacenRepository.save(almacen));
    }

    @Override
    @Transactional
    public AlmacenResponse updateAlmacen(Long id, AlmacenRequest request) {
        log.info("Actualizando almacen con ID: {}", id);
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Almacen no encontrado"));

        Sucursal sucursal = null;
        if (request.getIdSucursal() != null && !request.getIdSucursal().equals(almacen.getSucursal().getIdSucursal())) {
            sucursal = sucursalRepository.findById(request.getIdSucursal())
                    .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        }

        almacenMapper.updateEntity(almacen, request, sucursal);

        if (Boolean.TRUE.equals(request.getEsPrincipal())) {
            almacenRepository.findBySucursalIdSucursal(almacen.getSucursal().getIdSucursal()).forEach(a -> {
                if (!a.getId().equals(almacen.getId()) && Boolean.TRUE.equals(a.getEsPrincipal())) {
                    a.setEsPrincipal(false);
                    almacenRepository.save(a);
                }
            });
        }

        return almacenMapper.toResponse(almacenRepository.save(almacen));
    }

    @Override
    @Transactional
    public void deleteAlmacen(Long id) {
        log.info("Eliminando (borrado logico) almacen con ID: {}", id);
        Almacen entity = almacenRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Almacen no encontrado"));

        if (Boolean.TRUE.equals(entity.getEsPrincipal())) {
            entity.setEsPrincipal(false);
            List<Almacen> sucursalAlmacenes = almacenRepository.findBySucursalIdSucursal(
                    entity.getSucursal().getIdSucursal());
            sucursalAlmacenes.stream()
                    .filter(a -> !a.getId().equals(entity.getId()))
                    .findFirst()
                    .ifPresent(newPrincipal -> {
                        newPrincipal.setEsPrincipal(true);
                        almacenRepository.save(newPrincipal);
                    });
        }

        entity.setEstado(EstadoGeneral.ELIMINADO);
        almacenRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public AlmacenResponse getAlmacenById(Long id) {
        log.info("Consultando almacen con ID: {}", id);
        return almacenRepository.findById(id)
                .map(almacenMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Almacen no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlmacenResponse> getAllAlmacenes(Pageable pageable) {
        log.info("Obteniendo todos los almacenes del sistema paginados");
        Page<AlmacenResponse> page = almacenRepository.findAll(pageable)
                .map(almacenMapper::toResponse);
        return PageResponse.fromPage(page);
    }
}
