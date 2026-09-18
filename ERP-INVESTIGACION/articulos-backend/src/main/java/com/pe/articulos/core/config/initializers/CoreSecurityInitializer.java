package com.pe.articulos.core.config.initializers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.articulos.core.menu.entity.MenuItem;
import com.pe.articulos.core.menu.repository.MenuItemRepository;
import com.pe.articulos.modules.accesos.entity.AccesoMain;
import com.pe.articulos.modules.accesos.repository.AccesoMainRepository;
import com.pe.articulos.modules.permissions.entity.Permission;
import com.pe.articulos.modules.permissions.repository.PermissionRepository;
import com.pe.articulos.modules.roles.entity.Role;
import com.pe.articulos.modules.roles.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreSecurityInitializer {

    private final AccesoMainRepository accesoMainRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final MenuItemRepository menuItemRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void run() {
        log.info("🚀 Starting Core Security Seeding from JSON files...");
        try {
            seedAccesos(loadJson("data/seed/empresa/accesos.json"));
            seedPermissions(loadJson("data/seed/empresa/permisos.json"));
            seedRoles(loadJson("data/seed/empresa/roles.json"));
            seedMenus(loadJson("data/seed/empresa/menus.json"));

            log.info("✅ Core Security Seeding COMPLETED.");
        } catch (Exception e) {
            log.error("❌ CRITICAL ERROR: Could not load seed JSON files", e);
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

    private void seedAccesos(JsonNode accesosNode) {
        if (accesosNode == null) return;
        
        List<AccesoMain> accesosToSave = new ArrayList<>();
        accesosNode.forEach(n -> {
            String nombre = n.get("nombre").asText();
            if (accesoMainRepository.findByNombre(nombre).isEmpty()) {
                AccesoMain acceso = AccesoMain.builder()
                        .nombre(nombre)
                        .build();
                accesosToSave.add(acceso);
            }
        });
        if (!accesosToSave.isEmpty()) {
            accesoMainRepository.saveAll(accesosToSave);
            log.info("✓ Accesos faltantes restaurados: {}", accesosToSave.size());
        }
    }

    private void seedPermissions(JsonNode permsNode) {
        if (permsNode == null) return;
        
        AccesoMain accesoArticulos = accesoMainRepository.findByNombre("Articulos").orElse(null);
        int added = 0;
        for (JsonNode n : permsNode) {
            String name = n.get("name").asText();
            if (!permissionRepository.existsByName(name)) {
                Permission p = Permission.builder()
                        .name(name)
                        .description(n.get("description").asText())
                        .module(n.get("module").asText())
                        .active(true)
                        .acceso(accesoArticulos)
                        .build();
                permissionRepository.save(p);
                added++;
            }
        }
        if (added > 0) log.info("✓ Permisos faltantes restaurados: {}", added);
    }

    private void seedRoles(JsonNode rolesNode) {
        if (rolesNode == null) return;
        
        List<Permission> allPermissions = permissionRepository.findAll();
        AccesoMain accesoArticulos = accesoMainRepository.findByNombre("Articulos").orElse(null);
        int added = 0;

        for (JsonNode n : rolesNode) {
            String name = n.get("name").asText();
            if (roleRepository.findByName(name).isEmpty()) {
                Set<Permission> rolePerms = new HashSet<>();
                JsonNode pNode = n.get("permissions");
                if (pNode != null && pNode.isArray()) {
                    pNode.forEach(permNode -> {
                        String permName = permNode.asText();
                        if ("*".equals(permName)) {
                            rolePerms.addAll(allPermissions);
                        } else {
                            permissionRepository.findByName(permName).ifPresent(rolePerms::add);
                        }
                    });
                }

                Role role = Role.builder()
                        .name(name)
                        .description(n.get("description").asText())
                        .active(true)
                        .permissions(rolePerms)
                        .acceso(accesoArticulos)
                        .build();
                roleRepository.save(role);
                added++;
            }
        }
        if (added > 0) log.info("✓ Roles faltantes restaurados: {}", added);
    }

    private void seedMenus(JsonNode menusNode) {
        if (menusNode == null) return;
        seedMenuRecursive(menusNode, null);
    }

    private void seedMenuRecursive(JsonNode menusNode, MenuItem parent) {
        if (menusNode == null) return;
        AccesoMain accesoArticulos = accesoMainRepository.findByNombre("Articulos").orElse(null);

        for (JsonNode n : menusNode) {
            String titulo = n.get("titulo").asText();
            
            // Verificar si el menú ya existe
            MenuItem existingItem = null;
            if (parent == null) {
                existingItem = menuItemRepository.findRootItems().stream()
                    .filter(m -> m.getTitulo().equals(titulo))
                    .findFirst().orElse(null);
            } else {
                existingItem = menuItemRepository.findByParentId(parent.getId()).stream()
                    .filter(m -> m.getTitulo().equals(titulo))
                    .findFirst().orElse(null);
            }

            MenuItem savedItem = existingItem;
            
            if (existingItem == null) {
                String tipoControlStr = n.has("moduloRequerido") && !n.get("moduloRequerido").asText().equals("PUBLICO") 
                        ? "MODULO" : "PUBLICO";
                
                MenuItem item = MenuItem.builder()
                        .titulo(titulo)
                        .icono(n.has("icono") ? n.get("icono").asText() : null)
                        .ruta(n.has("ruta") ? n.get("ruta").asText() : null)
                        .orden(n.get("orden").asInt())
                        .tipo(MenuItem.TipoMenuItem.valueOf(n.get("tipo").asText()))
                        .tipoControl(MenuItem.TipoControl.valueOf(tipoControlStr))
                        .moduloRequerido(n.has("moduloRequerido") ? n.get("moduloRequerido").asText() : null)
                        .acceso(accesoArticulos)
                        .parent(parent)
                        .activo(true)
                        .build();
                
                savedItem = menuItemRepository.save(item);
                log.info("✓ Menú faltante restaurado: {}", titulo);
            }

            if (n.has("children")) {
                seedMenuRecursive(n.get("children"), savedItem);
            }
        }
    }
}
