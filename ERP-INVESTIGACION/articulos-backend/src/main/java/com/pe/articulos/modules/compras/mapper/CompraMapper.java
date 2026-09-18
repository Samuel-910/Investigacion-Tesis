
package com.pe.articulos.modules.compras.mapper;

import com.pe.articulos.modules.compras.dto.CompraRequest;
import com.pe.articulos.modules.compras.dto.CompraResponse;
import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.modules.proveedores.dto.ProveedorResponse;
import com.pe.articulos.modules.proveedores.entity.Proveedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true), uses = {
        DetalleCompraMapper.class })
public interface CompraMapper {

    @Mapping(target = "proveedor.id", source = "idProveedor")
    @Mapping(target = "estado", expression = "java(com.pe.articulos.core.enums.EstadoGeneral.REGISTRADO)")
    @Mapping(target = "detalles", source = "detalles")
    @Mapping(target = "solicitarFondo", source = "solicitarFondo")
    @Mapping(target = "moneda", constant = "PEN")
    Compra toEntity(CompraRequest request);

    @Mapping(target = "proveedor", source = "proveedor", qualifiedByName = "mapProveedorToResponse")
    @Mapping(target = "solicitarFondo", source = "solicitarFondo")
    @Mapping(target = "moneda", constant = "PEN")
    CompraResponse toResponse(Compra entity);

    @Named("mapProveedorToResponse")
    default ProveedorResponse mapProveedorToResponse(Proveedor proveedor) {
        if (proveedor == null)
            return null;
        return ProveedorResponse.builder()
                .id(proveedor.getId())
                .razonSocial(proveedor.getRazonSocial())
                .numDocIdent(proveedor.getNumDocIdent())
                .telefono(proveedor.getTelefono())
                .build();
    }
}
