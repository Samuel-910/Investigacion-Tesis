package com.pe.articulos.modules.descuentos.service.impl;

import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.descuentos.dto.DescuentoDTO;
import com.pe.articulos.modules.descuentos.entity.Descuento;
import com.pe.articulos.modules.descuentos.entity.DescuentoDetalle;
import com.pe.articulos.modules.descuentos.mapper.DescuentoMapper;
import com.pe.articulos.modules.descuentos.repository.DescuentoRepository;
import com.pe.articulos.modules.descuentos.service.DescuentoService;
import com.pe.articulos.modules.catalogo.repository.CatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.pe.articulos.modules.compania.repository.CompaniaPersonaVinculoRepository;
import com.pe.articulos.modules.compania.entity.CompaniaPersonaVinculo;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DescuentoServiceImpl implements DescuentoService {

    private final DescuentoRepository repository;
    private final CatalogoRepository catalogoRepository;
    private final DescuentoMapper mapper;
    private final CompaniaPersonaVinculoRepository companiaPersonaVinculoRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DescuentoDTO> listar(int page, int size, String nombre, Boolean activo, Long idCompania) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Descuento> pageResult = repository.listarFiltros(nombre, activo, idCompania, pageRequest);

        List<DescuentoDTO> content = pageResult.getContent().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getSize(),
                pageResult.getNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public DescuentoDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Descuento no encontrado: " + id));
    }

    @Override
    @Transactional
    public DescuentoDTO guardar(DescuentoDTO dto) {
        Descuento entity = mapper.toEntity(dto);

        // Asociar catálogos reales a los detalles
        if (entity.getDetalles() != null) {
            for (DescuentoDetalle detalle : entity.getDetalles()) {
                detalle.setDescuento(entity);
                if (detalle.getIdCatalogo() != null) {
                    detalle.setCatalogo(catalogoRepository.findById(detalle.getIdCatalogo())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Catálogo no encontrado: " + detalle.getIdCatalogo())));
                }
            }
        }

        Descuento saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DescuentoDTO> listarVigentes(Pageable pageable) {
        Page<Descuento> page = repository.listarVigentes(LocalDateTime.now(), pageable);
        return PageResponse.fromPage(page.map(mapper::toDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DescuentoDTO> listarAplicables(Long idPaciente) {
        List<Descuento> vigentes = repository.findAllVigentes(LocalDateTime.now());
        
        DatosPersonales paciente = datosPersonalesRepository.findById(idPaciente).orElse(null);
        if (paciente == null) {
            return new ArrayList<>();
        }

        List<CompaniaPersonaVinculo> vinculos = companiaPersonaVinculoRepository.findByPersonaIdAndEstado(idPaciente, EstadoGeneral.ACTIVO);
        
        List<Descuento> aplicables = new ArrayList<>();
        
        for (Descuento d : vigentes) {
            String usuariosAfectados = d.getUsuariosAfectados();
            if (usuariosAfectados == null || usuariosAfectados.trim().isEmpty() || "ALL".equals(usuariosAfectados)) {
                aplicables.add(d);
                continue;
            }
            
            try {
                JsonNode root = objectMapper.readTree(usuariosAfectados);
                boolean aplica = false;
                
                if (root.isObject() && root.has("mode") && "CONVENIO".equals(root.get("mode").asText())) {
                    Long idCompaniaReq = root.has("idCompania") ? root.get("idCompania").asLong() : null;
                    if (idCompaniaReq != null) {
                        aplica = vinculos.stream().anyMatch(v -> v.getCompania().getId().equals(idCompaniaReq));
                    }
                } else if (root.isArray()) {
                    for (JsonNode b : root) {
                        String idReq = b.has("id") ? b.get("id").asText() : null;
                        String dniReq = b.has("dni") ? b.get("dni").asText() : null;
                        
                        if ((idReq != null && idReq.equals(String.valueOf(paciente.getId()))) ||
                            (dniReq != null && dniReq.equals(paciente.getNumdoc()))) {
                            aplica = true;
                            break;
                        }
                    }
                }
                
                if (aplica) {
                    aplicables.add(d);
                }
            } catch (Exception e) {
                // Ignore parsing errors for this discount
            }
        }
        
        return aplicables.stream().map(mapper::toDTO).collect(Collectors.toList());
    }
}
