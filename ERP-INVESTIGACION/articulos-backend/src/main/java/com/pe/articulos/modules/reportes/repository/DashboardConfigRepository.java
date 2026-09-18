package com.pe.articulos.modules.reportes.repository;

import com.pe.articulos.modules.reportes.entity.DashboardConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DashboardConfigRepository extends JpaRepository<DashboardConfig, Long> {
        List<DashboardConfig> findByIdSucursalAndActivoTrue(Long idSucursal);

        @Query("SELECT DISTINCT d.categoria FROM DashboardConfig d WHERE d.idSucursal = :idSucursal AND d.activo = true")
        List<String> findDistinctCategoriaByIdSucursalAndActivoTrue(@Param("idSucursal") Long idSucursal);

        Optional<DashboardConfig> findFirstByIdSucursalAndCategoriaAndActivoTrue(Long idSucursal, String categoria);
}
