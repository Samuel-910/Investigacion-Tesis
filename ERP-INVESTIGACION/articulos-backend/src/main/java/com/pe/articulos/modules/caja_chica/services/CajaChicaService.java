package com.pe.articulos.modules.caja_chica.services;

import com.pe.articulos.modules.caja_chica.dto.*;
import com.pe.articulos.modules.caja_chica.entity.CajaChica;
import com.pe.articulos.modules.caja_chica.entity.CajaChicaMovimiento;
import com.pe.articulos.modules.caja_chica.repository.CajaChicaMovimientoRepository;
import com.pe.articulos.modules.caja_chica.repository.CajaChicaRepository;
import com.pe.articulos.modules.atributos.repository.MetodoPagoRepository;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralMovimientoRequest;
import com.pe.articulos.modules.caja_general.entity.CajaGeneralMovimiento;
import com.pe.articulos.modules.caja_general.service.CajaGeneralService;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CajaChicaService {

        private final CajaChicaRepository cajaChicaRepository;
        private final CajaChicaMovimientoRepository movimientoRepository;
        private final MetodoPagoRepository metodoPagoRepository;
        private final CajaGeneralService cajaGeneralService;
        private final DatosPersonalesRepository datosPersonalesRepository;

        @Transactional(readOnly = true)
        public List<CajaChicaResponse> listarCajas() {
                return cajaChicaRepository.findAll().stream()
                                .map(this::mapToCajaResponse)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public java.util.Optional<CajaChicaResponse> obtenerCajaAbierta(Long idPuntoVenta, String usuario) {
                return cajaChicaRepository.findTopByEstadoAndIdPuntoVentaAndIdUsuarioCajeroOrderByIdDesc(
                                CajaChica.EstadoCaja.ABIERTA, idPuntoVenta, usuario)
                                .map(this::mapToCajaResponse);
        }

        @Transactional
        public CajaChicaResponse crearCaja(CajaChicaRequest request, String usuario) {
                boolean existeAbierta = cajaChicaRepository.findAll().stream()
                                .anyMatch(c -> c.getEstado() == CajaChica.EstadoCaja.ABIERTA
                                                && c.getIdPuntoVenta().equals(request.getIdPuntoVenta())
                                                && c.getIdUsuarioCajero().equals(usuario));

                if (existeAbierta) {
                        throw new RuntimeException("Ya existe una caja abierta para este punto de venta y usuario");
                }

                BigDecimal saldoInicial = request.getSaldoInicial() != null ? request.getSaldoInicial()
                                : BigDecimal.ZERO;

                CajaChica caja = CajaChica.builder()
                                .nombre(request.getNombre())
                                .idSucursal(request.getIdSucursal())
                                .idPuntoVenta(request.getIdPuntoVenta())
                                .idUsuarioCajero(usuario)
                                .saldoInicial(saldoInicial)
                                .saldoActual(saldoInicial)
                                .estado(CajaChica.EstadoCaja.ABIERTA)
                                .usuarioCreacion(usuario)
                                .build();

                CajaChica savedCaja = cajaChicaRepository.save(caja);

                if (saldoInicial.compareTo(BigDecimal.ZERO) > 0) {
                        CajaChicaMovimiento movimientoInicial = CajaChicaMovimiento.builder()
                                        .cajaChica(savedCaja)
                                        .tipo(CajaChicaMovimiento.TipoMovimiento.INGRESO)
                                        .monto(saldoInicial)
                                        .descripcion("FONDO INICIAL AL ABRIR CAJA")
                                        .referencia("APERTURA")
                                        .metodoPago(metodoPagoRepository.findByDescripcionIgnoreCase("EFECTIVO")
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        "Método de pago EFECTIVO no encontrado")))
                                        .fecha(LocalDateTime.now())
                                        .usuario(usuario)
                                        .build();
                        movimientoRepository.save(movimientoInicial);
                }

                return mapToCajaResponse(savedCaja);
        }

        @Transactional
        public MovimientoResponse registrarMovimiento(MovimientoRequest request, String usuario) {
                if (request.getCajaChicaId() == null) {
                        throw new RuntimeException("El ID de la caja chica no puede ser nulo");
                }

                CajaChica caja = cajaChicaRepository.findById(request.getCajaChicaId())
                                .orElseThrow(() -> new RuntimeException("Caja chica no encontrada"));

                if (caja.getEstado() == CajaChica.EstadoCaja.CERRADA) {
                        throw new RuntimeException("La caja chica está cerrada");
                }

                CajaChicaMovimiento movimiento = CajaChicaMovimiento.builder()
                                .cajaChica(caja)
                                .tipo(request.getTipo())
                                .monto(request.getMonto())
                                .descripcion(request.getDescripcion())
                                .referencia(request.getReferencia())
                                .metodoPago(metodoPagoRepository.findByDescripcionIgnoreCase(
                                                request.getMetodoPago() != null ? request.getMetodoPago() : "EFECTIVO")
                                                .orElseThrow(
                                                                () -> new RuntimeException(
                                                                                "Método de pago no encontrado: "
                                                                                                + request.getMetodoPago())))
                                .fecha(LocalDateTime.now())
                                .usuario(usuario)
                                .build();

                if (request.getTipo() == CajaChicaMovimiento.TipoMovimiento.INGRESO) {
                        caja.setSaldoActual(caja.getSaldoActual().add(request.getMonto()));
                } else {
                        caja.setSaldoActual(caja.getSaldoActual().subtract(request.getMonto()));
                }

                cajaChicaRepository.save(caja);
                return mapToMovimientoResponse(movimientoRepository.save(movimiento));
        }

        @Transactional(readOnly = true)
        public Page<MovimientoResponse> listarMovimientos(Long cajaId, Pageable pageable) {
                return movimientoRepository.findByCajaChicaIdOrderByFechaDesc(cajaId, pageable)
                                .map(this::mapToMovimientoResponse);
        }

        @Transactional(readOnly = true)
        public CajaResumenDTO obtenerResumen(Long id) {
                CajaChica caja = cajaChicaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Caja chica no encontrada"));

                List<CajaChicaMovimiento> movimientos = movimientoRepository.findByCajaChicaId(id);

                Map<String, BigDecimal> ingresosPorMetodo = movimientos.stream()
                                .filter(m -> m.getTipo() == CajaChicaMovimiento.TipoMovimiento.INGRESO)
                                .collect(Collectors.groupingBy(
                                                m -> m.getMetodoPago() != null ? m.getMetodoPago().getDescripcion()
                                                                : "EFECTIVO",
                                                Collectors.reducing(BigDecimal.ZERO, CajaChicaMovimiento::getMonto,
                                                                BigDecimal::add)));

                Map<String, BigDecimal> egresosPorMetodo = movimientos.stream()
                                .filter(m -> m.getTipo() == CajaChicaMovimiento.TipoMovimiento.EGRESO)
                                .collect(Collectors.groupingBy(
                                                m -> m.getMetodoPago() != null ? m.getMetodoPago().getDescripcion()
                                                                : "EFECTIVO",
                                                Collectors.reducing(BigDecimal.ZERO, CajaChicaMovimiento::getMonto,
                                                                BigDecimal::add)));

                List<CajaResumenDTO.MetodoPagoResumenDTO> ingresosDTO = new ArrayList<>();
                ingresosPorMetodo.forEach((k, v) -> ingresosDTO.add(new CajaResumenDTO.MetodoPagoResumenDTO(k, v)));

                List<CajaResumenDTO.MetodoPagoResumenDTO> egresosDTO = new ArrayList<>();
                egresosPorMetodo.forEach((k, v) -> egresosDTO.add(new CajaResumenDTO.MetodoPagoResumenDTO(k, v)));

                BigDecimal totalIngresos = ingresosDTO.stream().map(CajaResumenDTO.MetodoPagoResumenDTO::getMonto)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalEgresos = egresosDTO.stream().map(CajaResumenDTO.MetodoPagoResumenDTO::getMonto)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return CajaResumenDTO.builder()
                                .cajaId(caja.getId())
                                .nombre(caja.getNombre())
                                .saldoInicial(caja.getSaldoInicial())
                                .ingresos(ingresosDTO)
                                .egresos(egresosDTO)
                                .totalIngresos(totalIngresos)
                                .totalEgresos(totalEgresos)
                                .saldoFinalTeorico(caja.getSaldoActual())
                                .build();
        }

        @Transactional
        public CajaChicaResponse cerrarCaja(Long id, BigDecimal saldoCierreReal, boolean transferirACajaGeneral,
                        String usuario) {
                CajaChica caja = cajaChicaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Caja chica no encontrada"));

                if (caja.getEstado() == CajaChica.EstadoCaja.CERRADA) {
                        throw new RuntimeException("La caja ya se encuentra cerrada");
                }

                caja.setEstado(CajaChica.EstadoCaja.CERRADA);
                caja.setSaldoCierreReal(saldoCierreReal);
                caja.setFechaCierre(LocalDateTime.now());

                if (transferirACajaGeneral && saldoCierreReal.compareTo(BigDecimal.ZERO) > 0) {
                        cajaGeneralService.registrarMovimiento(
                                        CajaGeneralMovimientoRequest.builder()
                                                        .idSucursal(caja.getIdSucursal())
                                                        .tipo(CajaGeneralMovimiento.TipoMovimiento.TRANSFERENCIA_RECIBIDA)
                                                        .monto(saldoCierreReal)
                                                        .descripcion("TRANSFERENCIA POR CIERRE DE CAJA CHICA: "
                                                                        + caja.getNombre())
                                                        .referencia("ID_CAJA_CHICA:" + caja.getId())
                                                        .metodoPago("EFECTIVO")
                                                        .usuario(usuario)
                                                        .build());
                }

                return mapToCajaResponse(cajaChicaRepository.save(caja));
        }

        @Transactional
        public CajaChicaResponse cerrarCajaDetallado(Long id, CierreCajaRequest request, String usuario) {
                CajaChica caja = cajaChicaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Caja chica no encontrada"));

                if (caja.getEstado() == CajaChica.EstadoCaja.CERRADA) {
                        throw new RuntimeException("La caja ya se encuentra cerrada");
                }

                if (request.getDiferencias() != null) {
                        for (DiferenciaArqueoDTO dif : request.getDiferencias()) {
                                BigDecimal diferencia = dif.getDiferencia();
                                if (diferencia != null && diferencia.compareTo(BigDecimal.ZERO) != 0) {
                                        CajaChicaMovimiento.TipoMovimiento tipoMov;
                                        BigDecimal montoAjuste;
                                        String desc;

                                        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
                                                tipoMov = CajaChicaMovimiento.TipoMovimiento.INGRESO;
                                                montoAjuste = diferencia;
                                                desc = "AJUSTE SOBRANTE ARQUEO: " + dif.getMetodoPago() + " (+S/ "
                                                                + diferencia + ")";
                                        } else {
                                                tipoMov = CajaChicaMovimiento.TipoMovimiento.EGRESO;
                                                montoAjuste = diferencia.negate();
                                                desc = "AJUSTE FALTANTE ARQUEO: " + dif.getMetodoPago() + " (-S/ "
                                                                + montoAjuste + ")";
                                        }

                                        CajaChicaMovimiento movimientoAjuste = CajaChicaMovimiento.builder()
                                                        .cajaChica(caja)
                                                        .tipo(tipoMov)
                                                        .monto(montoAjuste)
                                                        .descripcion(desc)
                                                        .referencia("AJUSTE ARQUEO")
                                                        .metodoPago(metodoPagoRepository
                                                                        .findByDescripcionIgnoreCase(
                                                                                        dif.getMetodoPago())
                                                                        .orElseThrow(() -> new RuntimeException(
                                                                                        "Método de pago no encontrado: "
                                                                                                        + dif.getMetodoPago())))
                                                        .fecha(LocalDateTime.now())
                                                        .usuario(usuario)
                                                        .build();
                                        movimientoRepository.save(movimientoAjuste);

                                        if (tipoMov == CajaChicaMovimiento.TipoMovimiento.INGRESO) {
                                                caja.setSaldoActual(caja.getSaldoActual().add(montoAjuste));
                                        } else {
                                                caja.setSaldoActual(caja.getSaldoActual().subtract(montoAjuste));
                                        }
                                }
                        }
                }

                caja.setEstado(CajaChica.EstadoCaja.CERRADA);
                caja.setSaldoCierreReal(request.getSaldoCierreReal());
                caja.setFechaCierre(LocalDateTime.now());

                if (request.isTransferirACajaGeneral()) {
                        if (request.getDiferencias() != null) {
                                for (DiferenciaArqueoDTO dif : request.getDiferencias()) {
                                        BigDecimal montoReal = dif.getMontoReal();
                                        if (montoReal != null && montoReal.compareTo(BigDecimal.ZERO) != 0) {
                                                cajaGeneralService.registrarMovimiento(
                                                                CajaGeneralMovimientoRequest.builder()
                                                                                .idSucursal(caja.getIdSucursal())
                                                                                .tipo(CajaGeneralMovimiento.TipoMovimiento.TRANSFERENCIA_RECIBIDA)
                                                                                .monto(montoReal)
                                                                                .descripcion("TRANSFERENCIA POR CIERRE DE CAJA CHICA ("
                                                                                                + dif.getMetodoPago()
                                                                                                                .toUpperCase()
                                                                                                + " REAL): "
                                                                                                + caja.getNombre())
                                                                                .referencia("ID_CAJA_CHICA:"
                                                                                                + caja.getId())
                                                                                .metodoPago(dif.getMetodoPago()
                                                                                                .toUpperCase())
                                                                                .usuario(usuario)
                                                                                .build());
                                        }
                                }
                        }
                }

                return mapToCajaResponse(cajaChicaRepository.save(caja));
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

        private CajaChicaResponse mapToCajaResponse(CajaChica caja) {
                return CajaChicaResponse.builder()
                                .id(caja.getId())
                                .nombre(caja.getNombre())
                                .saldoActual(caja.getSaldoActual())
                                .estado(caja.getEstado())
                                .saldoInicial(caja.getSaldoInicial())
                                .saldoCierreReal(caja.getSaldoCierreReal())
                                .idPuntoVenta(caja.getIdPuntoVenta())
                                .idUsuarioCajero(obtenerNombreUsuario(caja.getIdUsuarioCajero()))
                                .fechaCierre(caja.getFechaCierre())
                                .createdAt(caja.getCreatedAt())
                                .build();
        }

        private MovimientoResponse mapToMovimientoResponse(CajaChicaMovimiento mov) {
                return MovimientoResponse.builder()
                                .id(mov.getId())
                                .tipo(mov.getTipo())
                                .monto(mov.getMonto())
                                .descripcion(mov.getDescripcion())
                                .referencia(mov.getReferencia())
                                .metodoPago(mov.getMetodoPago() != null ? mov.getMetodoPago().getDescripcion() : null)
                                .fecha(mov.getFecha())
                                .usuario(obtenerNombreUsuario(mov.getUsuario()))
                                .build();
        }

        @Transactional
        public void eliminarMovimiento(Long id) {
                CajaChicaMovimiento mov = movimientoRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado: " + id));

                CajaChica caja = mov.getCajaChica();
                if (caja.getEstado() == CajaChica.EstadoCaja.CERRADA) {
                        throw new RuntimeException("La caja chica está cerrada, no se puede anular el movimiento");
                }

                if (mov.getTipo() == CajaChicaMovimiento.TipoMovimiento.INGRESO) {
                        caja.setSaldoActual(caja.getSaldoActual().subtract(mov.getMonto()));
                } else {
                        caja.setSaldoActual(caja.getSaldoActual().add(mov.getMonto()));
                }

                cajaChicaRepository.save(caja);
                mov.setEstado(3);
                movimientoRepository.save(mov);
        }
}
