package com.pe.articulos.modules.productos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.pe.articulos.modules.productos.entity.ClasificacionMovimiento;
import com.pe.articulos.modules.productos.entity.MovimientoDiverso;
import com.pe.articulos.modules.productos.entity.MovimientoDiversoDetalle;
import com.pe.articulos.modules.productos.dto.AjusteInventarioDetalleResponse;
import com.pe.articulos.modules.productos.dto.AjusteInventarioResponse;
import com.pe.articulos.modules.productos.dto.ClasificacionMovimientoResponse;
import com.pe.articulos.modules.productos.dto.MovimientoDiversoDetalleResponse;
import com.pe.articulos.modules.productos.dto.MovimientoDiversoResponse;
import com.pe.articulos.modules.productos.entity.AjusteInventario;
import com.pe.articulos.modules.productos.entity.AjusteInventarioDetalle;

@Mapper(componentModel = "spring")
public interface InventarioMapper {

    @Mapping(target = "estado", expression = "java(entity.getEstado().name())")
    ClasificacionMovimientoResponse toDto(ClasificacionMovimiento entity);

    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? entity.getEstado().name() : null)")
    @Mapping(target = "sucursalNombre", source = "sucursal.nombreSucursal")
    @Mapping(target = "idSucursal", source = "sucursal.idSucursal")
    @Mapping(target = "usuarioNombre", source = "usuario.nombreCompleto")
    @Mapping(target = "idUsuario", source = "usuario.id")
    MovimientoDiversoResponse toDto(MovimientoDiverso entity);

    @Mapping(target = "idCatalogo", source = "catalogo.id")
    @Mapping(target = "productoNombre", source = "catalogo.nombre")
    @Mapping(target = "clasificacionNombre", source = "clasificacion.nombre")
    @Mapping(target = "idClasificacion", source = "clasificacion.id")
    MovimientoDiversoDetalleResponse toDto(MovimientoDiversoDetalle entity);

    @Mapping(target = "clasificacion", source = "clasificacion")
    AjusteInventarioResponse toDto(AjusteInventario entity);

    @Mapping(target = "idCatalogo", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    AjusteInventarioDetalleResponse toDto(AjusteInventarioDetalle entity);
}
