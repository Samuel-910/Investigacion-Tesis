package com.pe.articulos.modules.venta_registro.service.impl;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.modules.atributos.repository.MetodoPagoRepository;
import com.pe.articulos.modules.caja_chica.dto.CajaChicaResponse;
import com.pe.articulos.modules.caja_chica.dto.MovimientoRequest;
import com.pe.articulos.modules.caja_chica.entity.CajaChicaMovimiento;
import com.pe.articulos.modules.caja_chica.services.CajaChicaService;
import com.pe.articulos.modules.catalogo.repository.CatalogoRepository;
import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.productos.entity.MovimientoDiverso;
import com.pe.articulos.modules.productos.entity.Producto;
import com.pe.articulos.modules.productos.repository.MovimientoDiversoRepository;
import com.pe.articulos.modules.productos.services.ProductoService;
import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.puntos.repository.PuntoRepository;
import com.pe.articulos.modules.reportes.service.ExcelExportService;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.modules.venta_registro.dto.CorrelatividadDTO;
import com.pe.articulos.modules.venta_registro.dto.CrearVentaRequest;
import com.pe.articulos.modules.venta_registro.dto.SerieCorrelativoDto;
import com.pe.articulos.modules.venta_registro.dto.VentaDetalleDTO;
import com.pe.articulos.modules.venta_registro.dto.VentaRegistroDTO;
import com.pe.articulos.modules.venta_registro.entity.VentaDetalle;
import com.pe.articulos.modules.venta_registro.entity.VentaRegistro;
import com.pe.articulos.modules.venta_registro.mapper.VentaMapper;
import com.pe.articulos.modules.venta_registro.repository.VentaRegistroRepository;
import com.pe.articulos.modules.venta_registro.service.PuntosDocumentoService;
import com.pe.articulos.modules.venta_registro.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.pe.articulos.core.shared.service.EmailService;
import com.pe.articulos.core.shared.service.WhatsAppService;
import com.pe.articulos.modules.documentos.services.DocumentoService;
import com.pe.articulos.core.security.SecurityUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@SuppressWarnings("null")
public class VentaServiceImpl implements VentaService {

    private final VentaRegistroRepository ventaRegistroRepository;
    private final PuntosDocumentoService puntosDocumentoService;
    private final VentaMapper ventaMapper;

    private final DatosPersonalesRepository datosPersonalesRepository;
    private final PuntoRepository puntoRepository;
    private final ProductoService productoService;
    private final CajaChicaService cajaChicaService;
    private final MetodoPagoRepository metodoPagoRepository;
    private final CatalogoRepository catalogoRepository;
    private final ExcelExportService excelExportService;
    private final MovimientoDiversoRepository movimientoDiversoRepository;
    private final EmailService emailService;
    private final WhatsAppService whatsappService;
    private final DocumentoService documentoService;
    private final com.pe.articulos.modules.venta_registro.repository.VentaReimpresionLogRepository reimpresionLogRepository;

