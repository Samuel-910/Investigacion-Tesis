package com.pe.articulos.modules.documentos.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.articulos.modules.documentos.entities.DocumentoFormato;
import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.modules.documentos.entities.Plantilla;
import com.pe.articulos.modules.documentos.repositories.DocumentoFormatoRepository;
import com.pe.articulos.modules.documentos.repositories.PlantillaRepository;
import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;
import com.pe.articulos.modules.venta_registro.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlantillaConfigService {

    private final PlantillaRepository plantillaRepository;
    private final DocumentoFormatoRepository formatoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void inicializarPlantillasPorDefecto() {
        try {
            ClassPathResource resource = new ClassPathResource("data/seed/sucursal/plantillas.json");
            if (!resource.exists()) {
                log.warn("El archivo seed de plantillas no existe: data/seed/sucursal/plantillas.json");
                return;
            }

            try (InputStream is = resource.getInputStream()) {
                List<Map<String, String>> plantillasJson = objectMapper.readValue(is, new TypeReference<List<Map<String, String>>>() {});
                for (Map<String, String> json : plantillasJson) {
                    String nombreFinal = json.get("nombre");

                    // Evitar duplicados
                    if (plantillaRepository.findByNombre(nombreFinal).isPresent()) {
                        continue;
                    }

                    Plantilla plantilla = new Plantilla();
                    plantilla.setNombre(nombreFinal);
                    plantilla.setOrientacion(json.get("orientacion"));
                    plantilla.setHtmlContenido(json.get("htmlContenido"));
                    plantilla.setHtmlTraducido(json.get("htmlTraducido"));
                    plantilla.setCssEstilo(json.get("cssEstilo"));

                    if (json.get("modulo") != null) {
                        try {
                            plantilla.setModulo(Modulo.valueOf(json.get("modulo")));
                        } catch (IllegalArgumentException e) {
                            log.error("Modulo inválido: {}", json.get("modulo"));
                        }
                    }

                    if (json.get("tipoDoc") != null) {
                        TipoDocumento tipoDoc = tipoDocumentoRepository.findByTipoDoc(json.get("tipoDoc")).orElse(null);
                        plantilla.setTipoDocumento(tipoDoc);
                    }

                    if (json.get("formatoNombre") != null) {
                        DocumentoFormato formato = formatoRepository.findByNombre(json.get("formatoNombre")).orElse(null);
                        plantilla.setFormato(formato);
                    }

                    plantillaRepository.save(plantilla);
                    log.info("Plantilla creada exitosamente: {}", nombreFinal);
                }
            }
        } catch (Exception e) {
            log.error("Error al inicializar plantillas por defecto", e);
        }
    }
}
