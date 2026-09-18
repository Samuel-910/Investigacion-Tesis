package com.pe.articulos.modules.productos.services;

import com.pe.articulos.modules.almacen.entity.Almacen;
import com.pe.articulos.modules.almacen.repository.AlmacenRepository;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.catalogo.repository.CatalogoRepository;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.productos.dto.TransferenciaDetalleRequest;
import com.pe.articulos.modules.productos.dto.TransferenciaRequest;
import com.pe.articulos.modules.productos.dto.ProductoRequest;
import com.pe.articulos.modules.productos.dto.ProductoResponse;
import com.pe.articulos.modules.productos.entity.Producto;
import com.pe.articulos.modules.productos.entity.TransferenciaSucursal;
import com.pe.articulos.modules.productos.entity.TransferenciaSucursalDetalle;
import com.pe.articulos.modules.productos.mapper.TransferenciaMapper;
import com.pe.articulos.modules.productos.repository.ProductoRepository;
import com.pe.articulos.modules.productos.repository.TransferenciaRepository;
import com.pe.articulos.modules.productos.repository.MovimientoDiversoRepository;
import com.pe.articulos.modules.puntos.repository.PuntoDocumentoRepository;
import com.pe.articulos.modules.sucursal.repository.SucursalRepository;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.modules.productos.entity.MovimientoDiverso;
import com.pe.articulos.modules.productos.entity.MovimientoDiversoDetalle;
import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.modules.puntos.entity.PuntoDocumento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.pe.articulos.modules.productos.dto.TransferenciaResponse;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class TransferenciaService {

        private final TransferenciaRepository transferenciaRepository;
        private final CatalogoRepository catalogoRepository;
        private final ProductoRepository productoRepository;
        private final ProductoService productoService;
        private final AlmacenRepository almacenRepository;
        private final TransferenciaMapper mapper;
        private final MovimientoDiversoRepository movimientoDiversoRepository;
        private final PuntoDocumentoRepository puntoDocumentoRepository;
        private final SucursalRepository sucursalRepository;
        private final DatosPersonalesRepository datosPersonalesRepository;

        @Transactional(readOnly = true)
        public TransferenciaResponse obtenerPorId(Long id) {
                return transferenciaRepository.findById(id)
                                .map(mapper::toDto)
                                .orElseThrow(() -> new RuntimeException("Transferencia no encontrada"));
        }

        @Transactional(readOnly = true)
        public List<TransferenciaResponse> listar(Long idSucursal) {
                return transferenciaRepository.findAllBySucursal(idSucursal).stream()
                                .map(mapper::toDto)
                                .collect(Collectors.toList());
        }

        @Transactional
        public void solicitar(TransferenciaRequest request) {
                TransferenciaSucursal transferencia = TransferenciaSucursal.builder()
                                .idSucursalOrigen(request.getIdSucursalOrigen())
                                .idSucursalDestino(request.getIdSucursalDestino())
                                .idUsuarioSolicita(request.getIdUsuario())
                                .motivo(request.getMotivo())
                                .estado(EstadoGeneral.SOLICITADO)
                                .detalles(new ArrayList<>())
                                .build();

                for (TransferenciaDetalleRequest detailReq : request.getDetalles()) {
                        Catalogo catalogo = catalogoRepository.findById(detailReq.getIdCatalogo())
                                        .orElseThrow(() -> new RuntimeException("Catálogo no encontrado"));

                        TransferenciaSucursalDetalle detalle = TransferenciaSucursalDetalle.builder()
                                        .transferencia(transferencia)
                                        .catalogo(catalogo)
                                        .cantidadSolicitada(detailReq.getCantidad())
                                        .build();
                        transferencia.getDetalles().add(detalle);
                }

                transferenciaRepository.save(transferencia);
        }

        @Transactional
        public void enviar(Long idTransferencia, Long idUsuario, List<TransferenciaDetalleRequest> detallesLote) {
                log.info("Iniciando ENVIO de transferencia ID: {}. Usuario: {}. Items a procesar: {}",
                                idTransferencia, idUsuario, detallesLote != null ? detallesLote.size() : 0);

                TransferenciaSucursal transferencia = transferenciaRepository.findById(idTransferencia)
                                .orElseThrow(() -> new RuntimeException("Transferencia no encontrada"));

                if (transferencia.getEstado() != EstadoGeneral.SOLICITADO) {
                        throw new RuntimeException("La transferencia no está en estado SOLICITADO");
                }

                // 1. Marcar como RECIBIDO directamente para que el stock esté disponible en
                // destino
                transferencia.setEstado(EstadoGeneral.RECIBIDO);
                transferencia.setIdUsuarioEnvia(idUsuario);
                transferencia.setIdUsuarioRecibe(idUsuario); // En este flujo atómico, el que envía también confirma la
                                                             // llegada
                transferencia.setFechaEnvio(LocalDateTime.now());
                transferencia.setFechaRecepcion(LocalDateTime.now());

                // Procesar movimientos en ambas sucursales
                if (detallesLote == null) {
                        throw new RuntimeException("La lista de detalles por lote no puede ser nula");
                }

                // Crear movimientos diversos para origen y destino
                MovimientoDiverso movSalida = generarMovimientoDiversoParaTransferencia(
                                transferencia.getIdSucursalOrigen(), idUsuario,
                                "SALIDA POR TRANSFERENCIA SUC " + transferencia.getIdSucursalDestino(),
                                Modulo.SALIDAS_DIVERSAS);
                movSalida = movimientoDiversoRepository.save(movSalida);

                MovimientoDiverso movIngreso = generarMovimientoDiversoParaTransferencia(
                                transferencia.getIdSucursalDestino(), idUsuario,
                                "INGRESO POR TRANSFERENCIA SUC " + transferencia.getIdSucursalOrigen(),
                                Modulo.INGRESOS_DIVERSOS);
                movIngreso = movimientoDiversoRepository.save(movIngreso);

                for (TransferenciaDetalleRequest detLote : detallesLote) {
                        TransferenciaSucursalDetalle detalle = transferencia.getDetalles().stream()
                                        .filter(d -> d.getCatalogo().getId().equals(detLote.getIdCatalogo()))
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Producto no encontrado en la transferencia"));

                        detalle.setCantidadEnviada(detLote.getCantidad());
                        detalle.setCantidadRecibida(detLote.getCantidad());
                        detalle.setNroLote(detLote.getNroLote());
                        detalle.setFechaVenc(detLote.getFechaVenc());

                        // --- A. SALIDA EN ORIGEN ---
                        Producto productoOrigen = productoRepository.findFirstByIdCatalogoAndIdSucursalAndNroLote(
                                        detLote.getIdCatalogo(), transferencia.getIdSucursalOrigen(),
                                        detLote.getNroLote())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "No hay stock del lote " + detLote.getNroLote()
                                                                        + " en la sucursal origen"));

                        BigDecimal costo = productoOrigen.getPrecioCompra() != null ? productoOrigen.getPrecioCompra()
                                        : BigDecimal.ZERO;
                        detalle.setCostoUnitario(costo);

                        MovimientoDiversoDetalle detMovSalida = new MovimientoDiversoDetalle();
                        detMovSalida.setMovimiento(movSalida);
                        detMovSalida.setCatalogo(detalle.getCatalogo());
                        detMovSalida.setCantidad(detLote.getCantidad());
                        detMovSalida.setCostoUnitario(costo);
                        detMovSalida.setNroLote(detLote.getNroLote());
                        detMovSalida.setFechaVencimiento(detLote.getFechaVenc());
                        detMovSalida.setTipo("SALIDA");
                        detMovSalida.setIdAlmacen(productoOrigen.getIdAlmacen());
                        movSalida.addDetalle(detMovSalida);

                        KardexDTO dtoSalida = KardexDTO.builder()
                                        .idAlmacen(productoOrigen.getIdAlmacen())
                                        .idCatalogo(detLote.getIdCatalogo())
                                        .idSucursal(transferencia.getIdSucursalOrigen())
                                        .idUsuario(idUsuario)
                                        .idDocumento("TR")
                                        .numDoc(movSalida.getNumDocumento() != null ? movSalida.getNumDocumento() : "TRA-" + transferencia.getId())
                                        .detalle("ENVIO POR TRANSFERENCIA A SUC. "
                                                        + transferencia.getIdSucursalDestino())
                                        .operacion("TRANSFERENCIA_SALIDA")
                                        .signo("-")
                                        .cantidad(detLote.getCantidad())
                                        .costoUnitario(costo)
                                        .origenId(movSalida.getId().toString())
                                        .origenTipo("MOV_DIVERSO")
                                        .nroLote(detLote.getNroLote())
                                        .fechaVenc(detLote.getFechaVenc())
                                        .presentacion(detalle.getCatalogo().getPresentacion())
                                        .observacion("Envío de transferencia " + transferencia.getId())
                                        .build();

                        productoService.actualizarStockKardex(productoOrigen.getIdProducto(), dtoSalida);
                        log.info("Kardex de SALIDA registrado en origen (Sucursal: {})",
                                        transferencia.getIdSucursalOrigen());

                        // --- B. INGRESO EN DESTINO ---
                        // Buscar o crear producto en destino (usando lógica simplificada: mismo almacén
                        // si es posible o el primero de la sucursal)
                        java.util.Optional<Producto> productoDestinoOpt = productoRepository
                                        .findFirstByIdCatalogoAndIdSucursalAndNroLote(
                                                        detLote.getIdCatalogo(), transferencia.getIdSucursalDestino(),
                                                        detLote.getNroLote());

                        Long idProductoDestino;
                        Long idAlmacenDestino;

                        if (productoDestinoOpt.isPresent()) {
                                Producto prodExistente = productoDestinoOpt.get();
                                idProductoDestino = prodExistente.getIdProducto();
                                idAlmacenDestino = prodExistente.getIdAlmacen();
                        } else {
                                List<Almacen> almacenesSuc = almacenRepository
                                                .findBySucursalIdSucursal(transferencia.getIdSucursalDestino());
                                if (almacenesSuc.isEmpty()) {
                                        throw new RuntimeException("La sucursal destino no tiene almacenes activos");
                                }
                                idAlmacenDestino = almacenesSuc.get(0).getId();

                                ProductoRequest pReq = new ProductoRequest();
                                pReq.setIdProducto(0L);
                                pReq.setIdCatalogo(detLote.getIdCatalogo());
                                pReq.setIdSucursal(transferencia.getIdSucursalDestino());
                                pReq.setIdAlmacen(idAlmacenDestino);
                                pReq.setStock(BigDecimal.ZERO);
                                pReq.setNroLote(detLote.getNroLote());
                                pReq.setFechaVencimiento(detLote.getFechaVenc());
                                pReq.setPrecioCompra(costo);

                                ProductoResponse nuevo = productoService.crear(pReq);
                                idProductoDestino = nuevo.getIdProducto();
                        }

                        MovimientoDiversoDetalle detMovIngreso = new MovimientoDiversoDetalle();
                        detMovIngreso.setMovimiento(movIngreso);
                        detMovIngreso.setCatalogo(detalle.getCatalogo());
                        detMovIngreso.setCantidad(detLote.getCantidad());
                        detMovIngreso.setCostoUnitario(costo);
                        detMovIngreso.setNroLote(detLote.getNroLote());
                        detMovIngreso.setFechaVencimiento(detLote.getFechaVenc());
                        detMovIngreso.setTipo("INGRESO");
                        detMovIngreso.setIdAlmacen(idAlmacenDestino);
                        movIngreso.addDetalle(detMovIngreso);

                        KardexDTO dtoIngreso = KardexDTO.builder()
                                        .idAlmacen(idAlmacenDestino)
                                        .idCatalogo(detLote.getIdCatalogo())
                                        .idSucursal(transferencia.getIdSucursalDestino())
                                        .idUsuario(idUsuario)
                                        .idDocumento("TR")
                                        .numDoc(movIngreso.getNumDocumento() != null ? movIngreso.getNumDocumento() : "TRA-" + transferencia.getId())
                                        .detalle("INGRESO POR TRANSFERENCIA DESDE SUC. "
                                                        + transferencia.getIdSucursalOrigen())
                                        .operacion("TRANSFERENCIA_INGRESO")
                                        .signo("+")
                                        .cantidad(detLote.getCantidad())
                                        .costoUnitario(costo)
                                        .origenId(movIngreso.getId().toString())
                                        .origenTipo("MOV_DIVERSO")
                                        .nroLote(detLote.getNroLote())
                                        .fechaVenc(detLote.getFechaVenc())
                                        .presentacion(detalle.getCatalogo().getPresentacion())
                                        .observacion("Recepción automática de transferencia " + transferencia.getId())
                                        .build();

                        productoService.actualizarStockKardex(idProductoDestino, dtoIngreso);
                        log.info("Kardex de INGRESO registrado en destino (Sucursal: {})",
                                        transferencia.getIdSucursalDestino());
                }

                movimientoDiversoRepository.save(movSalida);
                movimientoDiversoRepository.save(movIngreso);
                transferenciaRepository.save(transferencia);
                log.info("Transferencia ID: {} completada exitosamente (Origen y Destino actualizados)",
                                idTransferencia);
        }

        @Transactional
        public void recibir(Long idTransferencia, Long idUsuario, Long idAlmacenDestino) {
                log.info("Iniciando RECEPCION de transferencia ID: {}. Usuario: {}. Almacen Destino: {}",
                                idTransferencia, idUsuario, idAlmacenDestino);

                TransferenciaSucursal transferencia = transferenciaRepository.findById(idTransferencia)
                                .orElseThrow(() -> new RuntimeException("Transferencia no encontrada"));

                if (transferencia.getEstado() != EstadoGeneral.ENVIADO) {
                        throw new RuntimeException("La transferencia no está en estado ENVIADO");
                }

                transferencia.setEstado(EstadoGeneral.RECIBIDO);
                transferencia.setIdUsuarioRecibe(idUsuario);
                transferencia.setFechaRecepcion(LocalDateTime.now());

                MovimientoDiverso movIngreso = generarMovimientoDiversoParaTransferencia(
                                transferencia.getIdSucursalDestino(), idUsuario,
                                "RECEPCION POR TRANSFERENCIA DESDE SUC. " + transferencia.getIdSucursalOrigen(),
                                Modulo.INGRESOS_DIVERSOS);
                movIngreso = movimientoDiversoRepository.save(movIngreso);

                // Procesar ingreso en destino
                for (TransferenciaSucursalDetalle detalle : transferencia.getDetalles()) {
                        if (detalle.getCantidadEnviada() == null
                                        || detalle.getCantidadEnviada().compareTo(BigDecimal.ZERO) <= 0) {
                                log.warn("Omitiendo item {} porque la cantidad enviada es 0 o nula",
                                                detalle.getCatalogo().getNombre());
                                continue;
                        }

                        detalle.setCantidadRecibida(detalle.getCantidadEnviada());

                        // Buscar o crear producto (lote) en la sucursal DESTINO para sumar stock
                        java.util.Optional<Producto> productoDestinoOpt = productoRepository
                                        .findFirstByIdCatalogoAndIdSucursalAndNroLote(
                                                        detalle.getCatalogo().getId(),
                                                        transferencia.getIdSucursalDestino(), detalle.getNroLote());

                        Long idProductoDestino;
                        Long idAlmacenEfec = idAlmacenDestino;
                        BigDecimal costo = detalle.getCostoUnitario() != null ? detalle.getCostoUnitario()
                                        : BigDecimal.ZERO;

                        if (productoDestinoOpt.isPresent()) {
                                Producto prodExistente = productoDestinoOpt.get();
                                idProductoDestino = prodExistente.getIdProducto();
                                idAlmacenEfec = prodExistente.getIdAlmacen(); // Priorizar el almacén que ya tiene el
                                                                              // producto
                                log.info("Producto encontrado en destino ({}), usando su almacen: {}",
                                                idProductoDestino,
                                                idAlmacenEfec);
                        } else {
                                // Si el almacén enviado es 1 o inválido para esta sucursal, buscar el primero
                                // de la sucursal
                                List<Almacen> almacenesSuc = almacenRepository
                                                .findBySucursalIdSucursal(transferencia.getIdSucursalDestino());
                                if (almacenesSuc.isEmpty()) {
                                        throw new RuntimeException(
                                                        "La sucursal destino no tiene almacenes activos configurados");
                                }

                                // Si el ID enviado no pertenece a la sucursal, usamos el primero encontrado
                                boolean almacenValido = almacenesSuc.stream()
                                                .anyMatch(a -> a.getId().equals(idAlmacenDestino));
                                if (!almacenValido) {
                                        idAlmacenEfec = almacenesSuc.get(0).getId();
                                        log.info("Almacen enviado ({}) no es de la sucursal, usando: {}",
                                                        idAlmacenDestino, idAlmacenEfec);
                                }

                                // Crear nuevo producto/lote en destino
                                log.info("Producto no existe en destino para lote {}, creando en almacen {}...",
                                                detalle.getNroLote(),
                                                idAlmacenEfec);
                                ProductoRequest pReq = new ProductoRequest();
                                pReq.setIdProducto(0L);
                                pReq.setIdCatalogo(detalle.getCatalogo().getId());
                                pReq.setIdSucursal(transferencia.getIdSucursalDestino());
                                pReq.setIdAlmacen(idAlmacenEfec);
                                pReq.setStock(BigDecimal.ZERO);
                                pReq.setNroLote(detalle.getNroLote());
                                pReq.setFechaVencimiento(detalle.getFechaVenc());
                                pReq.setPrecioCompra(costo);

                                ProductoResponse nuevo = productoService.crear(pReq);
                                idProductoDestino = nuevo.getIdProducto();
                        }

                        MovimientoDiversoDetalle detMovIngreso = new MovimientoDiversoDetalle();
                        detMovIngreso.setMovimiento(movIngreso);
                        detMovIngreso.setCatalogo(detalle.getCatalogo());
                        detMovIngreso.setCantidad(detalle.getCantidadEnviada());
                        detMovIngreso.setCostoUnitario(costo);
                        detMovIngreso.setNroLote(detalle.getNroLote());
                        detMovIngreso.setFechaVencimiento(detalle.getFechaVenc());
                        detMovIngreso.setTipo("INGRESO");
                        detMovIngreso.setIdAlmacen(idAlmacenEfec);
                        movIngreso.addDetalle(detMovIngreso);

                        KardexDTO dto = KardexDTO.builder()
                                        .idAlmacen(idAlmacenEfec)
                                        .idCatalogo(detalle.getCatalogo().getId())
                                        .idSucursal(transferencia.getIdSucursalDestino())
                                        .idUsuario(idUsuario)
                                        .idDocumento("TR")
                                        .numDoc(movIngreso.getNumDocumento() != null ? movIngreso.getNumDocumento() : "TRA-" + transferencia.getId())
                                        .detalle("RECEPCION POR TRANSFERENCIA DESDE SUC. "
                                                        + transferencia.getIdSucursalOrigen())
                                        .operacion("TRANSFERENCIA_INGRESO")
                                        .signo("+")
                                        .cantidad(detalle.getCantidadEnviada())
                                        .costoUnitario(costo)
                                        .origenId(movIngreso.getId().toString())
                                        .origenTipo("MOV_DIVERSO")
                                        .nroLote(detalle.getNroLote())
                                        .fechaVenc(detalle.getFechaVenc())
                                        .presentacion(detalle.getCatalogo().getPresentacion())
                                        .observacion("Recepción de transferencia " + transferencia.getId())
                                        .build();

                        productoService.actualizarStockKardex(idProductoDestino, dto);
                        log.info("Kardex de RECEPCION registrado en destino (Sucursal: {})",
                                        transferencia.getIdSucursalDestino());
                }

                movimientoDiversoRepository.save(movIngreso);
                transferenciaRepository.save(transferencia);
                log.info("Transferencia ID: {} recepcionada exitosamente", idTransferencia);
        }

        @Transactional
        public void rechazar(Long idTransferencia, Long idUsuario, String motivo) {
                log.info("Iniciando RECHAZO de transferencia ID: {}. Usuario: {}. Motivo: {}",
                                idTransferencia, idUsuario, motivo);

                TransferenciaSucursal transferencia = transferenciaRepository.findById(idTransferencia)
                                .orElseThrow(() -> new RuntimeException("Transferencia no encontrada"));

                if (transferencia.getEstado() != EstadoGeneral.SOLICITADO) {
                        throw new RuntimeException("Solo se pueden rechazar transferencias en estado SOLICITADO");
                }

                transferencia.setEstado(EstadoGeneral.CANCELADO);
                transferencia.setMotivo(motivo != null ? motivo : "Rechazado por el usuario");
                transferencia.setIdUsuarioCancela(idUsuario);
                transferencia.setFechaCancelacion(LocalDateTime.now());

                transferenciaRepository.save(transferencia);
                log.info("Transferencia ID: {} rechazada exitosamente", idTransferencia);
        }

        private MovimientoDiverso generarMovimientoDiversoParaTransferencia(Long idSucursal, Long idUsuario, String motivo, Modulo modulo) {
                MovimientoDiverso movimiento = new MovimientoDiverso();
                movimiento.setFecha(LocalDateTime.now());
                movimiento.setMotivo(motivo);
                movimiento.setEstado(EstadoGeneral.ACTIVO);
                
                movimiento.setSucursal(sucursalRepository.findById(idSucursal)
                                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada")));
                movimiento.setUsuario(datosPersonalesRepository.findById(idUsuario)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado")));

                // Buscamos correlativo
                List<PuntoDocumento> documentos = puntoDocumentoRepository.findDocumentosActivosBySucursal(idSucursal, java.util.Arrays.asList(modulo));
                if (documentos.isEmpty()) {
                        documentos = puntoDocumentoRepository.findDocumentosActivosBySucursal(idSucursal, null);
                }
                
                if (!documentos.isEmpty()) {
                        PuntoDocumento puntoDoc = documentos.get(0);
                        Integer nuevoNumero = (puntoDoc.getNumero() != null ? puntoDoc.getNumero() : 0) + 1;
                        puntoDoc.setNumero(nuevoNumero);
                        puntoDocumentoRepository.save(puntoDoc);

                        movimiento.setSerie(puntoDoc.getSerie());
                        movimiento.setNumero(nuevoNumero);
                        movimiento.setNumDocumento(puntoDoc.getSerie() + "-" + String.format("%07d", nuevoNumero));
                } else {
                        movimiento.setNumDocumento((modulo == Modulo.SALIDAS_DIVERSAS ? "TS-" : "TI-") + System.currentTimeMillis() / 1000);
                }

                return movimiento;
        }
}
