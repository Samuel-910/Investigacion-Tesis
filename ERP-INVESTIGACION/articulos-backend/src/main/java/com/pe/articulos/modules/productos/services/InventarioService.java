package com.pe.articulos.modules.productos.services;

import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.catalogo.repository.CatalogoRepository;
import com.pe.articulos.modules.productos.entity.ClasificacionMovimiento;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.productos.dto.AjusteInventarioDetalleRequest;
import com.pe.articulos.modules.productos.dto.AjusteInventarioRequest;
import com.pe.articulos.modules.productos.dto.MovimientoDiversoBatchRequest;
import com.pe.articulos.modules.productos.dto.MovimientoDiversoDetalleRequest;
import com.pe.articulos.modules.productos.dto.ClasificacionMovimientoResponse;
import com.pe.articulos.modules.productos.dto.MovimientoDiversoResponse;
import com.pe.articulos.modules.productos.dto.MovimientoDiversoDetalleResponse;
import com.pe.articulos.modules.productos.dto.AjusteInventarioResponse;
import com.pe.articulos.modules.productos.entity.AjusteInventario;
import com.pe.articulos.modules.productos.entity.AjusteInventarioDetalle;
import com.pe.articulos.modules.productos.entity.MovimientoDiverso;
import com.pe.articulos.modules.productos.entity.MovimientoDiversoDetalle;
import com.pe.articulos.modules.productos.entity.Producto;
import com.pe.articulos.modules.productos.mapper.InventarioMapper;
import com.pe.articulos.modules.productos.repository.AjusteInventarioRepository;
import com.pe.articulos.modules.productos.repository.ClasificacionMovimientoRepository;
import com.pe.articulos.modules.productos.repository.MovimientoDiversoRepository;
import com.pe.articulos.modules.productos.repository.ProductoRepository;
import com.pe.articulos.modules.sucursal.repository.SucursalRepository;
import com.pe.articulos.modules.puntos.repository.PuntoDocumentoRepository;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.shared.dto.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class InventarioService {

        private final ProductoService productoService;
        private final ProductoRepository productoRepository;
        private final AjusteInventarioRepository ajusteRepository;
        private final CatalogoRepository catalogoRepository;
        private final ClasificacionMovimientoRepository clasificacionRepository;
        private final MovimientoDiversoRepository movimientoDiversoRepository;
        private final SucursalRepository sucursalRepository;
        private final DatosPersonalesRepository usuarioRepository;
        private final InventarioMapper inventarioMapper;

        @Autowired
        private PuntoDocumentoRepository puntoDocumentoRepository;

        @Transactional(readOnly = true)
        public PageResponse<AjusteInventarioResponse> listarAjustes(Integer idSucursal, Pageable pageable) {
                Page<AjusteInventario> result = ajusteRepository.findByIdSucursal(idSucursal, pageable);
                return PageResponse.fromPage(result.map(inventarioMapper::toDto));
        }

        @Transactional
        public void registrarAjusteAgrupado(AjusteInventarioRequest request) {
                AjusteInventario ajuste = AjusteInventario.builder()
                                .idSucursal(request.getIdSucursal())
                                .tipo(request.getTipo())
                                .motivo(request.getMotivo())
                                .idUsuario(request.getIdUsuario())
                                .correlativo(generarCorrelativo(request.getTipo()))
                                .detalles(new ArrayList<>())
                                .build();

                if (request.getIdClasificacion() != null) {
                        ajuste.setClasificacion(clasificacionRepository.findById(request.getIdClasificacion())
                                        .orElse(null));
                }

                String signo = "INGRESO".equals(request.getTipo()) ? "+" : "-";
                String operacion = "INGRESO".equals(request.getTipo()) ? "INGRESO_DIVERSO" : "SALIDA_DIVERSA";

                for (AjusteInventarioDetalleRequest detReq : request.getDetalles()) {
                        Catalogo catalogo = catalogoRepository.findById(detReq.getIdCatalogo())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Catalogo no encontrado: " + detReq.getIdCatalogo()));

                        Producto producto = productoRepository
                                        .findByIdProductoAndIdSucursal(detReq.getIdCatalogo(), request.getIdSucursal())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Producto no registrado en esta sucursal: "
                                                                        + detReq.getIdCatalogo()));

                        AjusteInventarioDetalle detalle = AjusteInventarioDetalle.builder()
                                        .ajuste(ajuste)
                                        .producto(catalogo)
                                        .cantidad(detReq.getCantidad())
                                        .costoUnitario(detReq.getCostoUnitario() != null ? detReq.getCostoUnitario()
                                                        : (producto.getPrecioCompra() != null
                                                                        ? producto.getPrecioCompra()
                                                                        : BigDecimal.ZERO))
                                        .build();

                        ajuste.getDetalles().add(detalle);

                        // Registrar en Kardex y actualizar Stock
                        KardexDTO kardexDTO = KardexDTO.builder()
                                        .idAlmacen(producto.getIdAlmacen())
                                        .idCatalogo(catalogo.getId())
                                        .idSucursal(request.getIdSucursal())
                                        .idUsuario(request.getIdUsuario())
                                        .idDocumento("00") // Interno
                                        .numDoc("AJU-" + System.currentTimeMillis())
                                        .detalle(request.getMotivo())
                                        .operacion(operacion)
                                        .signo(signo)
                                        .cantidad(detReq.getCantidad())
                                        .costoUnitario(detalle.getCostoUnitario())
                                        .observacion("Ajuste manual agrupado")
                                        .origenId("0")
                                        .origenTipo("AJU")
                                        .nroLote(producto.getNroLote())
                                        .fechaVenc(producto.getFechaVencimiento())
                                        .presentacion(catalogo.getPresentacion() != null ? catalogo.getPresentacion()
                                                        : "UND")
                                        .build();

                        productoService.actualizarStockKardex(producto.getIdProducto(), kardexDTO);
                }

                ajusteRepository.save(ajuste);
        }

        @Transactional
        public void registrarIngresoDiverso(Long idCatalogo, Long idSucursal, Long idAlmacen,
                        BigDecimal cantidad, BigDecimal costo, String nroLote, java.time.LocalDate fechaVenc,
                        String motivo, Long idUsuario, Long idClasificacion) {

                Catalogo catalogo = catalogoRepository.findById(idCatalogo)
                                .orElseThrow(() -> new RuntimeException("Catalogo no encontrado: " + idCatalogo));

                // 1. Crear el registro de Ajuste (Cabecera)
                AjusteInventario ajuste = AjusteInventario.builder()
                                .idSucursal(idSucursal)
                                .tipo("INGRESO")
                                .motivo(motivo)
                                .idUsuario(idUsuario)
                                .correlativo(generarCorrelativo("INGRESO"))
                                .detalles(new ArrayList<>())
                                .build();

                if (idClasificacion != null) {
                        ajuste.setClasificacion(clasificacionRepository.findById(idClasificacion).orElse(null));
                }

                AjusteInventarioDetalle detalle = AjusteInventarioDetalle.builder()
                                .ajuste(ajuste)
                                .producto(catalogo)
                                .cantidad(cantidad)
                                .costoUnitario(costo)
                                .build();

                ajuste.getDetalles().add(detalle);
                ajusteRepository.save(ajuste);

                // Normalizar datos para búsqueda y guardado
                String nroLoteNorm = nroLote != null ? nroLote.trim() : "";
                BigDecimal costoNorm = costo != null ? costo.setScale(2, java.math.RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // 2. Buscar si ya existe este lote exacto (Lote, Vencimiento, Costo) en esta
                // sucursal/almacén
                Optional<Producto> productoOpt = productoRepository.findExactLot(
                                idCatalogo, idSucursal, idAlmacen, nroLoteNorm, fechaVenc, costoNorm);

                Long idProducto;
                if (productoOpt.isPresent()) {
                        Producto productoExistente = productoOpt.get();
                        idProducto = productoExistente.getIdProducto();

                        // Si se proporciona una fecha de vencimiento, actualizarla
                        if (fechaVenc != null) {
                                productoExistente.setFechaVencimiento(fechaVenc);
                                productoRepository.save(productoExistente);
                        }
                } else {
                        // 3. Si no existe, crear un nuevo registro (Lote) heredando precios
                        com.pe.articulos.modules.productos.dto.ProductoRequest request = new com.pe.articulos.modules.productos.dto.ProductoRequest();
                        request.setIdProducto(0L);
                        request.setIdCatalogo(idCatalogo);
                        request.setIdSucursal(idSucursal);
                        request.setIdAlmacen(idAlmacen);
                        request.setStock(BigDecimal.ZERO); // Se actualizará vía Kardex
                        request.setPrecioCompra(costoNorm);
                        request.setNroLote(nroLoteNorm);
                        request.setFechaVencimiento(fechaVenc);

                        completarMetadatosProducto(request, catalogo, idSucursal);

                        com.pe.articulos.modules.productos.dto.ProductoResponse nuevo = productoService.crear(request);
                        idProducto = nuevo.getIdProducto();
                }

                // 4. Registrar el movimiento en Kardex
                KardexDTO dto = KardexDTO.builder()
                                .idAlmacen(idAlmacen)
                                .idCatalogo(idCatalogo)
                                .idSucursal(idSucursal)
                                .idUsuario(idUsuario)
                                .idDocumento("00")
                                .numDoc("AJU-" + ajuste.getId())
                                .detalle("INGRESO DIVERSO: " + motivo)
                                .operacion("INGRESO_DIVERSO")
                                .signo("+")
                                .cantidad(cantidad)
                                .costoUnitario(costo)
                                .observacion(motivo)
                                .origenId(ajuste.getId().toString())
                                .origenTipo("AJU")
                                .nroLote(nroLote)
                                .fechaVenc(fechaVenc)
                                .presentacion(catalogo.getPresentacion() != null ? catalogo.getPresentacion() : "UND")
                                .idClasificacion(idClasificacion)
                                .build();

                productoService.actualizarStockKardex(idProducto, dto);
        }

        @Transactional(readOnly = true)
        public BigDecimal obtenerUltimoCostoLote(Long idCatalogo, String nroLote) {
                return productoRepository.findFirstByIdCatalogoAndIdSucursalAndNroLote(idCatalogo, null, nroLote)
                                .map(Producto::getPrecioCompra)
                                .orElse(BigDecimal.ZERO);
        }

        @Transactional(readOnly = true)
        public BigDecimal obtenerUltimoCostoCompra(Long idCatalogo, Long idSucursal) {
                return productoRepository.findFirstByIdCatalogoAndIdSucursalOrderByFechaRegDesc(idCatalogo, idSucursal)
                                .map(Producto::getPrecioCompra)
                                .orElse(BigDecimal.ZERO);
        }

        private String generarCorrelativo(String tipo) {
                String prefijo = "INGRESO".equals(tipo) ? "ING-" : "SAL-";
                return prefijo + System.currentTimeMillis() / 1000;
        }

        @Transactional(readOnly = true)
        public PageResponse<ClasificacionMovimientoResponse> listarClasificaciones(String tipo, String q, int page,
                        int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

                Page<ClasificacionMovimiento> result;
                if (q != null && !q.isEmpty()) {
                        result = clasificacionRepository.findByNombreContainingIgnoreCase(q, pageable);
                } else if (tipo != null && !"AMBOS".equals(tipo) && !"".equals(tipo)) {
                        result = clasificacionRepository.findByTipo(tipo, pageable);
                } else {
                        result = clasificacionRepository.findAll(pageable);
                }
                return PageResponse.fromPage(result.map(inventarioMapper::toDto));
        }

        @Transactional
        public ClasificacionMovimientoResponse guardarClasificacion(ClasificacionMovimiento clasificacion) {
                return inventarioMapper.toDto(clasificacionRepository.save(clasificacion));
        }

        @Transactional
        public ClasificacionMovimientoResponse actualizarClasificacion(Long id, ClasificacionMovimiento clasificacion) {
                ClasificacionMovimiento existente = clasificacionRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Clasificación no encontrada"));
                existente.setNombre(clasificacion.getNombre());
                existente.setTipo(clasificacion.getTipo());
                existente.setEstado(clasificacion.getEstado());
                return inventarioMapper.toDto(clasificacionRepository.save(existente));
        }

        @Transactional
        public void eliminarClasificacion(Long id) {
                clasificacionRepository.deleteById(id);
        }

        // Sobrecarga para mantener compatibilidad si es necesario o simplificar
        // llamadas
        @Transactional
        public void registrarIngresoDiverso(Long idProducto, BigDecimal cantidad, BigDecimal costo, String motivo,
                        Long idUsuario) {
                Producto producto = productoRepository.findById(idProducto)
                                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                registrarIngresoDiverso(producto.getIdCatalogo(), producto.getIdSucursal(), producto.getIdAlmacen(),
                                cantidad, costo, producto.getNroLote(), producto.getFechaVencimiento(),
                                motivo, idUsuario, null);
        }

        @Transactional
        public void registrarSalidaDiversa(Long idProducto, BigDecimal cantidad, String motivo, Long idUsuario,
                        Long idClasificacion) {
                Producto producto = productoRepository.findById(idProducto)
                                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                if (producto.getIdAlmacen() == null || producto.getIdSucursal() == null) {
                        throw new RuntimeException("El producto no tiene almacén o sucursal asociado");
                }

                // 1. Crear el registro de Ajuste (Cabecera)
                AjusteInventario ajuste = AjusteInventario.builder()
                                .idSucursal(producto.getIdSucursal())
                                .tipo("SALIDA")
                                .motivo(motivo)
                                .idUsuario(idUsuario)
                                .correlativo(generarCorrelativo("SALIDA"))
                                .detalles(new ArrayList<>())
                                .build();

                if (idClasificacion != null) {
                        ajuste.setClasificacion(clasificacionRepository.findById(idClasificacion).orElse(null));
                }

                AjusteInventarioDetalle detalle = AjusteInventarioDetalle.builder()
                                .ajuste(ajuste)
                                .producto(producto.getCatalogo())
                                .cantidad(cantidad)
                                .costoUnitario(producto.getPrecioCompra() != null ? producto.getPrecioCompra()
                                                : BigDecimal.ZERO)
                                .build();

                ajuste.getDetalles().add(detalle);
                ajusteRepository.save(ajuste);

                KardexDTO dto = KardexDTO.builder()
                                .idAlmacen(producto.getIdAlmacen())
                                .idCatalogo(producto.getIdCatalogo())
                                .idSucursal(producto.getIdSucursal())
                                .idUsuario(idUsuario)
                                .idDocumento("00")
                                .numDoc("AJU-" + ajuste.getId())
                                .detalle("SALIDA DIVERSA: " + motivo)
                                .operacion("SALIDA_DIVERSA")
                                .signo("-")
                                .cantidad(cantidad)
                                .costoUnitario(detalle.getCostoUnitario())
                                .observacion(motivo)
                                .origenId(ajuste.getId().toString())
                                .origenTipo("AJU")
                                .nroLote(producto.getNroLote())
                                .fechaVenc(producto.getFechaVencimiento())
                                .presentacion(producto.getCatalogo() != null ? producto.getCatalogo().getPresentacion()
                                                : "UND")
                                .idClasificacion(idClasificacion)
                                .build();

                productoService.actualizarStockKardex(idProducto, dto);
        }

        @Transactional
        public MovimientoDiversoResponse registrarMovimientoMixtoBatch(
                        MovimientoDiversoBatchRequest request) {
                if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
                        return null;
                }

                boolean isCotizacion = "COTIZACION".equalsIgnoreCase(request.getEstado());

                // 1. Crear o Recuperar el registro central de Movimiento (Cabecera)
                MovimientoDiverso movimiento;
                if (request.getIdMovimiento() != null) {
                        movimiento = movimientoDiversoRepository.findById(request.getIdMovimiento())
                                        .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
                        if (movimiento.getEstado() != EstadoGeneral.COTIZACION) {
                                throw new RuntimeException("Solo se pueden editar movimientos en estado de borrador (COTIZACION)");
                        }
                        movimiento.getDetalles().clear(); // Limpiar detalles anteriores para reemplazarlos
                        movimientoDiversoRepository.flush(); // Forzar borrado de huerfanos
                } else {
                        movimiento = new MovimientoDiverso();
                }
                
                movimiento.setFecha(java.time.LocalDateTime.now());
                movimiento.setMotivo(request.getMotivo());
                movimiento.setEstado(isCotizacion ? EstadoGeneral.COTIZACION : EstadoGeneral.ACTIVO);

                // Formatear numDocumento como SERIE-NUMERO (ej. SD01-0000004)
                if (!isCotizacion && request.getIdPlantilla() != null) {
                        com.pe.articulos.modules.puntos.entity.PuntoDocumento puntoDoc = puntoDocumentoRepository.findById(request.getIdPlantilla())
                                        .orElseThrow(() -> new RuntimeException("Configuración de documento no encontrada (ID: " + request.getIdPlantilla() + ")"));
                        
                        Integer nuevoNumero = (puntoDoc.getNumero() != null ? puntoDoc.getNumero() : 0) + 1;
                        puntoDoc.setNumero(nuevoNumero);
                        puntoDocumentoRepository.save(puntoDoc);

                        movimiento.setSerie(puntoDoc.getSerie());
                        movimiento.setNumero(nuevoNumero);
                        movimiento.setNumDocumento(puntoDoc.getSerie() + "-" + String.format("%07d", nuevoNumero));
                } else if (!isCotizacion && request.getSerie() != null && request.getNumero() != null) {
                        movimiento.setSerie(request.getSerie());
                        movimiento.setNumero(request.getNumero());
                        movimiento.setNumDocumento(
                                        request.getSerie() + "-" + String.format("%07d", request.getNumero()));

                        // Fallback backward compatibility
                        List<com.pe.articulos.modules.puntos.entity.PuntoDocumento> puntos = puntoDocumentoRepository
                                        .findBySerie(request.getSerie());
                        if (!puntos.isEmpty()) {
                                com.pe.articulos.modules.puntos.entity.PuntoDocumento puntoDoc = puntos.get(0);
                                puntoDoc.setNumero(request.getNumero() + 1);
                                puntoDocumentoRepository.save(puntoDoc);
                        }
                } else if (movimiento.getNumDocumento() == null || !movimiento.getNumDocumento().startsWith("COT-")) {
                        // Fallback por si no viene el dato o es cotización (y no tiene ya un numDocumento generado)
                        String prefix = isCotizacion ? "COT-" : "MOV-";
                        movimiento.setNumDocumento(prefix + System.currentTimeMillis() / 1000);
                }

                movimiento.setSucursal(sucursalRepository.findById(Long.valueOf(request.getIdSucursal()))
                                .orElseThrow(() -> new RuntimeException(
                                                "Sucursal no encontrada: " + request.getIdSucursal())));
                movimiento.setUsuario(usuarioRepository.findById(request.getIdUsuario())
                                .orElseThrow(() -> new RuntimeException(
                                                "Usuario no encontrado: " + request.getIdUsuario())));

                // Guardar la cabecera primero para tener su ID
                movimiento = movimientoDiversoRepository.save(movimiento);

                // 2. Procesar Detalles y Afectar Stock/Kardex
                for (MovimientoDiversoDetalleRequest detReq : request.getDetalles()) {
                        Catalogo catalogo = catalogoRepository.findById(detReq.getIdCatalogo())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Catalogo no encontrado: " + detReq.getIdCatalogo()));

                        Long idProductoFinal;
                        String operacion;
                        String signo;

                        if ("INGRESO".equals(detReq.getTipo())) {
                                signo = "+";
                                operacion = "INGRESO_DIVERSO";

                                // Normalizar para búsqueda
                                String nroLoteNorm = detReq.getNroLote() != null ? detReq.getNroLote().trim() : "";
                                BigDecimal costoNorm = detReq.getCostoUnitario() != null
                                                ? detReq.getCostoUnitario().setScale(2, java.math.RoundingMode.HALF_UP)
                                                : BigDecimal.ZERO;

                                // Lógica de Ingreso (Lote exacto o nuevo)
                                if (isCotizacion) {
                                        idProductoFinal = null;
                                } else {
                                        Optional<Producto> productoOpt = productoRepository.findExactLot(
                                                        detReq.getIdCatalogo(), request.getIdSucursal(), detReq.getIdAlmacen(),
                                                        nroLoteNorm, detReq.getFechaVenc(), costoNorm);

                                        if (productoOpt.isPresent()) {
                                                Producto pExistente = productoOpt.get();
                                                idProductoFinal = pExistente.getIdProducto();
                                                if (detReq.getFechaVenc() != null) {
                                                        pExistente.setFechaVencimiento(detReq.getFechaVenc());
                                                        productoRepository.save(pExistente);
                                                }
                                        } else {
                                                com.pe.articulos.modules.productos.dto.ProductoRequest pReq = new com.pe.articulos.modules.productos.dto.ProductoRequest();
                                                pReq.setIdProducto(0L);
                                                pReq.setIdCatalogo(detReq.getIdCatalogo());
                                                pReq.setIdSucursal(request.getIdSucursal());
                                                pReq.setIdAlmacen(detReq.getIdAlmacen());
                                                pReq.setStock(BigDecimal.ZERO);
                                                pReq.setPrecioCompra(costoNorm);
                                                pReq.setNroLote(nroLoteNorm);
                                                pReq.setFechaVencimiento(detReq.getFechaVenc());

                                                completarMetadatosProducto(pReq, catalogo,
                                                                Long.valueOf(request.getIdSucursal()));

                                                com.pe.articulos.modules.productos.dto.ProductoResponse nuevoP = productoService
                                                                .crear(pReq);
                                                idProductoFinal = nuevoP.getIdProducto();
                                        }
                                }
                        } else {
                                signo = "-";
                                operacion = "SALIDA_DIVERSA";
                                // Para salidas, el idProducto ya debería venir o buscamos el lote exacto de
                                // donde restar
                                if (isCotizacion) {
                                        idProductoFinal = detReq.getIdProducto();
                                } else if (detReq.getIdProducto() != null) {
                                        idProductoFinal = detReq.getIdProducto();
                                } else {
                                        idProductoFinal = productoRepository.findExactLot(
                                                        detReq.getIdCatalogo(), request.getIdSucursal(),
                                                        detReq.getIdAlmacen(),
                                                        detReq.getNroLote(), detReq.getFechaVenc(),
                                                        detReq.getCostoUnitario())
                                                        .map(Producto::getIdProducto)
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        "No se encontró el lote exacto para la salida: "
                                                                                        + detReq.getNroLote()));
                                }
                        }

                        // Crear y vincular Detalle
                        MovimientoDiversoDetalle detalle = new MovimientoDiversoDetalle();
                        detalle.setCatalogo(catalogo);
                        detalle.setCantidad(detReq.getCantidad());
                        detalle.setCostoUnitario(detReq.getCostoUnitario() != null ? detReq.getCostoUnitario()
                                        : BigDecimal.ZERO);
                        detalle.setTipo(detReq.getTipo());
                        detalle.setNroLote(detReq.getNroLote());
                        detalle.setFechaVencimiento(detReq.getFechaVenc());
                        detalle.setObservacion(detReq.getObservacion());
                        detalle.setIdAlmacen(detReq.getIdAlmacen());
                        if (detReq.getIdClasificacion() != null) {
                                detalle.setClasificacion(clasificacionRepository.findById(detReq.getIdClasificacion())
                                                .orElse(null));
                        }

                        // Asociación bidireccional
                        movimiento.addDetalle(detalle);

                        // Es necesario guardar un momento el padre para que los detalles tomen PK si
                        // vamos a referenciar el ID en Kardex, o persistir el detalle explícitamente y
                        // luego el Kardex.
                        // Al usar cascade ALL, el flush debería asignar los ID.
                        movimiento = movimientoDiversoRepository.saveAndFlush(movimiento);

                        // 3. Registrar Kardex y Actualizar Stock referenciando al Detalle (hijo)
                        // originario
                        if (!isCotizacion) {
                                KardexDTO kardexDTO = KardexDTO.builder()
                                                .idAlmacen(detReq.getIdAlmacen())
                                                .idCatalogo(detReq.getIdCatalogo())
                                                .idSucursal(request.getIdSucursal())
                                                .idUsuario(request.getIdUsuario())
                                                .idDocumento("00")
                                                .numDoc(movimiento.getNumDocumento())
                                                .detalle(detReq.getTipo() + ": "
                                                                + (detReq.getObservacion() != null ? detReq.getObservacion()
                                                                                : request.getMotivo()))
                                                .operacion(operacion)
                                                .signo(signo)
                                                .cantidad(detReq.getCantidad())
                                                .costoUnitario(detalle.getCostoUnitario())
                                                .observacion(detReq.getObservacion() != null ? detReq.getObservacion()
                                                                : request.getMotivo())
                                                .origenId(movimiento.getId().toString())
                                                .origenTipo("MOV")
                                                .nroLote(detReq.getNroLote())
                                                .fechaVenc(detReq.getFechaVenc())
                                                .presentacion(catalogo.getPresentacion() != null ? catalogo.getPresentacion()
                                                                : "UND")
                                                .idClasificacion(detReq.getIdClasificacion())
                                                .build();

                                productoService.actualizarStockKardex(idProductoFinal, kardexDTO);
                        }
                }

                // Finalizar persistencia y asegurar que los IDs generados estén disponibles
                return inventarioMapper.toDto(movimientoDiversoRepository.saveAndFlush(movimiento));
        }

        public PageResponse<MovimientoDiversoResponse> buscarMovimientosDiversos(
                        Long idSucursal, java.time.LocalDate desde, java.time.LocalDate hasta, EstadoGeneral estado,
                        String buscar, String tipoDocumento, String serie, String numero, Pageable pageable) {

                // Convertir LocalDate a LocalDateTime
                java.time.LocalDateTime fechaDesde = (desde != null) ? desde.atStartOfDay() : null;
                java.time.LocalDateTime fechaHasta = (hasta != null) ? hasta.atTime(23, 59, 59) : null;

                Page<MovimientoDiverso> result = movimientoDiversoRepository.buscarPaginado(
                                idSucursal,
                                fechaDesde,
                                fechaHasta,
                                estado,
                                buscar,
                                tipoDocumento,
                                serie,
                                numero,
                                pageable);
                return PageResponse.fromPage(result.map(inventarioMapper::toDto));
        }

        public List<MovimientoDiversoDetalleResponse> obtenerDetallesPorMovimiento(
                        Long idMovimiento) {
                return movimientoDiversoRepository.findById(idMovimiento)
                                .map(MovimientoDiverso::getDetalles)
                                .orElse(new ArrayList<>())
                                .stream().map(inventarioMapper::toDto).collect(Collectors.toList());
        }

        public MovimientoDiversoResponse obtenerMovimientoDiversoPorId(Long idMovimiento) {
                return movimientoDiversoRepository.findById(idMovimiento)
                                .map(inventarioMapper::toDto)
                                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
        }

        @Transactional
        public void revertirMovimientoDiverso(Long idMovimiento, String motivoAnulacion, Long idUsuarioAtiende, boolean anularOriginal) {
                MovimientoDiverso original = movimientoDiversoRepository.findById(idMovimiento)
                                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));

                if (anularOriginal) {
                        if (original.getEstado() == EstadoGeneral.ANULADO) {
                                throw new RuntimeException("El movimiento ya se encuentra anulado");
                        }
                        original.setEstado(EstadoGeneral.ANULADO);
                        movimientoDiversoRepository.save(original);
                }

                // Generar Contra-movimiento
                MovimientoDiverso reverso = new MovimientoDiverso();
                reverso.setFecha(java.time.LocalDateTime.now());
                reverso.setMotivo(motivoAnulacion);
                reverso.setEstado(EstadoGeneral.ACTIVO);
                reverso.setNumDocumento("REV-" + original.getNumDocumento());
                reverso.setSucursal(original.getSucursal());
                reverso.setUsuario(usuarioRepository.findById(idUsuarioAtiende).orElse(original.getUsuario()));
                
                reverso = movimientoDiversoRepository.save(reverso);

                for (MovimientoDiversoDetalle det : original.getDetalles()) {
                        String operacionKardex;
                        String signoKardex;
                        String tipoReverso;

                        if ("INGRESO".equals(det.getTipo()) || "+".equals(det.getTipo())) {
                                operacionKardex = "ANULACION_INGRESO_DIVERSO";
                                signoKardex = "-";
                                tipoReverso = "SALIDA";
                        } else {
                                operacionKardex = "ANULACION_SALIDA_DIVERSA";
                                signoKardex = "+";
                                tipoReverso = "INGRESO";
                        }

                        // Crear detalle del reverso
                        MovimientoDiversoDetalle detReverso = new MovimientoDiversoDetalle();
                        detReverso.setCatalogo(det.getCatalogo());
                        detReverso.setCantidad(det.getCantidad());
                        detReverso.setCostoUnitario(det.getCostoUnitario());
                        detReverso.setTipo(tipoReverso);
                        detReverso.setNroLote(det.getNroLote());
                        detReverso.setFechaVencimiento(det.getFechaVencimiento());
                        detReverso.setObservacion(motivoAnulacion);
                        detReverso.setIdAlmacen(det.getIdAlmacen());
                        detReverso.setClasificacion(det.getClasificacion());
                        
                        reverso.addDetalle(detReverso);

                        // Afectar Kardex
                        Producto p = productoRepository.findExactLot(
                                        det.getCatalogo().getId(), original.getSucursal().getIdSucursal(), null,
                                        det.getNroLote(), det.getFechaVencimiento(), det.getCostoUnitario())
                                        .orElse(null);

                        Long idProducto = (p != null) ? p.getIdProducto() : null;
                        Long idAlmacen = (p != null) ? p.getIdAlmacen() : det.getIdAlmacen();

                        if (idProducto != null) {
                                KardexDTO kardexDTO = KardexDTO.builder()
                                                .idAlmacen(idAlmacen)
                                                .idCatalogo(det.getCatalogo().getId())
                                                .idSucursal(original.getSucursal().getIdSucursal())
                                                .idUsuario(idUsuarioAtiende)
                                                .idDocumento("00")
                                                .numDoc(reverso.getNumDocumento())
                                                .detalle(anularOriginal ? "ANULACIÓN: " + motivoAnulacion : "REVERSO: " + motivoAnulacion)
                                                .operacion(operacionKardex)
                                                .signo(signoKardex)
                                                .cantidad(det.getCantidad())
                                                .costoUnitario(det.getCostoUnitario())
                                                .observacion(motivoAnulacion)
                                                .origenId(reverso.getId().toString())
                                                .origenTipo("MOV")
                                                .nroLote(det.getNroLote())
                                                .fechaVenc(det.getFechaVencimiento())
                                                .presentacion(det.getCatalogo().getPresentacion() != null
                                                                ? det.getCatalogo().getPresentacion()
                                                                : "UND")
                                                .idClasificacion(det.getClasificacion() != null
                                                                ? det.getClasificacion().getId()
                                                                : null)
                                                .build();

                                productoService.actualizarStockKardex(idProducto, kardexDTO);
                        }
                }
                movimientoDiversoRepository.save(reverso);
        }

        private void completarMetadatosProducto(com.pe.articulos.modules.productos.dto.ProductoRequest request,
                        Catalogo catalogo, Long idSucursal) {
                if (catalogo != null) {
                        request.setPresentacion(catalogo.getPresentacion());
                        request.setManejaUnidad(catalogo.getManejaUnidad());
                        request.setManejaBlister(catalogo.getManejaBlister());
                        request.setFactorBlister(catalogo.getFactorBlister());
                        request.setManejaCaja(catalogo.getManejaCaja());
                        request.setFactorCaja(catalogo.getFactorCaja());
                }
        }
}
