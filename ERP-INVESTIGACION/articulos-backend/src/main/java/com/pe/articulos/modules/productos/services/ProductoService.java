package com.pe.articulos.modules.productos.services;

import com.pe.articulos.modules.productos.dto.ProductoRequest;
import com.pe.articulos.modules.productos.dto.ProductoResponse;
import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.exception.ConflictException;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.productos.entity.Producto;
import com.pe.articulos.modules.productos.mapper.ProductoMapper;
import com.pe.articulos.modules.productos.repository.ProductoRepository;
import com.pe.articulos.modules.catalogo.repository.CatalogoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.pe.articulos.modules.compras.repository.DetalleCompraRepository;
import com.pe.articulos.modules.compras.entity.DetalleCompra;
import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.kardex.service.KardexService;
import com.pe.articulos.modules.productos.dto.StockValorizadoDTO;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductoService {

    private final ProductoRepository repository;
    private final CatalogoRepository catalogoRepository;
    private final ProductoMapper mapper;
    private final DetalleCompraRepository detalleCompraRepository;
    private final KardexService kardexService;

    public long count() {
        return repository.count();
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        // Validar que existe el catálogo
        if (!catalogoRepository.existsById(request.getIdCatalogo())) {
            throw new ResourceNotFoundException("Catalogo", "id", request.getIdCatalogo());
        }

        // Validar que no exista el producto en la sucursal
        if (repository.existsById(request.getIdProducto())) {
            throw new ConflictException("Ya existe un producto con el ID: " + request.getIdProducto());
        }

        // Validar stock no negativo
        if (request.getStock() != null && request.getStock().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El stock real no puede ser negativo");
        }

        // Siempre forzar configuraciones de unidades desde el catálogo
        catalogoRepository.findById(request.getIdCatalogo()).ifPresent(cat -> {
            request.setManejaUnidad(cat.getManejaUnidad());
            request.setManejaBlister(cat.getManejaBlister());
            request.setFactorBlister(cat.getFactorBlister());
            request.setManejaCaja(cat.getManejaCaja());
            request.setFactorCaja(cat.getFactorCaja());
            if (cat.getPresentacion() != null) {
                request.setPresentacion(cat.getPresentacion());
            }
        });

        // Asignar usuario que crea
        if (request.getUsuarioCrea() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                request.setUsuarioCrea(auth.getName());
            }
        }

        Producto entity = mapper.toEntity(request);
        if (entity.getUsuarioCrea() == null) {
            entity.setUsuarioCrea(request.getUsuarioCrea());
        }
        Producto saved = repository.save(entity);

        if (request.getIdDetalleCompra() != null) {
            DetalleCompra detalle = detalleCompraRepository.findById(request.getIdDetalleCompra())
                    .orElse(null);
            if (detalle != null) {
                detalle.setRegistradoEnAlmacen(true);
                detalleCompraRepository.save(detalle);
            }
        }

        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long idProducto) {
        log.info("Obteniendo producto por ID: {}", idProducto);

        Producto entity = repository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "idProducto", idProducto));

        ProductoResponse response = mapper.toResponse(entity);
        enriquecerConKardex(response);
        return response;
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerEntidadPorId(Long idProducto) {
        return repository.findById(idProducto);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorIdYSucursal(Long idProducto, Long idSucursal) {
        log.info("Obteniendo producto {} de sucursal {}", idProducto, idSucursal);

        Producto entity = repository.findByIdProductoAndIdSucursal(idProducto, idSucursal)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado en la sucursal especificada"));

        ProductoResponse response = mapper.toResponse(entity);
        enriquecerConKardex(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<String> listarVendedores(Long idSucursal) {
        log.info("Listando vendedores de sucursal: {}", idSucursal);
        return repository.findDistinctUsuarioCreaByIdSucursal(idSucursal);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listarPorSucursal(Long idSucursal, Pageable pageable, boolean soloVenta) {
        log.info("Listando productos de sucursal: {}", idSucursal);

        Page<Producto> page = soloVenta 
            ? repository.findByIdSucursalWithFilter(idSucursal, soloVenta, pageable) 
            : repository.findByIdSucursal(idSucursal, pageable);
        return PageResponse.fromPage(page.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<StockValorizadoDTO> obtenerStockValorizado(Long idSucursal) {
        log.info("Generando reporte de stock valorizado para sucursal: {}", idSucursal);

        List<Producto> productos = repository.findByIdSucursal(idSucursal);

        return productos.stream().map(p -> {
            BigDecimal stock = p.getStock() != null ? p.getStock() : BigDecimal.ZERO;
            BigDecimal precioCompra = p.getPrecioCompra() != null ? p.getPrecioCompra() : BigDecimal.ZERO;

            return StockValorizadoDTO.builder()
                    .idProducto(p.getIdProducto())
                    .nombre(p.getCatalogo() != null ? p.getCatalogo().getNombre() : "SIN NOMBRE")
                    .laboratorio(p.getLaboratorio() != null ? p.getLaboratorio().getDescripcion() : "SIN LABORATORIO")
                    .almacen(p.getAlmacen() != null ? p.getAlmacen().getNombre() : "SIN ALMACEN")
                    .stock(stock)
                    .precioCompra(precioCompra)
                    .valorTotal(stock.multiply(precioCompra))
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<StockValorizadoDTO> obtenerStockValorizadoPaginado(Long idSucursal, Pageable pageable) {
        log.info("Generando reporte de stock valorizado paginado para sucursal: {}", idSucursal);

        Page<Producto> page = repository.findByIdSucursal(idSucursal, pageable);

        List<StockValorizadoDTO> content = page.getContent().stream().map(p -> {
            BigDecimal stock = p.getStock() != null ? p.getStock() : BigDecimal.ZERO;
            BigDecimal precioCompra = p.getPrecioCompra() != null ? p.getPrecioCompra() : BigDecimal.ZERO;

            return StockValorizadoDTO.builder()
                    .idProducto(p.getIdProducto())
                    .nombre(p.getCatalogo() != null ? p.getCatalogo().getNombre() : "SIN NOMBRE")
                    .laboratorio(p.getLaboratorio() != null ? p.getLaboratorio().getDescripcion() : "SIN LABORATORIO")
                    .almacen(p.getAlmacen() != null ? p.getAlmacen().getNombre() : "SIN ALMACEN")
                    .stock(stock)
                    .precioCompra(precioCompra)
                    .valorTotal(stock.multiply(precioCompra))
                    .build();
        }).toList();

        return new PageResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorCatalogo(Long idCatalogo) {
        log.info("Listando productos del catálogo: {}", idCatalogo);

        List<Producto> entities = repository.findByIdCatalogo(idCatalogo);
        return entities.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorCatalogoYSucursal(Long idCatalogo, Long idSucursal) {
        log.info("Listando productos del catálogo {} en sucursal {}", idCatalogo, idSucursal);

        List<Producto> entities = repository.findByIdCatalogoAndIdSucursal(idCatalogo, idSucursal);
        return entities.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public ProductoResponse actualizar(Long idProducto, ProductoRequest request) {
        log.info("Actualizando producto con ID: {}", idProducto);

        Producto entity = repository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "idProducto", idProducto));

        // Validar stock no negativo
        if (request.getStock() != null && request.getStock().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El stock no puede ser negativo");
        }

        mapper.updateEntityFromRequest(request, entity);
        Producto updated = repository.save(entity);

        log.info("Producto actualizado: {}", idProducto);
        return mapper.toResponse(updated);
    }

    @Transactional
    public void eliminar(Long idProducto) {
        log.info("Eliminando producto con ID: {}", idProducto);

        if (!repository.existsById(idProducto)) {
            throw new ResourceNotFoundException("Producto", "idProducto", idProducto);
        }

        repository.deleteById(idProducto);
        log.info("Producto eliminado: {}", idProducto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosProximosAVencer(Long idSucursal, Integer dias) {
        log.info("Listando productos próximos a vencer en {} días para sucursal: {}", dias, idSucursal);

        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(dias);

        List<Producto> entities = repository.findProductosProximosAVencer(idSucursal, fechaInicio, fechaFin);
        return entities.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosVencidos(Long idSucursal) {
        log.info("Listando productos vencidos en sucursal: {}", idSucursal);

        List<Producto> entities = repository.findProductosVencidos(idSucursal, LocalDate.now());
        return entities.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosSinStock(Long idSucursal) {
        log.info("Listando productos sin stock en sucursal: {}", idSucursal);

        List<Producto> entities = repository.findProductosSinStock(BigDecimal.ZERO);
        return entities.stream()
                .filter(p -> p.getIdSucursal().equals(idSucursal))
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> buscar(String q, Long idSucursal, String tipo, Pageable pageable, boolean soloVenta) {
        log.info("Buscando productos en sucursal {} por {}: {}", idSucursal, tipo, q);

        Page<Producto> page;
        if (tipo == null || tipo.equalsIgnoreCase("ALL")) {
            page = repository.searchBySucursal(idSucursal, q, soloVenta, pageable);
        } else if (tipo.equalsIgnoreCase("NOMBRE")) {
            page = repository.searchBySucursalAndNombre(idSucursal, q, soloVenta, pageable);
        } else if (tipo.equalsIgnoreCase("CODIGO")) {
            page = repository.searchBySucursalAndCodigo(idSucursal, q, soloVenta, pageable);
        } else if (tipo.equalsIgnoreCase("LABORATORIO")) {
            page = repository.searchBySucursalAndLaboratorio(idSucursal, q, soloVenta, pageable);
        } else {
            page = repository.searchBySucursal(idSucursal, q, soloVenta, pageable);
        }

        return PageResponse.fromPage(page.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> buscarAvanzado(
            Long idSucursal,
            String usuarioCrea,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Boolean rotaMas,
            Boolean queEntra,
            Boolean cercaVencer,
            Boolean faltanPrecio,
            Boolean listosVender,
            Pageable pageable) {

        log.info(
                "Búsqueda avanzada de productos en sucursal {}: usuario={}, desde={}, hasta={}, rotaMas={}, queEntra={}, cercaVencer={}, faltanPrecio={}, listosVender={}",
                idSucursal, usuarioCrea, fechaDesde, fechaHasta, rotaMas, queEntra, cercaVencer, faltanPrecio, listosVender);

        Specification<Producto> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("idSucursal"), idSucursal));
            predicates.add(cb.greaterThan(cb.coalesce(root.get("stock"), BigDecimal.ZERO), BigDecimal.ZERO)); // Solo items con stock

            if (usuarioCrea != null && !usuarioCrea.isEmpty() && !"TODOS".equals(usuarioCrea)) {
                predicates.add(cb.equal(root.get("usuarioCrea"), usuarioCrea));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaReg"), fechaDesde.atStartOfDay()));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaReg"), fechaHasta.atTime(23, 59, 59)));
            }

            if (Boolean.TRUE.equals(queEntra)) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("fechaReg"), LocalDate.now().minusDays(15).atStartOfDay()));
            }

            if (Boolean.TRUE.equals(rotaMas)) {
                List<Long> topIds = repository.findTopSellingCatalogoIds(idSucursal,
                        org.springframework.data.domain.PageRequest.of(0, 50));

                if (topIds != null && !topIds.isEmpty()) {
                    predicates.add(root.get("idCatalogo").in(topIds));
                }
            }
            if (Boolean.TRUE.equals(cercaVencer)) {
                LocalDate hoy = LocalDate.now();
                LocalDate limite = hoy.plusDays(90);
                predicates.add(cb.between(root.get("fechaVencimiento"), hoy, limite));
            }

            if (Boolean.TRUE.equals(faltanPrecio)) {
                predicates.add(cb.or(
                    cb.isNull(root.get("precioVentaUnitario")),
                    cb.lessThanOrEqualTo(root.get("precioVentaUnitario"), BigDecimal.ZERO)
                ));
            } else if (Boolean.TRUE.equals(listosVender)) {
                predicates.add(cb.and(
                    cb.isNotNull(root.get("precioVentaUnitario")),
                    cb.greaterThan(root.get("precioVentaUnitario"), BigDecimal.ZERO)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Producto> page = repository.findAll(spec, pageable);
        Page<ProductoResponse> responsePage = page.map(mapper::toResponse);
        responsePage.getContent().forEach(this::enriquecerConKardex);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listarParaDescuentos(Long idSucursal, Pageable pageable) {
        Page<Producto> page = repository.findDistinctCatalogosForDescuentos(idSucursal, pageable);
        return PageResponse.fromPage(page.map(mapper::toResponse));
    }

    private void enriquecerConKardex(ProductoResponse response) {
        if (response.getIdCatalogo() != null && response.getIdSucursal() != null) {
            kardexService
                    .obtenerUltimoMovimientoPorLote(response.getIdCatalogo(), response.getIdSucursal(),
                            response.getNroLote())
                    .ifPresent(k -> {
                        // El Kardex puede agrupar varios Productos (lotes) si tienen el mismo nroLote vacío.
                        // Para evitar inconsistencias donde el Kardex total es > 0 pero el lote individual es 0,
                        // priorizamos el stock real del Producto.
                        response.setStockKardex(response.getStock() != null ? response.getStock() : k.getSaldoCantidad());
                        response.setPmp(k.getSaldoCostoUnitario());
                    });
        }
        // Fallback si no hay movimientos
        if (response.getStockKardex() == null) {
            response.setStockKardex(response.getStock() != null ? response.getStock() : BigDecimal.ZERO);
        }
        if (response.getPmp() == null) {
            response.setPmp(response.getPrecioCompra());
        }
    }

    @Transactional
    public void actualizarStockKardex(Long idProducto, KardexDTO dto) {
        log.info("Actualizando stock y kardex para producto: {} (Sucursal: {}, Almacen: {}, Catalogo: {})",
                idProducto, dto.getIdSucursal(), dto.getIdAlmacen(), dto.getIdCatalogo());

        Producto producto = repository.findByIdForUpdate(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", idProducto));

        // CONVERSION A UNIDADES BASE
        BigDecimal originalCantidad = dto.getCantidad();
        BigDecimal cantidadBase = dto.getCantidad();
        BigDecimal costoBase = dto.getCostoUnitario();
        String presentacionOriginal = dto.getPresentacion() != null ? dto.getPresentacion().toUpperCase() : "UND";
        
        if (presentacionOriginal.contains("CAJA") || presentacionOriginal.equals("CJA")) {
            int factorRealCaja = (producto.getFactorCaja() != null && producto.getFactorCaja() > 0) ? producto.getFactorCaja() : 1;
            if (producto.getManejaBlister() != null && producto.getManejaBlister() && producto.getFactorBlister() != null && producto.getFactorBlister() > 0) {
                factorRealCaja = factorRealCaja * producto.getFactorBlister();
            }
            cantidadBase = dto.getCantidad().multiply(BigDecimal.valueOf(factorRealCaja));
            if (factorRealCaja > 0 && costoBase != null) {
                costoBase = costoBase.divide(BigDecimal.valueOf(factorRealCaja), 4, java.math.RoundingMode.HALF_UP);
            }
        } else if (presentacionOriginal.contains("BLISTER") || presentacionOriginal.equals("BLI") || presentacionOriginal.contains("BLÍSTER")) {
            int factor = (producto.getFactorBlister() != null && producto.getFactorBlister() > 0) ? producto.getFactorBlister() : 1;
            cantidadBase = dto.getCantidad().multiply(BigDecimal.valueOf(factor));
            if (factor > 0 && costoBase != null) {
                costoBase = costoBase.divide(BigDecimal.valueOf(factor), 4, java.math.RoundingMode.HALF_UP);
            }
        }

        if (cantidadBase.compareTo(originalCantidad) != 0) {
            dto.setCantidad(cantidadBase);
            dto.setCostoUnitario(costoBase);
            dto.setPresentacion("UND"); 
            String observacionActual = dto.getObservacion() != null ? dto.getObservacion() : "";
            dto.setObservacion(observacionActual + " (Equiv: " + originalCantidad + " " + presentacionOriginal + ")");
        }

        // 1. Actualizar Stock Físico Base
        BigDecimal stockActual = producto.getStock() != null ? producto.getStock() : BigDecimal.ZERO;
        if ("+".equals(dto.getSigno())) {
            producto.setStock(stockActual.add(cantidadBase));
        } else {
            if (stockActual.compareTo(cantidadBase) < 0 && (dto.getOperacion() == null || !dto.getOperacion().contains("ANULACION NC"))) {
                throw new com.pe.articulos.core.exception.BadRequestException("Stock secundario insuficiente para producto ID " + idProducto + ". Stock actual: " + stockActual + ", Solicitado: " + cantidadBase);
            }
            producto.setStock(stockActual.subtract(cantidadBase));
        }

        // --- CALCULO DE SUB STOCKS ---
        BigDecimal nuevoStock = producto.getStock();
        producto.setStockUnidad(nuevoStock);
        if (producto.getFactorBlister() != null && producto.getFactorBlister() > 0) {
            producto.setStockBlister(nuevoStock.divide(BigDecimal.valueOf(producto.getFactorBlister()), 2, java.math.RoundingMode.DOWN));
        } else {
            producto.setStockBlister(BigDecimal.ZERO);
        }
        if (producto.getFactorCaja() != null && producto.getFactorCaja() > 0) {
            int factorRealCaja = producto.getFactorCaja();
            if (producto.getManejaBlister() != null && producto.getManejaBlister() && producto.getFactorBlister() != null && producto.getFactorBlister() > 0) {
                factorRealCaja = factorRealCaja * producto.getFactorBlister();
            }
            producto.setStockCaja(nuevoStock.divide(BigDecimal.valueOf(factorRealCaja), 2, java.math.RoundingMode.DOWN));
        } else {
            producto.setStockCaja(BigDecimal.ZERO);
        }

        // 2. Si es ingreso, actualizar costo de compra
        if ("+".equals(dto.getSigno())) {
            producto.setPrecioCompra(
                    costoBase != null ? costoBase.setScale(2, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);
        }

        // 3. Persistir Producto
        repository.save(producto);

        // 4. Registrar en Kardex
        kardexService.registrarMovimiento(dto);

        log.info("Stock y Kardex actualizados correctamente.");
    }
}
