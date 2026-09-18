package com.pe.articulos.modules.catalogo.repository;

import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogoRepository extends JpaRepository<Catalogo, Long> {

        boolean existsByCodigo(String codigo);

        Optional<Catalogo> findByCodigo(String codigo);

        @Query("SELECT ps FROM Catalogo ps LEFT JOIN Categoria cat ON ps.idCategoria = cat.id WHERE " +
                        "( " +
                        "  ((:searchType IS NULL OR :searchType = 'ALL' OR :searchType = '') AND ( " +
                        "    LOWER(ps.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "    LOWER(ps.codigo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "  )) OR " +
                        "  (:searchType = 'NOMBRE' AND LOWER(ps.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR " +
                        "  (:searchType = 'CODIGO' AND LOWER(ps.codigo) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR " +
                        "  (:searchType = 'CATEGORIA' AND LOWER(cat.descripcion) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        ") " +
                        "AND (:tipo IS NULL OR ps.tipo = :tipo) " +
                        "AND (:idCategoria IS NULL OR ps.idCategoria = :idCategoria) " +
                        "AND (:esGenerico IS NULL OR ps.esGenerico = :esGenerico) " +
                        "AND (:manejaLotes IS NULL OR ps.manejaLotes = :manejaLotes) " +
                        "AND (:estado IS NULL OR ps.estado = :estado) " +
                        "AND (:tipoAfectacion IS NULL OR ps.tipoAfectacion = :tipoAfectacion)")
        Page<Catalogo> searchCatalogoGenerico(
                        @Param("searchTerm") String searchTerm,
                        @Param("searchType") String searchType,
                        @Param("tipo") Catalogo.Tipo tipo,
                        @Param("idCategoria") Long idCategoria,
                        @Param("esGenerico") Boolean esGenerico,
                        @Param("manejaLotes") Boolean manejaLotes,
                        @Param("estado") EstadoGeneral estado,
                        @Param("tipoAfectacion") String tipoAfectacion,
                        Pageable pageable);

        @Query("SELECT ps FROM Catalogo ps WHERE " +
                        "(:tipo IS NULL OR ps.tipo = :tipo) " +
                        "AND (:idCategoria IS NULL OR ps.idCategoria = :idCategoria) " +
                        "AND (:esGenerico IS NULL OR ps.esGenerico = :esGenerico) " +
                        "AND (:manejaLotes IS NULL OR ps.manejaLotes = :manejaLotes) " +
                        "AND (:estado IS NULL OR ps.estado = :estado) " +
                        "AND (:tipoAfectacion IS NULL OR ps.tipoAfectacion = :tipoAfectacion)")
        Page<Catalogo> listarCatalogoGenerico(
                        @Param("tipo") Catalogo.Tipo tipo, 
                        @Param("idCategoria") Long idCategoria, 
                        @Param("esGenerico") Boolean esGenerico, 
                        @Param("manejaLotes") Boolean manejaLotes,
                        @Param("estado") EstadoGeneral estado,
                        @Param("tipoAfectacion") String tipoAfectacion,
                        Pageable pageable);
}