    @Override
    public VentaRegistroDTO crearVenta(CrearVentaRequest request) {
        log.info("Creando nueva venta para paciente: {}", request.getIdPersonal());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        DatosPersonales usuarioActual = datosPersonalesRepository.findByLogin(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontróado: " + currentUsername));

        final Long idPuntoVenta;

        if (request.getPunto() != null) {
            idPuntoVenta = Long.valueOf(request.getPunto());
        } else if (usuarioActual.getPunto() != null) {
            idPuntoVenta = usuarioActual.getPunto().getPunto();
        } else {
            throw new ValidationException("Debe especificar un punto de venta o tener uno asignado. El usuario '"
                    + currentUsername + "' NO tiene un Punto de Venta asignado.");
        }

        Punto puntoEntity = puntoRepository.findById(idPuntoVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Punto de Venta no encontróado: " + idPuntoVenta));

        Long idSucursalReal = (puntoEntity.getSucursal() != null)
                ? puntoEntity.getSucursal().getIdSucursal()
                : null;

        CajaChicaResponse cajaAbierta = null;
        if (request.getEstado() != EstadoGeneral.COTIZACION) {
            cajaAbierta = cajaChicaService
                    .obtenerCajaAbierta(idPuntoVenta, String.valueOf(usuarioActual.getId()))
                    .orElseThrow(() -> new ValidationException(
                            "No tiene una caja chica abierta. Debe abrir caja antes de vender."));
        }

        String tipoDoc = request.getTipoDoc() != null ? request.getTipoDoc() : "03";
        SerieCorrelativoDto correlativo = null;
        if (request.getEstado() != EstadoGeneral.COTIZACION) {
            correlativo = puntosDocumentoService.generarCorrelativo(idPuntoVenta, tipoDoc, Modulo.VENTA);
        }

        String moneda = request.getMoneda() != null ? request.getMoneda() : "PEN";
        BigDecimal tc = "PEN".equals(moneda) ? BigDecimal.ONE : BigDecimal.ONE;

        Long idPers = null;
        String idPersUser = null;
        if (request.getIdPersonal() != null) {
            try {
                idPers = Long.valueOf(request.getIdPersonal());
            } catch (NumberFormatException e) {
                idPersUser = request.getIdPersonal();
            }
        }

        VentaRegistro venta = VentaRegistro.builder()
                .idPersonal(idPers)
                .idPersonalUser(idPersUser)
                .fecha(request.getFecha() != null ? request.getFecha() : LocalDate.now())
                .punto(idPuntoVenta)
                .idSucursal(idSucursalReal)
                .tipoDoc(tipoDoc)
                .serie(correlativo != null ? correlativo.getSerie() : null)
                .numero(correlativo != null ? correlativo.getNumero() : null)
                .numdoc(correlativo != null && correlativo.getNumero() != null ? String.format("%07d", correlativo.getNumero()) : null)
                .tipoPac(request.getTipoPac())
                .moneda(moneda)
                .tc(tc)
                .descuento(request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO)
                .idMedico(request.getIdMedico() != null && !request.getIdMedico().isEmpty()
                        ? Long.valueOf(request.getIdMedico())
                        : null)
                .observacion(request.getObservacion())
                .estado(request.getEstado() != null ? request.getEstado() : EstadoGeneral.VIGENTE)
                .idPlantilla(request.getIdPlantilla())
                .idUser(usuarioActual.getId().intValue())
                .nroDni(request.getNroDni())
                .nombrePac(request.getNombrePac())
                .tipoDni(request.getTipoDni())
                .importePago(request.getImportePago())
                .vuelto(request.getVuelto())
                .build();

        venta.setMetodoPago(metodoPagoRepository.findByDescripcionIgnoreCase(
                request.getMetodoPago() != null ? request.getMetodoPago() : "EFECTIVO")
                .orElse(null));

        if (request.getDetalles() != null) {
            java.util.Map<Long, BigDecimal> stockAcumulado = new java.util.HashMap<>();
            for (int i = 0; i < request.getDetalles().size(); i++) {
                var detalleReq = request.getDetalles().get(i);
                
                // --- VALIDACIÓN DE STOCK ---
                if (request.getEstado() != EstadoGeneral.COTIZACION && detalleReq.getIdArticulo() != null) {
                    Producto p = productoService.obtenerEntidadPorId(Long.valueOf(detalleReq.getIdArticulo()))
                            .orElseThrow(() -> new ValidationException("Producto no encontróado: " + detalleReq.getIdArticulo()));
                    
                    BigDecimal stockActual = p.getStock() != null ? p.getStock() : BigDecimal.ZERO;
                    BigDecimal cantidadSolicitada = detalleReq.getCantidad() != null ? detalleReq.getCantidad() : BigDecimal.ONE;
                    String unidadMedida = detalleReq.getUnidadMedida() != null ? detalleReq.getUnidadMedida().toUpperCase() : "UND";
                    
                    BigDecimal cantidadBaseReq = cantidadSolicitada;
                    if (unidadMedida.contains("CAJA") || unidadMedida.equals("CJA")) {
                        int factor = (p.getFactorCaja() != null && p.getFactorCaja() > 0) ? p.getFactorCaja() : 1;
                        if (p.getManejaBlister() != null && p.getManejaBlister() && p.getFactorBlister() != null && p.getFactorBlister() > 0) {
                            factor = factor * p.getFactorBlister();
                        }
                        cantidadBaseReq = cantidadSolicitada.multiply(BigDecimal.valueOf(factor));
                    } else if (unidadMedida.contains("BLISTER") || unidadMedida.equals("BLI") || unidadMedida.contains("BLSTER") || unidadMedida.contains("BLÍSTER")) {
                        int factor = (p.getFactorBlister() != null && p.getFactorBlister() > 0) ? p.getFactorBlister() : 1;
                        cantidadBaseReq = cantidadSolicitada.multiply(BigDecimal.valueOf(factor));
                    }
                    
                    BigDecimal acumulado = stockAcumulado.getOrDefault(p.getIdProducto(), BigDecimal.ZERO).add(cantidadBaseReq);
                    stockAcumulado.put(p.getIdProducto(), acumulado);

                    if (stockActual.compareTo(acumulado) < 0) {
                        throw new ValidationException("Stock insuficiente para el producto '" + (detalleReq.getGlosa() != null ? detalleReq.getGlosa() : p.getPresentacion()) + "'. Stock actual: " + stockActual.stripTrailingZeros().toPlainString() + " UND, Solicitado acumulado: " + acumulado.stripTrailingZeros().toPlainString() + " UND.");
                    }
                }
                // ---------------------------

                String inafecto = "N";
                if (detalleReq.getIdCatalogo() != null) {
                    inafecto = catalogoRepository.findById(Long.valueOf(detalleReq.getIdCatalogo()))
                            .map(c -> {
                                if ("EXONERADO".equalsIgnoreCase(c.getTipoAfectacion()))
                                    return "E";
                                if ("INAFECTO".equalsIgnoreCase(c.getTipoAfectacion()))
                                    return "I";
                                return "N";
                            })
                            .orElse("N");
                }

                VentaDetalle detalle = VentaDetalle.builder()
                        .item(i + 1)
                        .idExamen(detalleReq.getIdExamen() != null ? Long.valueOf(detalleReq.getIdExamen()) : null)
                        .idCatalogo(
                                detalleReq.getIdCatalogo() != null ? Long.valueOf(detalleReq.getIdCatalogo()) : null)
                        .idArticulo(
                                detalleReq.getIdArticulo() != null ? Long.valueOf(detalleReq.getIdArticulo()) : null)
                        .descripcion(detalleReq.getGlosa())
                        .cantidad(detalleReq.getCantidad() != null ? detalleReq.getCantidad() : BigDecimal.ONE)
                        .precioUnitario(detalleReq.getPrecioUnitario() != null ? detalleReq.getPrecioUnitario()
                                : BigDecimal.ZERO)
                        .precioVenta(detalleReq.getPrecioUnitario() != null ? detalleReq.getPrecioUnitario()
                                : BigDecimal.ZERO)
                        .inafecto(inafecto)
                        .descuento(detalleReq.getDescuento() != null ? detalleReq.getDescuento() : BigDecimal.ZERO)
                        .montoDescuento(detalleReq.getMontoDescuento() != null ? detalleReq.getMontoDescuento()
                                : BigDecimal.ZERO)
                        .porcentajeDescuento(
                                detalleReq.getPorcentajeDescuento() != null ? detalleReq.getPorcentajeDescuento()
                                        : BigDecimal.ZERO)
                        .porcDsc(detalleReq.getPorcDsc() != null ? detalleReq.getPorcDsc() : BigDecimal.ZERO)
                        .unidadMedida(detalleReq.getUnidadMedida())
                        .nroLote(detalleReq.getNroLote())
                        .fechaVenc(detalleReq.getFechaVenc())
                        .estado(EstadoGeneral.ACTIVO)
                        .build();

                detalle.calcularTotales();
                venta.agregarDetalle(detalle);
            }
        }

        venta.calcularTotales();

        VentaRegistro saved = ventaRegistroRepository.save(venta);
        if (EstadoGeneral.VIGENTE.equals(saved.getEstado()) && saved.getDetalles() != null) {
            log.info("Registrando salidas en Kardex para venta: {}", saved.getIdVenta());
            
            for (VentaDetalle det : saved.getDetalles()) {
                if (det.getIdArticulo() != null && det.getCantidad() != null && det.getCantidad().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    Long idProducto = det.getIdArticulo();
                    Optional<Producto> productoOpt = productoService.obtenerEntidadPorId(idProducto);
                    
                    if (productoOpt.isPresent()) {
                        Producto p = productoOpt.get();
                        String um = det.getUnidadMedida() != null ? det.getUnidadMedida().toUpperCase() : "UND";
                        java.math.BigDecimal costoBase = calcularCostoUnitarioKardex(p, um);
                        
                        KardexDTO kardexDTO = KardexDTO.builder()
                                .idAlmacen(p.getIdAlmacen())
                                .idCatalogo(p.getIdCatalogo())
                                .idSucursal(idSucursalReal)
                                .idUsuario(usuarioActual.getId())
                                .idDocumento(saved.getTipoDoc())
                                .numDoc(saved.getSerie() + "-" + saved.getNumero())
                                .detalle("VENTA " + saved.getSerie() + "-" + saved.getNumero())
                                .operacion("VENTA")
                                .signo("-")
                                .cantidad(det.getCantidad())
                                .costoUnitario(costoBase)
                                .observacion("Salida por venta: " + det.getCantidad() + " " + um)
                                .origenId(saved.getIdVenta().toString())
                                .origenTipo("VTA")
                                .nroLote(p.getNroLote())
                                .fechaVenc(p.getFechaVencimiento())
                                .presentacion(um)
                                .build();

                        try {
                            productoService.actualizarStockKardex(idProducto, kardexDTO);
                            log.info("Kardex actualizado para producto {} (-{} {})", idProducto, det.getCantidad(), um);
                        } catch (Exception e) {
                            log.error("CRITICAL: Error actualizando stock/kardex para producto {}: {}",
                                    idProducto, e.getMessage(), e);
                        }
                    }
                }
            }
        }

        log.info("Venta creada exitosamente: {} | Doc: {}-{}", saved.getIdVenta(), saved.getSerie(), saved.getNumero());

        try {
            cajaChicaService.registrarMovimiento(MovimientoRequest.builder()
                    .cajaChicaId(cajaAbierta.getId())
                    .tipo(CajaChicaMovimiento.TipoMovimiento.INGRESO)
                    .monto(saved.getTotal())
                    .descripcion("VENTA: " + saved.getSerie() + "-" + saved.getNumero())
                    .referencia("VTA-" + saved.getIdVenta())
                    .metodoPago(ventaMapper.getMetodoPagoDescripcion(saved))
                    .build(), String.valueOf(usuarioActual.getId()));
        } catch (Exception e) {
            log.error("Error al registrar movimiento en caja chica: {}", e.getMessage());
        }

        if (request.getCanjeNotaVentaId() != null) {
            try {
                log.info("Canjeando Nota de Venta ID: {}", request.getCanjeNotaVentaId());
                this.anularVenta(request.getCanjeNotaVentaId(),
                        "CANJE por documento " + saved.getSerie() + "-" + saved.getNumero());
            } catch (Exception e) {
                log.error("No se pudo anular la Nota de Venta {}: {}", request.getCanjeNotaVentaId(), e.getMessage());
            }
        }

        return ventaMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaRegistroDTO obtenerVentaPorId(Long idVenta) {
        VentaRegistro venta = ventaRegistroRepository.findByIdVentaWithDetalles(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontróada: " + idVenta));
        return ventaMapper.toDto(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaRegistroDTO> listarTodasVentas() {
        return ventaRegistroRepository.findAll().stream()
                .map(ventaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaRegistroDTO> listarVentasPorEstado(EstadoGeneral estado) {
        return ventaRegistroRepository.findByEstado(estado).stream()
                .map(ventaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaRegistroDTO> listarVentasPorFecha(LocalDate fecha) {
        return ventaRegistroRepository.findByFecha(fecha).stream()
                .map(ventaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaRegistroDTO> listarVentasPorPaciente(String idPersonal) {
        try {
            Long id = Long.valueOf(idPersonal);
            return ventaRegistroRepository.findByIdPersonal(id).stream()
                    .map(ventaMapper::toDto)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return ventaRegistroRepository.findByIdPersonalUser(idPersonal).stream()
                    .map(ventaMapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaRegistroDTO> listarVentasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return ventaRegistroRepository.findByFechaBetween(fechaInicio, fechaFin).stream()
                .map(ventaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public VentaRegistroDTO anularVenta(Long idVenta, String motivo) {
        VentaRegistro venta = ventaRegistroRepository.findByIdVentaWithDetalles(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontróada: " + idVenta));

        if (EstadoGeneral.ANULADO.equals(venta.getEstado())) {
            throw new ValidationException("La venta ya está anulada");
        }

        venta.setEstado(EstadoGeneral.ANULADO);
        venta.setFechaAnul(LocalDateTime.now());

        Authentication authInit = SecurityContextHolder.getContext().getAuthentication();
        String loginInit = (authInit != null && authInit.getName() != null) ? authInit.getName() : "SISTEMA";
        datosPersonalesRepository.findByLogin(loginInit).ifPresent(u -> venta.setIdUserAnul(u.getId().intValue()));

        if (motivo != null && !motivo.isEmpty()) {
            String obsActual = venta.getObservacion() != null ? venta.getObservacion() : "";
            venta.setObservacion(obsActual + " | ANULADO: " + motivo);
        }

        ventaRegistroRepository.save(venta);

        if ("07".equals(venta.getTipoDoc()) && venta.getRefDoc() != null) {
            String refDoc = venta.getRefDoc();
            try {
                String[] parts = refDoc.split("-");
                if (parts.length == 2) {
                    String refSerie = parts[0];
                    Integer refNumero = Integer.parseInt(parts[1]);
                    Optional<VentaRegistro> originalVentaOpt = ventaRegistroRepository.findBySerieAndNumero(refSerie,
                            refNumero);
                    if (originalVentaOpt.isPresent()) {
                        VentaRegistro originalVenta = originalVentaOpt.get();
                        if (originalVenta.getDetalles() != null) {
                            log.info("Reincorporando cantidades al saldo devuelto de la venta original ID: {}",
                                    originalVenta.getIdVenta());
                            for (VentaDetalle ncDet : venta.getDetalles()) {
                                originalVenta.getDetalles().stream()
                                        .filter(d -> d.getIdArticulo() != null
                                                && d.getIdArticulo().equals(ncDet.getIdArticulo())
                                                && d.getUnidadMedida().equals(ncDet.getUnidadMedida()))
                                        .findFirst()
                                        .ifPresent(originalDet -> {
                                            BigDecimal yaDevuelto = originalDet.getCantidadDevuelta() != null
                                                    ? originalDet.getCantidadDevuelta()
                                                    : BigDecimal.ZERO;
                                            BigDecimal aDescontar = ncDet.getCantidad() != null ? ncDet.getCantidad()
                                                    : BigDecimal.ZERO;

                                            BigDecimal nuevoDevuelto = yaDevuelto.subtract(aDescontar);
                                            if (nuevoDevuelto.compareTo(BigDecimal.ZERO) < 0) {
                                                nuevoDevuelto = BigDecimal.ZERO;
                                            }
                                            originalDet.setCantidadDevuelta(nuevoDevuelto);
                                            originalDet.calcularTotales();
                                            log.info("Item '{}': cantidad devuelta restaurada a {}",
                                                    originalDet.getGlosa(), nuevoDevuelto);
                                        });
                            }
                            originalVenta.calcularTotales();
                            ventaRegistroRepository.save(originalVenta);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error al revertir devoluciones en venta original para la NC {}: {}", venta.getIdVenta(),
                        e.getMessage());
            }
        }

        if ("01".equals(venta.getTipoDoc()) || "03".equals(venta.getTipoDoc())) {
            try {
                VentaRegistro notaCredito = new VentaRegistro();
                notaCredito.setTipoDoc("07"); // Nota de Credito
                notaCredito.setSerie("NC" + venta.getSerie().substring(1)); // Usually starts with F or B, so NC+001
                notaCredito.setNumero(venta.getNumero()); // Keep same number or generate new sequence
                notaCredito.setFecha(LocalDate.now());
                notaCredito.setMoneda(venta.getMoneda());
                notaCredito.setDescuento(venta.getDescuento());
                notaCredito.setIgv(venta.getIgv());
                notaCredito.setBaseImp(venta.getBaseImp());
                notaCredito.setValorExo(venta.getValorExo());
                notaCredito.setValorInaf(venta.getValorInaf());
                notaCredito.setTotal(venta.getTotal());
                notaCredito.setEstado(EstadoGeneral.VIGENTE);
                notaCredito.setIdPersonal(venta.getIdPersonal());
                notaCredito.setIdSucursal(venta.getIdSucursal());
                notaCredito.setPunto(venta.getPunto());
                notaCredito.setObservacion(
                        "ANULA A: " + venta.getSerie() + "-" + venta.getNumero() + " | MOTIVO: " + motivo);

                ventaRegistroRepository.save(notaCredito);
                log.info("Generada Nota de Crédito: {}-{} para Venta {}", notaCredito.getSerie(),
                        notaCredito.getNumero(), venta.getIdVenta());
            } catch (Exception e) {
                log.error("Error al generar Nota de Crédito automática: {}", e.getMessage());
            }
        }

        if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
            log.info("Anulando documento {}: procesando Kardex ({} items)", venta.getIdVenta(),
                    venta.getDetalles().size());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = (auth != null && auth.getName() != null) ? auth.getName() : "SISTEMA";
            DatosPersonales usuarioActual = datosPersonalesRepository.findByLogin(currentUsername)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontróado para anulación: " + currentUsername));
                            
            // Crear Movimiento Diverso por anulación
            com.pe.articulos.modules.productos.entity.MovimientoDiverso movAnul = new com.pe.articulos.modules.productos.entity.MovimientoDiverso();
            movAnul.setFecha(java.time.LocalDateTime.now());
            movAnul.setMotivo(motivo);
            movAnul.setEstado(EstadoGeneral.ACTIVO);
            com.pe.articulos.modules.documentos.entities.Modulo moduloAnul = "07".equals(venta.getTipoDoc()) ? 
            com.pe.articulos.modules.documentos.entities.Modulo.SALIDAS_DIVERSAS : 
            com.pe.articulos.modules.documentos.entities.Modulo.INGRESOS_DIVERSOS;

        com.pe.articulos.modules.venta_registro.dto.SerieCorrelativoDto correlativoTicket = null;
        try {
            correlativoTicket = puntosDocumentoService.generarCorrelativo(venta.getPunto(), "12", moduloAnul);
        } catch (Exception e1) {
            try {
                correlativoTicket = puntosDocumentoService.generarCorrelativo(venta.getPunto(), "12", com.pe.articulos.modules.documentos.entities.Modulo.INVENTARIO);
            } catch (Exception e2) {
                // Fallback silently
            }
        }

        if (correlativoTicket != null) {
            movAnul.setSerie(correlativoTicket.getSerie());
            movAnul.setNumero(correlativoTicket.getNumero());
            movAnul.setNumDocumento(correlativoTicket.getSerie() + "-" + String.format("%07d", correlativoTicket.getNumero()));
        } else {
            movAnul.setNumDocumento("TK-ANUL-" + System.currentTimeMillis() / 1000);
        }
            
            com.pe.articulos.modules.sucursal.entity.Sucursal sucursalAnul = new com.pe.articulos.modules.sucursal.entity.Sucursal();
            sucursalAnul.setIdSucursal(venta.getIdSucursal());
            movAnul.setSucursal(sucursalAnul);
            movAnul.setUsuario(usuarioActual);
            movAnul = movimientoDiversoRepository.save(movAnul);

            for (VentaDetalle det : venta.getDetalles()) {
                Long idGral = det.getIdArticulo() != null ? det.getIdArticulo() : det.getIdCatalogo();

                if (idGral != null) {
                    try {
                        Long idProducto = idGral;
                        Optional<Producto> productoOpt = productoService.obtenerEntidadPorId(idProducto);
                        if (productoOpt.isPresent()) {
                            var p = productoOpt.get();

                            Long sucursalId = venta.getIdSucursal();

                            String signoKardex = "07".equals(venta.getTipoDoc()) ? "-" : "+";
                            String operacionKardex = "07".equals(venta.getTipoDoc()) ? "SALIDA DIVERSA : ANULACION NC" : "INGRESO DIVERSO : ANULACION";
                            String detalleKardex = "07".equals(venta.getTipoDoc())
                                    ? "ANULACION NC " + venta.getSerie() + "-" + venta.getNumero()
                                    : "ANULACION VENTA " + venta.getSerie() + "-" + venta.getNumero();
                            String origenTipoKardex = "07".equals(venta.getTipoDoc()) ? "NC_ANUL" : "VTA_ANUL";

                            KardexDTO kardexDTO = KardexDTO.builder()
                                    .idAlmacen(p.getIdAlmacen())
                                    .idCatalogo(p.getIdCatalogo())
                                    .idSucursal(sucursalId)
                                    .idUsuario(usuarioActual.getId())
                                    .idDocumento(venta.getTipoDoc())
                                    .numDoc(venta.getSerie() + "-" + venta.getNumero())
                                    .detalle(detalleKardex)
                                    .operacion(operacionKardex)
                                    .signo(signoKardex)
                                    .cantidad(det.getCantidad())
                                    .costoUnitario(calcularCostoUnitarioKardex(p, det.getUnidadMedida()))
                                    .observacion("Retorno de stock por anulación")
                                    .origenId(venta.getIdVenta().toString())
                                    .origenTipo(origenTipoKardex)
                                    .nroLote(p.getNroLote())
                                    .fechaVenc(p.getFechaVencimiento())
                                    .presentacion(det.getUnidadMedida())
                                    .build();

                            productoService.actualizarStockKardex(idProducto, kardexDTO);
                            
                            // Agregar detalle al Movimiento Diverso
                            com.pe.articulos.modules.productos.entity.MovimientoDiversoDetalle movDet = new com.pe.articulos.modules.productos.entity.MovimientoDiversoDetalle();
                            movDet.setCatalogo(p.getCatalogo());
                            movDet.setCantidad(det.getCantidad());
                            movDet.setCostoUnitario(p.getPrecioCompra() != null ? p.getPrecioCompra() : java.math.BigDecimal.ZERO);
                            movDet.setTipo("07".equals(venta.getTipoDoc()) ? "SALIDA" : "INGRESO");
                            movDet.setNroLote(p.getNroLote());
                            movDet.setFechaVencimiento(p.getFechaVencimiento());
                            movDet.setObservacion(motivo);
                            movDet.setIdAlmacen(p.getIdAlmacen());
                            movAnul.addDetalle(movDet);
                                    
                                    // --- RESTAURAR CANTIDAD DEVUELTA EN VENTA ORIGINAL SI ES NC ---
                                    if ("07".equals(venta.getTipoDoc()) && venta.getRefDoc() != null) {
                                        try {
                                            String[] parts = venta.getRefDoc().split("-");
                                            if (parts.length == 2) {
                                                String origSerie = parts[0];
                                                Integer origNumero = Integer.parseInt(parts[1]);
                                                ventaRegistroRepository.findBySerieAndNumero(origSerie, origNumero)
                                                    .ifPresent(origVenta -> {
                                                        for (VentaDetalle origDet : origVenta.getDetalles()) {
                                                            if (origDet.getIdArticulo() != null && origDet.getIdArticulo().equals(idGral) && origDet.getUnidadMedida().equals(det.getUnidadMedida())) {
                                                                if (origDet.getCantidadDevuelta() != null) {
                                                                    origDet.setCantidadDevuelta(origDet.getCantidadDevuelta().subtract(det.getCantidad()));
                                                                    if (origDet.getCantidadDevuelta().compareTo(java.math.BigDecimal.ZERO) < 0) {
                                                                        origDet.setCantidadDevuelta(java.math.BigDecimal.ZERO);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        ventaRegistroRepository.save(origVenta);
                                                        log.info("Restaurada cantidad devuelta en venta original {}", venta.getRefDoc());
                                                    });
                                            }
                                        } catch (Exception e) {
                                            log.error("Error restaurando cantidad devuelta en venta original {}: {}", venta.getRefDoc(), e.getMessage());
                                        }
                                    }
                                    // ----------------------------------------------------------------
                            
                            log.info("Kardex actualizado para producto {} (Anulación Documento {})", idProducto,
                                    venta.getIdVenta());
                        } else {
                            log.warn("No se encontróó el producto con ID {} para retornar stock.", idGral);
                        }
                    } catch (Exception e) {
                        log.error("Error al retornar stock de producto {} por anulación: {}", idGral,
                                e.getMessage());
                    }
                }
            }
        }

        log.info("Documento anulado exitosamente: {}", idVenta);

        try {
            Authentication authAlt = SecurityContextHolder.getContext().getAuthentication();
            String loginAlt = authAlt != null ? authAlt.getName() : "SISTEMA";
            DatosPersonales userAlt = datosPersonalesRepository.findByLogin(loginAlt).orElse(null);

            if (userAlt != null) {
                cajaChicaService.obtenerCajaAbierta(venta.getPunto(), String.valueOf(userAlt.getId()))
                        .ifPresent(caja -> {
                            CajaChicaMovimiento.TipoMovimiento tipoMov = "07".equals(venta.getTipoDoc())
                                    ? CajaChicaMovimiento.TipoMovimiento.INGRESO
                                    : CajaChicaMovimiento.TipoMovimiento.EGRESO;
                            String descMov = "07".equals(venta.getTipoDoc())
                                    ? "ANULACION NC: " + venta.getSerie() + "-" + venta.getNumero()
                                    : "ANULACION VENTA: " + venta.getSerie() + "-" + venta.getNumero();
                            String refMov = "07".equals(venta.getTipoDoc())
                                    ? "NC-ANUL-" + venta.getIdVenta()
                                    : "VTA-ANUL-" + venta.getIdVenta();

                            cajaChicaService.registrarMovimiento(MovimientoRequest.builder()
                                    .cajaChicaId(caja.getId())
                                    .tipo(tipoMov)
                                    .monto(venta.getTotal())
                                    .descripcion(descMov)
                                    .referencia(refMov)
                                    .metodoPago(ventaMapper.getMetodoPagoDescripcion(venta))
                                    .build(), String.valueOf(userAlt.getId()));
                        });
            }
        } catch (Exception e) {
            log.error("Error al registrar anulación en caja chica: {}", e.getMessage());
        }

        return ventaMapper.toDto(venta);
    }

    @Override
    public VentaRegistroDTO anularCotizacion(Long idVenta) {
        VentaRegistro venta = ventaRegistroRepository.findByIdVentaWithDetalles(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontróada: " + idVenta));

        if (!EstadoGeneral.COTIZACION.equals(venta.getEstado())) {
            throw new ValidationException("El documento no es una cotización o ya ha cambiado de estado");
        }

        venta.setEstado(EstadoGeneral.ANULADO);
        venta.setFechaAnul(LocalDateTime.now());

        Authentication authInit = SecurityContextHolder.getContext().getAuthentication();
        String loginInit = (authInit != null && authInit.getName() != null) ? authInit.getName() : "SISTEMA";
        datosPersonalesRepository.findByLogin(loginInit).ifPresent(u -> venta.setIdUserAnul(u.getId().intValue()));

        String obsActual = venta.getObservacion() != null ? venta.getObservacion() : "";
        venta.setObservacion(obsActual + " | COTIZACION ANULADA DIRECTAMENTE");

        VentaRegistro saved = ventaRegistroRepository.save(venta);
        return ventaMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaRegistroDTO obtenerVentaPorNumdoc(String numeroDocumento) {
        String[] parts = numeroDocumento.split("-");
        if (parts.length != 2) {
            throw new ValidationException("Formato de documento inválido. Use: SERIE-NUMERO (ej: F001-5041)");
        }

        String serie = parts[0];
        Integer numero = Integer.parseInt(parts[1]);

        VentaRegistro venta = ventaRegistroRepository.findBySerieAndNumero(serie, numero)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Venta no encontróada con documento: " + numeroDocumento));
        return ventaMapper.toDto(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<VentaRegistroDTO> listarVentasPaginado(
            Long idPunto,
            org.springframework.data.domain.Pageable pageable) {
        if (idPunto != null) {
            return ventaRegistroRepository.findByPunto(idPunto, pageable).map(ventaMapper::toDto);
        }
        return ventaRegistroRepository.findAll(pageable).map(ventaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<VentaRegistroDTO> listarVentasPorEstadoPaginado(EstadoGeneral estado,
            Long idPunto,
            org.springframework.data.domain.Pageable pageable) {
        if (idPunto != null) {
            return ventaRegistroRepository.findByEstadoAndPunto(estado, idPunto, pageable).map(ventaMapper::toDto);
        }
        return ventaRegistroRepository.findByEstado(estado, pageable).map(ventaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<VentaRegistroDTO> buscarVentas(String q,
            Long idPunto,
            org.springframework.data.domain.Pageable pageable) {

        return ventaRegistroRepository.search(q, pageable).map(ventaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaRegistroDTO> buscarVentasAvanzado(
            String serie,
            String numero,
            Integer numeroDesde,
            Integer numeroHasta,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String idVendedor,
            String condicionPago,
            EstadoGeneral estado,
            Long puntoId,
            Long sucursalId,
            String tipoDoc,
            Pageable pageable) {

        Specification<VentaRegistro> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (serie != null && !serie.isEmpty()) {
                predicates.add(cb.equal(root.get("serie"), serie));
            }

            if (numero != null && !numero.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("numero"), Integer.parseInt(numero)));
                } catch (NumberFormatException e) {
                    predicates.add(cb.equal(cb.upper(root.get("numdoc")), "%" + numero.toUpperCase() + "%"));
                }
            }

            if (numeroDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("numero"), numeroDesde));
            }

            if (numeroHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("numero"), numeroHasta));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
            }

            if (idVendedor != null && !idVendedor.isEmpty() && !"TODOS".equals(idVendedor)) {

                predicates.add(cb.equal(root.get("idUser"), idVendedor));
            }

            if (condicionPago != null && !condicionPago.isEmpty() && !"TODOS".equals(condicionPago)) {

                if (root.get("metodoPago") != null) {
                    predicates.add(cb.equal(root.get("metodoPago").get("descripcion"), condicionPago));
                }
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (puntoId != null) {
                predicates.add(cb.equal(root.get("punto"), puntoId));
            }

            if (sucursalId != null) {
                predicates.add(cb.equal(root.get("idSucursal"), sucursalId));
            }

            if (tipoDoc != null && !tipoDoc.isEmpty() && !"TODOS".equals(tipoDoc)) {
                if ("OFICIAL".equals(tipoDoc)) {
                    predicates.add(root.get("tipoDoc").in("01", "03", "07", "08", "09", "12"));
                } else if ("FACTURA_BOLETA".equals(tipoDoc)) {
                    predicates.add(root.get("tipoDoc").in("01", "03"));
                } else if ("C".equals(tipoDoc)) {
                    predicates.add(cb.or(
                        cb.equal(root.get("estado"), EstadoGeneral.COTIZACION),
                        cb.and(
                            cb.equal(root.get("estado"), EstadoGeneral.ANULADO),
                            cb.like(root.get("observacion"), "%COTIZACION ANULADA DIRECTAMENTE%")
                        )
                    ));
                } else {
                    predicates.add(cb.equal(root.get("tipoDoc"), tipoDoc));
                }

                if (!"C".equals(tipoDoc)) {
                    predicates.add(cb.not(cb.or(
                        cb.equal(root.get("estado"), EstadoGeneral.COTIZACION),
                        cb.and(
                            cb.equal(root.get("estado"), EstadoGeneral.ANULADO),
                            cb.like(root.get("observacion"), "%COTIZACION ANULADA DIRECTAMENTE%")
                        )
                    )));
                }
            } else {
                // If TODOS or null, we might still want to exclude cotizaciones so they don't pollute general searches
                predicates.add(cb.not(cb.or(
                    cb.equal(root.get("estado"), EstadoGeneral.COTIZACION),
                    cb.and(
                        cb.equal(root.get("estado"), EstadoGeneral.ANULADO),
                        cb.like(root.get("observacion"), "%COTIZACION ANULADA DIRECTAMENTE%")
                    )
                )));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return ventaRegistroRepository.findAll(spec, pageable).map(ventaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorrelatividadDTO> obtenerCorrelatividad(
            LocalDate fechaInicio, LocalDate fechaFin, Long idSucursal, Long puntoId, String tipoDoc) {
        log.info("Obteniendo correlatividad de documentos entre {} y {} (Punto: {}, TipoDoc: {})",
                fechaInicio, fechaFin, puntoId, tipoDoc);

        List<CorrelatividadDTO> listaTotal = new ArrayList<>();

        List<Object[]> resultadosVentas = ventaRegistroRepository.obtenerCorrelatividadRaw(fechaInicio, fechaFin,
                idSucursal, puntoId, tipoDoc);
        listaTotal.addAll(resultadosVentas.stream().map(row -> {
            String tDoc = (String) row[0];
            String serie = (String) row[1];
            Integer desde = (Integer) row[2];
            Integer hasta = (Integer) row[3];
            Long cantidad = (Long) row[4];

            BigDecimal baseImp = row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO;
            BigDecimal igv = row[6] != null ? (BigDecimal) row[6] : BigDecimal.ZERO;
            BigDecimal valorExo = row[7] != null ? (BigDecimal) row[7] : BigDecimal.ZERO;
            BigDecimal valorInaf = row[8] != null ? (BigDecimal) row[8] : BigDecimal.ZERO;
            BigDecimal total = row[9] != null ? (BigDecimal) row[9] : BigDecimal.ZERO;

            String tipoDocDesc = switch (tDoc) {
                case "01" -> "FACTURA";
                case "03" -> "BOLETA";
                case "07" -> "NOTA DE CREDITO";
                case "08" -> "NOTA DE DEBITO";
                default -> "TICKET/OTROS";
            };

            return CorrelatividadDTO.builder()
                    .tipoDoc(tDoc)
                    .serie(serie)
                    .desde(desde)
                    .hasta(hasta)
                    .cantidad(cantidad)
                    .tipoDocDescripcion(tipoDocDesc)
                    .baseImp(baseImp)
                    .igv(igv)
                    .valorExo(valorExo)
                    .valorInaf(valorInaf)
                    .total(total)
                    .build();
        }).collect(Collectors.toList()));

        LocalDateTime start = fechaInicio.atStartOfDay();
        LocalDateTime end = fechaFin.atTime(LocalTime.MAX);

        if (puntoId != null) {
            }

        List<Object[]> resultadosDiversos = movimientoDiversoRepository.obtenerCorrelatividadRaw(start, end, idSucursal);

        listaTotal.addAll(resultadosDiversos.stream().map(row -> {
            String tipoVirtual = (String) row[0];
            String serie = (String) row[1];
            Integer desde = (Integer) row[2];
            Integer hasta = (Integer) row[3];
            Long cantidad = (Long) row[4];
            BigDecimal totalContable = row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO;

            return CorrelatividadDTO.builder()
                    .tipoDoc(tipoVirtual)
                    .serie(serie)
                    .desde(desde)
                    .hasta(hasta)
                    .cantidad(cantidad)
                    .tipoDocDescripcion(tipoVirtual.equals("ID") ? "INGRESO DIVERSO" : "SALIDA DIVERSA")
                    .baseImp(BigDecimal.ZERO)
                    .igv(BigDecimal.ZERO)
                    .valorExo(BigDecimal.ZERO)
                    .valorInaf(totalContable)
                    .total(totalContable)
                    .build();
        }).collect(Collectors.toList()));

        return listaTotal;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaRegistroDTO> obtenerDetalleCorrelatividad(
            LocalDate fechaInicio, LocalDate fechaFin, Long idSucursal, Long puntoId, String tipoDoc, String serie) {
        log.info("Obteniendo detalle de correlatividad de documentos entre {} y {} (Punto: {}, Tipo: {}, Serie: {})",
                fechaInicio, fechaFin, puntoId, tipoDoc, serie);

        if (tipoDoc.equals("ID") || tipoDoc.equals("SD")) {
            LocalDateTime start = fechaInicio.atStartOfDay();
            LocalDateTime end = fechaFin.atTime(LocalTime.MAX);

            if (puntoId != null) {
                }

            List<MovimientoDiverso> movimientos = movimientoDiversoRepository.findDetalleCorrelatividad(
                    start, end, idSucursal, tipoDoc, serie);

            return movimientos.stream().map(m -> {
                BigDecimal total = m.getDetalles().stream()
                        .map(d -> d.getCantidad().multiply(d.getCostoUnitario()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                return VentaRegistroDTO.builder()
                        .serie(m.getSerie())
                        .numero(m.getNumero())
                        .numeroDocumento(m.getSerie() + "-" + String.format("%07d", m.getNumero()))
                        .fecha(m.getFecha().toLocalDate())
                        .total(total)
                        .valorInaf(total)
                        .baseImp(BigDecimal.ZERO)
                        .igv(BigDecimal.ZERO)
                        .idUser(m.getUsuario() != null ? m.getUsuario().getLogin() : "--")
                        .estado(EstadoGeneral.VIGENTE)
                        .build();
            }).collect(Collectors.toList());
        }

        List<VentaRegistro> ventas = ventaRegistroRepository.findDetalleCorrelatividad(fechaInicio, fechaFin, idSucursal, puntoId,
                tipoDoc, serie);
        return ventas.stream().map(ventaMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VentaRegistroDTO realizarDevolucion(com.pe.articulos.modules.venta_registro.dto.DevolucionRequest request) {
        VentaRegistro venta = ventaRegistroRepository.findByIdVentaWithDetalles(request.getIdVenta())
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontróada: " + request.getIdVenta()));

        if (!EstadoGeneral.VIGENTE.equals(venta.getEstado())) {
            throw new ValidationException("No se pueden realizar devoluciones de una venta anulada.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        log.info("Iniciando procesamiento de devolución para Venta ID: {} por Usuario: {}", request.getIdVenta(),
                currentUsername);

        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            log.warn("INTENTO DE DEVOLUCION SIN ITEMS para Venta ID: {}. Request: {}", request.getIdVenta(), request);
            throw new ValidationException(
                    "Debe seleccionar al menos un producto con cantidad mayor a cero para devolver.");
        }

        log.info("Recibidos {} items para procesar en la devolución.", request.getDetalles().size());

        DatosPersonales usuarioActual = datosPersonalesRepository.findByLogin(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontróado: " + currentUsername));

        BigDecimal totalRefund = BigDecimal.ZERO;

        if (request.getDetalles() != null && !request.getDetalles().isEmpty()) {
            for (com.pe.articulos.modules.venta_registro.dto.DevolucionDetalleRequest detReq : request.getDetalles()) {
                VentaDetalle det = venta.getDetalles().stream()
                        .filter(d -> d.getIdMovart().equals(detReq.getIdDetalle()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Detalle de venta no encontróado: " + detReq.getIdDetalle()));

                BigDecimal yaDevuelto = det.getCantidadDevuelta() != null ? det.getCantidadDevuelta() : BigDecimal.ZERO;
                BigDecimal disponible = det.getCantidad().subtract(yaDevuelto);

                if (detReq.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                if (detReq.getCantidad().compareTo(disponible) > 0) {
                    log.error("Error de validación: Cantidad a devolver {} excede disponible {}", detReq.getCantidad(),
                            disponible);
                    throw new ValidationException("La cantidad a devolver (" + detReq.getCantidad()
                            + ") es mayor a la disponible (" + disponible + ") para el item: " + det.getGlosa());
                }

                log.info("Procesando item {}: Cantidad a devolver {}", det.getGlosa(), detReq.getCantidad());

                BigDecimal itemRefund = detReq.getCantidad()
                        .multiply(det.getTotal())
                        .divide(det.getCantidad(), 2, RoundingMode.HALF_UP);

                totalRefund = totalRefund.add(itemRefund);

                det.setCantidadDevuelta(yaDevuelto.add(detReq.getCantidad()));

                det.calcularTotales();

                log.info("Monto a reembolsar por este item: {} | Nuevo Total Item: {}", itemRefund, det.getTotal());

                if (det.getIdArticulo() != null) {
                    try {
                        Long idProducto = Long.valueOf(det.getIdArticulo());
                        Optional<Producto> productoOpt = productoService.obtenerEntidadPorId(idProducto);

                        if (productoOpt.isPresent()) {
                            Producto p = productoOpt.get();

                            Long sucursalId = venta.getIdSucursal();

                            KardexDTO kardexDTO = KardexDTO.builder()
                                    .idAlmacen(p.getIdAlmacen())
                                    .idCatalogo(p.getIdCatalogo())
                                    .idSucursal(sucursalId)
                                    .idUsuario(usuarioActual.getId())
                                    .idDocumento(venta.getTipoDoc()) // O usar un tipo específico para devolución si

                                    .numDoc(venta.getSerie() + "-" + venta.getNumero())
                                    .detalle("DEVOLUCION VENTA " + venta.getSerie() + "-" + venta.getNumero())
                                    .operacion("DEVOLUCION")
                                    .signo("+")
                                    .cantidad(detReq.getCantidad())
                                    .costoUnitario(calcularCostoUnitarioKardex(p, det.getUnidadMedida()))
                                    .observacion("Ingreso por devolución: "
                                            + (request.getMotivo() != null ? request.getMotivo() : ""))
                                    .origenId(venta.getIdVenta().toString())
                                    .origenTipo("VTA_DEV")
                                    .nroLote(p.getNroLote())
                                    .fechaVenc(p.getFechaVencimiento())
                                    .presentacion(det.getUnidadMedida())
                                    .build();

                            log.info("DATOS KARDEX: Producto={}, Sucursal={}, Almacen={}, Cantidad={}, Signo={}",
                                    idProducto, sucursalId, p.getIdAlmacen(), detReq.getCantidad(), "+");
                            productoService.actualizarStockKardex(idProducto, kardexDTO);
                            log.info("Stock actualizado exitosamente en Kardex para producto {}: +{}", idProducto,
                                    detReq.getCantidad());
                        } else {
                            log.warn("Producto ID {} no encontróado en catálogo. No se pudo afectar Kardex.",
                                    idProducto);
                        }
                    } catch (Exception e) {
                        log.error("CRITICAL: Error al retornar stock por devolución para item {}: {}",
                                det.getIdMovart(),
                                e.getMessage(), e);
                    }
                }
            }
        }

        final BigDecimal finalTotalRefund = totalRefund;

        if (finalTotalRefund.compareTo(BigDecimal.ZERO) > 0) {
            log.info("Registrando egreso en caja por monto total de devolución: {}", finalTotalRefund);
            try {
                log.info("Buscando caja abierta para Punto: {} y Usuario ID: {}", venta.getPunto(),
                        usuarioActual.getId());
                Optional<CajaChicaResponse> cajaOpt = cajaChicaService.obtenerCajaAbierta(venta.getPunto(),
                        String.valueOf(usuarioActual.getId()));
                if (cajaOpt.isPresent()) {
                    CajaChicaResponse caja = cajaOpt.get();
                    log.info("Caja encontróada (ID: {}). Registrando egreso de {}...", caja.getId(), finalTotalRefund);
                    cajaChicaService.registrarMovimiento(MovimientoRequest.builder()
                            .cajaChicaId(caja.getId())
                            .tipo(CajaChicaMovimiento.TipoMovimiento.EGRESO)
                            .monto(finalTotalRefund)
                            .descripcion("DEVOLUCION VENTA: " + venta.getSerie() + "-" + venta.getNumero()
                                    + " | " + (request.getMotivo() != null ? request.getMotivo() : ""))
                            .referencia("VTA-DEV-" + venta.getIdVenta())
                            .metodoPago(ventaMapper.getMetodoPagoDescripcion(venta))
                            .build(), String.valueOf(usuarioActual.getId()));
                    log.info("Movimiento de egreso registrado en caja ID: {}", caja.getId());
                } else {
                    log.warn(
                            "ATENCION: No se encontróó caja abierta para el punto {} y usuario {}. El egreso NO se registró.",
                            venta.getPunto(), usuarioActual.getId());
                }
            } catch (Exception e) {
                log.error("Error al registrar devolución en caja chica: {}", e.getMessage(), e);
            }
        }

        venta.calcularTotales();
        log.info("Nuevo total de la venta despues de devolucion: {}", venta.getTotal());
        ventaRegistroRepository.save(venta);
        log.info("Cambios en venta original ID {} guardados exitosamente.", venta.getIdVenta());

        log.info("Generando Nota de Crédito para la devolución de la venta ID {}", venta.getIdVenta());
        String seriePrefix = (venta.getSerie() != null && venta.getSerie().startsWith("F")) ? "F" : "B";
        SerieCorrelativoDto correlativo = puntosDocumentoService.generarCorrelativoConPrefijo(venta.getPunto(), "07",
                Modulo.VENTA, seriePrefix);

        VentaRegistro notaCredito = new VentaRegistro();
        notaCredito.setIdPersonal(usuarioActual.getId());
        notaCredito.setIdPersonalUser(currentUsername);
        notaCredito.setFecha(LocalDate.now());
        notaCredito.setPunto(venta.getPunto());
        notaCredito.setIdSucursal(venta.getIdSucursal());
        notaCredito.setTipoDoc("07");
        notaCredito.setSerie(correlativo.getSerie());
        notaCredito.setNumero(correlativo.getNumero());
        notaCredito.setNumdoc(String.format("%07d", correlativo.getNumero()));
        notaCredito.setTipoPac(venta.getTipoPac());
        notaCredito.setMoneda(venta.getMoneda());
        notaCredito.setTc(venta.getTc());
        notaCredito.setDescuento(BigDecimal.ZERO);
        notaCredito.setIdMedico(venta.getIdMedico());
        notaCredito.setObservacion(request.getMotivo());
        notaCredito.setEstado(EstadoGeneral.VIGENTE);

        notaCredito.setRefDoc(venta.getSerie() + "-" + venta.getNumero());
        notaCredito.setRefMotivo(request.getMotivoSunat() != null ? request.getMotivoSunat() : "07"); // 07 = Devolucion
                                                                                                      // por item por
                                                                                                      // defecto
        notaCredito.setRefObs(request.getMotivo());

        notaCredito.setRuc(venta.getRuc());
        notaCredito.setRazon(venta.getRazon());
        notaCredito.setNroDni(venta.getNroDni());
        notaCredito.setNombrePac(venta.getNombrePac());
        notaCredito.setDireccion(venta.getDireccion());
        notaCredito.setMetodoPago(venta.getMetodoPago());

        List<VentaDetalle> ncDetalles = new ArrayList<>();
        for (com.pe.articulos.modules.venta_registro.dto.DevolucionDetalleRequest detReq : request.getDetalles()) {
            VentaDetalle originalDet = venta.getDetalles().stream()
                    .filter(d -> d.getIdMovart().equals(detReq.getIdDetalle()))
                    .findFirst().orElse(null);

            if (originalDet != null && detReq.getCantidad() != null
                    && detReq.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
                VentaDetalle ncDet = new VentaDetalle();
                ncDet.setIdArticulo(originalDet.getIdArticulo());
                ncDet.setDescripcion(originalDet.getDescripcion());
                ncDet.setCantidad(detReq.getCantidad());
                ncDet.setPrecioUnitario(originalDet.getPrecioUnitario());
                ncDet.setPrecioVenta(originalDet.getPrecioVenta());
                ncDet.setInafecto(originalDet.getInafecto());
                ncDet.setUnidadMedida(originalDet.getUnidadMedida());
                ncDet.calcularTotales();
                ncDet.setVentaRegistro(notaCredito);
                ncDetalles.add(ncDet);
            }
        }

        notaCredito.setDetalles(ncDetalles);
        notaCredito.calcularTotales();
        notaCredito = ventaRegistroRepository.save(notaCredito);

        log.info("Nota de Crédito generada exitosamente: ID {}, Documento {}-{}", notaCredito.getIdVenta(),
                notaCredito.getSerie(), notaCredito.getNumero());

        return ventaMapper.toDto(notaCredito);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportarCorrelatividad(LocalDate fechaInicio,
            LocalDate fechaFin, Long idSucursal, Long puntoId, String tipoDoc) {
        List<CorrelatividadDTO> datos = obtenerCorrelatividad(fechaInicio, fechaFin, idSucursal, puntoId, tipoDoc);

        if (tipoDoc != null && !tipoDoc.isEmpty()) {
            if ("VENTA".equals(tipoDoc)) {
                datos = datos.stream().filter(d -> !List.of("ID", "SD").contains(d.getTipoDoc()))
                        .collect(Collectors.toList());
            } else if ("INGRESO_DIVERSO".equals(tipoDoc) || "ID".equals(tipoDoc)) {
                datos = datos.stream().filter(d -> "ID".equals(d.getTipoDoc())).collect(Collectors.toList());
            } else if ("SALIDA_DIVERSA".equals(tipoDoc) || "SD".equals(tipoDoc)) {
                datos = datos.stream().filter(d -> "SD".equals(d.getTipoDoc())).collect(Collectors.toList());
            }
        }

        return excelExportService.exportarCorrelatividad(fechaInicio, fechaFin, tipoDoc, datos);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] obtenerXmlVenta(Long idVenta) {
        VentaRegistro venta = ventaRegistroRepository.findByIdVentaWithDetalles(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontróada: " + idVenta));

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"\n");
        xml.append("         xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"\n");
        xml.append("         xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">\n");
        xml.append("    <cbc:UBLVersionID>2.1</cbc:UBLVersionID>\n");
        xml.append("    <cbc:CustomizationID>2.0</cbc:CustomizationID>\n");
        xml.append("    <cbc:ID>").append(venta.getSerie()).append("-").append(venta.getNumero()).append("</cbc:ID>\n");
        xml.append("    <cbc:IssueDate>").append(venta.getFecha()).append("</cbc:IssueDate>\n");
        xml.append("    <cbc:InvoiceTypeCode listID=\"0101\">").append(venta.getTipoDoc())
                .append("</cbc:InvoiceTypeCode>\n");
        xml.append("    <cbc:DocumentCurrencyCode>").append(venta.getMoneda()).append("</cbc:DocumentCurrencyCode>\n");

        xml.append("    <cac:AccountingSupplierParty>\n");
        xml.append("        <cac:Party>\n");
        xml.append("            <cac:PartyName><cbc:Name>MI EMPRESA S.A.C.</cbc:Name></cac:PartyName>\n");
        xml.append("            <cac:PartyTaxScheme>\n");
        xml.append("                <cbc:RegistrationName>MI EMPRESA S.A.C.</cbc:RegistrationName>\n");
        xml.append("                <cbc:CompanyID schemeID=\"6\">20123456789</cbc:CompanyID>\n");
        xml.append("                <cac:TaxScheme><cbc:ID>VAT</cbc:ID></cac:TaxScheme>\n");
        xml.append("            </cac:PartyTaxScheme>\n");
        xml.append("        </cac:Party>\n");
        xml.append("    </cac:AccountingSupplierParty>\n");

        xml.append("    <cac:AccountingCustomerParty>\n");
        xml.append("        <cac:Party>\n");
        xml.append("            <cac:PartyTaxScheme>\n");
        xml.append("                <cbc:RegistrationName>")
                .append(venta.getNombrePac() != null ? venta.getNombrePac() : "CLIENTE VARIOS")
                .append("</cbc:RegistrationName>\n");
        xml.append("                <cbc:CompanyID schemeID=\"").append("01".equals(venta.getTipoDni()) ? "1" : "6")
                .append("\">")
                .append(venta.getNroDni() != null ? venta.getNroDni() : "00000000").append("</cbc:CompanyID>\n");
        xml.append("                <cac:TaxScheme><cbc:ID>VAT</cbc:ID></cac:TaxScheme>\n");
        xml.append("            </cac:PartyTaxScheme>\n");
        xml.append("        </cac:Party>\n");
        xml.append("    </cac:AccountingCustomerParty>\n");

        xml.append("    <cac:LegalMonetaryTotal>\n");
        xml.append("        <cbc:LineExtensionAmount currencyID=\"").append(venta.getMoneda()).append("\">")
                .append(venta.getBaseImp()).append("</cbc:LineExtensionAmount>\n");
        xml.append("        <cbc:TaxInclusiveAmount currencyID=\"").append(venta.getMoneda()).append("\">")
                .append(venta.getTotal()).append("</cbc:TaxInclusiveAmount>\n");
        xml.append("        <cbc:PayableAmount currencyID=\"").append(venta.getMoneda()).append("\">")
                .append(venta.getTotal()).append("</cbc:PayableAmount>\n");
        xml.append("    </cac:LegalMonetaryTotal>\n");

        xml.append("</Invoice>");

        return xml.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public String obtenerHtmlVenta(Long idVenta) {
        VentaRegistroDTO venta = obtenerVentaPorId(idVenta);
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>");
        html.append(
                "body { font-family: sans-serif; color: #333; line-height: 1.5; padding: 20px; max-width: 800px; margin: auto; }");
        html.append(
                ".header { text-align: center; border-bottom: 2px solid #eee; padding-bottom: 10px; margin-bottom: 20px; }");
        html.append(".doc-info { display: flex; justify-content: space-between; margin-bottom: 20px; }");
        html.append(".table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
        html.append(".table th, .table td { border: 1px solid #eee; padding: 10px; text-align: left; }");
        html.append(".table th { background-color: #f9f9f9; font-weight: bold; }");
        html.append(".totals { text-align: right; }");
        html.append("</style></head><body>");

        html.append("<div class='header'>");
        html.append("<h1>").append("01".equals(venta.getTipoDoc()) ? "FACTURA ELECTRÓNICA" : "BOLETA ELECTRÓNICA")
                .append("</h1>");
        html.append("<h2>").append(venta.getSerie()).append("-").append(venta.getNumero()).append("</h2>");
        html.append("</div>");

        html.append("<div class='doc-info'>");
        html.append("<div><strong>CLIENTE:</strong> ").append(venta.getNombrePaciente())
                .append("<br><strong>DNI/RUC:</strong> ").append(venta.getNroDni()).append("</div>");
        html.append("<div><strong>FECHA:</strong> ").append(venta.getFecha()).append("<br><strong>MONEDA:</strong> ")
                .append(venta.getMoneda()).append("</div>");
        html.append("</div>");

        html.append(
                "<table class='table'><thead><tr><th>Ítem</th><th>Descripción</th><th>Cantidad</th><th>Precio</th><th>Total</th></tr></thead><tbody>");
        if (venta.getDetalles() != null) {
            for (var det : venta.getDetalles()) {
                html.append("<tr>");
                html.append("<td>").append(det.getItem()).append("</td>");
                html.append("<td>").append(det.getDescripcion()).append("</td>");
                html.append("<td>").append(det.getCantidad()).append("</td>");
                html.append("<td>").append(det.getPrecioUnitario()).append("</td>");
                html.append("<td>").append(det.getTotal()).append("</td>");
                html.append("</tr>");
            }
        }
        html.append("</tbody></table>");

        html.append("<div class='totals'>");
        html.append("<p><strong>OP. GRAVADA:</strong> ").append(venta.getMoneda()).append(" ")
                .append(venta.getBaseImp()).append("</p>");
        html.append("<p><strong>IGV (18%):</strong> ").append(venta.getMoneda()).append(" ").append(venta.getIgv())
                .append("</p>");
        html.append("<h2><strong>TOTAL: ").append(venta.getMoneda()).append(" ").append(venta.getTotal())
                .append("</strong></h2>");
        html.append("</div>");

        html.append("</body></html>");

        return html.toString();
    }

    @Override
    public void enviarPorEmail(Long idVenta, String email, Long idPlantilla) {
        log.info("Iniciando envío real de PDF para venta {} por email a {} usando plantilla {}", idVenta, email,
                idPlantilla);
        VentaRegistroDTO ventaDTO = obtenerVentaPorId(idVenta);

        Map<String, Object> datos = new java.util.HashMap<>();
        datos.put("idVenta", ventaDTO.getIdVenta());
        datos.put("serie", ventaDTO.getSerie());
        datos.put("numero", ventaDTO.getNumero());
        datos.put("fecha", ventaDTO.getFecha());
        datos.put("total", ventaDTO.getTotal());
        datos.put("moneda", ventaDTO.getMoneda());
        datos.put("baseImp", ventaDTO.getBaseImp());
        datos.put("igv", ventaDTO.getIgv());
        datos.put("nombrePac", ventaDTO.getNombrePaciente());
        datos.put("totalLetras", ventaDTO.getTotalLetras());
        datos.put("metodoPago", ventaDTO.getMetodoPago());
        datos.put("idUser", SecurityUtils.getCurrentUserLogin());

        List<Map<String, Object>> detalles = new ArrayList<>();
        for (VentaDetalleDTO d : ventaDTO.getDetalles()) {
            Map<String, Object> det = new HashMap<>();
            det.put("cantidad", d.getCantidad());
            det.put("descripcion", d.getDescripcion());
            det.put("precioUnitario", d.getPrecioUnitario());
            det.put("total", d.getTotal());
            det.put("unidadMedida", d.getUnidadMedida());
            detalles.add(det);
        }
        datos.put("detalles", detalles);

        byte[] pdf = documentoService.generarPdf(idPlantilla, datos);

        String subject = "Su comprobante de compra: " + ventaDTO.getSerie() + "-" + ventaDTO.getNumero();
        String body = "Gracias por su compra.\n\n" +
                "Adjuntamos su comprobante electrónico en formato PDF.\n\n" +
                "Gracias por su preferencia.";

        emailService.sendEmailWithAttachment(email, subject, body, pdf,
                ventaDTO.getSerie() + "-" + ventaDTO.getNumero() + ".pdf");

        log.info("PDF enviado exitosamente por email");
    }

    @Override
    public void enviarPorWhatsApp(Long idVenta, String telefono, Long idPlantilla) {
        log.info("Iniciando envío real de WhatsApp para venta {}", idVenta);

        VentaRegistro ventaEntity = ventaRegistroRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", idVenta));

        String telefonoFinal = telefono;

        if (telefonoFinal == null || telefonoFinal.trim().isEmpty() || "undefined".equals(telefonoFinal)) {
            if (ventaEntity.getPuntoVenta() != null && ventaEntity.getPuntoVenta().getSucursal() != null) {
                telefonoFinal = ventaEntity.getPuntoVenta().getSucursal().getCelular();
                if (telefonoFinal == null || telefonoFinal.isEmpty()) {
                    telefonoFinal = ventaEntity.getPuntoVenta().getSucursal().getTelefono();
                }
            }
        }

        if (telefonoFinal == null || telefonoFinal.isEmpty()) {
            throw new ValidationException("No se encontróó un número de teléfono válido para el envío de WhatsApp");
        }

        VentaRegistroDTO ventaDTO = ventaMapper.toDto(ventaEntity);
        StringBuilder detallesStr = new StringBuilder();
        if (ventaDTO.getDetalles() != null) {
            for (VentaDetalleDTO d : ventaDTO.getDetalles()) {
                detallesStr.append("- ").append(d.getDescripcion())
                        .append(" x").append(d.getCantidad())
                        .append(": ").append(ventaDTO.getMoneda()).append(" ").append(d.getTotal()).append("\n");
            }
        }

        byte[] pdfBytes = null;
        try {
            pdfBytes = obtenerPdfVenta(idVenta, idPlantilla);
        } catch (Exception e) {
            log.error("Error al generar PDF para WhatsApp: {}", e.getMessage());
            throw new RuntimeException("No se pudo generar el documento PDF para enviar por WhatsApp");
        }

        java.util.List<String> params = java.util.Arrays.asList(
                ventaDTO.getNombrePaciente() != null ? ventaDTO.getNombrePaciente() : "Cliente",
                ventaDTO.getMoneda() + " " + ventaDTO.getTotal());
        String fileName = ventaDTO.getSerie() + "-" + ventaDTO.getNumero() + ".pdf";

        whatsappService.sendWhatsAppTemplate(telefonoFinal, "recibo_venta", params, pdfBytes, fileName);
    }

    @Override
    public byte[] obtenerPdfVenta(Long idVenta, Long idPlantilla) {
        log.info("Generando PDF para venta {} con plantilla {}", idVenta, idPlantilla);
        VentaRegistroDTO ventaDTO = obtenerVentaPorId(idVenta);

        Map<String, Object> datos = new HashMap<>();
        datos.put("idVenta", ventaDTO.getIdVenta());
        datos.put("serie", ventaDTO.getSerie());
        datos.put("numero", ventaDTO.getNumero());
        datos.put("fecha", ventaDTO.getFecha());
        datos.put("total", ventaDTO.getTotal());
        datos.put("moneda", ventaDTO.getMoneda());
        datos.put("baseImp", ventaDTO.getBaseImp());
        datos.put("igv", ventaDTO.getIgv());
        datos.put("nombrePac", ventaDTO.getNombrePaciente());
        datos.put("totalLetras", ventaDTO.getTotalLetras());
        datos.put("metodoPago", ventaDTO.getMetodoPago());
        datos.put("idUser", SecurityUtils.getCurrentUserLogin());

        List<Map<String, Object>> detalles = new ArrayList<>();
        for (VentaDetalleDTO d : ventaDTO.getDetalles()) {
            Map<String, Object> det = new HashMap<>();
            det.put("cantidad", d.getCantidad());
            det.put("descripcion", d.getDescripcion());
            det.put("precioUnitario", d.getPrecioUnitario());
            det.put("total", d.getTotal());
            det.put("unidadMedida", d.getUnidadMedida());
            detalles.add(det);
        }
        datos.put("detalles", detalles);

        return documentoService.generarPdf(idPlantilla, datos);
    }

    @Override
    @Transactional
    public VentaRegistroDTO reimprimir(Long idVenta, String ip, String motivo) {
        VentaRegistro venta = ventaRegistroRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontróada"));
        
        com.pe.articulos.modules.venta_registro.entity.VentaReimpresionLog log = new com.pe.articulos.modules.venta_registro.entity.VentaReimpresionLog();
        log.setVenta(venta);
        log.setMotivo(motivo);
        log.setIp(ip);
        
        Long currentUserId = com.pe.articulos.core.security.SecurityUtils.getCurrentUserId();
        String currentUserName = com.pe.articulos.core.security.SecurityUtils.getCurrentUserLogin();
        
        log.setIdUsuario(currentUserId != null ? currentUserId.intValue() : 1);
        log.setNombreUsuario(currentUserName);
        
        log.setFechaReimpresion(LocalDateTime.now());
        reimpresionLogRepository.save(log);
        return ventaMapper.toDto(venta);
    }

    @Override
    public List<com.pe.articulos.modules.venta_registro.dto.VentaReimpresionLogDTO> obtenerHistorialReimpresiones(Long idVenta) {
        List<com.pe.articulos.modules.venta_registro.entity.VentaReimpresionLog> logs = reimpresionLogRepository.findByVenta_IdVentaOrderByFechaReimpresionDesc(idVenta);
        return logs.stream().map(l -> {
            com.pe.articulos.modules.venta_registro.dto.VentaReimpresionLogDTO dto = new com.pe.articulos.modules.venta_registro.dto.VentaReimpresionLogDTO();
            dto.setId(l.getId());
            dto.setMotivo(l.getMotivo());
            dto.setNombreUsuario(l.getNombreUsuario());
            dto.setIp(l.getIp());
            dto.setFechaReimpresion(l.getFechaReimpresion());
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public VentaRegistroDTO generarComprobanteReverso(Long idVenta, String motivo) {
        VentaRegistro venta = ventaRegistroRepository.findByIdVentaWithDetalles(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontróada: " + idVenta));

        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null && auth.getName() != null) ? auth.getName() : "SISTEMA";
        com.pe.articulos.modules.users.entity.DatosPersonales usuarioActual = datosPersonalesRepository.findByLogin(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontróado: " + currentUsername));

        String tipoReverso = "07";
        if ("07".equals(venta.getTipoDoc())) {
            tipoReverso = "08"; // Nota de Debito
        }
        
        String seriePrefix = (venta.getSerie() != null && venta.getSerie().startsWith("F")) ? "F" : "B";
        com.pe.articulos.modules.venta_registro.dto.SerieCorrelativoDto correlativo = puntosDocumentoService.generarCorrelativoConPrefijo(venta.getPunto(), tipoReverso, com.pe.articulos.modules.documentos.entities.Modulo.VENTA, seriePrefix);

        VentaRegistro comprobanteReverso = new VentaRegistro();
        comprobanteReverso.setIdPersonal(usuarioActual.getId());
        comprobanteReverso.setIdPersonalUser(currentUsername);
        comprobanteReverso.setFecha(LocalDate.now());
        comprobanteReverso.setPunto(venta.getPunto());
        comprobanteReverso.setIdSucursal(venta.getIdSucursal());
        comprobanteReverso.setTipoDoc(tipoReverso);
        comprobanteReverso.setSerie(correlativo.getSerie());
        comprobanteReverso.setNumero(correlativo.getNumero());
        comprobanteReverso.setNumdoc(String.format("%07d", correlativo.getNumero()));
        comprobanteReverso.setTipoPac(venta.getTipoPac());
        comprobanteReverso.setMoneda(venta.getMoneda());
        comprobanteReverso.setTc(venta.getTc());
        comprobanteReverso.setDescuento(venta.getDescuento());
        comprobanteReverso.setIdMedico(venta.getIdMedico());
        comprobanteReverso.setObservacion(motivo);
        comprobanteReverso.setEstado(EstadoGeneral.VIGENTE);
        
        comprobanteReverso.setRefDoc(venta.getSerie() + "-" + venta.getNumero());
        comprobanteReverso.setRefMotivo("01"); 
        comprobanteReverso.setRefObs(motivo);
        
        comprobanteReverso.setRuc(venta.getRuc());
        comprobanteReverso.setRazon(venta.getRazon());
        comprobanteReverso.setNroDni(venta.getNroDni());
        comprobanteReverso.setNombrePac(venta.getNombrePac());
        comprobanteReverso.setDireccion(venta.getDireccion());
        comprobanteReverso.setMetodoPago(venta.getMetodoPago());
        
        java.util.List<com.pe.articulos.modules.venta_registro.entity.VentaDetalle> reversoDetalles = new java.util.ArrayList<>();
        for (com.pe.articulos.modules.venta_registro.entity.VentaDetalle originalDet : venta.getDetalles()) {
            com.pe.articulos.modules.venta_registro.entity.VentaDetalle ncDet = new com.pe.articulos.modules.venta_registro.entity.VentaDetalle();
            ncDet.setEstado(EstadoGeneral.ACTIVO);
            ncDet.setIdArticulo(originalDet.getIdArticulo());
            ncDet.setIdCatalogo(originalDet.getIdCatalogo());
            ncDet.setItem(originalDet.getItem());
            ncDet.setGlosa(originalDet.getGlosa());
            ncDet.setDescripcion(originalDet.getDescripcion());
            ncDet.setCantidad(originalDet.getCantidad());
            ncDet.setPrecioUnitario(originalDet.getPrecioUnitario());
            ncDet.setPrecioVenta(originalDet.getPrecioVenta());
            ncDet.setValorUnitario(originalDet.getValorUnitario());
            ncDet.setInafecto(originalDet.getInafecto());
            ncDet.setUnidadMedida(originalDet.getUnidadMedida());
            ncDet.setPorcentajeDescuento(originalDet.getPorcentajeDescuento());
            ncDet.setMontoDescuento(originalDet.getMontoDescuento());
            ncDet.calcularTotales();
            ncDet.setVentaRegistro(comprobanteReverso);
            reversoDetalles.add(ncDet);
        }
        
        comprobanteReverso.setDetalles(reversoDetalles);
        comprobanteReverso.calcularTotales();
        comprobanteReverso = ventaRegistroRepository.save(comprobanteReverso);
        
        com.pe.articulos.modules.productos.entity.MovimientoDiverso mov = new com.pe.articulos.modules.productos.entity.MovimientoDiverso();
        mov.setFecha(java.time.LocalDateTime.now());
        mov.setMotivo(motivo);
        mov.setEstado(EstadoGeneral.ACTIVO);
        mov.setNumDocumento("REV-" + comprobanteReverso.getSerie() + "-" + comprobanteReverso.getNumero());
        
        com.pe.articulos.modules.sucursal.entity.Sucursal sucursal = new com.pe.articulos.modules.sucursal.entity.Sucursal();
        sucursal.setIdSucursal(venta.getIdSucursal());
        mov.setSucursal(sucursal);
        
        com.pe.articulos.modules.users.entity.DatosPersonales usuario = new com.pe.articulos.modules.users.entity.DatosPersonales();
        usuario.setId(usuarioActual.getId());
        mov.setUsuario(usuario);
        
        mov = movimientoDiversoRepository.save(mov);
        
        for (com.pe.articulos.modules.venta_registro.entity.VentaDetalle det : comprobanteReverso.getDetalles()) {
            Long idGral = det.getIdArticulo();
            if (idGral != null) {
                try {
                    java.util.Optional<com.pe.articulos.modules.productos.entity.Producto> productoOpt = productoService.obtenerEntidadPorId(idGral);
                    if (productoOpt.isPresent()) {
                        com.pe.articulos.modules.productos.entity.Producto p = productoOpt.get();
                        
                        String signoKardex = "07".equals(tipoReverso) ? "+" : "-";
                        String operacionKardex = "07".equals(tipoReverso) ? "INGRESO_DIVERSO" : "SALIDA_DIVERSA";
                        
                        com.pe.articulos.modules.productos.entity.MovimientoDiversoDetalle movDet = new com.pe.articulos.modules.productos.entity.MovimientoDiversoDetalle();
                        movDet.setCatalogo(p.getCatalogo());
                        movDet.setCantidad(det.getCantidad());
                        movDet.setCostoUnitario(p.getPrecioCompra() != null ? p.getPrecioCompra() : java.math.BigDecimal.ZERO);
                        movDet.setTipo("07".equals(tipoReverso) ? "INGRESO" : "SALIDA");
                        movDet.setNroLote(p.getNroLote());
                        movDet.setFechaVencimiento(p.getFechaVencimiento());
                        movDet.setObservacion(motivo);
                        movDet.setIdAlmacen(p.getIdAlmacen());
                        
                        mov.addDetalle(movDet);
                        
                        com.pe.articulos.modules.kardex.dto.KardexDTO kardexDTO = com.pe.articulos.modules.kardex.dto.KardexDTO.builder()
                                .idAlmacen(p.getIdAlmacen())
                                .idCatalogo(p.getIdCatalogo())
                                .idSucursal(venta.getIdSucursal())
                                .idUsuario(usuarioActual.getId())
                                .idDocumento(tipoReverso)
                                .numDoc(comprobanteReverso.getSerie() + "-" + comprobanteReverso.getNumero())
                                .detalle("REVERSO: " + comprobanteReverso.getSerie() + "-" + comprobanteReverso.getNumero())
                                .operacion(operacionKardex)
                                .signo(signoKardex)
                                .cantidad(det.getCantidad())
                                .costoUnitario(calcularCostoUnitarioKardex(p, det.getUnidadMedida()))
                                .observacion(motivo)
                                .origenId(comprobanteReverso.getIdVenta().toString())
                                .origenTipo("07".equals(tipoReverso) ? "NC" : "ND")
                                .nroLote(p.getNroLote())
                                .fechaVenc(p.getFechaVencimiento())
                                .presentacion(det.getUnidadMedida())
                                .build();
                        productoService.actualizarStockKardex(idGral, kardexDTO);
                    }
                } catch (Exception e) {}
            }
        }
        movimientoDiversoRepository.save(mov);
        
        return ventaMapper.toDto(comprobanteReverso);
    }


    private java.math.BigDecimal calcularCostoUnitarioKardex(com.pe.articulos.modules.productos.entity.Producto p, String unidadMedida) {
        java.math.BigDecimal costo = p.getPrecioCompra() != null ? p.getPrecioCompra() : java.math.BigDecimal.ZERO;
        String um = unidadMedida != null ? unidadMedida.toUpperCase() : "UND";
        if (um.contains("CAJA") || um.equals("CJA")) {
            int factor = (p.getFactorCaja() != null && p.getFactorCaja() > 0) ? p.getFactorCaja() : 1;
            if (p.getManejaBlister() != null && p.getManejaBlister() && p.getFactorBlister() != null && p.getFactorBlister() > 0) {
                factor *= p.getFactorBlister();
            }
            return costo.multiply(java.math.BigDecimal.valueOf(factor));
        } else if (um.contains("BLISTER") || um.equals("BLI") || um.contains("BLÍSTER")) {
            int factor = (p.getFactorBlister() != null && p.getFactorBlister() > 0) ? p.getFactorBlister() : 1;
            return costo.multiply(java.math.BigDecimal.valueOf(factor));
        }
        return costo;
    }

}