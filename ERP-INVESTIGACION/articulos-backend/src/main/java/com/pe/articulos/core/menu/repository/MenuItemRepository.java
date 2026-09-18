package com.pe.articulos.core.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.core.menu.entity.MenuItem;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Obtener items raíz (sin parent) filtrados por acceso y ordenados
     */
    @Query("SELECT m FROM MenuItem m WHERE m.parent IS NULL AND m.acceso.id = :accesoId AND m.activo = true ORDER BY m.orden")
    List<MenuItem> findRootItemsByAcceso(@Param("accesoId") Long accesoId);

    @Query("SELECT m FROM MenuItem m WHERE m.parent IS NULL AND m.activo = true ORDER BY m.orden")
    List<MenuItem> findRootItems();

    /**
     * Obtener hijos de un item específico
     */
    @Query("SELECT m FROM MenuItem m WHERE m.parent.id = :parentId AND m.activo = true ORDER BY m.orden")
    List<MenuItem> findByParentId(@Param("parentId") Long parentId);

    /**
     * Obtener todos los items activos
     */
    List<MenuItem> findByActivoTrueOrderByOrdenAsc();
}