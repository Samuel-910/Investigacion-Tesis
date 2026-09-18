package com.pe.articulos.modules.reportes.repository;

import com.pe.articulos.modules.reportes.entity.ReportColumnConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReportColumnConfigRepository extends JpaRepository<ReportColumnConfig, Long> {
    Optional<ReportColumnConfig> findByReportKeyAndIdSucursal(String reportKey, Long idSucursal);
}
