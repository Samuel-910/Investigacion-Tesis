
package com.pe.articulos.modules.compras.mapper;

import com.pe.articulos.modules.catalogo.dto.CatalogoResponse;
import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.compras.dto.DetalleCompraResponse;
import com.pe.articulos.modules.compras.dto.DetalleCompraRequest;
import com.pe.articulos.modules.compras.entity.DetalleCompra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface DetalleCompraMapper {

    @Mapping(target = "producto.id", source = "idProducto")
    @Mapping(target = "unidadMedida.id", source = "idUnidadMedida")
    @Mapping(target = "esBonificacion", defaultValue = "false")
    @Mapping(target = "tipoAfectacion", source = "tipoAfectacion", defaultValue = "GRAVADO_ONEROSO")
    @Mapping(target = "presentacion", source = "presentacion")
    DetalleCompra toEntity(DetalleCompraRequest request);

    List<DetalleCompra> toEntityList(List<DetalleCompraRequest> requests);

    @Mapping(target = "producto", source = "producto", qualifiedByName = "mapProductoToResponse")
    @Mapping(target = "idUnidadMedida", source = "unidadMedida.id")
    @Mapping(target = "unidad", source = "unidadMedida.nombre")
    @Mapping(target = "idCompra", source = "compra.id")
    @Mapping(target = "idProveedor", source = "compra.proveedor.id")
    @Mapping(target = "proveedorRazonSocial", source = "compra.proveedor.razonSocial")
    @Mapping(target = "serie", source = "compra.serie")
    @Mapping(target = "correlativo", source = "compra.correlativo")
    @Mapping(target = "fechaEmision", source = "compra.fechaEmision")
    DetalleCompraResponse toResponse(DetalleCompra entity);

    List<DetalleCompraResponse> toResponseList(List<DetalleCompra> entities);

    @Named("mapProductoToResponse")
    default CatalogoResponse mapProductoToResponse(Catalogo producto) {
        if (producto == null)
            return null;
        CatalogoResponse response = new CatalogoResponse();
        response.setId(producto.getId());
        response.setNombre(producto.getNombre());
        response.setCodigo(producto.getCodigo());
        response.setPresentacion(producto.getPresentacion());
        return response;
    }
}
