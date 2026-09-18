package com.pe.articulos.modules.compras.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.compras.dto.CronogramaPagoResponseDTO;
import com.pe.articulos.modules.compras.entity.CronogramaPago;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CronogramaPagoMapper {

    @Mapping(target = "idCompra", source = "compra.id")
    @Mapping(target = "proveedorRazonSocial", expression = "java((entity.getCompra() != null && entity.getCompra().getProveedor() != null) ? entity.getCompra().getProveedor().getRazonSocial() : null)")
    CronogramaPagoResponseDTO toResponse(CronogramaPago entity);
}
