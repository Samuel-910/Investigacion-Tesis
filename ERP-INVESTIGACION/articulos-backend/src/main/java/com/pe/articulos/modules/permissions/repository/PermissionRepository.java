package com.pe.articulos.modules.permissions.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.permissions.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

        // ========== BÚSQUEDA INDIVIDUAL ==========

        Optional<Permission> findByName(String name);

        boolean existsByName(String name);

        // ========== BÚSQUEDA POR CONJUNTO DE IDS ==========

        Set<Permission> findByIdIn(Set<Long> ids);

        // ========== LISTADOS SIN PAGINACIÓN ==========

        List<Permission> findByActiveTrue();

        List<Permission> findByActive(Boolean active);

        List<Permission> findByModuleAndActive(String module, Boolean active);

        @Query("SELECT p FROM Permission p WHERE p.module = :module ORDER BY p.name ASC")
        List<Permission> findByModuleOrderByName(@Param("module") String module);

        @Query("SELECT p FROM Permission p WHERE " +
                        "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
        List<Permission> searchPermissions(@Param("search") String search);

        // ========== LISTADOS CON PAGINACIÓN ==========

        Page<Permission> findByActive(Boolean active, Pageable pageable);

        Page<Permission> findByModule(String module, Pageable pageable);

        Page<Permission> findByModuleAndActive(String module, Boolean active, Pageable pageable);

        @Query("SELECT p FROM Permission p WHERE " +
                        "(:target = 'name' AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
                        "(:target = 'desc' AND LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
                        "(:target = 'all' AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))))")
        Page<Permission> searchPermissionsAdvanced(
                        @Param("search") String search,
                        @Param("target") String target,
                        Pageable pageable);
        // ========== CONSULTAS ESPECIALES ==========

        @Query("SELECT DISTINCT p.module FROM Permission p WHERE p.active = true ORDER BY p.module ASC")
        List<String> findDistinctModules();

        @Query("SELECT COUNT(p) FROM Permission p WHERE p.module = :module AND p.active = true")
        Long countActiveByModule(@Param("module") String module);

        @Query("SELECT COUNT(p) FROM Permission p WHERE p.active = true")
        Long countActive();

        Page<Permission> findByAccesoNombre(String nombreAcceso, Pageable pageable);

        Page<Permission> findByActiveTrueAndAccesoNombre(String nombreAcceso, Pageable pageable);

        List<Permission> findByActiveTrueAndAccesoNombre(String nombreAcceso);

        @Query("SELECT p FROM Permission p WHERE " +
                        "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
                        "AND p.acceso.nombre = :nombreAcceso")
        Page<Permission> searchByQueryAndAcceso(String query, String nombreAcceso, Pageable pageable);

        boolean existsByNameAndAccesoNombre(String name, String nombreAcceso);

        @Query("SELECT p FROM Permission p WHERE " +
                        "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(p.module) LIKE LOWER(CONCAT('%', :query, '%'))) " +
                        "AND p.acceso.nombre = :nombreAcceso")
        Page<Permission> searchPermissionsInAcceso(@Param("query") String query,
                        @Param("nombreAcceso") String nombreAcceso,
                        Pageable pageable);

        @Query("SELECT DISTINCT p.module FROM Permission p WHERE p.acceso.nombre = :nombreAcceso")
        List<String> findDistinctModulesInAcceso(@Param("nombreAcceso") String nombreAcceso);

        // Métodos por módulo
        Page<Permission> findByModuleAndAccesoNombre(String module, String accesoNombre, Pageable pageable);

        List<Permission> findByModuleAndAccesoNombre(String module, String accesoNombre);

        Page<Permission> findByModuleAndActiveTrueAndAccesoNombre(String module, String accesoNombre,
                        Pageable pageable);

        List<Permission> findByModuleAndActiveTrueAndAccesoNombre(String module, String accesoNombre);

}