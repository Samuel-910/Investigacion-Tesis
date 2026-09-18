package com.pe.articulos.modules.puntos.repository;

import com.pe.articulos.modules.puntos.entity.Punto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PuntoRepository extends JpaRepository<Punto, Long> {

        // Buscar por nombre (exacto)
        Optional<Punto> findByNombre(String nombre);

        // Buscar por nombre que contenga (case insensitive)
        List<Punto> findByNombreContainingIgnoreCase(String nombre);

        // Buscar por tipo
        List<Punto> findByTipo(String tipo);

        // Buscar por sucursal
        List<Punto> findByIdSucursal(Integer idSucursal);

        // Buscar por estado válido
        List<Punto> findByValido(String valido);

        // Buscar por tipo y sucursal
        List<Punto> findByTipoAndIdSucursal(String tipo, Integer idSucursal);

        // Buscar por estado y sucursal
        List<Punto> findByValidoAndIdSucursal(String valido, Integer idSucursal);

        // Buscar por estado y sucursal paginado
        Page<Punto> findByValidoAndIdSucursal(String valido, Integer idSucursal, Pageable pageable);

        // Buscar por abreviación
        Optional<Punto> findByAbreviacion(String abreviacion);

        // Query personalizada para buscar puntos activos
        @Query("SELECT p FROM Punto p WHERE p.valido = 'S' ORDER BY p.nombre")
        List<Punto> findAllActivos();

        // Query para buscar por múltiples criterios
        @Query("SELECT p FROM Punto p WHERE " +
                        "(CAST(:nombre AS string) IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%'))) AND " +
                        "(CAST(:tipo AS string) IS NULL OR p.tipo = CAST(:tipo AS string)) AND " +
                        "(:idSucursal IS NULL OR p.idSucursal = :idSucursal)")
        List<Punto> buscarPorCriterios(@Param("nombre") String nombre,
                        @Param("tipo") String tipo,
                        @Param("idSucursal") Integer idSucursal);

        // Verificar si existe por nombre
        boolean existsByNombre(String nombre);

        // Contar puntos por sucursal
        long countByIdSucursal(Integer idSucursal);

        // Query para buscar dinámicamente según el criterio seleccionado y sucursal
        @Query("SELECT p FROM Punto p LEFT JOIN p.sucursal s WHERE " +
                        "(p.idSucursal = :idSucursal) AND (" +
                        "(:tipo = 'ALL' AND (" +
                        "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
                        "LOWER(p.tipo) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
                        "LOWER(p.tippro) LIKE LOWER(CONCAT('%', :q, '%')))" +
                        ") OR " +
                        "(:tipo = 'NOMBRE' AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%'))) OR " +
                        "(:tipo = 'TIPO' AND LOWER(p.tipo) LIKE LOWER(CONCAT('%', :q, '%'))) OR " +
                        "(:tipo = 'PROCESO' AND LOWER(p.tippro) LIKE LOWER(CONCAT('%', :q, '%')))" +
                        ")")
        Page<Punto> buscarGlobal(@Param("q") String q,
                        @Param("tipo") String tipo,
                        @Param("idSucursal") Integer idSucursal,
                        Pageable pageable);
}
