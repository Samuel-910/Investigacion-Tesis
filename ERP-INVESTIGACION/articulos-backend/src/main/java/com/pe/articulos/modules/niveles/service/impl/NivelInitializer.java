package com.pe.articulos.modules.niveles.service.impl;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.niveles.entity.Nivel;
import com.pe.articulos.modules.niveles.repository.NivelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NivelInitializer implements CommandLineRunner {

    private final NivelRepository nivelRepository;

    @Override
    public void run(String... args) {
        log.info("Verificando existencia del nivel FARMACIA...");
        
        nivelRepository.findByNombreIgnoreCase("FARMACIA").ifPresentOrElse(
            nivel -> log.info("Nivel FARMACIA ya existe con ID: {}", nivel.getIdNivel()),
            () -> {
                log.info("Nivel FARMACIA no encontrado. Creando nivel por defecto...");
                Nivel farmacia = Nivel.builder()
                        .nombre("FARMACIA")
                        .tipo("SUCURSAL") // Valor común para esto
                        .estado(EstadoGeneral.ACTIVO)
                        .nivelJerarquia(1)
                        .orden(0)
                        .build();
                
                Nivel saved = nivelRepository.save(farmacia);
                log.info("Nivel FARMACIA creado exitosamente con ID: {}", saved.getIdNivel());
            }
        );
    }
}
