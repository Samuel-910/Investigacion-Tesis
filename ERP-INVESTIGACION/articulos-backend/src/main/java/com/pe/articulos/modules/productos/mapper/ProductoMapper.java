package com.pe.articulos.modules.productos.mapper;

import com.pe.articulos.modules.catalogo.mapper.CatalogoMapper;
import com.pe.articulos.modules.productos.dto.ProductoRequest;
import com.pe.articulos.modules.productos.dto.ProductoResponse;
import com.pe.articulos.modules.productos.entity.Producto;
import com.pe.articulos.modules.atributos.entity.BaseAtributo;
import com.pe.articulos.modules.atributos.repository.BaseAtributoRepository;
import com.pe.articulos.modules.atributos.repository.LaboratorioRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {
        CatalogoMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProductoMapper {

    @Autowired
    protected LaboratorioRepository laboratorioRepository;

    @Mapping(target = "idAlmacen", source = "almacen.id")
    @Mapping(target = "nombreAlmacen", source = "almacen.nombre")
    @Mapping(target = "laboratorio", expression = "java(getDescripcion(entity.getIdLaboratorio(), laboratorioRepository))")
    @Mapping(target = "idUbicacion", source = "ubicacion.id")
    @Mapping(target = "proveedorRazonSocial", source = "proveedor.razonSocial")
    public abstract ProductoResponse toResponse(Producto entity);

    @Mapping(target = "idProducto", ignore = true)
    @Mapping(target = "idAlmacen", source = "idAlmacen")
    @Mapping(target = "idUbicacion", source = "idUbicacion")
    @Mapping(target = "idCatalogo", source = "idCatalogo")
    public abstract Producto toEntity(ProductoRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "idAlmacen", source = "idAlmacen")
    @Mapping(target = "idUbicacion", source = "idUbicacion")
    @Mapping(target = "idCatalogo", source = "idCatalogo")
    public abstract void updateEntityFromRequest(ProductoRequest request, @MappingTarget Producto entity);

    protected <T extends BaseAtributo> String getDescripcion(Long id, BaseAtributoRepository<T> repository) {
        if (id == null)
            return null;
        return repository.findById(id).map(entity -> entity.getDescripcion())
                .orElse(null);
    }
}
