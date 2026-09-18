package com.pe.articulos.modules.descuentos.service;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.descuentos.dto.DescuentoDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DescuentoService {
    PageResponse<DescuentoDTO> listar(int page, int size, String nombre, Boolean activo, Long idCompania);

    DescuentoDTO obtenerPorId(Long id);

    DescuentoDTO guardar(DescuentoDTO dto);

    void eliminar(Long id);

    PageResponse<DescuentoDTO> listarVigentes(Pageable pageable);

    List<DescuentoDTO> listarAplicables(Long idPaciente);
}
