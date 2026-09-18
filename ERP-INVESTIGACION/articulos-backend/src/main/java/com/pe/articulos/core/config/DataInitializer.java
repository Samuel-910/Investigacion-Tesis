package com.pe.articulos.core.config;

import org.springframework.core.annotation.Order;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.pe.articulos.core.config.initializers.CoreSecurityInitializer;
import com.pe.articulos.core.config.initializers.CoreBusinessInitializer;
import com.pe.articulos.core.config.initializers.CoreBloquesInitializer;
import com.pe.articulos.core.config.initializers.CoreParametrosInitializer;
import com.pe.articulos.core.config.initializers.CoreDatabaseFunctionsInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CoreSecurityInitializer coreSecurityInitializer;
    private final CoreBusinessInitializer coreBusinessInitializer;
    private final CoreBloquesInitializer coreBloquesInitializer;
    private final CoreParametrosInitializer coreParametrosInitializer;
    private final CoreDatabaseFunctionsInitializer coreDatabaseFunctionsInitializer;

    @Override
    public void run(String... args) {
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║   📋 SISTEMA DE GESTIÓN DE LABORATORIO - DATA INITIALIZER    ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");
        log.info("");
        coreDatabaseFunctionsInitializer.run();
        coreSecurityInitializer.run();
        coreParametrosInitializer.run();
        coreBusinessInitializer.run();
        coreBloquesInitializer.run();
        log.info("");
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║        ✅ DATA INITIALIZATION COMPLETED SUCCESSFULLY!         ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");
        log.info("");

    }
}
