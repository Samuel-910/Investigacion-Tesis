package com.pe.articulos.modules.productos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.pe.articulos.modules.productos.dto.TransferenciaDetalleResponse;
import com.pe.articulos.modules.productos.dto.TransferenciaResponse;
import com.pe.articulos.modules.productos.entity.TransferenciaSucursal;
import com.pe.articulos.modules.productos.entity.TransferenciaSucursalDetalle;

@Mapper(componentModel = "spring")
public interface TransferenciaMapper {

    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? entity.getEstado().name() : null)")
    @Mapping(target = "sucursalOrigenNombre", source = "sucursalOrigen.nombreSucursal")
    @Mapping(target = "sucursalDestinoNombre", source = "sucursalDestino.nombreSucursal")
    @Mapping(target = "usuarioSolicitaNombre", source = "usuarioSolicita.nombreCompleto")
    @Mapping(target = "usuarioEnviaNombre", source = "usuarioEnvia.nombreCompleto")
    @Mapping(target = "usuarioRecibeNombre", source = "usuarioRecibe.nombreCompleto")
    @Mapping(target = "usuarioCancelaNombre", source = "usuarioCancela.nombreCompleto")
    TransferenciaResponse toDto(TransferenciaSucursal entity);

    @Mapping(target = "idCatalogo", source = "catalogo.id")
    @Mapping(target = "productoNombre", source = "catalogo.nombre")
    @Mapping(target = "productoCodigo", source = "catalogo.codigo")
    TransferenciaDetalleResponse toDto(TransferenciaSucursalDetalle entity);
}
