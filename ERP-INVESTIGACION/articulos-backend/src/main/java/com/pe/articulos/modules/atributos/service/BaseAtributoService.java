package com.pe.articulos.modules.atributos.service;

import com.pe.articulos.modules.atributos.entity.BaseAtributo;
import com.pe.articulos.modules.atributos.repository.BaseAtributoRepository;
import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("null")
public abstract class BaseAtributoService<T extends BaseAtributo> {

    protected final BaseAtributoRepository<T> repository;

    protected BaseAtributoService(BaseAtributoRepository<T> repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<T> buscar(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return repository.findAll(pageable);
        }
        return repository.findByDescripcionContainingIgnoreCase(query, pageable);
    }

    @Transactional(readOnly = true)
    public Page<T> buscarActivos(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {

            String desc = "";
            return repository.findByDescripcionContainingIgnoreCaseAndEstado(desc, EstadoGeneral.ACTIVO, pageable);
        }
        return repository.findByDescripcionContainingIgnoreCaseAndEstado(query, EstadoGeneral.ACTIVO, pageable);
    }

    @Transactional
    public T guardar(T entidad) {
        // Validaciones genéricas podrían ir aquí
        if (entidad.getId() == null && entidad.getEstado() == null) {
            entidad.setEstado(EstadoGeneral.ACTIVO); // Default create active if not specified
        }
        return repository.save(entidad);
    }

    @Transactional(readOnly = true)
    public T obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado con ID: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        // Soft delete
        T entidad = obtenerPorId(id);
        entidad.setEstado(EstadoGeneral.ELIMINADO);
        repository.save(entidad);
    }

    @Transactional
    public void activar(Long id) {
        T entidad = obtenerPorId(id);
        entidad.setEstado(EstadoGeneral.ACTIVO);
        repository.save(entidad);
    }
}
