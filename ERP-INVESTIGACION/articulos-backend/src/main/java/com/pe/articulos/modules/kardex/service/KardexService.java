package com.pe.articulos.modules.kardex.service;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.kardex.dto.KardexMapper;
import com.pe.articulos.modules.kardex.entity.Kardex;
import com.pe.articulos.modules.kardex.repository.KardexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class KardexService {

    private final KardexRepository kardexRepository;
    private final KardexMapper kardexMapper;

    @Transactional
    public void registrarMovimiento(KardexDTO dto) {
        log.info("Registrando movimiento en kardex para catálogo ID: {}", dto.getIdCatalogo());

        Optional<Kardex> lastConsolidated = kardexRepository.findLastMovement(dto.getIdCatalogo(),
                dto.getIdSucursal());

        Optional<Kardex> lastByLot = Optional.empty();
        if (dto.getNroLote() != null && !dto.getNroLote().isEmpty()) {
            lastByLot = kardexRepository.findLastMovementByLot(dto.getIdCatalogo(), dto.getIdSucursal(),
                    dto.getNroLote());
        }

        BigDecimal saldoCantAnt = lastByLot.map(Kardex::getSaldoCantidad).orElse(BigDecimal.ZERO);
        BigDecimal saldoCostoTotalAnt = lastConsolidated.map(Kardex::getSaldoCostoTotal)
                .orElse(BigDecimal.ZERO);
        BigDecimal saldoCantTotalAnt = lastConsolidated.map(Kardex::getSaldoCantidad)
                .orElse(BigDecimal.ZERO);
        BigDecimal pmpAnt = lastConsolidated.map(Kardex::getSaldoCostoUnitario).orElse(BigDecimal.ZERO);

        BigDecimal cantidadMov = dto.getCantidad();
        BigDecimal costoUniMov = dto.getCostoUnitario();
        BigDecimal costoTotalMov = cantidadMov.multiply(costoUniMov);

        BigDecimal saldoCantNuevo;
        BigDecimal saldoCantTotalNuevo;
        BigDecimal saldoCostoTotalNuevo;
        BigDecimal pmpNuevo;

        if ("+".equals(dto.getSigno())) {
            saldoCantNuevo = saldoCantAnt.add(cantidadMov);
            saldoCantTotalNuevo = saldoCantTotalAnt.add(cantidadMov);
            saldoCostoTotalNuevo = saldoCostoTotalAnt.add(costoTotalMov);

            if (saldoCantTotalNuevo.compareTo(BigDecimal.ZERO) > 0) {
                pmpNuevo = saldoCostoTotalNuevo.divide(saldoCantTotalNuevo, 4, RoundingMode.HALF_UP);
            } else {
                pmpNuevo = BigDecimal.ZERO;
            }
        } else {
            saldoCantNuevo = saldoCantAnt.subtract(cantidadMov);
            saldoCantTotalNuevo = saldoCantTotalAnt.subtract(cantidadMov);
            pmpNuevo = pmpAnt;
            saldoCostoTotalNuevo = saldoCantTotalNuevo.multiply(pmpNuevo);
            
            // El usuario solicitó que la fila de salida registre el PRECIO DE VENTA (con descuentos) 
            // en lugar del costo promedio. Se respeta el valor que viene en el DTO.
            if (dto.getCostoUnitario() != null && dto.getCostoUnitario().compareTo(BigDecimal.ZERO) > 0) {
                costoUniMov = dto.getCostoUnitario();
                costoTotalMov = cantidadMov.multiply(costoUniMov);
            } else {
                costoTotalMov = cantidadMov.multiply(pmpNuevo);
                costoUniMov = pmpNuevo;
            }
        }

        Kardex kardex = Kardex.builder()
                .idArticuloKardex(generarIdKardex())
                .idAlmArticulo(dto.getIdAlmacen())
                .idCatalogo(dto.getIdCatalogo())
                .idSucursal(dto.getIdSucursal())
                .idUsuario(dto.getIdUsuario())
                .idDocumento(dto.getIdDocumento())
                .numDoc(dto.getNumDoc())
                .fecha(LocalDateTime.now())
                .fechaVenc(dto.getFechaVenc())
                .nroLote(dto.getNroLote())
                .operacion(dto.getOperacion())
                .detalle(dto.getDetalle())
                .observacion(dto.getObservacion())
                .signo(dto.getSigno())
                .cantidad(cantidadMov)
                .costoUnitario(costoUniMov)
                .costoTotal(costoTotalMov)
                .saldoCantidad(saldoCantNuevo)
                .saldoCostoUnitario(pmpNuevo)
                .saldoCostoTotal(saldoCostoTotalNuevo)
                .origenId(dto.getOrigenId())
                .origenTipo(dto.getOrigenTipo())
                .presentacion(dto.getPresentacion())
                .idClasificacion(dto.getIdClasificacion())
                .build();

        kardexRepository.save(kardex);
    }

    @Transactional(readOnly = true)
    public PageResponse<KardexDTO> listarMovimientos(Long idCatalogo, Long idSucursal, Pageable pageable) {
        Page<Kardex> page = kardexRepository
                .findByIdCatalogoAndIdSucursalOrderByFechaDescIdArticuloKardexDesc(idCatalogo, idSucursal, pageable);
        return mapToPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<KardexDTO> listarMovimientosConFiltros(Long idCatalogo, Long idSucursal,
            LocalDate desde, LocalDate hasta,
            Pageable pageable) {
        
        LocalDateTime start = (desde != null) ? desde.atStartOfDay() : null;
        LocalDateTime end = (hasta != null) ? hasta.atTime(23, 59, 59) : null;

        Page<Kardex> page = kardexRepository
                .findByIdCatalogoAndIdSucursalAndFechaBetweenOrderByFechaDescIdArticuloKardexDesc(idCatalogo,
                        idSucursal, start, end,
                        pageable);
        return mapToPageResponse(page);
    }

    private PageResponse<KardexDTO> mapToPageResponse(Page<Kardex> page) {
        List<KardexDTO> dtoList = page.getContent().stream()
                .map(kardexMapper::toDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                dtoList,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }

    @Transactional(readOnly = true)
    public Optional<KardexDTO> obtenerUltimoMovimiento(Long idCatalogo, Long idSucursal) {
        return kardexRepository.findLastMovement(idCatalogo, idSucursal).map(kardexMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<KardexDTO> obtenerUltimoMovimientoPorLote(Long idCatalogo, Long idSucursal,
            String nroLote) {
        if (nroLote == null || nroLote.isEmpty()) {
            return obtenerUltimoMovimiento(idCatalogo, idSucursal);
        }
        return kardexRepository.findLastMovementByLot(idCatalogo, idSucursal, nroLote).map(kardexMapper::toDto);
    }

    @Transactional(readOnly = true)
    public PageResponse<KardexDTO> buscarMovimientosGlobal(Long idSucursal, LocalDate desde, LocalDate hasta,
            String signo, String nombre, Long idClasificacion, Pageable pageable) {
        
        LocalDateTime start = (desde != null) ? desde.atStartOfDay() : null;
        LocalDateTime end = (hasta != null) ? hasta.atTime(23, 59, 59) : null;

        Page<Kardex> page = kardexRepository.searchGlobal(idSucursal, start, end, signo, nombre, idClasificacion,
                pageable);
        return mapToPageResponse(page);
    }

    private String generarIdKardex() {
        LocalDateTime now = LocalDateTime.now();
        String ts = now.format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
        int rand = (int) (Math.random() * 999);
        return String.format("K%s%03d", ts, rand);
    }
}
