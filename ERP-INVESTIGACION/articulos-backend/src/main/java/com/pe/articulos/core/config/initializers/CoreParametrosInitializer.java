package com.pe.articulos.core.config.initializers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.modules.atributos.entity.AccionTerapeutica;
import com.pe.articulos.modules.atributos.repository.AccionTerapeuticaRepository;
import com.pe.articulos.modules.atributos.entity.Categoria;
import com.pe.articulos.modules.atributos.repository.CategoriaRepository;
import com.pe.articulos.modules.atributos.entity.Laboratorio;
import com.pe.articulos.modules.atributos.repository.LaboratorioRepository;
import com.pe.articulos.modules.clinica.entity.Clinica;
import com.pe.articulos.modules.clinica.repository.ClinicaRepository;

import com.pe.articulos.modules.productos.entity.ClasificacionMovimiento;
import com.pe.articulos.modules.productos.repository.ClasificacionMovimientoRepository;
import com.pe.articulos.modules.reportes.entity.DashboardConfig;
import com.pe.articulos.modules.reportes.repository.DashboardConfigRepository;
import com.pe.articulos.modules.documentos.entities.DocumentoFormato;
import com.pe.articulos.modules.documentos.repositories.DocumentoFormatoRepository;
import com.pe.articulos.modules.atributos.entity.MetodoPago;
import com.pe.articulos.modules.atributos.repository.MetodoPagoRepository;
import com.pe.articulos.modules.atributos.entity.PrincipioActivo;
import com.pe.articulos.modules.atributos.repository.PrincipioActivoRepository;
import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;
import com.pe.articulos.modules.venta_registro.repository.TipoDocumentoRepository;
import com.pe.articulos.modules.puntos.entity.Tipo;
import com.pe.articulos.modules.puntos.repository.TipoRepository;
import com.pe.articulos.modules.venta_registro.entity.TipoPaciente;
import com.pe.articulos.modules.venta_registro.repository.TipoPacienteRepository;
import com.pe.articulos.modules.atributos.entity.Ubicacion;
import com.pe.articulos.modules.atributos.repository.UbicacionRepository;
import com.pe.articulos.modules.catalogo.entity.UnidadMedida;
import com.pe.articulos.modules.catalogo.repository.UnidadMedidaRepository;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreParametrosInitializer {

    private final ClasificacionMovimientoRepository clasificacionMovimientoRepository;
    private final DashboardConfigRepository dashboardConfigRepository;
    private final DocumentoFormatoRepository documentoFormatoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PrincipioActivoRepository principioActivoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final TipoRepository tipoRepository;
    private final TipoPacienteRepository tipoPacienteRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    
    private final AccionTerapeuticaRepository accionTerapeuticaRepository;
    private final CategoriaRepository categoriaRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final ClinicaRepository clinicaRepository;

    private final com.pe.articulos.modules.documentos.services.PlantillaConfigService plantillaConfigService;

    private final ObjectMapper objectMapper;

    @Transactional
    public void run() {
        log.info("🚀 Starting Parametros Seeding from JSON files...");
        try {
            seed(accionTerapeuticaRepository, "data/seed/empresa/parametros/accion_terapeutica.json", new TypeReference<List<AccionTerapeutica>>() {});
            seed(categoriaRepository, "data/seed/empresa/parametros/categoria.json", new TypeReference<List<Categoria>>() {});
            seed(laboratorioRepository, "data/seed/empresa/parametros/laboratorio.json", new TypeReference<List<Laboratorio>>() {});
            seed(clinicaRepository, "data/seed/empresa/clinica.json", new TypeReference<List<Clinica>>() {});
            
            seed(clasificacionMovimientoRepository, "data/seed/empresa/parametros/clasificacion_movimiento.json", new TypeReference<List<ClasificacionMovimiento>>() {});
            seed(dashboardConfigRepository, "data/seed/sucursal/dashboard_config.json", new TypeReference<List<DashboardConfig>>() {});
            seed(documentoFormatoRepository, "data/seed/empresa/parametros/documento_formato.json", new TypeReference<List<DocumentoFormato>>() {});
            seed(metodoPagoRepository, "data/seed/empresa/parametros/metodo_pago.json", new TypeReference<List<MetodoPago>>() {});
            seed(principioActivoRepository, "data/seed/empresa/parametros/principio_activo.json", new TypeReference<List<PrincipioActivo>>() {});
            seed(tipoDocumentoRepository, "data/seed/empresa/parametros/tipo_documento.json", new TypeReference<List<TipoDocumento>>() {});
            seed(tipoRepository, "data/seed/empresa/parametros/tipo.json", new TypeReference<List<Tipo>>() {});
            seed(tipoPacienteRepository, "data/seed/empresa/parametros/tipo_paciente.json", new TypeReference<List<TipoPaciente>>() {});
            seed(ubicacionRepository, "data/seed/sucursal/ubicacion.json", new TypeReference<List<Ubicacion>>() {});
            seed(unidadMedidaRepository, "data/seed/empresa/parametros/unidad_medida.json", new TypeReference<List<UnidadMedida>>() {});

            plantillaConfigService.inicializarPlantillasPorDefecto();

            log.info("✅ Parametros Seeding COMPLETED.");
        } catch (Exception e) {
            log.error("❌ CRITICAL ERROR: Could not load parametros json files", e);
        }
    }

    private <T> void seed(org.springframework.data.jpa.repository.JpaRepository<T, ?> repository, String filePath, TypeReference<List<T>> typeReference) throws Exception {
        if (repository.count() == 0) {
            try (InputStream is = new ClassPathResource(filePath).getInputStream()) {
                List<T> entities = objectMapper.readValue(is, typeReference);
                
                // Reset IDs for fresh insert
                for(T entity : entities) {
                    try {
                        // Intentar buscar campos anotados con @Id y establecerlos en null
                        Class<?> currentClass = entity.getClass();
                        boolean found = false;
                        while (currentClass != null && currentClass != Object.class && !found) {
                            for (java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
                                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                                    field.setAccessible(true);
                                    field.set(entity, null);
                                    found = true;
                                    break;
                                }
                            }
                            currentClass = currentClass.getSuperclass();
                        }
                    } catch(Exception ignored) {
                        log.warn("Could not reset ID for {}", entity.getClass().getSimpleName());
                    }
                }
                
                repository.saveAll(entities);
                log.info("✓ {} successfully seeded ({} records).", filePath, entities.size());
            }
        }
    }
}
