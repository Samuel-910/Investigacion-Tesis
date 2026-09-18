package com.pe.articulos.modules.catalogo.mapper;

import com.pe.articulos.modules.catalogo.dto.CatalogoRequest;
import com.pe.articulos.modules.catalogo.dto.CatalogoResponse;
import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.atributos.entity.BaseAtributo;
import com.pe.articulos.modules.atributos.repository.AccionTerapeuticaRepository;
import com.pe.articulos.modules.atributos.repository.BaseAtributoRepository;
import com.pe.articulos.modules.atributos.repository.CategoriaRepository;
import com.pe.articulos.modules.atributos.repository.LaboratorioRepository;
import com.pe.articulos.modules.atributos.repository.PrincipioActivoRepository;
import com.pe.articulos.modules.niveles.repository.NivelRepository;
import com.pe.articulos.modules.catalogo.repository.UnidadMedidaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public abstract class CatalogoMapper {

    @Autowired
    protected CategoriaRepository categoriaRepository;
    @Autowired
    protected LaboratorioRepository laboratorioRepository;
    @Autowired
    protected PrincipioActivoRepository principioActivoRepository;
    @Autowired
    protected AccionTerapeuticaRepository accionTerapeuticaRepository;
    @Autowired
    protected NivelRepository nivelRepository;
    @Autowired
    protected UnidadMedidaRepository unidadMedidaRepository;

    @Mapping(target = "principioActivo", expression = "java(getDescripcion(entity.getIdPrincipioActivo(), principioActivoRepository))")
    @Mapping(target = "accionTerapeutica", expression = "java(getDescripcion(entity.getIdAccionTerapeutica(), accionTerapeuticaRepository))")
    @Mapping(target = "categoria", expression = "java(getDescripcion(entity.getIdCategoria(), categoriaRepository))")
    @Mapping(target = "nivel", expression = "java(getNivelDescripcion(entity.getIdNivel()))")
    @Mapping(target = "unidadBase", expression = "java(getUnidadNombre(entity.getIdUnidadBase()))")
    @Mapping(target = "unidadIntermedia", expression = "java(getUnidadNombre(entity.getIdUnidadIntermedia()))")
    @Mapping(target = "unidadMayor", expression = "java(getUnidadNombre(entity.getIdUnidadMayor()))")
    public abstract CatalogoResponse toResponse(Catalogo entity);

    @Mapping(target = "nivel", expression = "java(request.getIdNivel() != null ? nivelRepository.findById(request.getIdNivel()).orElseThrow(() -> new com.pe.articulos.core.exception.ResourceNotFoundException(\"Nivel\", \"id\", request.getIdNivel())) : null)")
    public abstract Catalogo toEntity(CatalogoRequest request);

    @Mapping(target = "nivel", expression = "java(request.getIdNivel() != null ? nivelRepository.findById(request.getIdNivel()).orElseThrow(() -> new com.pe.articulos.core.exception.ResourceNotFoundException(\"Nivel\", \"id\", request.getIdNivel())) : entity.getNivel())")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntityFromRequest(CatalogoRequest request, @MappingTarget Catalogo entity);

    protected <T extends BaseAtributo> String getDescripcion(Long id, BaseAtributoRepository<T> repository) {
        if (id == null)
            return null;
        return repository.findById(id).map(entity -> entity.getDescripcion())
                .orElse(null);
    }

    protected String getNivelDescripcion(Long id) {
        if (id == null)
            return null;
        return nivelRepository.findById(id).map(entity -> entity.getNombre())
                .orElse(null);
    }

    protected String getUnidadNombre(Long id) {
        if (id == null)
            return null;
        return unidadMedidaRepository.findById(id).map(entity -> entity.getNombre())
                .orElse(null);
    }
}
