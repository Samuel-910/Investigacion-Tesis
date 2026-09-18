package com.pe.articulos.modules.users.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.pe.articulos.modules.users.entity.UserPermission;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
        
        @Query("SELECT up FROM UserPermission up WHERE up.user.id = :id")
        List<UserPermission> findByUserId(@Param("id") Long id);
        
        @Query("SELECT up FROM UserPermission up WHERE up.user.id = :id AND up.active = :active")
        List<UserPermission> findByUserIdAndActive(@Param("id") Long id, @Param("active") Boolean active);
        
        @Query("SELECT up FROM UserPermission up WHERE up.user.id = :id AND up.permission.id = :permissionId")
        Optional<UserPermission> findByUserIdAndPermissionId(@Param("id") Long id,
                        @Param("permissionId") Long permissionId);
        
        @Query("SELECT up FROM UserPermission up WHERE up.expiresAt IS NOT NULL " +
                        "AND up.expiresAt BETWEEN :now AND :futureDate AND up.active = true")
        List<UserPermission> findExpiringPermissions(
                        @Param("now") LocalDateTime now,
                        @Param("futureDate") LocalDateTime futureDate);
        
        @Query("SELECT up FROM UserPermission up WHERE up.expiresAt IS NOT NULL " +
                        "AND up.expiresAt < :now AND up.active = true")
        List<UserPermission> findExpiredPermissions(@Param("now") LocalDateTime now);
        
        @Query("SELECT COUNT(up) FROM UserPermission up WHERE up.user.id = :id AND up.active = :active")
        long countByUserIdAndActive(@Param("id") Long id, @Param("active") Boolean active);
        
        List<UserPermission> findByGrantedBy(Long grantedBy);
        
        @Modifying
        @Query("DELETE FROM UserPermission up WHERE up.user.id = :id")
        void deleteByUserId(@Param("id") Long id);
        
        @Modifying
        @Query("DELETE FROM UserPermission up WHERE up.user.id = :id AND up.permission.id IN :permissionIds")
        void deleteByUserIdAndPermissionIdIn(@Param("id") Long id, @Param("permissionIds") List<Long> permissionIds);
        
        @Query("SELECT DISTINCT up.user.id FROM UserPermission up WHERE up.active = true")
        List<Long> findUsersWithDirectPermissions();
        
        @Query("SELECT up.user.id, COUNT(up) FROM UserPermission up WHERE up.active = true GROUP BY up.user.id")
        List<Object[]> countPermissionsByUser();
}
