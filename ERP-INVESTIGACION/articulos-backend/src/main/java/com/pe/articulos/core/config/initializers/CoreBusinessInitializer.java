package com.pe.articulos.core.config.initializers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.almacen.repository.AlmacenRepository;
import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.puntos.repository.PuntoRepository;
import com.pe.articulos.modules.puntos.repository.TipoRepository;
import com.pe.articulos.modules.roles.repository.RoleRepository;
import com.pe.articulos.modules.sucursal.repository.SucursalRepository;
import com.pe.articulos.modules.sucursal.service.SucursalService;
import com.pe.articulos.modules.sucursal.dto.SucursalDto;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.modules.accesos.repository.AccesoMainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreBusinessInitializer {

    private final SucursalRepository sucursalRepository;
    private final SucursalService sucursalService;
    private final PuntoRepository puntoRepository;
    private final TipoRepository tipoRepository;
    private final AlmacenRepository almacenRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final RoleRepository roleRepository;
    private final AccesoMainRepository accesoMainRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Transactional
    public void run() {
        log.info("🚀 Starting Core Business Seeding from JSON files...");
        try {
            seedSucursales(loadJson("data/seed/empresa/sucursales.json"));
            seedUsuarios(loadJson("data/seed/empresa/usuarios.json"));

            log.info("✅ Core Business Seeding COMPLETED.");
        } catch (Exception e) {
            log.error("❌ CRITICAL ERROR: Could not load business seed JSON files", e);
        }
    }

    private JsonNode loadJson(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readTree(is);
        } catch (Exception e) {
            log.error("❌ No se pudo cargar " + path, e);
            return null;
        }
    }

    private void seedSucursales(JsonNode nodes) {
        if (nodes == null)
            return;
        for (JsonNode n : nodes) {
            String nombre = n.get("nombreSucursal").asText();
            if (sucursalRepository.findAll().stream().noneMatch(s -> s.getNombreSucursal().equals(nombre))) {
                SucursalDto s = new SucursalDto();
                s.setNombreSucursal(nombre);
                s.setDireccion(n.has("direccion") ? n.get("direccion").asText() : null);
                s.setTelefono(n.has("telefono") ? n.get("telefono").asText() : null);
                s.setEstado(EstadoGeneral.valueOf(n.has("estado") ? n.get("estado").asText() : "ACTIVO"));

                try {
                    sucursalService.crear(s);
                    log.info("✓ Sucursal creada vía Service (incluye Punto y Docs): {}", nombre);
                } catch (Exception e) {
                    log.error("❌ Error creando sucursal vía Service: {}", nombre, e);
                }
            }
        }
    }

    private void seedUsuarios(JsonNode nodes) {
        if (nodes == null)
            return;
        for (JsonNode n : nodes) {
            String login = n.get("login").asText();
            datosPersonalesRepository.findByLogin(login).ifPresentOrElse(
                    u -> {
                    },
                    () -> {
                        DatosPersonales u = new DatosPersonales();
                        u.setLogin(login);
                        u.setEmail(n.has("email") ? n.get("email").asText() : null);
                        u.setPasswd(passwordEncoder.encode(n.has("password") ? n.get("password").asText() : "12345"));
                        u.setNombre(n.has("nombre") ? n.get("nombre").asText() : login);
                        u.setApepat(n.has("apepat") ? n.get("apepat").asText() : "");
                        u.setApemat(n.has("apemat") ? n.get("apemat").asText() : "");
                        u.setVerNombre(u.getNombre());
                        u.setVerApepat(u.getApepat());
                        u.setVerApemat(u.getApemat());
                        u.setSexo("M");
                        u.setTipodoc("01");
                        u.setNumdoc(n.has("numdoc") ? n.get("numdoc").asText() : "00000000");
                        u.setRhc(n.has("rhc") ? n.get("rhc").asText() : "HC-" + login);
                        if (n.has("nacfec")) {
                            u.setNacfec(LocalDate.parse(n.get("nacfec").asText()));
                        }
                        u.setActive(true);

                        if (u.getRoles() == null)
                            u.setRoles(new HashSet<>());
                        if (n.has("roles")) {
                            for (JsonNode rn : n.get("roles")) {
                                roleRepository.findByName(rn.asText()).ifPresent(r -> u.getRoles().add(r));
                            }
                        }

                        if (n.has("punto")) {
                            String puntoNombre = n.get("punto").asText();
                            puntoRepository.findAll().stream()
                                    .filter(p -> p.getNombre() != null && p.getNombre().equals(puntoNombre))
                                    .findFirst()
                                    .ifPresent(u::setPunto);
                        }

                        if (n.has("sucursales")) {
                            if (u.getSucursalesAsignadas() == null)
                                u.setSucursalesAsignadas(new HashSet<>());
                            for (JsonNode sn : n.get("sucursales")) {
                                sucursalRepository.findAll().stream()
                                        .filter(s -> s.getNombreSucursal().equals(sn.asText()))
                                        .findFirst()
                                        .ifPresent(s -> {
                                            u.getSucursalesAsignadas().add(s);
                                            if (u.getSucursalActual() == null)
                                                u.setSucursalActual(s);
                                        });
                            }
                        }

                        if (n.has("modulos")) {
                            if (u.getModulosAsignados() == null)
                                u.setModulosAsignados(new HashSet<>());
                            for (JsonNode mn : n.get("modulos")) {
                                accesoMainRepository.findByNombre(mn.asText())
                                        .ifPresent(u.getModulosAsignados()::add);
                            }
                        }

                        datosPersonalesRepository.save(u);
                        log.info("✓ Usuario creado: {}", login);
                    });
        }
    }
}
