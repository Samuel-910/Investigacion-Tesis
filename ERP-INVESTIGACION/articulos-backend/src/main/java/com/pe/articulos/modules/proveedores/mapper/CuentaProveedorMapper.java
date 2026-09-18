package com.pe.articulos.modules.proveedores.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.pe.articulos.modules.proveedores.dto.CuentaProveedorMovimientoResponse;
import com.pe.articulos.modules.proveedores.dto.CuentaProveedorResponse;
import com.pe.articulos.modules.proveedores.dto.DeudaProveedorResponse;
import com.pe.articulos.modules.proveedores.entity.CuentaProveedor;
import com.pe.articulos.modules.proveedores.entity.CuentaProveedorMovimiento;
import com.pe.articulos.modules.proveedores.entity.DeudaProveedor;

@Mapper(componentModel = "spring")
public interface CuentaProveedorMapper {

    @Mapping(target = "idProveedor", source = "proveedor.id")
    @Mapping(target = "proveedorRazonSocial", source = "proveedor.razonSocial")
    CuentaProveedorResponse toCuentaResponse(CuentaProveedor entity);

    @Mapping(target = "idCuenta", source = "cuenta.id")
    CuentaProveedorMovimientoResponse toMovimientoResponse(CuentaProveedorMovimiento entity);

    @Mapping(target = "idCuentaProveedor", source = "cuentaProveedor.id")
    @Mapping(target = "proveedorRazonSocial", source = "cuentaProveedor.proveedor.razonSocial")
    DeudaProveedorResponse toDeudaResponse(DeudaProveedor entity);
}
