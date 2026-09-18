package com.pe.articulos.core.reports.audit.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.core.reports.audit.entity.AuditLog;

@Repository
public interface AuditRepository extends JpaRepository<AuditLog, Long> {

        @Query("SELECT a FROM AuditLog a WHERE " +
                        "(:username IS NULL OR a.username LIKE :username) AND " +
                        "a.action IN :actions AND " +
                        "a.timestamp >= :startDate AND " +
                        "a.timestamp <= :endDate " +
                        "ORDER BY a.timestamp DESC")
        List<AuditLog> searchLogs(
                        @Param("username") String username,
                        @Param("actions") List<String> actions,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT a FROM AuditLog a WHERE " +
                        "(:username IS NULL OR a.username LIKE :username) AND " +
                        "a.timestamp >= :startDate AND " +
                        "a.timestamp <= :endDate " +
                        "ORDER BY a.timestamp DESC")
        List<AuditLog> searchLogsNoActions(
                        @Param("username") String username,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

        List<AuditLog> findAllByOrderByTimestampDesc();
}
