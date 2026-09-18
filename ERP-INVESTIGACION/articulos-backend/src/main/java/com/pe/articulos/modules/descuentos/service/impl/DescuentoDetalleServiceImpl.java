package com.pe.articulos.modules.descuentos.service.impl;

import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.modules.descuentos.dto.DescuentoDetalleDTO;
import com.pe.articulos.modules.descuentos.entity.Descuento;
import com.pe.articulos.modules.descuentos.entity.DescuentoDetalle;
import com.pe.articulos.modules.descuentos.mapper.DescuentoDetalleMapper;
import com.pe.articulos.modules.descuentos.repository.DescuentoDetalleRepository;
import com.pe.articulos.modules.descuentos.repository.DescuentoRepository;
import com.pe.articulos.modules.descuentos.service.DescuentoDetalleService;
import com.pe.articulos.modules.catalogo.repository.CatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DescuentoDetalleServiceImpl implements DescuentoDetalleService {

    private final DescuentoDetalleRepository repository;
    private final DescuentoRepository descuentoRepository;
    private final CatalogoRepository catalogoRepository;
    private final DescuentoDetalleMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DescuentoDetalleDTO> listarPorDescuento(Long idDescuento, Pageable pageable) {
        Page<DescuentoDetalle> page = repository.findByDescuentoId(idDescuento, pageable);
        return PageResponse.fromPage(page.map(mapper::toDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public DescuentoDetalleDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle de descuento no encontrado: " + id));
    }

    @Override
    @Transactional
    public DescuentoDetalleDTO guardar(Long idDescuento, DescuentoDetalleDTO dto) {
        Descuento descuento = descuentoRepository.findById(idDescuento)
                .orElseThrow(() -> new ResourceNotFoundException("Descuento maestro no encontrado: " + idDescuento));

        DescuentoDetalle entity = mapper.toEntity(dto);
        entity.setDescuento(descuento);

        if (dto.getIdCatalogo() != null) {
            entity.setCatalogo(catalogoRepository.findById(dto.getIdCatalogo())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Catálogo no encontrado: " + dto.getIdCatalogo())));
        }

        DescuentoDetalle saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
