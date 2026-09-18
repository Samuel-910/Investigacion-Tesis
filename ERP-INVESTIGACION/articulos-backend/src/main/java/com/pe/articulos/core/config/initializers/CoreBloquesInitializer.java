package com.pe.articulos.core.config.initializers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.articulos.modules.documentos.entities.Bloque;
import com.pe.articulos.modules.documentos.repositories.BloqueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreBloquesInitializer {

    private final BloqueRepository bloqueRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void run() {
        log.info("🚀 Starting Document Blocks Seeding from JSON files...");
        try (InputStream is = new ClassPathResource("data/seed/empresa/bloques.json").getInputStream()) {
            List<Bloque> bloquesFromJson = objectMapper.readValue(is, new TypeReference<List<Bloque>>() {});
            
            int added = 0;
            for (Bloque b : bloquesFromJson) {
                // Remove id so it can be safely saved as new if not exists
                b.setId(null); 
                
                if (bloqueRepository.findByNombreAndCategoria(b.getNombre(), b.getCategoria()).isEmpty()) {
                    bloqueRepository.save(b);
                    added++;
                }
            }
            if (added > 0) {
                log.info("✓ {} Bloques faltantes restaurados exitosamente.", added);
            }
            log.info("✅ Document Blocks Seeding COMPLETED.");
        } catch (Exception e) {
            log.error("❌ CRITICAL ERROR: Could not load bloques.json", e);
        }
    }
}
