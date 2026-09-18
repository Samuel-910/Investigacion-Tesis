package com.pe.articulos.modules.documentos.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.clinica.entity.Clinica;
import com.pe.articulos.modules.clinica.service.ClinicaService;
import com.pe.articulos.modules.compras.dto.CompraResponse;
import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.modules.compras.entity.DetalleCompra;
import com.pe.articulos.modules.compras.service.CompraService;
import com.pe.articulos.modules.documentos.entities.Plantilla;
import com.pe.articulos.modules.documentos.repositories.PlantillaRepository;
import com.pe.articulos.modules.compras.repository.CompraRepository;
import com.pe.articulos.modules.sucursal.repository.SucursalRepository;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class DocumentoImpresionService {

    private final DocumentoService documentoService;
    private final PlantillaRepository plantillaRepository;
    private final ClinicaService clinicaService;
    private final CompraService compraService;
    private final CompraRepository compraRepository;
    private final SucursalRepository sucursalRepository;
    private final ObjectMapper objectMapper;

    public String generarHtmlCompra(Long idCompra) {
        CompraResponse compra = compraService.obtener(idCompra);
        Clinica clinica = clinicaService.obtenerPrincipal();

        // Buscar la plantilla específica para compras o usar default
        String htmlBase = "";
        Optional<Plantilla> plantillaOpt = plantillaRepository.findByNombre("Compra A4 Premium");
        if (plantillaOpt.isPresent()) {
            Plantilla plantilla = plantillaOpt.get();
            htmlBase = plantilla.getHtmlTraducido();
            if (htmlBase == null || htmlBase.isEmpty()) {
                htmlBase = plantilla.getHtmlContenido();
            }
        } else {
            // HTML por defecto básico
            htmlBase = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Orden de Compra</title><style>body { font-family: sans-serif; padding: 20px; } table { width: 100%; border-collapse: collapse; margin-top: 20px; } th, td { border: 1px solid #ddd; padding: 8px; text-align: left; } th { background-color: #f2f2f2; } .header { margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 10px; } .info { margin-bottom: 20px; display: flex; justify-content: space-between; }</style></head><body>"
                    +
                    "<div class=\"header\"><h2>ORDEN DE COMPRA</h2><h3>{{clinica.nombre}}</h3><p>{{clinica.direccion}} | Tel: {{clinica.telefono}}</p></div>"
                    +
                    "<div class=\"info\"><div><h4>Proveedor:</h4><p>{{cliente.nombre}}<br>RUC/DNI: {{cliente.documento}}</p></div>"
                    +
                    "<div><h4>Documento:</h4><p>Tipo: {{documento.tipo_nombre}}<br>Número: {{documento.serie}}-{{documento.numdoc}}<br>Fecha: {{documento.fecha}}</p></div></div>"
                    +
                    "<table><thead><tr><th>Cant.</th><th>Unid.</th><th>Producto</th><th>P. Unitario</th><th>Total</th></tr></thead>"
                    +
                    "<tbody>{{tabla_items}}</tbody>" +
                    "<tfoot><tr><th colspan=\"4\" style=\"text-align: right;\">Total a Pagar ({{documento.moneda_nombre}}):</th><th>S/ {{documento.total}}</th></tr></tfoot>"
                    +
                    "</table></body></html>";
        }

        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Object> clinicaMap = objectMapper.convertValue(clinica, new TypeReference<Map<String, Object>>() {
        });

        // SOBREESCRIBIR CON DATOS DE LA SUCURSAL DEL USUARIO LOGUEADO
        Long idSucursal = SecurityUtils.getCurrentUserSucursalId();
        if (idSucursal != null) {
            sucursalRepository.findById(idSucursal).ifPresent(suc -> {
                clinicaMap.put("direccion", suc.getDireccion());
                clinicaMap.put("telefono", suc.getTelefono());
            });
        }

        // Convertir entidades a Maps para que procesarEtiquetas funcione
        dataMap.put("clinica", clinicaMap);

        Map<String, Object> docMap = new HashMap<>();
        docMap.put("tipo_nombre", compra.getTipoComprobante());
        docMap.put("serie", compra.getSerie());
        docMap.put("numdoc", compra.getCorrelativo());
        docMap.put("fecha", compra.getFechaEmision());
        docMap.put("total", compra.getTotalPagar());
        docMap.put("valorVenta", compra.getSubtotal());
        docMap.put("igv", compra.getIgv());
        docMap.put("descuento", compra.getPercepcion() != null ? compra.getPercepcion() : 0);
        docMap.put("condicionPago", compra.getCondicionPago());
        docMap.put("moneda_nombre", compra.getMoneda());
        docMap.put("totalLetras", "");
        dataMap.put("documento", docMap);

        Map<String, Object> provMap = new HashMap<>();
        provMap.put("nombre", compra.getProveedor().getRazonSocial());
        provMap.put("documento", compra.getProveedor().getNumDocIdent());
        dataMap.put("cliente", provMap);

        StringBuilder tablaHtml = new StringBuilder();
        if (compra.getDetalles() != null) {
            compra.getDetalles().forEach(detalle -> {
                String nombreProducto = detalle.getProducto() != null ? detalle.getProducto().getNombre()
                        : detalle.getDescripcion();
                tablaHtml.append("<tr class=\"border-b border-gray-100 hover:bg-gray-50 transition-colors\">")
                        .append("<td class=\"py-3 px-4\">").append(detalle.getCantidad()).append("</td>")
                        .append("<td class=\"py-3 px-4 italic text-gray-400 text-xs text-left\">Und</td>")
                        .append("<td class=\"py-3 px-4\"><p class=\"font-bold uppercase\">").append(nombreProducto)
                        .append("</p>")
                        .append("<span class=\"text-[10px] bg-gray-100 px-1 text-gray-500 rounded font-mono\">Lote: ")
                        .append(detalle.getLote() != null ? detalle.getLote() : "S/L")
                        .append(" | Venc: ")
                        .append(detalle.getFechaVencimiento() != null ? detalle.getFechaVencimiento() : "S/V")
                        .append("</span></td>")
                        .append("<td class=\"py-3 px-4 text-right italic\">S/ ").append(detalle.getPrecioUnitario())
                        .append("</td>")
                        .append("<td class=\"py-3 px-4 text-right font-semibold\">S/ ").append(detalle.getValorVenta())
                        .append("</td>")
                        .append("</tr>");
            });
        }
        dataMap.put("tabla_items", tablaHtml.toString());

        return documentoService.procesarEtiquetas(htmlBase, dataMap);
    }

    public String generarHtmlMatriz(String nombreGrupo, Long idSucursal) {
        Clinica clinica = clinicaService.obtenerPrincipal();
        List<Compra> grupo = compraRepository
                .findByNombreGrupoAndIdSucursal(nombreGrupo, idSucursal,
                        PageRequest.of(0, 100))
                .getContent();

        if (grupo.isEmpty()) {
            throw new RuntimeException("No se encontraron órdenes para el grupo: " + nombreGrupo);
        }

        Set<Catalogo> productosSet = new HashSet<>();
        for (Compra c : grupo) {
            if (c.getDetalles() != null) {
                for (DetalleCompra d : c.getDetalles()) {
                    if (d.getProducto() != null) {
                        productosSet.add(d.getProducto());
                    }
                }
            }
        }

        List<Catalogo> productos = new ArrayList<>(productosSet);
        productos.sort(Comparator.comparing(c -> c.getNombre()));

        StringBuilder tablaHtml = new StringBuilder();

        tablaHtml.append("<table><thead><tr><th>Código</th><th>Producto</th><th>Cant. Base</th>");
        for (Compra c : grupo) {
            String provName = c.getProveedor().getNombreComercial() != null ? c.getProveedor().getNombreComercial()
                    : c.getProveedor().getRazonSocial();
            if (c.getEstado() == EstadoGeneral.SOLICITADO) {
                tablaHtml.append("<th style='background-color: #d4edda'>").append(provName)
                        .append(" <br><small>(GANADOR)</small></th>");
            } else {
                tablaHtml.append("<th>").append(provName).append("</th>");
            }
        }
        tablaHtml.append("</tr></thead><tbody>");

        for (Catalogo p : productos) {
            tablaHtml.append("<tr>");
            tablaHtml.append("<td>").append(p.getCodigo() != null ? p.getCodigo() : "-").append("</td>");
            tablaHtml.append("<td>").append(p.getNombre()).append("</td>");

            BigDecimal cantidadBase = BigDecimal.ZERO;
            for (Compra c : grupo) {
                if (c.getDetalles() != null) {
                    for (DetalleCompra d : c.getDetalles()) {
                        if (d.getProducto() != null && d.getProducto().getId().equals(p.getId())) {
                            cantidadBase = d.getCantidad();
                            break;
                        }
                    }
                }
                if (cantidadBase.compareTo(BigDecimal.ZERO) > 0)
                    break;
            }
            tablaHtml.append("<td>").append(cantidadBase).append("</td>");

            for (Compra c : grupo) {
                boolean encontrado = false;
                if (c.getDetalles() != null) {
                    for (DetalleCompra d : c.getDetalles()) {
                        if (d.getProducto() != null && d.getProducto().getId().equals(p.getId())) {
                            String cellClass = c.getEstado() == EstadoGeneral.SOLICITADO
                                    ? " style='background-color: #d4edda'"
                                    : "";
                            tablaHtml.append("<td").append(cellClass).append(">")
                                    .append("S/ ").append(d.getPrecioUnitario());
                            if (d.getPorcentajeDescuento() != null
                                    && d.getPorcentajeDescuento().compareTo(BigDecimal.ZERO) > 0) {
                                tablaHtml.append("<br><small>Dscto: ").append(d.getPorcentajeDescuento())
                                        .append("%</small>");
                            }
                            tablaHtml.append("<br><strong>Tot: S/ ").append(d.getValorVenta()).append("</strong>")
                                    .append("</td>");
                            encontrado = true;
                            break;
                        }
                    }
                }
                if (!encontrado) {
                    String cellClass = c.getEstado() == EstadoGeneral.SOLICITADO
                            ? " style='background-color: #d4edda; color: #999;'"
                            : " style='color: #999;'";
                    tablaHtml.append("<td").append(cellClass).append(">-</td>");
                }
            }
            tablaHtml.append("</tr>");
        }

        tablaHtml.append("</tbody><tfoot><tr><th colspan='3' style='text-align:right'>Totales:</th>");
        for (Compra c : grupo) {
            String cellClass = c.getEstado() == EstadoGeneral.SOLICITADO
                    ? " style='background-color: #d4edda'"
                    : "";
            tablaHtml.append("<th").append(cellClass).append(">S/ ").append(c.getTotalPagar()).append("</th>");
        }
        tablaHtml.append("</tr></tfoot></table>");

        String htmlBase = "";
        Optional<Plantilla> plantillaOpt = plantillaRepository.findByNombre("Matriz Comparativa A4");
        if (plantillaOpt.isPresent()) {
            Plantilla plantilla = plantillaOpt.get();
            htmlBase = plantilla.getHtmlTraducido();
            if (htmlBase == null || htmlBase.isEmpty()) {
                htmlBase = plantilla.getHtmlContenido();
            }
        } else {
            htmlBase = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Matriz Comparativa</title><style>body { font-family: sans-serif; padding: 20px; } table { width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 12px; } th, td { border: 1px solid #ddd; padding: 6px; text-align: left; } th { background-color: #f2f2f2; } .header { margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 10px; text-align: center; }</style></head><body>"
                    +
                    "<div class=\"header\"><h2>MATRIZ COMPARATIVA DE PRECIOS</h2><h3>GRUPO: {{grupo.nombre}}</h3><p>{{clinica.nombre}}</p></div>"
                    +
                    "{{tabla_matriz}}</body></html>";
        }

        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Object> clinicaMap = objectMapper.convertValue(clinica, new TypeReference<Map<String, Object>>() {
        });
        if (idSucursal != null) {
            sucursalRepository.findById(idSucursal).ifPresent(suc -> {
                clinicaMap.put("direccion", suc.getDireccion());
                clinicaMap.put("telefono", suc.getTelefono());
            });
        }
        dataMap.put("clinica", clinicaMap);

        Map<String, Object> grupoMap = new HashMap<>();
        grupoMap.put("nombre", nombreGrupo);
        dataMap.put("grupo", grupoMap);
        dataMap.put("tabla_matriz", tablaHtml.toString());

        return documentoService.procesarEtiquetas(htmlBase, dataMap);
    }
}
