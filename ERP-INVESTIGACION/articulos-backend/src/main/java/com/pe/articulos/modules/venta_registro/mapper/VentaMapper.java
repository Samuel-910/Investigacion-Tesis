package com.pe.articulos.modules.venta_registro.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.modules.venta_registro.dto.VentaDetalleDTO;
import com.pe.articulos.modules.venta_registro.dto.VentaRegistroDTO;
import com.pe.articulos.modules.venta_registro.entity.VentaDetalle;
import com.pe.articulos.modules.venta_registro.entity.VentaRegistro;

import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class VentaMapper {

    @Autowired
    protected DatosPersonalesRepository datosPersonalesRepository;

    @Mapping(target = "idPersonal", expression = "java(venta.getIdPersonal() != null ? String.valueOf(venta.getIdPersonal()) : venta.getIdPersonalUser())")
    @Mapping(target = "metodoPago", expression = "java(getMetodoPagoDescripcion(venta))")
    @Mapping(target = "punto", expression = "java(venta.getPuntoVenta() != null ? venta.getPuntoVenta().getNombre() : (venta.getPunto() != null ? String.valueOf(venta.getPunto()) : null))")
    @Mapping(target = "numeroDocumento", expression = "java(venta.getSerie() + \"-\" + String.format(\"%07d\", venta.getNumero() != null ? venta.getNumero() : 0))")
    @Mapping(target = "nombrePaciente", expression = "java(getNombrePaciente(venta))")
    @Mapping(target = "nombreVendedor", expression = "java(getNombreVendedor(venta))")
    @Mapping(target = "emailVendedor", expression = "java(getEmailVendedor(venta))")
    @Mapping(target = "rolVendedor", expression = "java(getRolVendedor(venta))")
    @Mapping(target = "estadoDescripcion", expression = "java(com.pe.articulos.core.enums.EstadoGeneral.VIGENTE.equals(venta.getEstado()) ? \"Vigente\" : \"Anulado\")")
    @Mapping(target = "idMedico", expression = "java(venta.getIdMedico() != null ? String.valueOf(venta.getIdMedico()) : null)")
    @Mapping(target = "idUser", expression = "java(venta.getIdUser() != null ? String.valueOf(venta.getIdUser()) : null)")
    @Mapping(target = "idAlmacen", expression = "java(venta.getIdAlmacen() != null ? String.valueOf(venta.getIdAlmacen()) : null)")
    @Mapping(target = "idPersonalDig", expression = "java(venta.getIdPersonalDig() != null ? String.valueOf(venta.getIdPersonalDig()) : null)")
    @Mapping(target = "totalLetras", expression = "java(com.pe.articulos.core.util.NumberToWords.convert(venta.getTotal(), venta.getMoneda()))")
    @Mapping(target = "observacion", expression = "java(venta.getObservacion() != null ? venta.getObservacion() : venta.getObs())")
    @Mapping(target = "celularSucursal", expression = "java(venta.getPuntoVenta() != null && venta.getPuntoVenta().getSucursal() != null ? venta.getPuntoVenta().getSucursal().getCelular() : null)")
    public abstract VentaRegistroDTO toDto(VentaRegistro venta);

    @Mapping(target = "idDetalle", source = "idMovart")
    @Mapping(target = "idVenta", expression = "java(detalle.getVentaRegistro() != null ? detalle.getVentaRegistro().getIdVenta() : null)")
    @Mapping(target = "idCatalogo", expression = "java(detalle.getIdCatalogo() != null ? Long.valueOf(detalle.getIdCatalogo()) : null)")
    @Mapping(target = "idArticulo", expression = "java(detalle.getIdArticulo() != null ? String.valueOf(detalle.getIdArticulo()) : null)")
    @Mapping(target = "cantidad", expression = "java(detalle.getCantidad() != null ? detalle.getCantidad().subtract(detalle.getCantidadDevuelta() != null ? detalle.getCantidadDevuelta() : java.math.BigDecimal.ZERO) : java.math.BigDecimal.ZERO)")
    @Mapping(target = "idOrden", expression = "java(detalle.getIdOrden() != null ? String.valueOf(detalle.getIdOrden()) : null)")
    @Mapping(target = "glosa", source = "descripcion")
    public abstract VentaDetalleDTO toDetalleDto(VentaDetalle detalle);

    protected String getNombrePaciente(VentaRegistro venta) {
        if (venta.getIdPersonal() != null) {
            Long idPac = venta.getIdPersonal();
            return datosPersonalesRepository.findByid(idPac)
                    .map(dp -> dp.getNombreCompleto())
                    .orElse(String.valueOf(idPac));
        } else if (venta.getIdPersonalUser() != null && !venta.getIdPersonalUser().isEmpty()) {
            return datosPersonalesRepository.findByIdPersonalUser(venta.getIdPersonalUser())
                    .map(dp -> dp.getNombreCompleto())
                    .orElse(venta.getIdPersonalUser());
        }
        return "--";
    }

    protected String getNombreVendedor(VentaRegistro venta) {
        if (venta.getIdUser() != null) {
            Optional<DatosPersonales> userOpt = datosPersonalesRepository.findByid(Long.valueOf(venta.getIdUser()));
            if (userOpt.isPresent()) {
                return userOpt.get().getNombreCompleto();
            }
        }
        return venta.getIdUser() != null ? String.valueOf(venta.getIdUser()) : "--";
    }

    protected String getEmailVendedor(VentaRegistro venta) {
        if (venta.getIdUser() != null) {
            Optional<DatosPersonales> userOpt = datosPersonalesRepository.findByid(Long.valueOf(venta.getIdUser()));
            if (userOpt.isPresent() && userOpt.get().getEmail() != null) {
                return userOpt.get().getEmail();
            }
        }
        return "--";
    }

    protected String getRolVendedor(VentaRegistro venta) {
        if (venta.getIdUser() != null) {
            Optional<DatosPersonales> userOpt = datosPersonalesRepository.findByid(Long.valueOf(venta.getIdUser()));
            if (userOpt.isPresent() && userOpt.get().getRoles() != null && !userOpt.get().getRoles().isEmpty()) {
                return userOpt.get().getRoles().iterator().next().getName();
            }
        }
        return "--";
    }

    public String getMetodoPagoDescripcion(VentaRegistro venta) {
        if (venta == null || venta.getMetodoPago() == null) {
            return "EFECTIVO";
        }
        Object mp = venta.getMetodoPago();
        if (mp instanceof String) {
            return (String) mp;
        }
        if (mp instanceof com.pe.articulos.modules.atributos.entity.MetodoPago) {
            return ((com.pe.articulos.modules.atributos.entity.MetodoPago) mp).getDescripcion();
        }
        return "EFECTIVO";
    }
}
