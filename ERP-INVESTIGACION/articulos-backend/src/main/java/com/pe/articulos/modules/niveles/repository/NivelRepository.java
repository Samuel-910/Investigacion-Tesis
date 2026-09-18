package com.pe.articulos.modules.niveles.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.pe.articulos.modules.niveles.entity.Nivel;

import java.util.List;
import java.util.Optional;
import com.pe.articulos.core.enums.EstadoGeneral;

@Repository
public interface NivelRepository extends JpaRepository<Nivel, Long> {

        // Buscar por nombre
        Optional<Nivel> findByNombre(String nombre);

        Optional<Nivel> findByNombreIgnoreCase(String nombre);

        // Buscar niveles raíz (sin padre)
        List<Nivel> findByIdNivelPadreIsNullOrderByOrden();

        Page<Nivel> findByIdNivelPadreIsNull(Pageable pageable);

        // Buscar hijos directos de un nivel
        List<Nivel> findByIdNivelPadreOrderByOrden(Long idNivelPadre);

        // **NUEVO: Buscar hijos ordenados por código numérico**
        List<Nivel> findByIdNivelPadreOrderByNumNivel(Long idNivelPadre);

        // Buscar por nivel de jerarquía
        List<Nivel> findByNivelJerarquiaOrderByOrden(Integer nivelJerarquia);

        // Buscar por estado
        List<Nivel> findByEstadoOrderByOrden(EstadoGeneral estado);

        // Buscar con filtros
        @Query("SELECT n FROM Nivel n WHERE " +
                        "(:nombre IS NULL OR LOWER(n.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:tipo IS NULL OR n.tipo = :tipo) AND " +
                        "(:estado IS NULL OR n.estado = :estado) AND " +
                        "(:idNivelPadre IS NULL OR n.idNivelPadre = :idNivelPadre)")
        Page<Nivel> buscarConFiltros(
                        @Param("nombre") String nombre,
                        @Param("tipo") String tipo,
                        @Param("estado") EstadoGeneral estado,
                        @Param("idNivelPadre") Long idNivelPadre,
                        Pageable pageable);

        // Obtener jerarquía completa desde un nivel (recursivo)
        @Query(value = "WITH RECURSIVE jerarquia AS (" +
                        "  SELECT * FROM niveles WHERE id_nivel = :idNivel " +
                        "  UNION ALL " +
                        "  SELECT n.* FROM niveles n " +
                        "  INNER JOIN jerarquia j ON n.id_nivel_padre = j.id_nivel" +
                        ") SELECT * FROM jerarquia ORDER BY nivel_jerarquia, orden", nativeQuery = true)
        List<Nivel> obtenerJerarquiaCompleta(@Param("idNivel") Long idNivel);

        // Contar hijos directos
        Long countByIdNivelPadre(Long idNivelPadre);

        // Verificar si existe por nombre y padre
        boolean existsByNombreIgnoreCaseAndIdNivelPadre(String nombre, Long idNivelPadre);

        // **NUEVO: Verificar si existe código en niveles raíz**
        boolean existsByNumNivelAndIdNivelPadreIsNull(String numNivel);

        // **NUEVO: Verificar si existe código en general**
        boolean existsByNumNivel(String numNivel);

        // **NUEVO: Buscar por código**
        Optional<Nivel> findByNumNivel(String numNivel);

        // Obtener nivel máximo de jerarquía
        @Query("SELECT MAX(n.nivelJerarquia) FROM Nivel n")
        Integer obtenerNivelMaximoJerarquia();

        Long countByEstado(EstadoGeneral estado);

        @Query("SELECT DISTINCT n FROM Nivel n WHERE EXISTS " +
                        "(SELECT 1 FROM Nivel h WHERE h.idNivelPadre = n.idNivel)")
        List<Nivel> findNivelesConHijos();

        @Query("SELECT n FROM Nivel n WHERE n.idNivelPadre = :idNivelPadre " +
                        "ORDER BY n.numNivel DESC")
        List<Nivel> findTopByIdNivelPadreOrderByNumNivelDesc(@Param("idNivelPadre") Long idNivelPadre);
}