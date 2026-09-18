package com.pe.articulos.core.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.core.security.entity.FailureReason;
import com.pe.articulos.core.security.entity.IntrusionAttempt;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IntrusionAttemptRepository extends JpaRepository<IntrusionAttempt, Long> {

        long countByIpAddressAndAttemptTimeAfter(String ipAddress, LocalDateTime sinceTime);

        long countByUsernameAndAttemptTimeAfter(String username, LocalDateTime sinceTime);

        List<IntrusionAttempt> findByIpAddressAndAttemptTimeAfterOrderByAttemptTimeDesc(
                        String ipAddress,
                        LocalDateTime sinceTime);

        List<IntrusionAttempt> findByUsernameAndAttemptTimeAfterOrderByAttemptTimeDesc(
                        String username,
                        LocalDateTime sinceTime);

        List<IntrusionAttempt> findByFailureReasonAndAttemptTimeBetween(
                        FailureReason reason,
                        LocalDateTime startTime,
                        LocalDateTime endTime);

        void deleteByAttemptTimeBefore(LocalDateTime beforeTime);

        @Query("SELECT COUNT(i) >= :threshold FROM IntrusionAttempt i " +
                        "WHERE i.ipAddress = :ipAddress " +
                        "AND i.attemptTime >= :sinceTime")
        boolean isIpBlocked(
                        @Param("ipAddress") String ipAddress,
                        @Param("sinceTime") LocalDateTime sinceTime,
                        @Param("threshold") long threshold);

        @Query("SELECT COUNT(i) >= :threshold FROM IntrusionAttempt i " +
                        "WHERE i.username = :username " +
                        "AND i.attemptTime >= :sinceTime")
        boolean hasExcessiveFailedAttempts(
                        @Param("username") String username,
                        @Param("sinceTime") LocalDateTime sinceTime,
                        @Param("threshold") long threshold);

        List<IntrusionAttempt> findByAttemptTimeAfterOrderByAttemptTimeDesc(LocalDateTime sinceTime);
}
