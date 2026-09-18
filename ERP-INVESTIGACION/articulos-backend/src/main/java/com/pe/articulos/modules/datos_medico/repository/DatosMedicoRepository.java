package com.pe.articulos.modules.datos_medico.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.datos_medico.entity.DatosMedico;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.pe.articulos.core.enums.EstadoGeneral;

@Repository
public interface DatosMedicoRepository extends JpaRepository<DatosMedico, Long> {

        // ========== BÚSQUEDA SIN PAGINACIÓN ==========

        Optional<DatosMedico> findByNroCmp(String nroCmp);

        boolean existsByNroCmp(String nroCmp);

        List<DatosMedico> findByNombreMedContainingIgnoreCase(String nombreMed);

        List<DatosMedico> findByEstado(EstadoGeneral estado);

        List<DatosMedico> findByTipo(String tipo);

        List<DatosMedico> findByIdArea(Long idArea);

        List<DatosMedico> findByIdSucursal(Long idSucursal);

        List<DatosMedico> findByIdAreaAndEstado(Long idArea, EstadoGeneral estado);

        List<DatosMedico> findByIdSucursalAndEstado(Long idSucursal, EstadoGeneral estado);

        List<DatosMedico> findByTipoMedico(String tipoMedico);

        @Query("SELECT d FROM DatosMedico d WHERE d.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO")
        Page<DatosMedico> findAllActivos(Pageable pageable);

        @Query("SELECT d FROM DatosMedico d WHERE d.vacaciones = 'S' " +
                        "AND d.fechaInicioVacaciones <= :fecha " +
                        "AND d.fechaFinVacaciones >= :fecha")
        Page<DatosMedico> findEnVacaciones(@Param("fecha") LocalDate fecha, Pageable pageable);

        @Query("SELECT COUNT(d) FROM DatosMedico d WHERE d.vacaciones = 'S' " +
                        "AND d.fechaInicioVacaciones <= :fecha " +
                        "AND d.fechaFinVacaciones >= :fecha")
        long countEnVacaciones(@Param("fecha") LocalDate fecha);

        @Query("SELECT d FROM DatosMedico d WHERE d.capacitado = 'N' " +
                        "OR d.fechaCapacitacion IS NULL " +
                        "OR d.fechaCapacitacion < :fechaLimite")
        Page<DatosMedico> findPendientesCapacitacion(@Param("fechaLimite") LocalDate fechaLimite, Pageable pageable);

        @Query("SELECT COUNT(d) FROM DatosMedico d WHERE d.capacitado = 'N' " +
                        "OR d.fechaCapacitacion IS NULL " +
                        "OR d.fechaCapacitacion < :fechaLimite")
        long countPendientesCapacitacion(@Param("fechaLimite") LocalDate fechaLimite);

        @Query("SELECT d FROM DatosMedico d WHERE d.emergencia = 'S' AND d.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO")
        Page<DatosMedico> findMedicosEmergencia(Pageable pageable);

        @Query("SELECT d FROM DatosMedico d WHERE d.tipoMedico = :tipoMedico " +
                        "AND d.idArea = :idArea AND d.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO")
        Page<DatosMedico> findByTipoMedicoAndArea(
                        @Param("tipoMedico") String tipoMedico,
                        @Param("idArea") Long idArea,
                        Pageable pageable);

        @Query("SELECT d FROM DatosMedico d WHERE d.ultimaLiquidacion < :fechaLimite " +
                        "AND d.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO ORDER BY d.ultimaLiquidacion ASC")
        Page<DatosMedico> findProximosALiquidar(@Param("fechaLimite") LocalDate fechaLimite, Pageable pageable);

        @Query("SELECT d FROM DatosMedico d WHERE " +
                        "YEAR(CURRENT_DATE) - YEAR(d.fechaIngreso) >= :anios " +
                        "AND d.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO")
        Page<DatosMedico> findConAniosServicio(@Param("anios") int anios, Pageable pageable);

        // ========== BÚSQUEDA CON PAGINACIÓN ==========

        Page<DatosMedico> findByEstado(EstadoGeneral estado, Pageable pageable);

        Page<DatosMedico> findByTipo(String tipo, Pageable pageable);

        Page<DatosMedico> findByIdArea(Long idArea, Pageable pageable);

        Page<DatosMedico> findByIdSucursal(Long idSucursal, Pageable pageable);

        Page<DatosMedico> findByTipoMedico(String tipoMedico, Pageable pageable);

        @Query("SELECT d FROM DatosMedico d WHERE " +
                        "(:nombreMed IS NULL OR LOWER(d.nombreMed) LIKE LOWER(CONCAT('%', :nombreMed, '%'))) AND " +
                        "(:nroCmp IS NULL OR d.nroCmp = :nroCmp) AND " +
                        "(:estado IS NULL OR d.estado = :estado) AND " +
                        "(:tipo IS NULL OR d.tipo = :tipo) AND " +
                        "(:idArea IS NULL OR d.idArea = :idArea) AND " +
                        "(:idSucursal IS NULL OR d.idSucursal = :idSucursal) AND " +
                        "(:vacaciones IS NULL OR d.vacaciones = :vacaciones) AND " +
                        "(:tipoMedico IS NULL OR d.tipoMedico = :tipoMedico)")
        Page<DatosMedico> buscarConFiltros(
                        @Param("nombreMed") String nombreMed,
                        @Param("nroCmp") String nroCmp,
                        @Param("estado") EstadoGeneral estado,
                        @Param("tipo") String tipo,
                        @Param("idArea") Long idArea,
                        @Param("idSucursal") Long idSucursal,
                        @Param("vacaciones") String vacaciones,
                        @Param("tipoMedico") String tipoMedico,
                        Pageable pageable);

        // ========== CONSULTAS ESPECIALES ==========

        @Query("SELECT d FROM DatosMedico d " +
                        "LEFT JOIN FETCH d.area " +
                        "LEFT JOIN FETCH d.sucursal " +
                        "WHERE d.id = :id")
        Optional<DatosMedico> findByIdWithRelaciones(@Param("id") Long id);

        Long countByEstado(EstadoGeneral estado);

        Long countByTipo(String tipo);

        Long countByIdArea(Long idArea);

        Long countByIdSucursal(Long idSucursal);

        @Query("SELECT COUNT(d) FROM DatosMedico d WHERE d.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO AND d.tipo = :tipo")
        Long countActivosByTipo(@Param("tipo") String tipo);
}