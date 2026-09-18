package com.pe.articulos.modules.compras.service.impl;

import com.pe.articulos.modules.compras.dto.OrdenCreateRequest;
import com.pe.articulos.modules.compras.dto.CompraRequest;
import com.pe.articulos.modules.compras.dto.CompraResponse;
import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.modules.compras.entity.DetalleCompra;
import com.pe.articulos.modules.compras.repository.DetalleCompraRepository;
import com.pe.articulos.modules.compras.repository.CompraRepository;
import com.pe.articulos.modules.compras.service.CompraService;
import com.pe.articulos.modules.compras.mapper.CompraMapper;
import com.pe.articulos.modules.compras.mapper.DetalleCompraMapper;
import com.pe.articulos.modules.compras.dto.DetalleCompraResponse;
import com.pe.articulos.modules.proveedores.entity.Proveedor;
import com.pe.articulos.modules.proveedores.repository.ProveedorRepository;
import com.pe.articulos.modules.proveedores.service.CuentaProveedorService;
import com.pe.articulos.core.enums.EstadoGeneral;
import jakarta.transaction.Transactional;
import com.pe.articulos.modules.finanzas.FlujoAprobacionService;
import com.pe.articulos.modules.aprobaciones.service.SolicitudAnulacionService;
import com.pe.articulos.modules.aprobaciones.entity.SolicitudesAnulacion.TipoSolicitud;
import com.pe.articulos.modules.kardex.service.KardexService;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.almacen.repository.AlmacenRepository;
import lombok.RequiredArgsConstructor;
import com.pe.articulos.core.shared.dto.PageResponse;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CompraServiceImpl implements CompraService {

    private final FlujoAprobacionService flujoAprobacionService;
    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProveedorRepository proveedorRepository;

    private final CompraMapper compraMapper;
    private final DetalleCompraMapper detalleCompraMapper;
    private final CuentaProveedorService cuentaProveedorService;
    private final SolicitudAnulacionService solicitudAnulacionService;
    private final KardexService kardexService;
    private final AlmacenRepository almacenRepository;

    @Override
    @Transactional
    public CompraResponse registrar(CompraRequest request, String userId) {

        // 1. Convertir DTO a Entidad
        Compra compra = compraMapper.toEntity(request);
        compra.setIdSucursal(request.getIdSucursal()); // Setear Sucursal

        // 2. Asociar Proveedor (Validar existencia)
        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        compra.setProveedor(proveedor);
        compra.setEstado(EstadoGeneral.REGISTRADO);

        // 3. Procesar Detalles y Calcular Totales
        calcularTotalesYCompletarDetalles(compra, Boolean.TRUE.equals(request.getIsAjusteManual()));

        // Si es manual, nos aseguramos de tomar los valores del request si vienen
        if (Boolean.TRUE.equals(request.getIsAjusteManual())) {
            if (request.getValorVentaGravado() != null)
                compra.setValorVentaGravado(request.getValorVentaGravado());
            if (request.getValorVentaExonerado() != null)
                compra.setValorVentaExonerado(request.getValorVentaExonerado());
            if (request.getValorVentaInafecto() != null)
                compra.setValorVentaInafecto(request.getValorVentaInafecto());
            if (request.getIgv() != null)
                compra.setIgv(request.getIgv());
            // Re-ejecutamos cálculo para asegurar consistencia de total/subtotal
            calcularTotalesYCompletarDetalles(compra, true);
        }

        // 4. Guardar Cabecera y Detalles (CascadeType.ALL)
        Compra compraGuardada = compraRepository.save(compra);

        // 5. Gestionar Deuda y Pagos en Cuenta Proveedor
        gestionarCuentaProveedor(compraGuardada, request, userId);

        if (Boolean.TRUE.equals(request.getSolicitarFondo())) {
            flujoAprobacionService.solicitarPagoCompra(compraGuardada, userId);
        }

        // 6. Registrar en Kardex
        almacenRepository.findFirstBySucursalIdSucursal(compraGuardada.getIdSucursal()).ifPresent(almacen -> {
            if (compraGuardada.getDetalles() != null) {
                for (DetalleCompra detalle : compraGuardada.getDetalles()) {
                    if (detalle.getCantidad() != null && detalle.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal cantidadBase = detalle.getCantidad();
                        BigDecimal costoBase = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : BigDecimal.ZERO;

                        // Aplicar factor si existe (para compras en CAJA / BLISTER)
                        if (detalle.getFactorConversion() != null && detalle.getFactorConversion() > 1) {
                            cantidadBase = cantidadBase.multiply(BigDecimal.valueOf(detalle.getFactorConversion()));
                            costoBase = costoBase.divide(BigDecimal.valueOf(detalle.getFactorConversion()), 4, RoundingMode.HALF_UP);
                        }

                        KardexDTO kardexDTO = KardexDTO.builder()
                                .idAlmacen(almacen.getId())
                                .idCatalogo(detalle.getProducto().getId())
                                .idSucursal(compraGuardada.getIdSucursal())
                                .idUsuario(userId != null ? Long.parseLong(userId) : 1L)
                                .idDocumento(compraGuardada.getTipoComprobante() != null ? compraGuardada.getTipoComprobante() : "00")
                                .numDoc(compraGuardada.getSerie() + "-" + compraGuardada.getCorrelativo())
                                .detalle("COMPRA " + compraGuardada.getSerie() + "-" + compraGuardada.getCorrelativo())
                                .operacion("COMPRA")
                                .signo("+")
                                .cantidad(cantidadBase)
                                .costoUnitario(costoBase)
                                .observacion("Ingreso de mercaderia por compra")
                                .origenId(compraGuardada.getId().toString())
                                .origenTipo("CPA")
                                .nroLote(detalle.getLote() != null ? detalle.getLote() : "S/L")
                                .fechaVenc(detalle.getFechaVencimiento())
                                .presentacion(detalle.getPresentacion() != null ? detalle.getPresentacion() : "UND")
                                .build();
                        kardexService.registrarMovimiento(kardexDTO);
                    }
                }
            }
        });

        return compraMapper.toResponse(compraGuardada);
    }

    @Override
    @Transactional
    public CompraResponse crearOrden(OrdenCreateRequest request, String userId) {
        // 1. Crear Entidad con valores por defecto
        Compra compra = new Compra();
        compra.setIdSucursal(request.getIdSucursal());
        compra.setNombreGrupo(request.getNombreGrupo());
        compra.setEstado(EstadoGeneral.REGISTRADO);

        // 2. Asociar Proveedor
        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        compra.setProveedor(proveedor);

        // 3. Mapear Detalles
        if (request.getDetalles() != null) {
            List<DetalleCompra> detalles = detalleCompraMapper.toEntityList(request.getDetalles());
            detalles.forEach(d -> d.setCompra(compra));
            compra.setDetalles(detalles);
        }

        // 4. Calcular Totales
        boolean isAjusteManual = request.getValorVentaGravado() != null;
        if (isAjusteManual) {
            compra.setValorVentaGravado(request.getValorVentaGravado());
            compra.setValorVentaExonerado(request.getValorVentaExonerado());
            compra.setValorVentaInafecto(request.getValorVentaInafecto());
            compra.setIgv(request.getIgv());
        }

        calcularTotalesYCompletarDetalles(compra, isAjusteManual);

        // 5. Guardar
        Compra compraGuardada = compraRepository.save(compra);

        return compraMapper.toResponse(compraGuardada);
    }

    @Override
    @Transactional
    public CompraResponse actualizarOrden(Long id, OrdenCreateRequest request, String userId) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        compra.setProveedor(proveedor);

        compra.setNombreGrupo(request.getNombreGrupo());

        compra.getDetalles().clear();
        if (request.getDetalles() != null) {
            List<DetalleCompra> nuevosDetalles = detalleCompraMapper.toEntityList(request.getDetalles());
            nuevosDetalles.forEach(d -> d.setCompra(compra));
            compra.getDetalles().addAll(nuevosDetalles);
        }

        boolean isAjusteManual = request.getValorVentaGravado() != null;
        if (isAjusteManual) {
            compra.setValorVentaGravado(request.getValorVentaGravado());
            compra.setValorVentaExonerado(request.getValorVentaExonerado());
            compra.setValorVentaInafecto(request.getValorVentaInafecto());
            compra.setIgv(request.getIgv());
        }

        calcularTotalesYCompletarDetalles(compra, isAjusteManual);

        Compra compraGuardada = compraRepository.save(compra);

        return compraMapper.toResponse(compraGuardada);
    }

    private void gestionarCuentaProveedor(Compra compra, CompraRequest request, String userId) {
        String descripcion = String.format("COMPRA: %s %s-%s",
                compra.getTipoComprobante(),
                compra.getSerie(),
                compra.getCorrelativo());

        // 1. Registrar CARGO por el total de la compra
        cuentaProveedorService.registrarMovimiento(
                compra.getProveedor().getId(),
                "CARGO",
                compra.getTotalPagar(),
                descripcion,
                userId,
                compra.getIdSucursal(),
                false,
                null);

        // 2. El registro del ABONO (pago) se realizará cuando Kafka procese el pago.
    }

    @Override
    @Transactional
    public CompraResponse actualizar(Long id, CompraRequest request, String userId) {
        Compra compraExistente = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        // Actualizar datos básicos
        compraExistente.setFechaEmision(request.getFechaEmision());
        compraExistente.setTipoComprobante(request.getTipoComprobante());
        compraExistente.setSerie(request.getSerie());
        compraExistente.setPercepcion(request.getPercepcion());
        compraExistente.setAjusteRedondeo(request.getAjusteRedondeo());
        compraExistente.setSolicitarFondo(request.getSolicitarFondo());

        // Soporte para ajuste manual de totales
        if (Boolean.TRUE.equals(request.getIsAjusteManual())) {
            if (request.getValorVentaGravado() != null)
                compraExistente.setValorVentaGravado(request.getValorVentaGravado());
            if (request.getValorVentaExonerado() != null)
                compraExistente.setValorVentaExonerado(request.getValorVentaExonerado());
            if (request.getValorVentaInafecto() != null)
                compraExistente.setValorVentaInafecto(request.getValorVentaInafecto());
            if (request.getIgv() != null)
                compraExistente.setIgv(request.getIgv());
            // subtotal y total se calcularán en calcularTotalesYCompletarDetalles
        }

        compraExistente.setIdSucursal(request.getIdSucursal());

        // Actualizar proveedor
        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        compraExistente.setProveedor(proveedor);

        // Actualizar detalles
        if (compraExistente.getDetalles() != null) {
            compraExistente.getDetalles().clear();
        }

        // Re-mapear detalles desde el request
        Compra temp = compraMapper.toEntity(request);
        if (temp.getDetalles() != null) {
            temp.getDetalles().forEach(d -> {
                d.setCompra(compraExistente);
                compraExistente.getDetalles().add(d);
            });
        }

        calcularTotalesYCompletarDetalles(compraExistente, Boolean.TRUE.equals(request.getIsAjusteManual()));

        // Nota: El estado se maneja independientemente ahora (ej. mediante Kafka/Caja
        // General).
        // No auto-promover a PAGADO ni gestionar deuda aquí, ya que eso se maneja
        // al solicitar el pago o al procesarlo en Caja.

        Compra compraGuardada = compraRepository.save(compraExistente);

        if (Boolean.TRUE.equals(request.getSolicitarFondo())) {
            flujoAprobacionService.solicitarPagoCompra(compraGuardada, userId);
        }

        return compraMapper.toResponse(compraGuardada);
    }

    @Override
    public CompraResponse obtener(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        return compraMapper.toResponse(compra);
    }

    @Override
    public PageResponse<CompraResponse> listar(Long idSucursal, String estado, Pageable pageable) {
        Page<Compra> compras;
        if (estado != null) {
            compras = compraRepository.buscar(idSucursal, null, null, null,
                    com.pe.articulos.core.enums.EstadoGeneral.fromCodigo(estado), pageable);
        } else {
            compras = compraRepository.listarComprasFinales(idSucursal, pageable);
        }
        return new PageResponse<>(
                compras.getContent().stream().map(compraMapper::toResponse).collect(Collectors.toList()),
                compras.getTotalElements(),
                compras.getTotalPages(),
                compras.getSize(),
                compras.getNumber());
    }

    @Override
    public PageResponse<CompraResponse> listarOrdenes(Long idSucursal, Pageable pageable) {
        return listar(idSucursal, "REGISTRADO", pageable);
    }

    @Override
    public PageResponse<CompraResponse> buscar(Long idSucursal, Long idProveedor, LocalDate fechaInicio,
            LocalDate fechaFin,
            String estado, Pageable pageable) {
        com.pe.articulos.core.enums.EstadoGeneral estadoEnum = (estado != null)
                ? com.pe.articulos.core.enums.EstadoGeneral.fromCodigo(estado)
                : null;
        Page<Compra> compras = compraRepository.buscar(idSucursal, idProveedor, fechaInicio, fechaFin, estadoEnum,
                pageable);
        return new PageResponse<>(
                compras.getContent().stream().map(compraMapper::toResponse).collect(Collectors.toList()),
                compras.getTotalElements(),
                compras.getTotalPages(),
                compras.getSize(),
                compras.getNumber());
    }

    @Override
    @Transactional
    public void solicitarAnulacion(Long id, String motivo) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        compra.setEstado(EstadoGeneral.PENDIENTE_ANULACION);

        // Registro de auditoría
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String currentUserName = (auth != null) ? auth.getName() : "SISTEMA";
        compra.setIdUserAnul(currentUserName);
        compra.setFechaAnulacion(java.time.LocalDateTime.now());

        compraRepository.save(compra);

        // Crear la solicitud de anulación
        solicitudAnulacionService.solicitar(TipoSolicitud.COMPRA, id, motivo != null ? motivo : "Sin motivo");

    }

    @Override
    @Transactional
    public void confirmarAnulacion(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        compra.setEstado(EstadoGeneral.ANULADO);
        compraRepository.save(compra);
    }

    @Override
    public PageResponse<DetalleCompraResponse> buscarDetallesPorProducto(String term, Long idSucursal,
            Pageable pageable) {
        Page<DetalleCompra> detalles = detalleCompraRepository.buscarPorProducto(term, idSucursal, pageable);
        return new PageResponse<>(
                detalles.getContent().stream().map(detalleCompraMapper::toResponse).collect(Collectors.toList()),
                detalles.getTotalElements(),
                detalles.getTotalPages(),
                detalles.getSize(),
                detalles.getNumber());
    }

    @Override
    public java.math.BigDecimal obtenerPrecioMaximo(Long idCatalogo) {
        java.math.BigDecimal max = detalleCompraRepository.obtenerPrecioMaximo(idCatalogo);
        return max != null ? max : java.math.BigDecimal.ZERO;
    }

    @Override
    public PageResponse<DetalleCompraResponse> obtenerHistorialPorProducto(Long idCatalogo, Pageable pageable) {
        Page<DetalleCompra> detalles = detalleCompraRepository.findLatestByProducto(idCatalogo, pageable);
        return new PageResponse<>(
                detalles.getContent().stream().map(detalleCompraMapper::toResponse).collect(Collectors.toList()),
                detalles.getTotalElements(),
                detalles.getTotalPages(),
                detalles.getSize(),
                detalles.getNumber());
    }

    @Override
    public PageResponse<String> listarGrupos(Long idSucursal, Pageable pageable) {
        Page<String> grupos = compraRepository.findUniqueGroupNames(idSucursal, pageable);
        return new PageResponse<>(
                grupos.getContent(),
                grupos.getTotalElements(),
                grupos.getTotalPages(),
                grupos.getSize(),
                grupos.getNumber());
    }

    @Override
    public PageResponse<DetalleCompraResponse> obtenerProductosPorGrupo(String nombreGrupo, Long idSucursal,
            Pageable pageable) {
        // Buscamos la primera compra del grupo para obtener su "requerimiento"
        // (productos y cantidades)
        Page<Compra> grupo = compraRepository.findByNombreGrupoAndIdSucursal(nombreGrupo, idSucursal, pageable);

        if (grupo.isEmpty()) {
            return new PageResponse<>(List.of(), 0, 0, 0, 0);
        }

        // Tomamos los detalles de la primera orden del grupo como referencia
        List<DetalleCompra> detalles = grupo.getContent().get(0).getDetalles();
        List<DetalleCompraResponse> response = detalleCompraMapper.toResponseList(detalles);

        return new PageResponse<>(response, response.size(), 1, response.size(), 0);
    }

    @Override
    public PageResponse<Long> obtenerProveedoresPorGrupo(String nombreGrupo, Long idSucursal, Pageable pageable) {
        Page<Compra> grupo = compraRepository.findByNombreGrupoAndIdSucursal(nombreGrupo, idSucursal, pageable);

        List<Long> proveedoresIds = grupo.getContent().stream()
                .filter(c -> c.getProveedor() != null)
                .map(c -> c.getProveedor().getId())
                .distinct()
                .toList();

        return new PageResponse<>(proveedoresIds, proveedoresIds.size(), 1, proveedoresIds.size(), 0);
    }

    private void calcularTotalesYCompletarDetalles(Compra compra, boolean isAjusteManual) {
        BigDecimal totalValorVentaGravado = BigDecimal.ZERO;
        BigDecimal totalValorVentaExonerado = BigDecimal.ZERO;
        BigDecimal totalValorVentaInafecto = BigDecimal.ZERO;
        BigDecimal totalIgv = BigDecimal.ZERO;
        BigDecimal totalIgvDescuento = BigDecimal.ZERO;

        if (compra.getDetalles() == null || compra.getDetalles().isEmpty()) {
            if (!isAjusteManual) {
                compra.setValorVentaGravado(BigDecimal.ZERO);
                compra.setValorVentaExonerado(BigDecimal.ZERO);
                compra.setValorVentaInafecto(BigDecimal.ZERO);
                compra.setIgv(BigDecimal.ZERO);
                compra.setIgvDescuento(BigDecimal.ZERO);
                compra.setSubtotal(BigDecimal.ZERO);
                compra.setTotal(BigDecimal.ZERO);
                compra.setTotalPagar(BigDecimal.ZERO);
            }
            return;
        }

        for (DetalleCompra detalle : compra.getDetalles()) {
            detalle.setCompra(compra);

            BigDecimal cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ZERO;
            BigDecimal precio = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : BigDecimal.ZERO;

            // Descuentos
            BigDecimal d1 = detalle.getPorcentajeDescuento() != null ? detalle.getPorcentajeDescuento()
                    : BigDecimal.ZERO;
            BigDecimal d2 = detalle.getPorcentajeDescuento2() != null ? detalle.getPorcentajeDescuento2()
                    : BigDecimal.ZERO;

            BigDecimal valorBruto = cantidad.multiply(precio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal montoDescuento1 = valorBruto.multiply(d1.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal valorTrasD1 = valorBruto.subtract(montoDescuento1);
            BigDecimal montoDescuento2 = valorTrasD1.multiply(d2.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal valorNeto = valorTrasD1.subtract(montoDescuento2).setScale(2, RoundingMode.HALF_UP);

            // Determinar tipo de afectación de forma robusta
            String tipo = detalle.getTipoAfectacion() != null ? detalle.getTipoAfectacion().toUpperCase() : "GRAVADO";

            // Códigos SUNAT comunes: 10 (Gravado), 20 (Exonerado), 30 (Inafecto), 40
            // (Exportación)
            boolean isGravado = tipo.contains("GRAVADO") || tipo.equals("10");
            boolean isExonerado = tipo.contains("EXONERADO") || tipo.equals("20");
            boolean isInafecto = tipo.contains("INAFECTO") || tipo.equals("30") || tipo.equals("40");

            detalle.setBaseImp(BigDecimal.ZERO);
            detalle.setIgv(BigDecimal.ZERO);
            detalle.setValorExo(BigDecimal.ZERO);
            detalle.setValorInaf(BigDecimal.ZERO);
            detalle.setIgvDescuento(BigDecimal.ZERO);

            if (Boolean.TRUE.equals(detalle.getEsBonificacion())) {
                detalle.setValorVenta(BigDecimal.ZERO);
            } else {
                detalle.setValorVenta(valorNeto);
                if (isGravado) {
                    BigDecimal igvLinea = valorNeto.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
                    detalle.setBaseImp(valorNeto);
                    detalle.setIgv(igvLinea);
                    detalle.setTipoAfectacion("10"); // Normalizar a código SUNAT
                    totalValorVentaGravado = totalValorVentaGravado.add(valorNeto);
                    totalIgv = totalIgv.add(igvLinea);

                    BigDecimal igvDescuento = (valorBruto.subtract(valorNeto)).multiply(new BigDecimal("0.18"))
                            .setScale(2, RoundingMode.HALF_UP);
                    detalle.setIgvDescuento(igvDescuento);
                    totalIgvDescuento = totalIgvDescuento.add(igvDescuento);
                } else if (isExonerado) {
                    detalle.setValorExo(valorNeto);
                    detalle.setTipoAfectacion("20");
                    totalValorVentaExonerado = totalValorVentaExonerado.add(valorNeto);
                } else if (isInafecto) {
                    detalle.setValorInaf(valorNeto);
                    detalle.setTipoAfectacion("30");
                    totalValorVentaInafecto = totalValorVentaInafecto.add(valorNeto);
                } else {
                    // Default a Gravado si no se reconoce nada
                    detalle.setBaseImp(valorNeto);
                    detalle.setTipoAfectacion("10");
                    totalValorVentaGravado = totalValorVentaGravado.add(valorNeto);
                }
            }
        }

        if (!isAjusteManual) {
            compra.setValorVentaGravado(totalValorVentaGravado);
            compra.setValorVentaExonerado(totalValorVentaExonerado);
            compra.setValorVentaInafecto(totalValorVentaInafecto);
            compra.setIgv(totalIgv);
            compra.setIgvDescuento(totalIgvDescuento);
        }

        BigDecimal gravado = compra.getValorVentaGravado() != null ? compra.getValorVentaGravado() : BigDecimal.ZERO;
        BigDecimal exonerado = compra.getValorVentaExonerado() != null ? compra.getValorVentaExonerado()
                : BigDecimal.ZERO;
        BigDecimal inafecto = compra.getValorVentaInafecto() != null ? compra.getValorVentaInafecto() : BigDecimal.ZERO;
        BigDecimal igv = compra.getIgv() != null ? compra.getIgv() : BigDecimal.ZERO;
        BigDecimal ajuste = compra.getAjusteRedondeo() != null ? compra.getAjusteRedondeo() : BigDecimal.ZERO;

        // El subtotal es la suma de todas las bases antes de impuestos
        BigDecimal subtotal = gravado.add(exonerado).add(inafecto).setScale(2, RoundingMode.HALF_UP);
        // El total del documento incluye el subtotal + IGV
        BigDecimal totalDoc = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);

        compra.setSubtotal(subtotal);
        compra.setTotal(totalDoc);

        BigDecimal percepcionPerc = compra.getPercepcion() != null ? compra.getPercepcion() : BigDecimal.ZERO;
        BigDecimal percepcionMonto = totalDoc
                .multiply(percepcionPerc.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);

        compra.setTotalPagar(totalDoc.add(percepcionMonto).add(ajuste).setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    @Transactional
    public CompraResponse seleccionarGanadora(Long idCompra, String nombreGrupo) {
        Compra ganadora = compraRepository.findById(idCompra)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        Long idSucursal = ganadora.getIdSucursal();

        // 1. Actualizar la ganadora a VALIDADO (Orden aprobada lista para registrar
        // factura)
        ganadora.setEstado(EstadoGeneral.VALIDADO);
        compraRepository.save(ganadora);

        // 2. Buscar todas las órdenes del mismo grupo y sucursal
        Page<Compra> grupo = compraRepository.findByNombreGrupoAndIdSucursal(nombreGrupo, idSucursal,
                PageRequest.of(0, 100));

        // 3. Cancelar todas las demás que estén en estado REGISTRADO
        for (Compra c : grupo.getContent()) {
            if (!c.getId().equals(idCompra) && c.getEstado() == EstadoGeneral.REGISTRADO) {
                c.setEstado(EstadoGeneral.CANCELADO);
                compraRepository.save(c);
            }
        }

        return compraMapper.toResponse(ganadora);
    }
}
