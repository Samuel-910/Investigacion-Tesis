package com.pe.articulos.modules.users.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface DatosPersonalesRepository extends JpaRepository<DatosPersonales, Long> {
        
        
        
        Optional<DatosPersonales> findByLogin(String login);
        
        Optional<DatosPersonales> findByEmail(String email);
        
        Optional<DatosPersonales> findByid(Long id);
        
        Optional<DatosPersonales> findByNumdoc(String numdoc);
        
        @Query("SELECT d FROM DatosPersonales d WHERE d.login = :idPersonalUser")
        Optional<DatosPersonales> findByIdPersonalUser(@Param("idPersonalUser") String idPersonalUser);
        
        
        
        boolean existsByLogin(String login);
        
        boolean existsByEmail(String email);
        
        boolean existsByid(Long id);
        
        boolean existsByNumdoc(String numdoc);
        
        @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DatosPersonales d " +
                        "WHERE d.login = :login AND d.id <> :excludeId")
        boolean existsByLoginExcludingId(@Param("login") String login, @Param("excludeId") Long excludeId);
        
        @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DatosPersonales d " +
                        "WHERE d.email = :email AND d.id <> :excludeId")
        boolean existsByEmailExcludingId(@Param("email") String email, @Param("excludeId") Long excludeId);
        
        @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DatosPersonales d " +
                        "WHERE d.numdoc = :numdoc AND d.id <> :excludeId")
        boolean existsByNumdocExcludingId(@Param("numdoc") String numdoc, @Param("excludeId") Long excludeId);
        
        
        
        @Query("SELECT d FROM DatosPersonales d WHERE d.login = :loginOrEmail OR d.email = :loginOrEmail")
        Optional<DatosPersonales> findByLoginOrEmail(@Param("loginOrEmail") String loginOrEmail);
        
        @Query("SELECT d FROM DatosPersonales d WHERE d.login = :login AND d.active = true")
        Optional<DatosPersonales> findActiveByLogin(@Param("login") String login);
        
        @Query("SELECT d FROM DatosPersonales d WHERE d.email = :email AND d.active = true")
        Optional<DatosPersonales> findActiveByEmail(@Param("email") String email);
        
        @Query("SELECT d FROM DatosPersonales d " +
                        "WHERE (d.login = :loginOrEmail OR d.email = :loginOrEmail) AND d.active = true")
        Optional<DatosPersonales> findActiveByLoginOrEmail(@Param("loginOrEmail") String loginOrEmail);
        
        
        
        @Query("SELECT DISTINCT d FROM DatosPersonales d " +
                        "LEFT JOIN FETCH d.roles r " +
                        "LEFT JOIN FETCH r.permissions " +
                        "WHERE d.login = :login")
        Optional<DatosPersonales> findByLoginWithRoles(@Param("login") String login);
        
        @Query("SELECT DISTINCT d FROM DatosPersonales d " +
                        "LEFT JOIN FETCH d.roles r " +
                        "LEFT JOIN FETCH r.permissions " +
                        "WHERE d.email = :email")
        Optional<DatosPersonales> findByEmailWithRoles(@Param("email") String email);
        
        @Query("SELECT DISTINCT d FROM DatosPersonales d " +
                        "LEFT JOIN FETCH d.roles r " +
                        "LEFT JOIN FETCH r.permissions " +
                        "WHERE d.id = :id")
        Optional<DatosPersonales> findByidWithRoles(@Param("id") Long id);
        
        @Query("SELECT DISTINCT d FROM DatosPersonales d " +
                        "LEFT JOIN FETCH d.roles r " +
                        "LEFT JOIN FETCH r.permissions " +
                        "WHERE d.login = :loginOrEmail OR d.email = :loginOrEmail")
        Optional<DatosPersonales> findByLoginOrEmailWithRoles(@Param("loginOrEmail") String loginOrEmail);
        
        
        
        @Query("SELECT d FROM DatosPersonales d WHERE d.active = true")
        Page<DatosPersonales> findAllActive(Pageable pageable);
        
        @Query("SELECT d FROM DatosPersonales d WHERE d.active = false")
        Page<DatosPersonales> findAllInactive(Pageable pageable);
        
        @Query("SELECT d FROM DatosPersonales d WHERE " +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.numdoc) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<DatosPersonales> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);
        
        @Query("SELECT d FROM DatosPersonales d WHERE (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.numdoc) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.login) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<DatosPersonales> searchByAnyTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
        
        @Query("SELECT d FROM DatosPersonales d WHERE d.active = true AND (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.numdoc) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<DatosPersonales> searchActiveByName(@Param("searchTerm") String searchTerm, Pageable pageable);
        
        @Query("SELECT d FROM DatosPersonales d JOIN d.roles r WHERE r.name = :roleName")
        Page<DatosPersonales> findByRoleName(@Param("roleName") String roleName, Pageable pageable);
        
        @Query("SELECT d FROM DatosPersonales d JOIN d.roles r " +
                        "WHERE r.name = :roleName AND d.active = true")
        Page<DatosPersonales> findActiveByRoleName(@Param("roleName") String roleName, Pageable pageable);
        
        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r " +
                        "WHERE r.name IN :roleNames")
        Page<DatosPersonales> findByRoleNames(@Param("roleNames") List<String> roleNames, Pageable pageable);
        
        @Query("SELECT d FROM DatosPersonales d WHERE LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%'))")
        Page<DatosPersonales> searchByEmail(@Param("email") String email, Pageable pageable);
        
        @Query("SELECT d FROM DatosPersonales d WHERE LOWER(d.login) LIKE LOWER(CONCAT('%', :login, '%'))")
        Page<DatosPersonales> searchByLogin(@Param("login") String login, Pageable pageable);
        
        
        
        @Query("SELECT COUNT(d) FROM DatosPersonales d WHERE d.active = true")
        long countActiveUsers();
        
        @Query("SELECT COUNT(d) FROM DatosPersonales d WHERE d.active = false")
        long countInactiveUsers();
        
        @Query("SELECT COUNT(DISTINCT d) FROM DatosPersonales d JOIN d.roles r WHERE r.name = :roleName")
        long countByRoleName(@Param("roleName") String roleName);
        
        @Query("SELECT COUNT(DISTINCT d) FROM DatosPersonales d JOIN d.roles r " +
                        "WHERE r.name = :roleName AND d.active = true")
        long countActiveByRoleName(@Param("roleName") String roleName);
        
        @Query("SELECT DISTINCT d FROM DatosPersonales d " +
               "LEFT JOIN d.roles r LEFT JOIN r.acceso a " +
               "LEFT JOIN d.modulosAsignados m " +
               "WHERE (LOWER(a.nombre) = LOWER(:moduloNombre) OR LOWER(m.nombre) = LOWER(:moduloNombre)) " +
               "AND d.active = true")
        Page<DatosPersonales> findActiveByModuloNombre(@Param("moduloNombre") String moduloNombre, Pageable pageable);
        
        
        
        @Query("SELECT d FROM DatosPersonales d WHERE d.email LIKE CONCAT('%@', :domain)")
        Page<DatosPersonales> findByEmailDomain(@Param("domain") String domain, Pageable pageable);
        
        
        
        @Query("SELECT d FROM DatosPersonales d WHERE " +
                        "(:nombre IS NULL OR LOWER(d.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:apepat IS NULL OR LOWER(d.apepat) LIKE LOWER(CONCAT('%', :apepat, '%'))) AND " +
                        "(:email IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
                        "(:active IS NULL OR d.active = :active)")
        Page<DatosPersonales> findByFilters(
                        @Param("nombre") String nombre,
                        @Param("apepat") String apepat,
                        @Param("email") String email,
                        @Param("active") Boolean active,
                        Pageable pageable);
}
