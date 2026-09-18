package com.pe.articulos.modules.caja_general.service.impl;

import com.pe.articulos.modules.atributos.entity.MetodoPago;
import com.pe.articulos.modules.atributos.repository.MetodoPagoRepository;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralMovimientoRequest;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralMovimientoResponse;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralResponse;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralSaldoResponse;
import com.pe.articulos.modules.caja_general.entity.CajaGeneral;
import com.pe.articulos.modules.caja_general.entity.CajaGeneralMovimiento;
import com.pe.articulos.modules.caja_general.entity.CajaGeneralSaldo;
import com.pe.articulos.modules.caja_general.repository.CajaGeneralMovimientoRepository;
import com.pe.articulos.modules.caja_general.repository.CajaGeneralRepository;
import com.pe.articulos.modules.caja_general.repository.CajaGeneralSaldoRepository;
import com.pe.articulos.modules.caja_general.service.CajaGeneralService;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import com.pe.articulos.core.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CajaGeneralServiceImpl implements CajaGeneralService {

    private final CajaGeneralRepository cajaGeneralRepository;
    private final CajaGeneralMovimientoRepository movimientoRepository;
    private final CajaGeneralSaldoRepository saldoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;

    @Override
    @Transactional
    public CajaGeneralResponse obtenerPorSucursal(Long idSucursal) {
        CajaGeneral caja = obtenerOInicializar(idSucursal);
        List<CajaGeneralSaldo> saldos = saldoRepository.findByCajaGeneralId(caja.getId());
        
        return mapToResponse(caja, saldos);
    }

    @Override
    @Transactional
    public CajaGeneralMovimientoResponse registrarMovimiento(CajaGeneralMovimientoRequest request) {
        CajaGeneral caja = obtenerOInicializar(request.getIdSucursal());

        if (request.getTipo() == CajaGeneralMovimiento.TipoMovimiento.TRANSFERENCIA_ENTRE_METODOS) {
            return procesarTransferencia(caja, request);
        }

        MetodoPago metodoPago = buscarMetodoPago(request.getMetodoPago());

        if (request.getTipo() == CajaGeneralMovimiento.TipoMovimiento.EGRESO) {
            validarSaldoSuficiente(caja, metodoPago, request.getMonto());
        }

        CajaGeneralMovimiento movimiento = CajaGeneralMovimiento.builder()
                .cajaGeneral(caja)
                .tipo(request.getTipo())
                .monto(request.getMonto())
                .descripcion(request.getDescripcion())
                .referencia(request.getReferencia())
                .metodoPago(metodoPago)
                .fecha(LocalDateTime.now())
                .usuario(request.getUsuario())
                .build();

        actualizarSaldos(caja, metodoPago, request.getMonto(), request.getTipo(), true);

        return mapToMovimientoResponse(movimientoRepository.save(movimiento));
    }

    private CajaGeneralMovimientoResponse procesarTransferencia(CajaGeneral caja, CajaGeneralMovimientoRequest request) {
        MetodoPago origen = buscarMetodoPago(request.getMetodoPagoOrigen());
        MetodoPago destino = buscarMetodoPago(request.getMetodoPagoDestino());

        validarSaldoSuficiente(caja, origen, request.getMonto());

        actualizarSaldos(caja, origen, request.getMonto(), CajaGeneralMovimiento.TipoMovimiento.EGRESO, true);
        actualizarSaldos(caja, destino, request.getMonto(), CajaGeneralMovimiento.TipoMovimiento.INGRESO, true);

        CajaGeneralMovimiento movimiento = CajaGeneralMovimiento.builder()
                .cajaGeneral(caja)
                .tipo(request.getTipo())
                .monto(request.getMonto())
                .descripcion(String.format("Transferencia de %s a %s: %s", 
                        origen.getDescripcion(), destino.getDescripcion(), request.getDescripcion()))
                .referencia(request.getReferencia())
                .metodoPago(origen)
                .fecha(LocalDateTime.now())
                .usuario(request.getUsuario())
                .build();

        return mapToMovimientoResponse(movimientoRepository.save(movimiento));
    }

    @Override
    @Transactional
    public CajaGeneralMovimientoResponse actualizarMovimiento(Long id, CajaGeneralMovimientoRequest request) {
        CajaGeneralMovimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado: " + id));

        CajaGeneral caja = mov.getCajaGeneral();

        actualizarSaldos(caja, mov.getMetodoPago(), mov.getMonto(), mov.getTipo(), false);

        MetodoPago nuevoMetodo = buscarMetodoPago(request.getMetodoPago());
        actualizarSaldos(caja, nuevoMetodo, request.getMonto(), request.getTipo(), true);

        mov.setTipo(request.getTipo());
        mov.setMonto(request.getMonto());
        mov.setDescripcion(request.getDescripcion());
        mov.setReferencia(request.getReferencia());
        mov.setMetodoPago(nuevoMetodo);
        mov.setUsuario(request.getUsuario());

        return mapToMovimientoResponse(movimientoRepository.save(mov));
    }

    @Override
    @Transactional
    public void eliminarMovimiento(Long id) {
        CajaGeneralMovimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado: " + id));

        actualizarSaldos(mov.getCajaGeneral(), mov.getMetodoPago(), mov.getMonto(), mov.getTipo(), false);
        mov.setEstado(3);
        movimientoRepository.save(mov);
    }

    private void actualizarSaldos(CajaGeneral caja, MetodoPago mp, BigDecimal monto, 
                                 CajaGeneralMovimiento.TipoMovimiento tipo, boolean esNuevo) {
        boolean sumar = esIngreso(tipo);
        if (!esNuevo) sumar = !sumar;

        if (sumar) {
            caja.setSaldoActual(caja.getSaldoActual().add(monto));
        } else {
            caja.setSaldoActual(caja.getSaldoActual().subtract(monto));
        }
        cajaGeneralRepository.save(caja);

        CajaGeneralSaldo saldoMetodo = saldoRepository.findByCajaGeneralIdAndMetodoPagoId(caja.getId(), mp.getId())
                .orElseGet(() -> saldoRepository.save(CajaGeneralSaldo.builder()
                        .cajaGeneral(caja)
                        .metodoPago(mp)
                        .saldoActual(BigDecimal.ZERO)
                        .build()));

        if (sumar) {
            saldoMetodo.setSaldoActual(saldoMetodo.getSaldoActual().add(monto));
        } else {
            saldoMetodo.setSaldoActual(saldoMetodo.getSaldoActual().subtract(monto));
        }
        saldoRepository.save(saldoMetodo);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CajaGeneralMovimientoResponse> listarMovimientos(Long idSucursal, String metodoPago, String query, String searchType, Pageable pageable) {
        CajaGeneral caja = obtenerOInicializar(idSucursal);
        
        String cleanMetodoPago = (metodoPago == null || metodoPago.isEmpty() || "TODOS".equalsIgnoreCase(metodoPago)) ? null : metodoPago;
        String cleanQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();
        String cleanSearchType = (searchType == null || searchType.trim().isEmpty()) ? "ALL" : searchType.trim();

        org.springframework.data.domain.Page<CajaGeneralMovimientoResponse> page = movimientoRepository.buscarMovimientosFiltrados(
                caja.getId(),
                cleanMetodoPago,
                cleanQuery,
                cleanSearchType,
                pageable
        ).map(this::mapToMovimientoResponse);

        return PageResponse.fromPage(page);
    }

    private void validarSaldoSuficiente(CajaGeneral caja, MetodoPago mp, BigDecimal monto) {
        CajaGeneralSaldo saldoMetodo = saldoRepository.findByCajaGeneralIdAndMetodoPagoId(caja.getId(), mp.getId())
                .orElse(null);
        
        BigDecimal saldoActual = (saldoMetodo != null) ? saldoMetodo.getSaldoActual() : BigDecimal.ZERO;
        
        if (saldoActual.compareTo(monto) < 0) {
            throw new RuntimeException("Saldo insuficiente en " + mp.getDescripcion() + ". Saldo disponible: S/ " + saldoActual);
        }
    }

    private MetodoPago buscarMetodoPago(String desc) {
        return metodoPagoRepository.findByDescripcionIgnoreCase(desc != null ? desc : "EFECTIVO")
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado: " + desc));
    }

    private CajaGeneral obtenerOInicializar(Long idSucursal) {
        return cajaGeneralRepository.findByIdSucursal(idSucursal)
                .orElseGet(() -> {
                    CajaGeneral nuevaCaja = CajaGeneral.builder()
                            .nombre("CAJA GENERAL SUCURSAL " + idSucursal)
                            .idSucursal(idSucursal)
                            .saldoActual(BigDecimal.ZERO)
                            .usuarioCreacion("SYSTEM")
                            .build();
                    return cajaGeneralRepository.save(nuevaCaja);
                });
    }

    private boolean esIngreso(CajaGeneralMovimiento.TipoMovimiento tipo) {
        return tipo == CajaGeneralMovimiento.TipoMovimiento.INGRESO ||
                tipo == CajaGeneralMovimiento.TipoMovimiento.TRANSFERENCIA_RECIBIDA;
    }

    private CajaGeneralResponse mapToResponse(CajaGeneral caja, List<CajaGeneralSaldo> saldos) {
        return CajaGeneralResponse.builder()
                .id(caja.getId())
                .nombre(caja.getNombre())
                .idSucursal(caja.getIdSucursal())
                .saldoActual(caja.getSaldoActual())
                .saldosPorMetodo(saldos.stream()
                        .map(s -> CajaGeneralSaldoResponse.builder()
                                .metodoPago(s.getMetodoPago().getDescripcion())
                                .saldo(s.getSaldoActual())
                                .build())
                        .collect(Collectors.toList()))
                .fechaActualizacion(caja.getFechaActualizacion())
                .build();
    }

    private String obtenerNombreUsuario(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            return "";
        }
        try {
            Long userId = Long.parseLong(usuario);
            return datosPersonalesRepository.findById(userId)
                    .map(dp -> {
                        String nombreCompleto = dp.getNombreCompleto();
                        return nombreCompleto != null ? nombreCompleto : dp.getLogin();
                    })
                    .orElse(usuario);
        } catch (NumberFormatException e) {
            return datosPersonalesRepository.findByLogin(usuario)
                    .map(dp -> {
                        String nombreCompleto = dp.getNombreCompleto();
                        return nombreCompleto != null ? nombreCompleto : dp.getLogin();
                    })
                    .orElse(usuario);
        }
    }

    private CajaGeneralMovimientoResponse mapToMovimientoResponse(CajaGeneralMovimiento mov) {
        return CajaGeneralMovimientoResponse.builder()
                .id(mov.getId())
                .tipo(mov.getTipo())
                .monto(mov.getMonto())
                .descripcion(mov.getDescripcion())
                .referencia(mov.getReferencia())
                .metodoPago(mov.getMetodoPago() != null ? mov.getMetodoPago().getDescripcion() : "EFECTIVO")
                .fecha(mov.getFecha())
                .usuario(obtenerNombreUsuario(mov.getUsuario()))
                .build();
    }
}
