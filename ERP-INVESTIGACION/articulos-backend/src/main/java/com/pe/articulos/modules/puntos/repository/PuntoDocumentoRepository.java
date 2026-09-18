package com.pe.articulos.modules.puntos.repository;

import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.puntos.entity.PuntoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PuntoDocumentoRepository extends JpaRepository<PuntoDocumento, Long> {

       // Buscar todos los documentos de un punto específico
       List<PuntoDocumento> findByPuntoPunto(Long puntoId);

       Page<PuntoDocumento> findByPuntoPunto(Long puntoId, Pageable pageable);

       // Buscar por tipo de documento
       List<PuntoDocumento> findByTipoDocumentoTipoDoc(String tipoDoc);

       List<PuntoDocumento> findByTipoDocumentoTipoDocIgnoreCase(String tipoDoc);

       boolean existsByPuntoAndSerie(Punto punto, String serie);

       // Buscar por serie
       List<PuntoDocumento> findBySerie(String serie);

       // Buscar por tipo de documento y serie
       List<PuntoDocumento> findByTipoDocumentoTipoDocAndSerie(String tipoDoc, String serie);

       // Buscar por estado
       List<PuntoDocumento> findByEstado(com.pe.articulos.core.enums.EstadoGeneral estado);

       // Buscar documentos de un punto por tipo y módulo
       @Query("SELECT pd FROM PuntoDocumento pd WHERE pd.punto.punto = :puntoId AND pd.tipoDocumento.tipoDoc = :tipoDoc AND pd.modulo = :modulo")
       List<PuntoDocumento> findByPuntoAndTipoDocAndModulo(@Param("puntoId") Long puntoId,
                     @Param("tipoDoc") String tipoDoc,
                     @Param("modulo") com.pe.articulos.modules.documentos.entities.Modulo modulo);

       // Buscar el último número de documento por serie y tipo
       @Query("SELECT pd FROM PuntoDocumento pd WHERE pd.serie = :serie AND pd.tipoDocumento.tipoDoc = :tipoDoc " +
                     "ORDER BY pd.numero DESC")
       List<PuntoDocumento> findTopBySerieAndTipoDocOrderByNumeroDesc(@Param("serie") String serie,
                     @Param("tipoDoc") String tipoDoc);

       // Verificar si existe un documento con serie y número específico
       boolean existsBySerieAndNumero(String serie, Integer numero);

       // Obtener documentos por punto con estado activo y módulo
       @Query("SELECT pd FROM PuntoDocumento pd WHERE pd.punto.punto = :puntoId AND (:modulos IS NULL OR pd.modulo IN :modulos) AND pd.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO")
       List<PuntoDocumento> findDocumentosActivosByPunto(@Param("puntoId") Long puntoId, @Param("modulos") List<com.pe.articulos.modules.documentos.entities.Modulo> modulos);

       // Contar documentos por punto
       long countByPuntoPunto(Long puntoId);

       // Buscar por usuario (id_personal_user)
       List<PuntoDocumento> findByIdPersonalUser(Integer idPersonalUser);

       // Query personalizada para buscar por múltiples criterios
       @Query("SELECT pd FROM PuntoDocumento pd WHERE " +
                     "(:puntoId IS NULL OR pd.punto.punto = :puntoId) AND " +
                     "(:tipoDoc IS NULL OR pd.tipoDocumento.tipoDoc = :tipoDoc) AND " +
                     "(:estado IS NULL OR pd.estado = :estado)")
       List<PuntoDocumento> buscarPorCriterios(@Param("puntoId") Long puntoId,
                     @Param("tipoDoc") String tipoDoc,
                     @Param("estado") com.pe.articulos.core.enums.EstadoGeneral estado);

       @Query("SELECT DISTINCT pd.tipoDocumento FROM PuntoDocumento pd WHERE pd.modulo = :modulo AND (:sucursalId IS NULL OR pd.punto.sucursal.idSucursal = :sucursalId) AND pd.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO")
       List<com.pe.articulos.modules.venta_registro.entity.TipoDocumento> findDistinctTipoDocumentoByModuloAndSucursal(@Param("modulo") com.pe.articulos.modules.documentos.entities.Modulo modulo, @Param("sucursalId") Long sucursalId);

       @Query("SELECT pd FROM PuntoDocumento pd WHERE (:sucursalId IS NULL OR pd.punto.sucursal.idSucursal = :sucursalId) AND (:modulos IS NULL OR pd.modulo IN :modulos) AND pd.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO")
       List<PuntoDocumento> findDocumentosActivosBySucursal(@Param("sucursalId") Long sucursalId, @Param("modulos") List<com.pe.articulos.modules.documentos.entities.Modulo> modulos);
}
