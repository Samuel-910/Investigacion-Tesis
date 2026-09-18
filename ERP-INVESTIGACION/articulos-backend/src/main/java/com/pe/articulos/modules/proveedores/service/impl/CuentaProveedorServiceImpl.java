package com.pe.articulos.modules.proveedores.service.impl;

import com.pe.articulos.modules.proveedores.entity.CuentaProveedor;
import com.pe.articulos.modules.proveedores.entity.CuentaProveedorMovimiento;
import com.pe.articulos.modules.proveedores.entity.DeudaProveedor;
import com.pe.articulos.modules.proveedores.entity.Proveedor;
import com.pe.articulos.modules.proveedores.mapper.CuentaProveedorMapper;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralMovimientoRequest;
import com.pe.articulos.modules.caja_general.entity.CajaGeneralMovimiento.TipoMovimiento;
import com.pe.articulos.modules.caja_general.service.CajaGeneralService;
import com.pe.articulos.modules.proveedores.dto.CuentaProveedorMovimientoResponse;
import com.pe.articulos.modules.proveedores.dto.CuentaProveedorResponse;
import com.pe.articulos.modules.proveedores.dto.DeudaProveedorResponse;
import com.pe.articulos.modules.proveedores.repository.CuentaProveedorMovimientoRepository;
import com.pe.articulos.modules.proveedores.repository.CuentaProveedorRepository;
import com.pe.articulos.modules.proveedores.repository.DeudaProveedorRepository;
import com.pe.articulos.modules.proveedores.repository.ProveedorRepository;
import com.pe.articulos.modules.proveedores.service.CuentaProveedorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CuentaProveedorServiceImpl implements CuentaProveedorService {

    private final CuentaProveedorRepository cuentaRepository;
    private final CuentaProveedorMovimientoRepository movimientoRepository;
    private final DeudaProveedorRepository deudaRepository;
    private final ProveedorRepository proveedorRepository;
    private final CajaGeneralService cajaGeneralService;
    private final CuentaProveedorMapper mapper;

    private CuentaProveedor obtenerCuentaEntity(Long idProveedor) {
        return cuentaRepository.findByProveedorId(idProveedor)
                .orElseGet(() -> {
                    Proveedor proveedor = proveedorRepository.findById(idProveedor)
                            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
                    CuentaProveedor nuevaCuenta = CuentaProveedor.builder()
                            .proveedor(proveedor)
                            .saldoTotal(BigDecimal.ZERO)
                            .build();
                    return cuentaRepository.save(nuevaCuenta);
                });
    }

    @Override
    @Transactional
    public CuentaProveedorResponse obtenerCuentaPorProveedor(Long idProveedor) {
        return mapper.toCuentaResponse(obtenerCuentaEntity(idProveedor));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CuentaProveedorMovimientoResponse> listarMovimientos(Long idProveedor, Pageable pageable) {
        CuentaProveedor cuenta = obtenerCuentaEntity(idProveedor);
        Page<CuentaProveedorMovimiento> page = movimientoRepository
                .findByCuentaIdOrderByFechaRegistroDesc(cuenta.getId(), pageable);
        return PageResponse.fromPage(page.map(mapper::toMovimientoResponse));
    }

    @Override
    @Transactional
    public CuentaProveedorMovimientoResponse registrarMovimiento(Long idProveedor, String tipo, BigDecimal monto,
            String descripcion, String idUser, Long idSucursal, boolean pagarDesdeCajaGeneral, String metodoPago) {

        log.info("Registrando movimiento {} para proveedor {} por monto {}", tipo, idProveedor, monto);
        CuentaProveedor cuenta = obtenerCuentaEntity(idProveedor);

        CuentaProveedorMovimiento movimiento = CuentaProveedorMovimiento.builder()
                .cuenta(cuenta)
                .tipo(tipo)
                .monto(monto)
                .descripcion(descripcion)
                .idUser(idUser)
                .build();

        // Actualizar saldo de cuenta proveedor
        if ("ABONO".equalsIgnoreCase(tipo)) {
            cuenta.setSaldoTotal(cuenta.getSaldoTotal().subtract(monto));

            // FIFO logic para deudas
            BigDecimal montoRestante = monto;
            List<DeudaProveedor> deudasPendientes = deudaRepository
                    .findByCuentaProveedorIdAndEstadoOrderByFechaEmisionAsc(cuenta.getId(), "PENDIENTE");

            for (DeudaProveedor deuda : deudasPendientes) {
                if (montoRestante.compareTo(BigDecimal.ZERO) <= 0) {
                    break; // Pago agotado
                }

                if (deuda.getSaldoPendiente().compareTo(montoRestante) <= 0) {
                    // Abono cubre la deuda completa
                    montoRestante = montoRestante.subtract(deuda.getSaldoPendiente());
                    deuda.setSaldoPendiente(BigDecimal.ZERO);
                    deuda.setEstado("PAGADO");
                } else {
                    // Abono cubre solo parte de esta deuda
                    deuda.setSaldoPendiente(deuda.getSaldoPendiente().subtract(montoRestante));
                    montoRestante = BigDecimal.ZERO;
                }
                deudaRepository.save(deuda);
            }

            // Si es un abono y se indica pagar desde caja general
            if (pagarDesdeCajaGeneral && idSucursal != null) {
                cajaGeneralService.registrarMovimiento(
                        CajaGeneralMovimientoRequest.builder()
                                .idSucursal(idSucursal)
                                .tipo(TipoMovimiento.PAGO_PROVEEDOR)
                                .monto(monto)
                                .descripcion("PAGO A PROVEEDOR: " + cuenta.getProveedor().getRazonSocial() + " - "
                                        + descripcion)
                                .referencia("CUENTA_PROV_MOV:" + cuenta.getId())
                                .metodoPago(metodoPago != null ? metodoPago : "EFECTIVO")
                                .usuario(idUser)
                                .build());
            }
        } else if ("CARGO".equalsIgnoreCase(tipo)) {
            cuenta.setSaldoTotal(cuenta.getSaldoTotal().add(monto));

            int plazoDias = cuenta.getProveedor().getPlazoDias() != null ? cuenta.getProveedor().getPlazoDias() : 0;

            DeudaProveedor nuevaDeuda = DeudaProveedor.builder()
                    .cuentaProveedor(cuenta)
                    .montoOriginal(monto)
                    .saldoPendiente(monto)
                    .fechaVencimiento(LocalDate.now().plusDays(plazoDias))
                    .estado("PENDIENTE")
                    .referenciaCargo(descripcion)
                    .build();

            deudaRepository.save(nuevaDeuda);
        }

        cuentaRepository.save(cuenta);
        CuentaProveedorMovimiento savedMovimiento = movimientoRepository.save(movimiento);
        return mapper.toMovimientoResponse(savedMovimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaProveedorResponse> listarCuentasConSaldoPendiente() {
        return cuentaRepository.findAllBySaldoTotalGreaterThan(BigDecimal.ZERO)
                .stream()
                .map(mapper::toCuentaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeudaProveedorResponse> listarDeudasPendientesPorProveedor(Long idProveedor) {
        CuentaProveedor cuenta = obtenerCuentaEntity(idProveedor);
        return deudaRepository.findByCuentaProveedorIdAndEstadoOrderByFechaEmisionAsc(cuenta.getId(), "PENDIENTE")
                .stream()
                .map(mapper::toDeudaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeudaProveedorResponse> listarDeudasPaginadas(String searchTerm, Pageable pageable) {
        Page<DeudaProveedor> page = deudaRepository.findPendientesConFiltro("PENDIENTE", searchTerm, pageable);
        return PageResponse.fromPage(page.map(mapper::toDeudaResponse));
    }
}
