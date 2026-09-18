
package com.pe.articulos.modules.compras.service.impl;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.compras.dto.DetalleCompraResponse;
import com.pe.articulos.modules.compras.entity.DetalleCompra;
import com.pe.articulos.modules.compras.repository.DetalleCompraRepository;
import com.pe.articulos.modules.compras.service.DetalleCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;

import com.pe.articulos.modules.compras.mapper.DetalleCompraMapper;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DetalleCompraServiceImpl implements DetalleCompraService {

    private final DetalleCompraRepository detalleCompraRepository;
    private final DetalleCompraMapper detalleCompraMapper;

    @Override
    public PageResponse<DetalleCompraResponse> listarPorCompra(Long idCompra, Pageable pageable) {
        Page<DetalleCompra> page = detalleCompraRepository.findByCompraId(idCompra, pageable);
        return new PageResponse<>(
                page.getContent().stream().map(detalleCompraMapper::toResponse).collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }

    @Override
    public DetalleCompraResponse obtener(Long id) {
        DetalleCompra detalle = detalleCompraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de compra no encontrado"));
        return detalleCompraMapper.toResponse(detalle);
    }
}
