package com.pe.articulos.modules.roles.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.roles.entity.Role;
import com.pe.articulos.modules.permissions.entity.Permission;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Page<Role> findByActiveTrue(Pageable pageable);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);

    @Query("SELECT p FROM Permission p WHERE p.module = :module")
    Page<Permission> findByModule(@Param("module") String module, Pageable pageable);

    @Query("SELECT p FROM Permission p WHERE p.module = :module ORDER BY p.name ASC")
    List<Permission> findByModuleOrderByName(@Param("module") String module);

    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Role> searchByName(@Param("name") String name, Pageable pageable);

    Page<Role> findByAccesoNombre(String accesoNombre, Pageable pageable);

    // Buscar activos de un módulo
    Page<Role> findByActiveTrueAndAccesoNombre(String accesoNombre, Pageable pageable);

    // Buscar por ID y asegurar el módulo (Seguridad extra)
    Optional<Role> findByIdAndAccesoNombre(Long id, String accesoNombre);

    // Búsqueda por filtro (Query personalizada)
    @Query("SELECT r FROM Role r WHERE UPPER(r.name) LIKE UPPER(CONCAT('%', :query, '%')) " +
            "AND r.acceso.nombre = :accesoNombre")
    Page<Role> searchByNameAndAcceso(String query, String accesoNombre, Pageable pageable);

    // Mantén tus métodos con Fetch Join si los usas para evitar el problema de N+1
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id AND r.acceso.nombre = :accesoNombre")
    Optional<Role> findByIdWithPermissionsAndAcceso(Long id, String accesoNombre);
}