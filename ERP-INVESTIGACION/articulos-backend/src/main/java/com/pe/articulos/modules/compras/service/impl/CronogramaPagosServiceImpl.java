package com.pe.articulos.modules.compras.service.impl;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.shared.service.FileStorageService;
import com.pe.articulos.modules.compras.entity.CronogramaPago;
import com.pe.articulos.modules.compras.repository.CronogramaPagoRepository;
import com.pe.articulos.modules.compras.repository.CompraRepository;
import com.pe.articulos.modules.compras.service.CronogramaPagosService;
import com.pe.articulos.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class CronogramaPagosServiceImpl implements CronogramaPagosService {

        private final CronogramaPagoRepository cronogramaPagoRepository;
        private final CompraRepository compraRepository;
        private final FileStorageService fileStorageService;
        private final KafkaProducerService kafkaProducerService;
        private final com.pe.articulos.modules.compras.mapper.CronogramaPagoMapper cronogramaPagoMapper;

        @Override
        public PageResponse<com.pe.articulos.modules.compras.dto.CronogramaPagoResponseDTO> listarDeudas(Map<String, Object> params, Pageable pageable) {
                Page<CronogramaPago> page = cronogramaPagoRepository.findAll(pageable);
                return PageResponse.fromPage(page.map(cronogramaPagoMapper::toResponse));
        }

        @Override
        @Transactional
        public com.pe.articulos.modules.compras.dto.CronogramaPagoResponseDTO registrarPagoCuota(Long idCronograma, String numeroOperacion,
                        org.springframework.web.multipart.MultipartFile voucher) {
                CronogramaPago cuota = cronogramaPagoRepository.findById(idCronograma)
                                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                                                "Cuota no encontrada"));

                if (cuota.getEstado() == EstadoGeneral.PAGADO) {
                        throw new IllegalStateException("La cuota ya está pagada.");
                }

                String path = null;
                if (voucher != null && !voucher.isEmpty()) {
                        path = fileStorageService.storeFile(voucher);
                }

                String infoPago = "OP: " + numeroOperacion;
                if (path != null)
                        infoPago += " | FILE: " + path;

                cuota.setComprobantePago(infoPago);
                cuota.setEstado(EstadoGeneral.PAGADO);
                cuota.setFechaPagoReal(LocalDateTime.now());

                log.info("Cuota {} pagada. Info: {}", idCronograma, infoPago);

                CronogramaPago saved = cronogramaPagoRepository.save(cuota);

                kafkaProducerService.enviarNotificacion(
                                null,
                                "COMPRAS",
                                "Pago Realizado",
                                "Se ha registrado el pago de la cuota " + cuota.getNumeroCuota() + " de la compra #"
                                                + cuota.getCompra().getId(),
                                "SUCCESS");

                return cronogramaPagoMapper.toResponse(saved);
        }

        @Override
        @Transactional
        public com.pe.articulos.modules.compras.dto.CronogramaPagoResponseDTO solicitarPago(Long idCronograma) {
                CronogramaPago cuota = cronogramaPagoRepository.findById(idCronograma)
                                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                                                "Cuota no encontrada"));

                if (cuota.getEstado() == EstadoGeneral.PAGADO) {
                        throw new IllegalStateException("No se puede solicitar el pago de una cuota ya pagada.");
                }

                cuota.setEstado(EstadoGeneral.SOLICITADO);
                log.info("Pago solicitado para la cuota {}", idCronograma);

                CronogramaPago saved = cronogramaPagoRepository.save(cuota);

                kafkaProducerService.enviarNotificacion(
                                null,
                                "COMPRAS",
                                "Solicitud de Pago",
                                "Se ha solicitado el pago para la compra #" + cuota.getCompra().getId() + " - Cuota "
                                                + cuota.getNumeroCuota(),
                                "INFO");

                return cronogramaPagoMapper.toResponse(saved);
        }

        @Override
        @Transactional
        public com.pe.articulos.modules.compras.dto.CronogramaPagoResponseDTO generarCronograma(Long idCompra,
                        com.pe.articulos.modules.compras.dto.SolicitudPagoCompraRequest request) {
                com.pe.articulos.modules.compras.entity.Compra compra = compraRepository.findById(idCompra)
                                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                                                "Compra no encontrada"));

                BigDecimal montoCuota = compra.getTotalPagar();
                EstadoGeneral estado = EstadoGeneral.SOLICITADO;

                if (request != null && request.isUsarSaldoFavor()) {
                        // The user requested to use saldo a favor.
                        BigDecimal saldoAUsar = request.getMontoUsarSaldo();
                        if (saldoAUsar != null && saldoAUsar.compareTo(BigDecimal.ZERO) > 0) {
                                montoCuota = montoCuota.subtract(saldoAUsar);
                                if (montoCuota.compareTo(BigDecimal.ZERO) <= 0) {
                                        montoCuota = BigDecimal.ZERO;
                                        estado = EstadoGeneral.PAGADO;
                                }
                        }
                }

                CronogramaPago cuota = CronogramaPago.builder()
                                .compra(compra)
                                .numeroCuota(1)
                                .fechaVencimiento(java.time.LocalDate.now())
                                .montoCuota(montoCuota)
                                .estado(estado)
                                .build();

                if (estado == EstadoGeneral.PAGADO) {
                        cuota.setComprobantePago("COMPENSACIÓN CON SALDO A FAVOR");
                        cuota.setFechaPagoReal(LocalDateTime.now());
                        compra.setEstado(EstadoGeneral.PAGADO);
                } else {
                        compra.setEstado(EstadoGeneral.SOLICITADO);
                }

                Long currentUserId = com.pe.articulos.core.security.SecurityUtils.getCurrentUserId();
                compra.setSolicitanteId(currentUserId);

                compraRepository.save(compra);

                CronogramaPago saved = cronogramaPagoRepository.save(cuota);

                // Notificar via Kafka solo si hay un monto pendiente a solicitar
                if (montoCuota.compareTo(BigDecimal.ZERO) > 0) {
                        String moneda = compra.getMoneda() != null ? compra.getMoneda() : "S/";
                        if (moneda.equals("PEN"))
                                moneda = "S/";
                        else if (moneda.equals("USD"))
                                moneda = "$";

                        java.util.Map<String, Object> contenidoObj = java.util.Map.of(
                                        "mensaje",
                                        "Se ha generado la deuda y solicitado fondos para la compra #" + compra.getId()
                                                        + " de " + compra.getProveedor().getRazonSocial(),
                                        "idCompra", compra.getId(),
                                        "idSucursal", compra.getIdSucursal() != null ? compra.getIdSucursal() : 0,
                                        "solicitanteId", currentUserId != null ? currentUserId : 0);

                        kafkaProducerService.enviarNotificacion(
                                        null,
                                        "COMPRAS",
                                        "Solicitud de Pago: " + moneda + " " + montoCuota,
                                        contenidoObj,
                                        "INFO");
                }

                return cronogramaPagoMapper.toResponse(saved);
        }
}
