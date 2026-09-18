package com.pe.articulos.modules.descuentos.service.impl;

import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.modules.descuentos.dto.VentaBeneficioDTO;
import com.pe.articulos.modules.descuentos.entity.VentaBeneficio;
import com.pe.articulos.modules.descuentos.mapper.VentaBeneficioMapper;
import com.pe.articulos.modules.descuentos.repository.VentaBeneficioRepository;
import com.pe.articulos.modules.descuentos.service.VentaBeneficioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class VentaBeneficioServiceImpl implements VentaBeneficioService {

    private final VentaBeneficioRepository repository;
    private final VentaBeneficioMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VentaBeneficioDTO> listarPorVenta(Long idVenta, Pageable pageable) {
        Page<VentaBeneficio> page = repository.findByVenta_IdVenta(idVenta, pageable);
        return PageResponse.fromPage(page.map(mapper::toDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public VentaBeneficioDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("VentaBeneficio no encontrado: " + id));
    }

    @Override
    @Transactional
    public VentaBeneficioDTO guardar(VentaBeneficioDTO dto) {
        VentaBeneficio entity = mapper.toEntity(dto);
        VentaBeneficio saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
