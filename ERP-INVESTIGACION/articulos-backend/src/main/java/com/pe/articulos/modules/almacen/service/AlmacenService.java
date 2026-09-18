package com.pe.articulos.modules.almacen.service;

import com.pe.articulos.modules.almacen.dto.AlmacenRequest;
import com.pe.articulos.modules.almacen.dto.AlmacenResponse;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

public interface AlmacenService {
    PageResponse<AlmacenResponse> getAlmacenesBySucursal(Long idSucursal, Pageable pageable);

    AlmacenResponse createAlmacen(AlmacenRequest request);

    AlmacenResponse updateAlmacen(Long id, AlmacenRequest request);

    void deleteAlmacen(Long id);

    AlmacenResponse getAlmacenById(Long id);

    PageResponse<AlmacenResponse> getAllAlmacenes(Pageable pageable);
}
