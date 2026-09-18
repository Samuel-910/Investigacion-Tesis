package com.pe.articulos.modules.documentos.services;

import java.util.HashSet;

import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.modules.venta_registro.repository.TipoDocumentoRepository;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.modules.documentos.dto.BloqueDTO;
import com.pe.articulos.modules.documentos.dto.BloqueRequest;
import com.pe.articulos.modules.documentos.dto.PlantillaDTO;
import com.pe.articulos.modules.documentos.dto.PlantillaRequest;
import com.pe.articulos.modules.documentos.entities.Bloque;
import com.pe.articulos.modules.documentos.entities.Plantilla;
import com.pe.articulos.modules.documentos.mappers.BloqueMapper;
import com.pe.articulos.modules.documentos.mappers.PlantillaMapper;
import com.pe.articulos.modules.documentos.repositories.BloqueRepository;
import com.pe.articulos.modules.documentos.repositories.DocumentoFormatoRepository;
import com.pe.articulos.modules.documentos.repositories.PlantillaRepository;
import com.pe.articulos.modules.sucursal.repository.SucursalRepository;
import com.pe.articulos.core.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DocumentoService {

    private final BloqueRepository bloqueRepository;
    private final PlantillaRepository plantillaRepository;
    private final DocumentoFormatoRepository formatoRepository;
    private final com.pe.articulos.modules.clinica.repository.ClinicaRepository clinicaRepository;
    private final com.pe.articulos.modules.users.repository.DatosPersonalesRepository datosPersonalesRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SucursalRepository sucursalRepository;
    private final BloqueMapper bloqueMapper;
    private final PlantillaMapper plantillaMapper;

    public PageResponse<BloqueDTO> listarBloques(Pageable pageable) {
        Page<Bloque> page = bloqueRepository.findAll(pageable);
        return PageResponse.fromPage(page.map(bloqueMapper::toDTO));
    }

    public PageResponse<BloqueDTO> listarBloquesPorContexto(Modulo modulo, Pageable pageable) {
        Page<Bloque> page;
        if (modulo == null) {
            page = bloqueRepository.findAll(pageable);
        } else {
            page = bloqueRepository.findByModulosContaining(modulo, pageable);
        }
        return PageResponse.fromPage(page.map(bloqueMapper::toDTO));
    }

    public BloqueDTO obtenerBloque(Long id) {
        return bloqueMapper.toDTO(obtenerBloqueEntidad(id));
    }

    private Bloque obtenerBloqueEntidad(Long id) {
        return bloqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bloque no encontrado"));
    }

    @Transactional
    public BloqueDTO guardarBloque(BloqueRequest request) {
        log.info("Guardando bloque: {}", request.getNombre());
        Bloque bloque = Bloque.builder()
                .nombre(request.getNombre())
                .htmlContenido(request.getHtmlContenido())
                .cssEstilo(request.getCssEstilo())
                .categoria(request.getCategoria())
                .modulos(request.getModulos() != null ? new HashSet<>(request.getModulos()) : new HashSet<>())
                .build();
        return bloqueMapper.toDTO(bloqueRepository.save(bloque));
    }

    @Transactional
    public BloqueDTO actualizarBloque(Long id, BloqueRequest request) {
        log.info("Actualizando bloque: {}", id);
        Bloque bloque = obtenerBloqueEntidad(id);
        bloque.setNombre(request.getNombre());
        bloque.setHtmlContenido(request.getHtmlContenido());
        bloque.setCssEstilo(request.getCssEstilo());
        bloque.setCategoria(request.getCategoria());
        bloque.setModulos(request.getModulos() != null ? new HashSet<>(request.getModulos()) : new HashSet<>());
        return bloqueMapper.toDTO(bloqueRepository.save(bloque));
    }

    @Transactional
    public void eliminarBloque(Long id) {
        log.info("Eliminando bloque: {}", id);
        Bloque bloque = obtenerBloqueEntidad(id);
        bloque.setEstado(3); // Soft delete
        bloqueRepository.save(bloque);
    }

    // --- CRUD PLANTILLAS ---

    public PageResponse<PlantillaDTO> listarPlantillas(Pageable pageable) {
        Page<Plantilla> page = plantillaRepository.findAll(pageable);
        return PageResponse.fromPage(page.map(plantillaMapper::toDTO));
    }

    public PageResponse<PlantillaDTO> listarPlantillasPorContexto(Modulo modulo, String tipoDoc, Pageable pageable) {
        Page<Plantilla> page;
        if (modulo != null && tipoDoc != null) {
            page = tipoDocumentoRepository.findByTipoDoc(tipoDoc)
                    .map(td -> plantillaRepository.findByModuloAndTipoDocumento(modulo, td, pageable))
                    .orElse(Page.empty(pageable));
        } else if (modulo != null) {
            page = plantillaRepository.findByModulo(modulo, pageable);
        } else {
            page = plantillaRepository.findAll(pageable);
        }
        return PageResponse.fromPage(page.map(plantillaMapper::toDTO));
    }

    public PlantillaDTO obtenerPlantilla(Long id) {
        return plantillaMapper.toDTO(obtenerPlantillaEntidad(id));
    }

    private Plantilla obtenerPlantillaEntidad(Long id) {
        return plantillaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada"));
    }

    @Transactional
    public PlantillaDTO guardarPlantilla(PlantillaRequest request) {
        log.info("Guardando plantilla: {}", request.getNombre());
        Plantilla plantilla = Plantilla.builder()
                .nombre(request.getNombre())
                .htmlContenido(request.getHtmlContenido())
                .htmlTraducido(request.getHtmlTraducido())
                .cssEstilo(request.getCssEstilo())
                .orientacion(request.getOrientacion())
                .modulo(request.getModulo())
                .isDefault(request.isDefault())
                .build();

        if (request.getTipoDocumento() != null) {
            tipoDocumentoRepository.findByTipoDoc(request.getTipoDocumento())
                    .ifPresent(plantilla::setTipoDocumento);
        }

        if (request.getFormatoId() != null) {
            formatoRepository.findById(request.getFormatoId())
                    .ifPresent(plantilla::setFormato);
        }

        return plantillaMapper.toDTO(plantillaRepository.save(plantilla));
    }

    @Transactional
    public PlantillaDTO actualizarPlantilla(Long id, PlantillaRequest request) {
        log.info("Actualizando plantilla: {}", id);
        Plantilla plantilla = obtenerPlantillaEntidad(id);
        plantilla.setNombre(request.getNombre());
        plantilla.setHtmlContenido(request.getHtmlContenido());
        plantilla.setHtmlTraducido(request.getHtmlTraducido());
        plantilla.setCssEstilo(request.getCssEstilo());
        plantilla.setOrientacion(request.getOrientacion());
        plantilla.setModulo(request.getModulo());
        plantilla.setDefault(request.isDefault());

        if (request.getTipoDocumento() != null) {
            tipoDocumentoRepository.findByTipoDoc(request.getTipoDocumento())
                    .ifPresent(plantilla::setTipoDocumento);
        } else {
            plantilla.setTipoDocumento(null);
        }

        if (request.getFormatoId() != null) {
            formatoRepository.findById(request.getFormatoId())
                    .ifPresent(plantilla::setFormato);
        } else {
            plantilla.setFormato(null);
        }

        return plantillaMapper.toDTO(plantillaRepository.save(plantilla));
    }

    @Transactional
    public void eliminarPlantilla(Long id) {
        log.info("Eliminando plantilla: {}", id);
        Plantilla plantilla = obtenerPlantillaEntidad(id);
        plantilla.setEstado(3); // Soft delete
        plantillaRepository.save(plantilla);
    }

    // --- PROCESAMIENTO Y PDF ---

    /**
     * Genera un PDF a partir de una plantilla e inyecta datos
     */
    public byte[] generarPdf(Long plantillaId, Map<String, Object> datosOriginales) {
        log.info("Iniciando generación de PDF para plantilla ID: {}", plantillaId);

        try {
            Plantilla plantilla = obtenerPlantillaEntidad(plantillaId);

            // 1. Enriquecer datos para la plantilla
            Map<String, Object> datosEnriquecidos = prepararDatosParaPlantilla(datosOriginales);

            String htmlContenido = (plantilla.getHtmlTraducido() != null
                    && !plantilla.getHtmlTraducido().trim().isEmpty())
                            ? plantilla.getHtmlTraducido()
                            : plantilla.getHtmlContenido();

            String htmlFinal = htmlContenido;

            // 2. Procesar tabla de items PRIMERO para evitar que procesarEtiquetas la
            // consuma
            if (htmlFinal.contains("{{tabla_items}}")) {
                String htmlTabla = generarHtmlTablaItems(datosOriginales);
                htmlFinal = htmlFinal.replace("{{tabla_items}}", htmlTabla);
            }

            // 3. Procesar etiquetas básicas
            htmlFinal = procesarEtiquetas(htmlFinal, datosEnriquecidos);

            // 4. Agregar estilos y estructura HTML básica
            String fullHtml = "<html><head><meta charset=\"UTF-8\"><style>" +
                    "body { font-family: Arial, sans-serif; margin: 0; padding: 0; }" +
                    plantilla.getCssEstilo() +
                    "@page { size: " +
                    (plantilla.getFormato() != null ? plantilla.getFormato().getNombre() : "A4") +
                    " " + plantilla.getOrientacion() + "; margin: 0; }" +
                    ".text-right { text-align: right; }" +
                    ".font-bold { font-weight: bold; }" +
                    ".uppercase { text-transform: uppercase; }" +
                    "</style></head><body>" + htmlFinal + "</body></html>";

            log.debug("HTML Final generado (fragmento): {}", fullHtml.substring(0, Math.min(200, fullHtml.length())));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ConverterProperties properties = new ConverterProperties();

            HtmlConverter.convertToPdf(fullHtml, baos, properties);

            byte[] pdfBytes = baos.toByteArray();
            log.info("PDF generado exitosamente. Tamaño: {} bytes", pdfBytes.length);

            if (pdfBytes.length < 100) {
                log.warn("ATENCIÓN: El PDF generado es sospechosamente pequeño ({} bytes)", pdfBytes.length);
            }

            return pdfBytes;

        } catch (Exception e) {
            log.error("Error crítico generando PDF: {}", e.getMessage(), e);
            // Retornar un PDF mínimo con el mensaje de error para que no sea un 0x19 bytes
            return "Error generando PDF".getBytes();
        }
    }

    private Map<String, Object> prepararDatosParaPlantilla(Map<String, Object> vta) {
        Map<String, Object> context = new java.util.HashMap<>();

        // --- CLINICA ---
        clinicaRepository.findAll().stream().findFirst().ifPresent(c -> {
            Map<String, Object> clinica = new java.util.HashMap<>();
            clinica.put("razonSocial", c.getRazonSocial());
            clinica.put("ruc", c.getRuc());
            clinica.put("web", c.getWeb());

            // AGREGAR DATOS DE SUCURSAL DEL USUARIO LOGUEADO
            Long idSucursal = SecurityUtils.getCurrentUserSucursalId();
            if (idSucursal != null) {
                sucursalRepository.findById(idSucursal).ifPresent(s -> {
                    clinica.put("direccion", s.getDireccion());
                    clinica.put("telefono", s.getTelefono());
                });
            }

            context.put("clinica", clinica);
        });

        // --- DOCUMENTO (VENTA) ---
        Map<String, Object> doc = new java.util.HashMap<>();
        doc.put("idVenta", vta.get("idVenta"));
        doc.put("serie", vta.get("serie"));
        doc.put("numdoc", vta.get("numero"));
        doc.put("fecha", vta.get("fecha"));
        doc.put("metodoPago", vta.get("metodoPago"));
        doc.put("tipoDoc", vta.get("tipoDoc"));
        doc.put("tipo_nombre", "01".equals(vta.get("tipoDoc")) ? "FACTURA ELECTRÓNICA" : "BOLETA DE VENTA ELECTRÓNICA");
        doc.put("moneda", vta.get("moneda"));
        doc.put("tc", vta.get("tc"));

        // Montos
        doc.put("valorAfecto", vta.get("baseImp")); // baseImp is usually the taxable base
        doc.put("valorInaf", vta.get("valorInaf"));
        doc.put("valorExo", vta.get("valorExo"));
        doc.put("baseImp", vta.get("baseImp"));
        doc.put("igv", vta.get("igv"));
        doc.put("total", vta.get("total"));
        doc.put("descuento", vta.get("descuento"));
        doc.put("importePago", vta.get("importePago"));
        doc.put("vuelto", vta.get("vuelto"));
        doc.put("totalLetras", vta.get("totalLetras"));

        // Paciente / Cliente
        doc.put("nombrePac", vta.get("nombrePac"));
        doc.put("ruc", vta.get("ruc"));
        doc.put("razon", vta.get("razon"));
        doc.put("direccRuc", vta.get("direcRuc"));
        doc.put("nroDni", vta.get("nroDni"));
        doc.put("tipoDni", vta.get("tipoDni"));
        doc.put("direccion", vta.get("direccion"));

        // Clínico / Otros
        doc.put("nhc", vta.get("nhc"));
        doc.put("codAfi", vta.get("codAfi"));
        doc.put("autorizador", vta.get("autorizador"));
        doc.put("fechaAutoriza", vta.get("fechaAutoriza"));
        doc.put("idMedico", vta.get("idMedico"));
        doc.put("nroHab", vta.get("nroHab"));
        doc.put("obs", vta.get("observacion"));

        context.put("documento", doc);

        // --- CLIENTE (PACIENTE) - Mapeo duplicado para compatibilidad ---
        Map<String, Object> cliente = new java.util.HashMap<>();
        cliente.put("nombre", vta.get("nombrePac") != null ? vta.get("nombrePac") : vta.get("razon"));
        cliente.put("documento", vta.get("ruc") != null ? vta.get("ruc") : vta.get("nroDni"));
        cliente.put("direccion", vta.get("direcRuc") != null ? vta.get("direcRuc") : vta.get("direccion"));
        context.put("cliente", cliente);

        // --- USUARIO (VENDEDOR) ---
        Map<String, Object> usuario = new java.util.HashMap<>();
        String login = (String) vta.get("idUser");
        usuario.put("username", login);
        datosPersonalesRepository.findByLogin(login).ifPresent(u -> {
            usuario.put("nombre", u.getNombreCompleto());
            usuario.put("nombreReal", u.getNombreCompleto());
            if (u.getPunto() != null) {
                Map<String, Object> punto = new java.util.HashMap<>();
                punto.put("nombre", u.getPunto().getNombre());
                context.put("punto", punto);
            }
        });
        context.put("usuario", usuario);

        return context;
    }

    @SuppressWarnings("unchecked")
    private String generarHtmlTablaItems(Map<String, Object> datosVenta) {
        List<Map<String, Object>> detalles = (List<Map<String, Object>>) datosVenta.get("detalles");
        if (detalles == null || detalles.isEmpty())
            return "";

        StringBuilder html = new StringBuilder();
        for (Map<String, Object> det : detalles) {
            String lote = det.get("nroLote") != null ? det.get("nroLote").toString() : "-";
            String vence = det.get("fechaVenc") != null ? det.get("fechaVenc").toString() : "";
            String um = det.get("unidadMedida") != null ? det.get("unidadMedida").toString() : "";

            html.append("<tr class=\"border-b border-gray-100\">");
            html.append("<td class=\"py-2 px-4 border\">").append(det.get("cantidad")).append(" ").append(um)
                    .append("</td>");
            html.append("<td class=\"py-2 px-4 border\">")
                    .append(det.get("descripcion") != null ? det.get("descripcion") : det.get("glosa")).append("</td>");
            html.append("<td class=\"py-2 px-4 border text-center\">").append(lote).append(" ").append(vence)
                    .append("</td>");
            html.append("<td class=\"py-2 px-4 border text-right\">").append(det.get("precioUnitario")).append("</td>");
            html.append("<td class=\"py-2 px-4 border text-right\">").append(det.get("total")).append("</td>");
            html.append("</tr>");
        }
        return html.toString();
    }

    /**
     * Reemplaza etiquetas {{objeto.propiedad}} por valores de un mapa
     */
    public String procesarEtiquetas(String html, Map<String, Object> datos) {
        if (html == null || datos == null)
            return html;

        Pattern pattern = Pattern.compile("\\{\\{(.+?)\\}\\}");
        Matcher matcher = pattern.matcher(html);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String tag = matcher.group(1).trim();
            Object valor = obtenerValorProfundo(datos, tag);

            // iText/HTML escape if necessary, for now literal
            String replacement = valor != null ? valor.toString() : "";
            // Evitar caracteres que rompan matcher.appendReplacement (como $ o \)
            replacement = Matcher.quoteReplacement(replacement);

            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Object obtenerValorProfundo(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

}
