package com.pe.articulos.modules.compras.service.impl;

import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.catalogo.repository.CatalogoRepository;
import com.pe.articulos.modules.compras.dto.IntercambioRequest;
import com.pe.articulos.modules.compras.repository.DetalleCompraRepository;
import com.pe.articulos.modules.compras.service.IntercambioService;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.productos.entity.Producto;
import com.pe.articulos.modules.productos.repository.ProductoRepository;
import com.pe.articulos.modules.productos.services.ProductoService;
import com.pe.articulos.modules.proveedores.dto.ProveedorResponse;
import com.pe.articulos.modules.proveedores.entity.Proveedor;
import com.pe.articulos.modules.proveedores.mapper.ProveedorMapper;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class IntercambioServiceImpl implements IntercambioService {

        private final DetalleCompraRepository detalleCompraRepository;
        private final ProductoService productoService;
        private final ProductoRepository productoRepository;
        private final CatalogoRepository catalogoRepository;
        private final DatosPersonalesRepository datosPersonalesRepository;
        private final com.pe.articulos.modules.proveedores.repository.ProveedorRepository proveedorRepository;
        private final ProveedorMapper proveedorMapper;

        @Override
        public ProveedorResponse obtenerProveedorPorProductoYLote(Long idProducto, String lote,
                        org.springframework.data.domain.Pageable pageable) {
                Optional<Producto> productoOpt = productoRepository.findById(idProducto);
                if (productoOpt.isEmpty()) {
                        return null;
                }
                if (productoOpt.get().getProveedor() != null) {
                        return proveedorMapper.toResponse(productoOpt.get().getProveedor());
                }

                // Fallback: Si es un producto muy antiguo sin idProveedor en la entidad,
                // buscarlo en las compras
                Long idCatalogo = productoOpt.get().getIdCatalogo();
                org.springframework.data.domain.Page<Proveedor> proveedores = detalleCompraRepository
                                .findProveedorByProductoAndLote(idCatalogo, lote, PageRequest.of(0, 1));
                return proveedores.isEmpty() ? null : proveedorMapper.toResponse(proveedores.getContent().get(0));
        }

        @Override
        @Transactional
        public void procesarIntercambio(IntercambioRequest request) {
                String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
                DatosPersonales usuarioActual = datosPersonalesRepository.findByLogin(currentUsername)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                Long idProdOrigen = request.getIdProductoOrigen();
                if (idProdOrigen == null) {
                        throw new RuntimeException("El ID del producto de origen es obligatorio");
                }

                Producto pOrigen = productoRepository.findById(idProdOrigen)
                                .orElseThrow(() -> new RuntimeException(
                                                "Producto origen no encontrado (Stock no disponible)"));

                if (pOrigen.getStock().compareTo(request.getCantidad()) < 0) {
                        throw new RuntimeException(
                                        "Stock insuficiente para el intercambio. Disponible: " + pOrigen.getStock());
                }

                String provNombre = "";
                if (request.getIdProveedor() != null) {
                        provNombre = proveedorRepository.findById(request.getIdProveedor())
                                        .map(Proveedor::getRazonSocial)
                                        .orElse("");
                }

                String numDoc = "INT-" + System.currentTimeMillis();

                // 1. Salida de Kardex del producto original
                KardexDTO kardexSalida = KardexDTO.builder()
                                .idAlmacen(pOrigen.getIdAlmacen())
                                .idCatalogo(pOrigen.getIdCatalogo())
                                .idSucursal(pOrigen.getIdSucursal())
                                .idUsuario(usuarioActual.getId())
                                .idDocumento("00") // 00 = OTROS
                                .numDoc(numDoc)
                                .detalle("INTERCAMBIO (SALIDA) - PROV: " + provNombre + " - MOTIVO: "
                                                + (request.getMotivo() != null ? request.getMotivo() : "Sin motivo"))
                                .operacion("INTERCAMBIO_SALIDA")
                                .signo("-")
                                .cantidad(request.getCantidad())
                                .costoUnitario(pOrigen.getPrecioCompra())
                                .nroLote(pOrigen.getNroLote())
                                .fechaVenc(pOrigen.getFechaVencimiento())
                                .presentacion(pOrigen.getPresentacion())
                                .origenId(pOrigen.getIdProducto().toString())
                                .origenTipo("INTC_SAL")
                                .observacion("Salida por intercambio")
                                .build();
                productoService.actualizarStockKardex(request.getIdProductoOrigen(), kardexSalida);

                // 2. Buscar o crear producto de destino
                Optional<Producto> oDestino = productoRepository
                                .findFirstByIdCatalogoAndIdSucursalAndIdAlmacenAndNroLote(
                                                request.getIdCatalogoDestino(), pOrigen.getIdSucursal(),
                                                pOrigen.getIdAlmacen(),
                                                request.getLoteDestino());

                Producto pDestino;
                if (oDestino.isPresent()) {
                        pDestino = oDestino.get();
                } else {
                        Catalogo catDestino = catalogoRepository.findById(request.getIdCatalogoDestino())
                                        .orElseThrow(() -> new RuntimeException("Catálogo destino no encontrado"));

                        pDestino = Producto.builder()
                                        .idCatalogo(catDestino.getId())
                                        .idSucursal(pOrigen.getIdSucursal())
                                        .idAlmacen(pOrigen.getIdAlmacen())
                                        .nroLote(request.getLoteDestino())
                                        .fechaVencimiento(request.getFechaVencDestino())
                                        .stock(BigDecimal.ZERO)
                                        .precioCompra(request.getPrecioCompraDestino() != null
                                                        ? request.getPrecioCompraDestino()
                                                        : pOrigen.getPrecioCompra())
                                        .presentacion(catDestino.getPresentacion())
                                        .usuarioCrea(usuarioActual.getLogin())
                                        // Heredar precios del catálogo o de otro lote si es necesario (simplificado
                                        // aquí)
                                        .precioVentaUnitario(pOrigen.getPrecioVentaUnitario())
                                        .manejaUnidad(true)
                                        .idProveedor(request.getIdProveedor() != null ? request.getIdProveedor()
                                                        : pOrigen.getIdProveedor())
                                        .build();
                        pDestino = productoRepository.save(pDestino);
                }

                // 3. Ingreso de Kardex del producto nuevo
                KardexDTO kardexIngreso = KardexDTO.builder()
                                .idAlmacen(pDestino.getIdAlmacen())
                                .idCatalogo(pDestino.getIdCatalogo())
                                .idSucursal(pDestino.getIdSucursal())
                                .idUsuario(usuarioActual.getId())
                                .idDocumento("00")
                                .numDoc(numDoc)
                                .detalle("INTERCAMBIO (INGRESO) - PROV: " + provNombre + " - LOTE ORIGEN: "
                                                + pOrigen.getNroLote())
                                .operacion("INTERCAMBIO_INGRESO")
                                .signo("+")
                                .cantidad(request.getCantidadDestino() != null ? request.getCantidadDestino()
                                                : request.getCantidad())
                                .costoUnitario(request.getPrecioCompraDestino() != null
                                                ? request.getPrecioCompraDestino()
                                                : pDestino.getPrecioCompra())
                                .nroLote(pDestino.getNroLote())
                                .fechaVenc(pDestino.getFechaVencimiento())
                                .presentacion(pDestino.getPresentacion())
                                .origenId(pDestino.getIdProducto().toString())
                                .origenTipo("INTC_ENT")
                                .observacion("Ingreso por intercambio")
                                .build();

                productoService.actualizarStockKardex(pDestino.getIdProducto(), kardexIngreso);
        }
}
